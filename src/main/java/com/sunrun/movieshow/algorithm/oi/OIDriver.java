package com.sunrun.movieshow.algorithm.oi;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.util.UUID;

/**
 * 逆转（反转排序）排序，实现单词频度计算。
 */
public class OIDriver {
    public static void main(String[] args) throws Exception {
        Job job = Job.getInstance(new Configuration());

        job.setJarByClass(OIDriver.class);
        job.setMapperClass(RelativeFrequencyMapper.class);
        job.setReducerClass(RelativeFrequencyReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setOutputValueClass(PairWord.class);
        job.setOutputKeyClass(DoubleWritable.class);

        job.setPartitionerClass(OrderInversionPartitioner.class);
        //job.setNumReduceTasks(4);
        // add your group comparator class.
        //job.setGroupingComparatorClass(TemperatureGroupComparator.class);


        FileInputFormat.setInputPaths(job,new Path("words.txt"));
        FileOutputFormat.setOutputPath(job,new Path("output_" + UUID.randomUUID()) );
        System.exit(job.waitForCompletion(true)? 1:0);

    }
}
