package com.sunrun.movieshow.algorithm.nbc;

import com.sunrun.movieshow.algorithm.common.SparkHelper;
import edu.umd.cloud9.io.pair.PairOfStrings;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.util.List;
import java.util.Map;

// 朴素贝叶斯分类器
// 阶段1：训练阶段：使用训练数据建立一个朴素贝叶斯分类器 BuildNBClassifier
// 阶段2：测试阶段：使用新建立的NBC对新数据进行分类 NBCTester
public class NBCTester {
    public static void main(String[] args) {
        JavaSparkContext sc = SparkHelper.getSparkContext("NBCTester");

        // 实例存储文件的根目录
        String dfsUrl = "hdfs://10.21.1.24:9000/ball/";

        // == 1.导入要分类的数据集
        JavaRDD<String> testRdd = sc.textFile("data/nbc/test.txt");
        /**
         * 1,晴,热,高,弱
         * 2,晴,热,高,强
         * 3,阴,热,高,弱
         * 4,雨,温暖,高,弱
         */

        // == 2.加载分类器模型
        JavaPairRDD<PairOfStrings, DoubleWritable> modelRDD = sc.newAPIHadoopFile(dfsUrl + "nbc/pt",
                SequenceFileInputFormat.class,
                PairOfStrings.class,
                DoubleWritable.class,
                new Configuration()
        );
        // System.out.println(ptRDD.collect());
        /**
         *((高, 不),0.8), ((高, 不),0.8), ((高, 不),0.8),...
         */

        // == 3.使用map复制返回的对象:((高, 不),0.8)
        JavaPairRDD<Tuple2<String, String>, Double> ptRDD = modelRDD.mapToPair(t -> {
            // pairStrings left and right (feature-v,classification)
            Tuple2<String, String> K = new Tuple2<>(t._1.getLeftElement(), t._1.getRightElement());
            // V - the probably
            Double V = new Double(t._2.get());
            return new Tuple2<>(K, V);
        });

        // == 4.广播分类器
        Broadcast<Map<Tuple2<String, String>, Double>> broadcastPT = sc.broadcast(ptRDD.collectAsMap());

        //  == 5.广播所有分类类别
        JavaRDD<String> ctRDD = sc.textFile(dfsUrl + "nbc/ct");
        final Broadcast<List<String>> broadcastCT = sc.broadcast(ctRDD.collect());



        // == 6.对新数据进行分类: argMax II(P(C=c) * P(Ai|c))
        JavaPairRDD<String, String> testResult = testRdd.mapToPair(line -> {
            // broadcast value
            Map<Tuple2<String, String>, Double> pt = broadcastPT.getValue();
            List<String> ct = broadcastCT.getValue();

            // 解析新数据的每一个特征值
            String[] featureValues = line.split(",");

            // 选择类别
            String selectedClasses = "";

            // 当前的最大概率
            double maxPosterior = 0.0;

            // 计算:
            for (String Ci : ct) {
                // P(Ci)
                Double posterior = pt.get(new Tuple2<>("class", Ci));

                // 0-id n-true classify
                for (int i = 1; i < featureValues.length - 1; i++) {
                    // P(Ai|Ci)
                    Double probably = pt.get(new Tuple2<>(featureValues[i], Ci));
                    // 这里的逻辑有待探讨，可以理解为，当前类别下，没有这种特征值出现，那么当
                    // 一条数据的特征值为此值时，II(P(C=c) * P(Ai|c)) = 0,也就是该类别不可能被选择。
                    if (probably == null) {
                        posterior = 0.0;
                        break;
                    } else {
                        // P(Ci) * P(Ai|Ci)
                        posterior *= probably.doubleValue();
                    }
                }

                System.out.println(line + "," + Ci + posterior);

                if (selectedClasses == null) {
                    // 计算第1个分类的值
                    selectedClasses = Ci;
                    maxPosterior = posterior;
                } else {
                    if (posterior > maxPosterior) {
                        selectedClasses = Ci;
                        maxPosterior = posterior;
                    }
                }
            }
            return new Tuple2<>(line, "预测结果:" + selectedClasses);
        });

        testResult.saveAsTextFile(dfsUrl + "test02");
        /***
         * [root@h24 ~]# hadoop fs -cat /ball/test02/p*
         * (1,晴,热,高,弱,不)
         * (2,晴,热,高,强,不)
         * (3,阴,热,高,弱,是)
         * (4,雨,温暖,高,弱,不)
         */
    }


}
