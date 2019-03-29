package com.sunrun.movieshow.service;

import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;


@Service
public class SimilarityService {
    public HashMap<String, Object> jaccard(String a, String b, String separate){
        HashMap<String,Object> result = new HashMap<>();
        Set<String> setA = new HashSet<>();
        Set<String> setB = new HashSet<>();

        Arrays.stream(a.split(separate)).forEach(e -> {
            setA.add(e);
        });

        Arrays.stream(b.split(separate)).forEach(e -> {
            setB.add(e);
        });

        // transfer b
        System.out.println("a: " + setA);
        result.put("a", a);

        // transfer b
        System.out.println("b: " + setB);
        result.put("b", b);

        // intersection
        Set<String> intersection = new HashSet<>();
        intersection.addAll(setA);
        intersection.retainAll(setB);
        System.out.println("intersection a and b: " + intersection);
        result.put("intersection", intersection);

        // union
        Set<String> union = new HashSet<>();
        union.addAll(setA);
        union.addAll(setB);
        System.out.println("union a and b: " + union);
        result.put("union", union);

        // compute
        DecimalFormat format = new DecimalFormat("#.###%");
        result.put("jaccard",  format.format(intersection.size() / (union.size() * 1.0)));
        result.put("message",  "success!");
        return result;
    }

    public HashMap<String, Object> cosine(String a, String b, String separate) {
        HashMap<String,Object> result = new HashMap<>();
        List<Integer> listA = new ArrayList<>();
        List<Integer> listB = new ArrayList<>();

        Arrays.stream(a.split(separate)).forEach(e -> {
            listA.add(Integer.valueOf(e));
        });
        result.put("a", listA);

        Arrays.stream(b.split(separate)).forEach(e -> {
            listB.add(Integer.valueOf(e));
        });
        result.put("b", listB);

        if(listB.size() == listA.size()){
            int numerator = 0;
            int denominatorA = 0;
            int denominatorB = 0;
            for (int i = 0; i < listA.size(); i++) {
                numerator += listA.get(i) * listB.get(i);
                denominatorA += Math.pow(listA.get(i),2);
                denominatorB += Math.pow(listB.get(i),2);
            }
            double denominator = Math.sqrt(denominatorA * denominatorB);
            // compute
            DecimalFormat format = new DecimalFormat("#.###%");
            result.put("cosine",  format.format(numerator / denominator));
            result.put("message", "success!");
        }else{
            result.put("message", "error: setA length must be equals setB!");
        }

        return result;
    }

    /***
     * 举个例子，例如，有5个国家的国民生产总值分别为 10, 20, 30, 50 和 80 亿美元。 假设这5个国家 (顺序相同) 的贫困百分比分别为 11%, 12%, 13%, 15%, and 18% 。 令 x 和 y 分别为包含上述5个数据的向量: x = (1, 2, 3, 5, 8) 和 y = (0.11, 0.12, 0.13, 0.15, 0.18)。
     * 利用通常的方法计算两个向量之间的夹角  (参见 数量积), 未中心化 的相关系数是:
     * 10,20,30,50,80
     * 0.11,0.12,0.13,0.15,0.18
     */
    public HashMap<String, Object> pearson(String a, String b, String separate) {
        HashMap<String,Object> result = new HashMap<>();
        List<Double> listA = new ArrayList<>();
        List<Double> listB = new ArrayList<>();

        Arrays.stream(a.split(separate)).forEach(e -> {
            listA.add(Double.valueOf(e));
        });
        result.put("a", listA);

        Arrays.stream(b.split(separate)).forEach(e -> {
            listB.add(Double.valueOf(e));
        });
        result.put("b", listB);

        int n = listA.size();
        if(listB.size() == n){
            Double sumX = 0.0;
            Double sumY = 0.0;
            Double sumXY = 0.0;
            Double sumSquareX = 0.0;
            Double sumSquareY = 0.0;

            System.out.println("a:" + listA);
            System.out.println("b:" + listB);
            for (int i = 0; i < listA.size(); i++) {
                double x = listA.get(i);
                double y = listB.get(i);
                sumXY += x * y;
                sumX += x;
                sumY += y;
                sumSquareX += x*x;
                sumSquareY += y*y;
            }


            double z = (n * sumXY - sumX * sumY);
            double m = Math.sqrt(n * sumSquareX - sumX * sumX) * Math.sqrt(n * sumSquareY - sumY * sumY);
            System.out.println(z + ":" + m);
            double pearson =  z/m ;
            result.put("pearson", pearson);
            result.put("message", "success!");
        }else{
            result.put("message", "error: setA length must be equals setB!");
        }
        return result;
    }
}
