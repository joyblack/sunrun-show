package com.sunrun.movieshow.algorithm.knn;

import com.google.common.base.Splitter;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.util.*;

/**
 * KNN分类问题是找出一个数据集中与给定查询数据点最近的K个数据点。
 * 这个操作也成为KNN连接(KNN-join)。可以定义为：
 * 给定两个数据集R合S，对R中的每一个对象，我们希望从S中找出K个最近的相邻对象。
 */
public class KNNTester {
    /**
     * 1. 获取Spark 上下文对象
     * @return
     */
    public static JavaSparkContext getSparkContext(String appName){
        SparkConf sparkConf = new SparkConf()
                .setAppName(appName)
                //.setSparkHome(sparkHome)
                .setMaster("local[*]")
                // 串行化器
                .set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
                .set("spark.testing.memory", "2147480000");

        return new JavaSparkContext(sparkConf);
    }

    /**
     * 2. 将数字字符串转换为Double数组
     * @param str 数字字符串: "1,2,3,4,5"
     * @param delimiter 数字之间的分隔符：","
     * @return Double数组
     */
    public static List<Double> transferToDoubleList(String str, String delimiter){
        // 使用Google Splitter切割字符串
        Splitter splitter = Splitter.on(delimiter).trimResults();
        Iterable<String> tokens = splitter.split(str);
        if(tokens == null){
            return null;
        }
        List<Double> list = new ArrayList<>();
        for (String token : tokens) {
            list.add(Double.parseDouble(token));
        }
        return list;
    }

    /**
     * 计算距离
     * @param rRecord R数据集的一条记录
     * @param sRecord S数据集的一条记录
     * @param d 记录的维度
     * @return 两条记录的欧氏距离
     */
    public static double calculateDistance(String rRecord, String sRecord, int d){
        double distance = 0D;
        List<Double> r = transferToDoubleList(rRecord,",");
        List<Double> s = transferToDoubleList(sRecord,",");
        // 若维度不一致，说明数据存在问题，返回NAN
        if(r.size() != d || s.size() != d){
            distance =  Double.NaN;
        } else{
            // 保证维度一致之后，计算欧氏距离
            double sum = 0D;
            for (int i = 0; i < s.size(); i++) {
                double diff = s.get(i) - r.get(i);
                sum += diff * diff;
            }
            distance = Math.sqrt(sum);
        }
        return distance;
    }

    /**
     * 根据（距离，类别），找出距离最低的K个近邻
     * @param neighbors 当前求出的近邻数量
     * @param k 寻找多少个近邻
     * @return K个近邻组成的SortedMap
     */
    public static SortedMap<Double, String>findNearestK(Iterable<Tuple2<Double,String>> neighbors, int k){
        TreeMap<Double, String> kNeighbors = new TreeMap<>();
        for (Tuple2<Double, String> neighbor : neighbors) {
            // 距离
            Double distance = neighbor._1;
            // 类别
            String classify = neighbor._2;
            kNeighbors.put(distance, classify);
            // 如果当前已经写入K个元素，那么删除掉距离最远的一个元素（位于末端）
            if(kNeighbors.size() > k){
                kNeighbors.remove(kNeighbors.lastKey());
            }
        }
        return kNeighbors;
    }

    /**
     * 计算对每个类别的投票次数
     * @param kNeighbors 选取的K个最近的点
     * @return 对每个类别的投票结果
     */
    public static Map<String, Integer> buildClassifyCount(Map<Double, String> kNeighbors){
        HashMap<String, Integer> majority = new HashMap<>();
        for (Map.Entry<Double, String> entry : kNeighbors.entrySet()) {
            String classify = entry.getValue();
            Integer count = majority.get(classify);
            // 当前没有出现过，设置为1，否则+1
            if(count == null){
                majority.put(classify,1);
            }else{
                majority.put(classify,count + 1);
            }
        }
        return  majority;
    }

    /**
     * 根据投票结果，选取最终的类别
     * @param majority 投票结果
     * @return 最终的类别
     */
    public static String classifyByMajority(Map<String, Integer> majority){
        String selectedClassify = null;
        int maxVotes = 0;
        // 从投票结果中选取票数最多的一类作为最终选举结果
        for (Map.Entry<String, Integer> entry : majority.entrySet()) {
            if(selectedClassify == null){
                selectedClassify = entry.getKey();
                maxVotes = entry.getValue();
            }else{
                int nowVotes = entry.getValue();
                if(nowVotes > maxVotes){
                    selectedClassify = entry.getKey();
                    maxVotes = nowVotes;
                }
            }
        }
        return selectedClassify;
    }



    public static void main(String[] args) {
        // === 1.创建SparkContext
        JavaSparkContext sc = getSparkContext("KNN");

        // === 2.KNN算法相关参数：广播共享对象
        String HDFSUrl = "output";
        // k（K）
        Broadcast<Integer> broadcastK = sc.broadcast(6);
        // d（维度）
        Broadcast<Integer> broadcastD = sc.broadcast(2);

        // === 3.为查询和训练数据集创建RDD
        // R and S
        String RPath = "data/knn/R.txt";
        String SPath = "data/knn/S.txt";
        JavaRDD<String> R = sc.textFile(RPath);
        JavaRDD<String> S = sc.textFile(SPath);
//        // === 将R和S的数据存储到hdfs
//        R.saveAsTextFile(HDFSUrl + "S");
//        S.saveAsTextFile(HDFSUrl + "R");

        // === 5.计算R&S的笛卡尔积
        JavaPairRDD<String, String> cart = R.cartesian(S);
        /**
         * (1000;3.0,3.0,100;c1;1.0,1.0)
         * (1000;3.0,3.0,101;c1;1.1,1.2)
         */

        // === 6.计算R中每个点与S各个点之间的距离:(rid,(distance,classify))
        // (1000;3.0,3.0,100;c1;1.0,1.0) => 1000 is rId, 100 is sId, c1 is classify.
        JavaPairRDD<String, Tuple2<Double, String>> knnPair = cart.mapToPair(t -> {
            String rRecord = t._1;
            String sRecord = t._2;

            // 1000;3.0,3.0
            String[] splitR = rRecord.split(";");
            String rId = splitR[0]; // 1000
            String r = splitR[1];// "3.0,3.0"

            // 100;c1;1.0,1.0
            String[] splitS = sRecord.split(";");
            // sId对于当前算法没有多大意义，我们只需要获取类别细信息，即第二个字段的信息即可
            String sId = splitS[0]; // 100
            String classify = splitS[1]; // c1
            String s = splitS[2];// "3.0,3.0"

            // 获取广播变量中的维度信息
            Integer d = broadcastD.value();
            // 计算当前两个点的距离
            double distance = calculateDistance(r, s, d);
            Tuple2<Double, String> V = new Tuple2<>(distance, classify);
            // (Rid,(distance,classify))
            return new Tuple2<>(rId, V);
        });
        /**
         * (1005,(2.801785145224379,c3))
         * (1006,(4.75078940808788,c2))
         * (1006,(4.0224370722237515,c2))
         * (1006,(3.3941125496954263,c2))
         * (1006,(12.0074976577137,c3))
         * (1006,(11.79025020938911,c3)
         */


        // === 7. 按R中的r根据每个记录进行分组
        JavaPairRDD<String, Iterable<Tuple2<Double, String>>> knnGrouped = knnPair.groupByKey();
        // (1005,[(12.159358535712318,c1),....,(7.3171032519706865,c3), (7.610519036176179,c3)]),
        // (1000,[(2.8284271247461903,c1), (2.6172504656604803,c1), (2.690724....])

        // === 8.找出每个R节点的k个近邻
        JavaPairRDD<String, String> knnOutput = knnGrouped.mapValues(t -> {
            // K
            Integer k = broadcastK.value();
            SortedMap<Double, String> nearestK = findNearestK(t, k);
            // {2.596150997149434=c3, 2.801785145224379=c3, 2.8442925306655775=c3, 3.0999999999999996=c3, 3.1384709652950433=c3, 3.1622776601683795=c3}

            // 统计每个类别的投票次数
            Map<String, Integer> majority = buildClassifyCount(nearestK);
            // {c3=1, c1=5}

            // 按多数优先原则选择最终分类
            String selectedClassify = classifyByMajority(majority);
            return selectedClassify;
        });

        // 存储最终结果
        knnOutput.coalesce(1).saveAsTextFile(HDFSUrl + "/result");
        /**
         * [root@h24 hadoop]# hadoop fs -cat /output/result/p*
         * (1005,c3)
         * (1001,c3)
         * (1006,c2)
         * (1003,c1)
         * (1000,c1)
         * (1004,c1)
         */
    }
}
