package com.sunrun.movieshow.algorithm.secondsort;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/***
 * 二次排序。
 * 使用文件 student.txt 学生信息
 * 学号 专业 分数
 * 201124133160,1994-01-02,97.65
 * 201124133155,1992-12-02,88.55
 * 201124133178,1993-04-13,70.72
 * ...
 * 按专业分组，分数排序
 */
public class StudentDriver {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("second-rank").setMaster("local").set("spark.testing.memory", "2147480000");
        JavaSparkContext sc = new JavaSparkContext(conf);


        // 1. 加载文件，使用1个分区
        JavaRDD<String> lines = sc.textFile("student.txt");
        // 2. turn students ele.
        JavaRDD<Student> map = lines.map(line -> {
            System.out.println(line);
                    String[] strings = line.split(",");
                    String id = strings[0];
                    String major = strings[1];
                    Double score = Double.valueOf(strings[2]);
                    return new Student(id, major, score);
                }
        );

        // 3.debug 1: show old list
        System.out.println("=== debug1: the old list is ===");
        map.foreach(student -> System.out.println(student));

        // 4.group by major
        JavaPairRDD<String, Iterable<Student>> groupStudentRDD = map.groupBy(stu -> stu.getMajor());
        System.out.println("=== debug2:group by major is ===");
        groupStudentRDD.foreach(student -> System.out.println(student));

        // 5.order by score
        JavaPairRDD<String, List<Student>> sortedStudentsPairRDD = groupStudentRDD.mapValues(students -> {
            List<Student> studentList = new ArrayList<>();
            for (Student student : students) {
                studentList.add(student);
            }
            return studentList.stream().sorted(Comparator.comparing(Student::getScore)).collect(Collectors.toList());
        });
        System.out.println("=== debug3:group by scores ====");
        sortedStudentsPairRDD.foreach(s -> System.out.println(s._1 + ":" + s._2));

        // reduce result
        System.out.println("=== debug3:group by scores and sorted values result ====");
        JavaRDD<Iterator<Student>> map1 = sortedStudentsPairRDD.map(s -> s._2.iterator());
        System.out.println();

    }

}
