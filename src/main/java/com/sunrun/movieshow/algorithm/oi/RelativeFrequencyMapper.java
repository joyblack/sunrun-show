package com.sunrun.movieshow.algorithm.oi;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class RelativeFrequencyMapper extends Mapper<LongWritable, Text, PairWord,LongWritable> {

    // 窗口大小配置
    private int neighborWindow;

    private PairWord pair = new PairWord();

    // 每次写入的对象，节省开销
    private final LongWritable totalCount = new LongWritable();
    private static final LongWritable ONE_COUNT = new LongWritable();


    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        // 驱动器将设置"neighbor.window"，读取窗口大小配置
        neighborWindow = context.getConfiguration().getInt("neighbor.window",2);
    }

    /**
     *
     * @param key 行号
     * @param value 单词集
     * @param context 上下文对象
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] tokens = value.toString().split(" ");
        if(tokens.length < 2){
            return;
        }

        for (int i = 0; i < tokens.length; i++) {
            String word = tokens[i];
            pair.setLeftElement(word);

            // 获取当前当次的邻域索引，进行访问
            int start = (i - neighborWindow < 0) ? 0 : i - neighborWindow;
            int end = (i + neighborWindow > tokens.length) ? tokens.length - 1: i + neighborWindow;
            for (int j = start; j < end; j++) {
                // 邻域不包括自身
                if( i == j){
                    continue;
                }
                // 写入右词，并删除多余的空格
                pair.setRightElement(tokens[j].replaceAll("\\W",""));
                context.write(pair, ONE_COUNT);
            }
            // 当前pair的邻域单词总数
            pair.setRightElement("*");
            totalCount.set(end - start);
            context.write(pair, totalCount);
        }
    }


}
