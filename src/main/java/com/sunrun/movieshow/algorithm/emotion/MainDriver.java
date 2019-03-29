package com.sunrun.movieshow.algorithm.emotion;

import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.stream.Collectors;

/**
 * 在进行特征统计的时候，也可以对句子段落进行筛选，比如只需要统计一些包含我们需要关注的关键信息的目标内容。
 * 例如如果我们分析某些数据里对于“人工智能”的情感问题，那么可以考虑筛掉那些不包含“人工智能”的样本。
 */
public class MainDriver {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("emotion").setMaster("local").set("spark.testing.memory", "471859500");

        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        // 1.load emotion library.
        // positive
        JavaPairRDD<String, Integer> positivePairRDD = sc.textFile("ntusd-positive.txt").mapToPair(t -> new Tuple2<>(t, 1));
        // negative
        JavaPairRDD<String, Integer> negativePairRDD = sc.textFile("ntusd-negative.txt").mapToPair(t -> new Tuple2<>(t, 1));

        // 2.load analysis text.
        JavaPairRDD<String, Integer> dataPairRDD = sc.wholeTextFiles("emotion.txt").map(c -> {
            return ToAnalysis.parse(c._2).getTerms().stream()
                    .filter(t -> {
                        return t.getNatureStr() != "null" && !t.getNatureStr().equals("w");
                    })
                    .map(t -> t.getName())
                    .collect(Collectors.toList());

        }).flatMap(t -> t.iterator()).mapToPair(word -> new Tuple2<>(word, 1));

        // 3.join the p and negative library
        JavaPairRDD<String, Tuple2<Integer, Integer>> joinP = dataPairRDD.join(positivePairRDD);
        JavaPairRDD<String, Tuple2<Integer, Integer>> joinN = dataPairRDD.join(negativePairRDD);
        System.out.println("=== debug:show join result ===");
        System.out.println("=== positive ===");
        System.out.println(joinP.collect());
        System.out.println("=== negative ===");
        System.out.println(joinN.collect());

        long result = joinP.count() - joinN.count();

        if(result > 0){
            System.out.println("积极、正面情绪");
        }else if(result < 0){
            System.out.println("消极、负面情绪");
        }else{
            System.out.println("中性情绪");
        }

    }
}
