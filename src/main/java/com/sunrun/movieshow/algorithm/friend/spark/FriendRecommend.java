package com.sunrun.movieshow.algorithm.friend.spark;

import avro.shaded.com.google.common.collect.ImmutableCollection;
import com.sunrun.movieshow.algorithm.common.SparkHelper;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

import java.util.*;

/**
 * 这次根据输入给每个用户推荐N个好友
 * == 输入
 * 1 2,3,4,5,6,7,8
 * 2 1,3,4,5,7
 * 3 1,2
 * 4 1,2,6
 * 5 1,2
 * 6 1,4
 * 7 1,2
 * 8 1
 *  第一列代表的是用户id，剩下的代表的是该用户的直接好友id。
 * 例如，我们考察第一列，他代表的含义便是：用户1的直接好友有2~8.
 *
 * == 输出
 *
 */
public class FriendRecommend {

    static Tuple2<Long,Long> T2(long a, long b) {
        return new Tuple2<Long,Long>(a, b);
    }

    static Tuple2<Long,Tuple2<Long,Long>> T2(long a, Tuple2<Long,Long> b) {
        return new Tuple2<Long,Tuple2<Long,Long>>(a, b);
    }

    public static void main(String[] args) {
        JavaSparkContext sc = SparkHelper.getSparkContext("friend recommend");

        // 1.加载文件
        JavaRDD<String> rdd = sc.textFile("data/ft.txt");

        // 2.获取所有可能的好友对
        //
        JavaPairRDD<Long, Tuple2<Long, Long>> pairRDD = rdd.flatMapToPair(line -> {
            String[] t = line.split(" ");
            // main user
            Long user = Long.parseLong(t[0]);

            // friends
            String[] fStrings = t[1].split(",");


            List<Long> friends = new ArrayList<>();
            List<Tuple2<Long, Tuple2<Long, Long>>> mapperOutput = new ArrayList<>();
            for (String fString : fStrings) {
                long toUser = Long.parseLong(fString);
                friends.add(toUser);
                // -1代表直接好友关系
                Tuple2<Long, Long> directFriend = T2(toUser, -1);
                // (1,(3,-1))
                mapperOutput.add(T2(user, directFriend));
            }

            // 注意这里的算法和我们的寻找共同好友的算法是一样的
            for (int i = 0; i < friends.size(); i++) {
                for (int j = 0; j < friends.size(); j++) {
                    // 可能的好友1
                    Tuple2<Long, Long> possible1 = T2(friends.get(i), user);
                    mapperOutput.add(T2(friends.get(j), possible1));

                    // 可能的好友2
                    Tuple2<Long, Long> possible2 = T2(friends.get(j), user);
                    mapperOutput.add(T2(friends.get(i), possible2));
                }
            }

            return mapperOutput.iterator();
        });

        // 3.分组
        JavaPairRDD<Long, Iterable<Tuple2<Long, Long>>> group = pairRDD.groupByKey();

        // 4.找出交集
        // Find intersection of all List<List<Long>>
        // mapValues[U](f: (V) => U): JavaPairRDD[K, U]
        // Pass each value in the key-value pair RDD through a map function without changing the keys;
        // this also retains the original RDD's partitioning.
        JavaPairRDD<Long, String> recommendations =
                group.mapValues(new Function< Iterable<Tuple2<Long, Long>>, // input
                                        String                        // final output
                                        >() {
                    @Override
                    public String call(Iterable<Tuple2<Long, Long>> values) {

                        // mutualFriends.key = the recommended friend
                        // mutualFriends.value = the list of mutual friends
                        final Map<Long, List<Long>> mutualFriends = new HashMap<Long, List<Long>>();
                        for (Tuple2<Long, Long> t2 : values) {
                            final Long toUser = t2._1;
                            final Long mutualFriend = t2._2;
                            final boolean alreadyFriend = (mutualFriend == -1);

                            if (mutualFriends.containsKey(toUser)) {
                                if (alreadyFriend) {
                                    mutualFriends.put(toUser, null);
                                }
                                else if (mutualFriends.get(toUser) != null) {
                                    mutualFriends.get(toUser).add(mutualFriend);
                                }
                            }
                            else {
                                if (alreadyFriend) {
                                    mutualFriends.put(toUser, null);
                                }
                                else {
                                    List<Long> list1 = new ArrayList<Long>(Arrays.asList(mutualFriend));
                                    mutualFriends.put(toUser, list1);
                                }
                            }
                        }
                        return buildRecommendations(mutualFriends);
                    }
                });


        sc.close();

    }

    static String buildRecommendations(Map<Long, List<Long>> mutualFriends) {
        StringBuilder recommendations = new StringBuilder();
        for (Map.Entry<Long, List<Long>> entry : mutualFriends.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            recommendations.append(entry.getKey());
            recommendations.append(" (");
            recommendations.append(entry.getValue().size());
            recommendations.append(": ");
            recommendations.append(entry.getValue());
            recommendations.append("),");
        }
        return recommendations.toString();
    }
}
