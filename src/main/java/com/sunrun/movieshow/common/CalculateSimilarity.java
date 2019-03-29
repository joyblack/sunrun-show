package com.sunrun.movieshow.common;

import scala.Tuple3;
import scala.Tuple7;

import java.io.Serializable;

// 辅助类
public class CalculateSimilarity implements Serializable {
    /**
     * t7_1: m1_score
     * t7_2: number_of_m1_scores
     * t7_3: m2_score
     * t7_4: number_of_m2_scores
     * t7_5: m1_score * m2_score
     * t7_6: Square(m1_score)
     * t7_7: Square(m2_score)
      */
    public static Tuple3<Double, Double, Double> calculateCorRelation(Iterable<Tuple7<
            Integer,Integer,Integer,Integer,Integer,Integer,Integer>> t7s){
        int groupSize = 0;// 各个向量的长度（即传入的t7的长度）
        int m1m2ProductSum = 0;// (m1_score * m2_score)之和
        int score1Sum = 0;// score1的和
        int score2Sum = 0; // score2之和
        int score1SquareSum = 0;// score1Square之和
        int score2SquareSum = 0; // score2Square之和
        int maxNumberOfScore1 = 0; // max1: number of score1
        int maxNumberOfScore2 = 0; // max2: number of score2

        for (Tuple7<Integer, Integer, Integer, Integer, Integer, Integer, Integer> t7 : t7s) {
            groupSize ++; // the t7s length.
            score1Sum += t7._1(); //  Sum(m1_score).
            score2Sum += t7._3(); // Sum(m2_score).
            m1m2ProductSum += t7._5(); // Sum(m1_score * m2_score)
            score1SquareSum += t7._6(); // Sum(Square(m1_score))
            score2SquareSum += t7._7(); //Sum(Square(m2_score))

            // get Max(numberOfScore1)
            int numberOfScore1 = t7._2();
            if(numberOfScore1 > maxNumberOfScore1){
                maxNumberOfScore1 = numberOfScore1;
            }

            // get Max(numberOfScore2)
            int numberOfScore2 = t7._4();
            if(numberOfScore2 > maxNumberOfScore2){
                maxNumberOfScore2 = numberOfScore2;
            }
        }
        // (pearson, cosine, jaccad)
        return new Tuple3<Double, Double ,Double>(
                calculatePerSonSimilarity(groupSize,m1m2ProductSum,score1Sum,score2Sum,score1SquareSum,score2SquareSum),
                calculateCosSimilarity(m1m2ProductSum, score1SquareSum, score2SquareSum),
                calculateJacardSimilarity(groupSize, maxNumberOfScore1, maxNumberOfScore2)
        );
    }

    // 皮尔逊公式
    private static double calculatePerSonSimilarity(double n, double Exy, double Ex, double Ey, double Ex2, double Ey2) {
        double numerator = n * Exy - Ex * Ey;
        double denominator = Math.sqrt(n * Ex2 - Ex * Ex)
                * Math.sqrt(n * Ey2 - Ey * Ey);
  //      System.out.println(denominator);
        return numerator / denominator;
    }

    // 余弦相似度
    private static double calculateCosSimilarity(double Exy, double Ex2, double Ey2){
        return Exy/(Math.sqrt(Ex2 * Ey2));
    }

    // 杰卡德相似度
    private static double calculateJacardSimilarity(double common, double total1, double total2){
        return common/(total1 + total2 - common);
    }
}
