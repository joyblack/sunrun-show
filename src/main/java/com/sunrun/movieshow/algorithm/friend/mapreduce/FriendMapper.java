package com.sunrun.movieshow.algorithm.friend.mapreduce;

import edu.umd.cloud9.io.pair.PairOfStrings;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FriendMapper extends Mapper<LongWritable, Text, Text, Text> {

    private static final Text KEY = new Text();
    private static final Text VALUE = new Text();

    // 获取朋友列表
    static String getFriends(String[] tokens) {
        // 不可能再有共同好友
        if (tokens.length == 2) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < tokens.length; i++) {
            builder.append(tokens[i]);
            if (i < (tokens.length - 1)) {
                builder.append(",");
            }
        }
        return builder.toString();
    }


    // 使key有序，这里的有序只的是key的两个用户id有序，和整体数据无关
    static String buildSortedKey(String user, String friend) {
        long p = Long.parseLong(user);
        long f = Long.parseLong(friend);
        if (p < f) {
            return user + "," + friend;
        } else {
            return friend + "," + user;
        }
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] tokes = value.toString().split(" ");

        // user
        String user = tokes[0];

        // value
        VALUE.set(getFriends(tokes));

        // rescue keys
        for (int i = 1; i < tokes.length ; i++) {
            String otherU = tokes[i];
            KEY.set(buildSortedKey(user,otherU));
            context.write(KEY,VALUE);
        }
    }
}
