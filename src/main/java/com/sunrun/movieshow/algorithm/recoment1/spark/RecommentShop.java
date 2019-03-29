package com.sunrun.movieshow.algorithm.recoment1.spark;

import com.sunrun.movieshow.algorithm.common.SparkHelper;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

public class RecommentShop {
    public static void main(String[] args) {
        JavaSparkContext sc = SparkHelper.getSparkContext("Recorment");
        JavaRDD<String> rdd = sc.textFile("data/reccment1/transaction.txt");

        // 1.获取userId-itemId列表
        JavaPairRDD<String, String> pair = rdd.mapToPair(line -> {
            String[] tokens = line.split(" ");
            return new Tuple2<>(tokens[0], tokens[1]);
        });

        // 2.


    }
}
