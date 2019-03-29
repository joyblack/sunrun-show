package com.sunrun.movieshow.algorithm.oi;

import edu.umd.cloud9.io.pair.PairOfStrings;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

public class OrderInversionPartitioner extends Partitioner<PairOfStrings, IntWritable> {
    @Override
    public int getPartition(PairOfStrings key, IntWritable value, int numberOfPartitions) {
        // key = left,right word
        String leftWord = key.getLeftElement();
        return (int)Math.abs(hash(leftWord) % numberOfPartitions);
    }

    private static long hash(String word){
        // 质数
        long h = 1125899906842597L;
        int length = word.length();
        for (int i = 0; i < length; i++) {
            h = 31 * h + word.charAt(i);
        }
        return h;
    }
}
