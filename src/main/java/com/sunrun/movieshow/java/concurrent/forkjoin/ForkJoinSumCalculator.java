package com.sunrun.movieshow.java.concurrent.forkjoin;

import java.util.concurrent.RecursiveTask;

public class ForkJoinSumCalculator extends RecursiveTask<Long> {
    // compute array
    private final long[] numbers;
    private final int start;
    private final int end;

    // 不再切分的阈值
    private static final long THRESHOLD = 10_000L;

    private ForkJoinSumCalculator(long[] numbers, int start, int end){
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    // 用于创建主任务
    public ForkJoinSumCalculator(long[] numbers){
        this(numbers,0,numbers.length);
    }

    @Override
    protected Long compute() {
        // 计算当前处理的数组长度
        int length = end - start;

        // 判断是否需要切分
        if(length <= THRESHOLD){
            // 小于当前阈值，顺序计算结果
            return computeSequentially();
        }
        // == 否则切分计算
        ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length / 2);
        // 利用另一个ForkJoinPool线程异步执行新创建的子任务
        leftTask.fork();

        ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length / 2, end);
        // 这里也可以调用join，但比起再次分配线程，最好还是直接调用compute好些。
        Long rightResult = rightTask.compute();

        // 获取切分的另一个任务的结果，这一句代码放在最后，避免阻塞
        Long leftResult = leftTask.join();

        // 返回结算结果
        return leftResult + rightResult;
    }

    /**
     * 顺序计算结果
     * @return 当前顺序计算的结果
     */
    private Long computeSequentially() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }
}
