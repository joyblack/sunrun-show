package com.sunrun.movieshow.algorithm.friend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class POJOFriend {
    public static Set<String> intersection(Set<String> A, Set<String> B){
        if(A == null || B == null){
            return null;
        }

        if(A.isEmpty() || B. isEmpty()){
            return null;
        }

        Set<String> result = new HashSet<>();
        result.addAll(A);
        result.retainAll(B);
        return result;
    }

    public static void main(String[] args) {
        Set<String> A = new HashSet<>();
        Set<String> B = new HashSet<>();

        A.add("A");
        A.add("B");
        A.add("C");

        B.add("B");
        B.add("C");
        B.add("D");

        System.out.println(intersection(A,B));
        /**
         * [B, C]
         */

    }
}
