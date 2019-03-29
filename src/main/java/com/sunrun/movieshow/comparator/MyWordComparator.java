package com.sunrun.movieshow.comparator;

import scala.Tuple2;

import java.io.Serializable;
import java.util.Comparator;
//自定义比较器类
public class MyWordComparator implements Comparator<Tuple2<String, Integer>>, Serializable {
    @Override
    public int compare(Tuple2<String, Integer> o1, Tuple2<String, Integer> o2) {
        return -o1._2.compareTo(o2._2);   //返回TopN
    }
}