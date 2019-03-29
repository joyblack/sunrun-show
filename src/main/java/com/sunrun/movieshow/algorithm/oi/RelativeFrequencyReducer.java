package com.sunrun.movieshow.algorithm.oi;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class RelativeFrequencyReducer extends Reducer<PairWord, LongWritable,PairWord, DoubleWritable> {

    // 当前处理的词汇，注意保证初始值不可能在语料库中出现；
    private String currentWord = "NOT_DEFINED";

    // 统计当前出现的次数
    private double totalCount = 0;

    private final DoubleWritable relativeCount = new DoubleWritable();

    @Override
    protected void reduce(PairWord key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        // 如果右词是*,则
        if(key.getRightElement().equals("*")){
            if(key.getLeftElement().equals(currentWord)){
                totalCount += getTotalCount(values);
            }else{
                currentWord = key.getLeftElement();
                totalCount = getTotalCount(values);
            }
        }else{
            int count = getTotalCount(values);
            relativeCount.set((double) count / totalCount);
            context.write(key, relativeCount);
        }
    }

    private int getTotalCount(Iterable<LongWritable> values) {
        int sum = 0;
        for (LongWritable value : values) {
            sum += value.get();
        }
        return sum;
    }
}
