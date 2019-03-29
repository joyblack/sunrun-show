package com.sunrun.movieshow.service;

import com.google.common.base.Splitter;
import com.sunrun.movieshow.bean.KNNData;
import com.sunrun.movieshow.bean.ScatterData;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;
import shapeless.Tuple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class KNNService {

    @Autowired
    private JavaSparkContext sc;

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
    public static SortedMap<Double, String> findNearestK(Iterable<Tuple2<Double,String>> neighbors, int k){
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


    public HashMap<String, Object> getData() {
        List<ScatterData> ser = new ArrayList<>();

        HashMap<String,Integer> classifyMap = new HashMap<>();

        List<String> legends = new ArrayList<>();

        String path = "data/knn/S.txt";
        HashMap<String,List<Double[]>> cp = new HashMap<>();
        try {
            Files.readAllLines(Paths.get(path)).stream().forEach(line ->{
                // 100;c1;1.0,1.0
                String[] splitS = line.split(";");
                // sId对于当前算法没有多大意义，我们只需要获取类别细信息，即第二个字段的信息即可
                String sId = splitS[0]; // 100
                // 类别
                String classify = splitS[1];

                // 该类别没处理过，则新增一个ScatterData元素
                Integer classifyIndex = classifyMap.get(classify);
                if(classifyIndex == null){
                    classifyIndex = ser.size();
                    classifyMap.put(classify,ser.size());
                    ser.add(new ScatterData("scatter",new ArrayList<>(),classify));
                    legends.add(classify);
                }

                // 获取当前类别的点集
                ScatterData scatterData = ser.get(classifyIndex);
                String[] split = splitS[2].split(",");// "3.0,3.0"
                Double[] point = new Double[2];
                point[0] = Double.parseDouble(split[0]);
                point[1] = Double.parseDouble(split[1]);
                scatterData.getData().add(point);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<String,Object> result = new HashMap<>();
        result.put("ser",ser);
        result.put("legends",legends);
        return result;
    }


}
