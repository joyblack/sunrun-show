package com.sunrun.movieshow.algorithm.nbc;

import edu.umd.cloud9.io.pair.PairOfStrings;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* 阶段1：使用训练数据建立一个分类器:一组概率表
* 输入数据（id,天气，温度，湿度，风力，是否适合打球）
1,晴,热,高,弱,不
2,晴,热,高,强,是
...
*/
public class BuildNBCClassifier implements Serializable {
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

    // 2. 将Map(Tuple2,Double) -> List<Tuple2<PairOfString,DoubleWritable>>
    // PairOfStrings是一个实现了Writable接口的类，这样就可以支持Hadoop写入了
    public static List<Tuple2<PairOfStrings, DoubleWritable>> toWritableList(Map<Tuple2<String,String>,Double> PT){
        List<Tuple2<PairOfStrings, DoubleWritable>> list = new ArrayList<>();
        for (Map.Entry<Tuple2<String, String>, Double> entry : PT.entrySet()) {
            list.add(new Tuple2<>(new PairOfStrings(entry.getKey()._1,entry.getKey()._2),new DoubleWritable(entry.getValue())));
        }
        return list;
    }

    public static void main(String[] args) {
        JavaSparkContext sc = getSparkContext("buildBNC");
        JavaRDD<String> training = sc.textFile("data/nbc/ball.txt");
        String path = "data/nbc/basket/";

        // == 1.获得数据集大小，用于概率计算
        long trainingDataSize = training.count();

        // == 2.转换数据集： line -> ((feature,classification),1)
        JavaPairRDD<Tuple2<String, String>, Integer> pairs = training.flatMapToPair(line -> {
            List<Tuple2<Tuple2<String, String>, Integer>> result = new ArrayList<>();
            String[] tokens = line.split(",");
            // 0->id, 1-(n-1) -> A(feature), n -> T(classification)
            String nowClassification = tokens[tokens.length - 1];
            for (int i = 1; i < tokens.length - 1; i++) {
                // like ((晴,是),1)
                result.add(new Tuple2<>(new Tuple2<>(tokens[i], nowClassification), 1));
            }
            // 最后还要统计每个类别的总出现次数，因此，需要单独的对最终class进行统计((class,是),1)
            result.add(new Tuple2<>(new Tuple2<>("class", nowClassification), 1));
            return result.iterator();
        });
        //pairs.saveAsTextFile(dfsUrl + "pair");
        /**
         * ((晴,不),1)
         * ((热,不),1)
         * ((高,不),1)
         * ((弱,不),1)
         * ((class,不),1)
         *  ...
         */

        // == 3.计算每种特征出现的次数
        JavaPairRDD<Tuple2<String, String>, Integer> count = pairs.reduceByKey((c1, c2) -> {
            return c1 + c2;
        });
        //count.saveAsTextFile(dfsUrl + "count");
        /**
         * [root@h24 ~]# hadoop fs -cat /ball/count/p*
         * ((强,是),3)
         * ((雨,是),3)
         * ((弱,是),6)
         * ((class,不),5)
         * ((晴,不),3)
         * ((class,是),9)
         * ((冷,是),3)
         * ...
         */

        // == 4.将归约数据转换为map
        Map<Tuple2<String, String>, Integer> countAsMap = count.collectAsMap();

        // == 5.建立分类器数据结构：概率表PT、分类表CT
        HashMap<Tuple2<String, String>, Double> PT = new HashMap<>();
        ArrayList<String> CT = new ArrayList<>();
        for (Map.Entry<Tuple2<String, String>, Integer> entry : countAsMap.entrySet()) {
            // (feature,classification)
            Tuple2<String, String> key = entry.getKey();
            String feature = key._1;
            String classification = key._2;

            // K: new Tuple2<>(feature, classification) V: compute probably
            Tuple2<String, String> K = new Tuple2<>(feature, classification);

            // class type:target feature classification P(C)
            if(feature.equals("class")){
                CT.add(classification);
                // target feature times / total，总类型的概率为类别出现次数/总记录数
                PT.put(K, (double)entry.getValue() / trainingDataSize);
            }else{
                // 获取某个分类出现的总次数。(Yes? No?) P(Ai|C=Ci) = 属性A值在类别C下出现的次数/类别C的出现次数
                Tuple2<String, String> K2 = new Tuple2<>("class", classification);
                Integer times = countAsMap.get(K2);
                // 该类别没出现过，则概率设为0.0（其实不可能为0）
                if(times == 0){
                    PT.put(K,0.0);
                }else{
                    PT.put(K, (double)entry.getValue() / times);
                }
            }
        }
        // System.out.println(PT);
        // (class,是)=0.6428571428571429, (冷,是)=0.3333333333333333, (晴,不)=0.6, (雨,是)=0.3333333333333333, (高,不)=0.8,
        // System.out.println(CT);
        //  [不, 是]

        // == 6. 保存分类器的数据结构
        // ==== 6.1.转化为持久存储数据类型
        List<Tuple2<PairOfStrings, DoubleWritable>> ptList = toWritableList(PT);
        JavaPairRDD<PairOfStrings, DoubleWritable> ptRDD = sc.parallelizePairs(ptList);
        // ==== 6.2.存储到Hadoop
        ptRDD.coalesce(1).saveAsNewAPIHadoopFile(path + "pt", // 存储路径
                PairOfStrings.class,// K
                DoubleWritable.class, // V
                SequenceFileOutputFormat.class// 输出格式类
        );

        System.out.println("#######################################");
        System.out.println("Dear Arya:");
        System.out.println("已经将学习模型数据存储在文件夹 " + path + "pt 下...");

        // == 7.保存分类列表
        JavaRDD<String> ctRDD = sc.parallelize(CT);
        ctRDD.coalesce(1).saveAsTextFile(path + "ct");
        System.out.println("#######################################");
        System.out.println("Dear Arya:");
        System.out.println("已经将分类数据存储在文件夹 " + path + "ct 下...");

        /**
         * [root@h24 ~]# hadoop fs -cat /ball/nbc/ct/*
         * 不
         * 是
         *
         * [root@h24 ~]# hadoop fs -cat /ball/nbc/pt/*
         * SEQ$edu.umd.cloud9.io.pair.PairOfStrings#org.apache.hadoop.io.DoubleWritableռ
         * ...
         */

        System.out.println("complete training...");

    }




}
