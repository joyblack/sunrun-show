package com.sunrun.movieshow.java.concurrent.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.LongStream;

public class ForkJoinSumCalculatorTest{
    public static void main(String[] args) {
        // 需要计算的数组
        long n = 1_000_000;
        // 1~1000000
        long[] numbers = LongStream.rangeClosed(1, n).toArray();
        // create ForkJoinSumCalculator instance.
        ForkJoinSumCalculator task = new ForkJoinSumCalculator(numbers);
        // create pool and set forkjoin calculator to it.
        System.out.println(new ForkJoinPool().invoke(task));
        /**
         * 500000500000
         */
    }
}
