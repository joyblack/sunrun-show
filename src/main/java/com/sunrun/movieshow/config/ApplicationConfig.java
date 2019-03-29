package com.sunrun.movieshow.config;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    // @Value("${spark.home}")
//    private String sparkHome;

    @Value("${spark.app.name}")
    private String appName;

    @Value("${spark.master}")
    private String master;

    // spark config
    public SparkConf sparkConf(){
        SparkConf sparkConf = new SparkConf()
                .setAppName(appName)
                .setMaster(master)
                .set("spark.testing.memory", "2147480000");

        return sparkConf;
    }

    // spark context
    @Bean
    public JavaSparkContext javaSparkContext(){
        return new JavaSparkContext(sparkConf());
    }
}
