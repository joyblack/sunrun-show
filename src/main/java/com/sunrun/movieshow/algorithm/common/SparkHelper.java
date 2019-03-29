package com.sunrun.movieshow.algorithm.common;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

public class SparkHelper {
    /**
     * 获取Spark 上下文对象
     * @return JavaSparkContext
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

    public static SparkSession getSparkSession(String appName){
        SparkConf sparkConf = new SparkConf()
                .setAppName(appName)
                //.setSparkHome(sparkHome)
                .setMaster("local[*]")
                // 串行化器
                .set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
                .set("spark.testing.memory", "2147480000");
        return SparkSession.builder().config(sparkConf).getOrCreate();
    }
}
