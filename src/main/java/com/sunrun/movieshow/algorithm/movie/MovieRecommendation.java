package com.sunrun.movieshow.algorithm.movie;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple7;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 电影推荐：依据相似度算法寻找相似度高的电影列表
 */
public class MovieRecommendation {
    public static void main(String[] args) {
        String inputPath = "movie_recommend.txt";;
        String outputPath = "out_put" + UUID.randomUUID();
        System.out.println("Check input parameters...");
        System.out.println("The input file path: " + inputPath);
        System.out.println("The output file path: " + outputPath);

        // 2.Get spark context.
        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("movie-recommend").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        // 3.Get input file DataSet.
        JavaRDD<String> firstRDD = sc.textFile(inputPath);
        System.out.println("====== debug1: line: K=V=line ======");
        firstRDD.foreach(line -> System.out.println("debug 1: " + line));

        // 4. 找出谁对电影进行了评论：Tuple(movie,Tuple(user,score))
        JavaPairRDD<String, Tuple2<String, Integer>> moviesRDD = firstRDD.mapToPair(
                line -> {
                    // 解析列
                    String[] tokens = line.split("\t");
                    String user = tokens[0];
                    String movie = tokens[1];
                    Integer score = new Integer(tokens[2]);
                    // 返回Tuple(movie,Tuple(user,score))
                    return new Tuple2<String, Tuple2<String, Integer>>(movie, new Tuple2<>(user, score));
                }
        );
        System.out.println("====== debug2: moviesRDD: K = <movie>, V = Tuple2<user,score> ======");
        moviesRDD.foreach( debug2 -> {
            System.out.println("debug2: key = " + debug2._1 + "\t value = " + debug2._2);
        });

        // 5.按movie对movieRDD进行分组：Tuple2<movie, Iterate<Tuple<user, score>>>
        JavaPairRDD<String, Iterable<Tuple2<String, Integer>>> moviesGroupRDD = moviesRDD.groupByKey();
        System.out.println("===== debug3: moviesGroupRDD: K = <movie>, V = Iterable<Tuple2<user,score>> ======");
        moviesGroupRDD.foreach(debug3 -> {
            System.out.println("debug2: key = " + debug3._1 + "\t" + debug3._2);
        });

        // 6.找出每个电影的评分数量（即评论人数）：Tuple2<user, Tuple3<movie,score, numberOfScorer>>
        JavaPairRDD<String, Tuple3<String, Integer, Integer>> usersRDD = moviesGroupRDD.flatMapToPair(mg -> {
            // 当前处理的电影
            String movie = mg._1;
            // 统计评分人数
            int countScores = 0;
            // 从同一个电影对应的多个组中收集所有用户-评分信息(user,score)
            List<Tuple2<String, Integer>> listUserAndScores = new ArrayList<>();
            // (user,score)
            for (Tuple2<String, Integer> userAndScore : mg._2) {
                countScores++;
                // put to list.
                listUserAndScores.add(userAndScore);
            }
            // 返回结果 List(<user,<movie,score,numberOfScores>>)
            List<Tuple2<String, Tuple3<String, Integer, Integer>>> result = new ArrayList<>();
            for (Tuple2<String, Integer> listUserAndScore : listUserAndScores) {
                String user = listUserAndScore._1;
                Integer score = listUserAndScore._2;
                // 组合为T3(movie,score,numberOfScores)
                Tuple3<String, Integer, Integer> t3 = new Tuple3<>(movie, score, countScores);
                // 写入结果
                result.add(new Tuple2<>(user, t3));
            }
            // iterator扁平化
            return result.iterator();
        });
        System.out.println("===== debug4: usersRDD: K = <user>, V = Tuple3<movie,score,numberOfScores> ======");
        usersRDD.foreach(u -> {
            System.out.println("debug4: key = " + u._1 + "\t" + "value = " + u._2);
        });

        // 7.进行自连接：以user为键
        JavaPairRDD<String, Tuple2<Tuple3<String, Integer, Integer>, Tuple3<String, Integer, Integer>>> joinRDD = usersRDD.join(usersRDD);
        System.out.println("===== debug5: joinRDD: K = <user>, V = Tuple2<Tuple3<movie,score,numberOfScores>,Tuple3<movie,score,numberOfScores>> ======");
        joinRDD.foreach(join -> {
            System.out.println("debug5: key = " + join._1 + "\t" + "value = " + join._2);
        });

        // 8.删除重复的(movie1,movie2)对,确保对于任意的键值对，都有movie1 < movie2
        JavaPairRDD<String, Tuple2<Tuple3<String, Integer, Integer>, Tuple3<String, Integer, Integer>>> filterJoinRDD = joinRDD.filter(j -> {
            // (movie, score, numberOfScore)
            Tuple3<String, Integer, Integer> v1 = j._2._1;
            Tuple3<String, Integer, Integer> v2 = j._2._2;
            // get movie name
            String movieName1 = v1._1();
            String movieName2 = v2._1();
            if (movieName1.compareTo(movieName2) < 0) {
                return true;
            } else {
                return false;
            }
        });
        System.out.println("===== debug6: filterJoinRDD: K = <user>, V = Tuple2<Tuple3<movie,score,numberOfScores>,Tuple3<movie,score,numberOfScores>> ======");
        filterJoinRDD.foreach(filterJoin -> {
            System.out.println("debug6: key = " + filterJoin._1 + "\t" + "value = " + filterJoin._2);
        });

        // 9.生成所有的(movie1,movie2)组合
        JavaPairRDD<
                Tuple2<String, String>,
                Tuple7<Integer, Integer, Integer, Integer, Integer, Integer, Integer>> moviePairsRDD = filterJoinRDD.mapToPair(fj -> {
            // key = user去掉
            // String user = fj._1;

            // 获得两个电影的信息: Tuple(movie,score,numberOfScores)
            Tuple3<String, Integer, Integer> m1 = fj._2._1;
            Tuple3<String, Integer, Integer> m2 = fj._2._2;

            // 组合两个电影的名字，作为返回的键
            Tuple2<String, String> m1m2Key = new Tuple2<>(m1._1(), m2._1());

            // 获取两个电影的分数计算系数，作为返回的V
            Tuple7<Integer, Integer, Integer, Integer, Integer, Integer, Integer> value = new Tuple7<>(
                    m1._2(), //m1 score
                    m1._3(), //m1 hot（number of scores）
                    m2._2(), // m2 score
                    m2._3(), // m2 hot（number of scores）
                    m1._2() * m2._2(), // m1.score * m2.score
                    m1._2() * m1._2(), // square of m1.score
                    m2._2() * m2._2() // square of m2.score
            );
            return new Tuple2<>(m1m2Key, value);
        });
        System.out.println("===== debug7: moviePairsRDD: K = Tuple<m1,m2>, V = Tuple7<...> ======");
        moviePairsRDD.foreach(moviePair -> {
            System.out.println("debug7: key = " + moviePair._1 + "\t" + "value = " + moviePair._2);
        });

        // 10.电影对分组: (movie1,movie2) -> list(<m1_score,number_score,....>)
        JavaPairRDD<Tuple2<String, String>,
                Iterable<Tuple7<Integer, Integer, Integer, Integer, Integer, Integer, Integer>>> moviePairGroupRDD = moviePairsRDD.groupByKey();
        System.out.println("===== debug8: moviePairs: K = Tuple<m1,m2>, V = List(Tuple7<...>) ======");
        moviePairGroupRDD.foreach(moviePairGroup -> {
            System.out.println("debug8: key = " + moviePairGroup._1 + "\t" + "value = " + moviePairGroup._2);
        });

        // end.计算相似度数据
        JavaPairRDD<Tuple2<String, String>, Tuple3<Double, Double, Double>> correlations = moviePairGroupRDD.mapValues(value -> {
            return CalculateSimilarity.calculateCorRelation(value);
        });
        System.out.println("====== Movie Similarity collect is: ======");
        correlations.mapToPair(c ->{
            String key = "(" + c._1._1 + "," +  c._1._2 + ")";
            return new Tuple2<>(key, c._2);
        }).sortByKey().foreach(r -> {
            System.out.println(r._1  + "\t => \t" + r._2);
        });







    }

}
