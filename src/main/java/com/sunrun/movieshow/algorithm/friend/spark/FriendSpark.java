package com.sunrun.movieshow.algorithm.friend.spark;

import avro.shaded.com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Sets;
import com.sunrun.movieshow.algorithm.common.SparkHelper;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import shapeless.Tuple;

import java.util.*;

/**
 * 共同好友的Spark解决方案
 * 输入：
 * [root@h24 ~]# hadoop fs -cat /friend/input/*
 * A B C D E
 * B A C D
 * C A B D E
 * D A B C
 * E A C
 * F A
 * 即第一列代表当前用户，后面的数据代表其好友列表。
 *
 * 输出
 * (A,B) => C,D
 *  两个好友之间的公共好友。
 */
public class FriendSpark {
    // 构建有序key，避免重复
    static Tuple2<String,String> buildSortedKey(String u1, String u2){
        if(u1.compareTo(u2) < 0){
            return new Tuple2<>(u1,u2);
        }else{
            return new Tuple2<>(u2,u1);
        }
    }

    public static void main(String[] args) {
        // 1.读取配置文件
        String hdfsUrl = "hdfs://10.21.1.24:9000/friend/";
        JavaSparkContext sc = SparkHelper.getSparkContext("CommonFriend");
        JavaRDD<String> rdd = sc.textFile(hdfsUrl + "input");

        // 2.解析内容
        /**
         * A B C
         * ((A,B),(B,C))
         * ((A,C),(B,C))
         */
        JavaPairRDD<Tuple2<String, String>, List<String>> pairs = rdd.flatMapToPair(line -> {
            String[] tokes = line.split(" ");

            // 当前处理的用户
            String user = tokes[0];

            // 该用户的好友列表
            List<String> friends = new ArrayList<>();
            for (int i = 1; i < tokes.length; i++) {
                friends.add(tokes[i]);
            }

            List<Tuple2<Tuple2<String, String>, List<String>>> result = new ArrayList<>();
            // 算法处理，注意顺序,依次抽取每一个好友和当前用户配对作为key，好友列表作为value输出，
            // 但如果该用户只有一个好友的话，那么他们的共同好友应该设置为空集
            if (friends.size() == 1) {
                result.add(new Tuple2<>(buildSortedKey(user, friends.get(0)), new ArrayList<>()));
            } else {
                for (String friend : friends) {
                    Tuple2<String, String> K = buildSortedKey(user, friend);
                    result.add(new Tuple2<>(K, friends));
                }
            }

            return result.iterator();
        });

        /**
         *  pairs.saveAsTextFile(hdfsUrl + "output1");
         * ((A,B),[B, C, D, E])
         * ((A,C),[B, C, D, E])
         * ((A,D),[B, C, D, E])
         * ((A,E),[B, C, D, E])
         * ((A,B),[A, C, D])
         * ((B,C),[A, C, D])
         * ((B,D),[A, C, D])
         * ((A,C),[A, B, D, E])
         * ((B,C),[A, B, D, E])
         * ((C,D),[A, B, D, E])
         * ((C,E),[A, B, D, E])
         * ((A,D),[A, B, C])
         * ((B,D),[A, B, C])
         * ((C,D),[A, B, C])
         * ((A,E),[A, C])
         * ((C,E),[A, C])
         * ((A,F),[])
         */

        // 3.直接计算共同好友，步骤是group以及reduce的合并过程。
        JavaPairRDD<Tuple2<String, String>, List<String>> commonFriends = pairs.reduceByKey((a, b) -> {
            List<String> intersection = new ArrayList<>();
            for (String item : b) {
                if (a.contains(item)) {
                    intersection.add(item);
                }
            }
            return intersection;
        });

        commonFriends.saveAsTextFile(hdfsUrl + "commonFriend");
        /**
         * [root@h24 ~]# hadoop fs -cat /friend/commonFriend/p*
         * ((A,E),[C])
         * ((C,D),[A, B])
         * ((A,D),[B, C])
         * ((C,E),[A])
         * ((A,F),[])
         * ((A,B),[C, D])
         * ((B,C),[A, D])
         * ((A,C),[B, D, E])
         * ((B,D),[A, C])
         */


    }
}
