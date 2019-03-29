package com.sunrun.movieshow.algorithm.markov;

import com.sunrun.movieshow.algorithm.common.SparkHelper;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;
public class SparkMarkov {
    public static void main(String[] args) {
        JavaSparkContext sc = SparkHelper.getSparkContext("markov");

        // 1.归约到不同的分区
        JavaRDD<String> rdd = sc.textFile("data/markov").coalesce(8);

        // 2.得到(顾客ID,(交易时间，交易额))
        JavaPairRDD<String, Tuple2<Long, Integer>> pairRDD = rdd.mapToPair(line -> {
            // tokens[0] = customer-id
            // tokens[1] = transaction-id
            // tokens[2] = purchase-date
            // tokens[3] = amount
            String[] tokens = line.split(",");
            if (tokens.length != 4) {
                return null;
            } else {
                long date = 0;
                try {
                    date = DateUtil.getDateAsMilliSeconds(tokens[2]);
                }
                catch(Exception e) {
                    // 会有异常数据
                }
                int amount = Integer.parseInt(tokens[3]);
                Tuple2<Long, Integer> V = new Tuple2<>(date, amount);
                return new Tuple2<>(tokens[0], V);
            }
        });
        /**
         * (V31E55G4FI,(1356969600000,123))
         * (301UNH7I2F,(1356969600000,148))
         * (PP2KVIR4LD,(1356969600000,163))
         * (AC57MM3WNV,(1356969600000,188))
         * ...
         */

        // 4.依据交易ID进行分组 - 用户信息在最后的状态链中是没用的，但他可以限定一些状态切换的内在关系，
        // 这在很多算法过程中都是隐含条件，需要经验判断。
        JavaPairRDD<String, Iterable<Tuple2<Long, Integer>>> customerRDD = pairRDD.groupByKey();
        /**
         * (V31E55G4FI,(1356969600000,123),1356969600000,148)....)
         * ...
         */

        // 5.创建马尔科夫状态序列 (c_id,<(time1,amount),(time2,amount)>) => (String)
        JavaPairRDD<String, List<String>> stateSequence =
                customerRDD.mapValues((Iterable<Tuple2<Long,Integer>> dateAndAmount) -> {
                    List<Tuple2<Long,Integer>> list = toList(dateAndAmount);
                    Collections.sort(list, TupleComparatorAscending.INSTANCE);
                    // now convert sorted list (be date) into a "state sequence"
                    List<String> stateSequence1 = toStateSequence(list);
                    return stateSequence1;
                });

        /**
         * stateSequence.saveAsTextFile("out/" + UUID.randomUUID());
         * 顾客id，与其相关的状态序列
         * (K40E0LA5DL,[LL, MG, SG, SL, SG, MG, SL, SG, SL, SG, LL])
         * (ICF0KFGK12,[SG, SL, SE, SG, LL, LG])
         * (4N0B1U5HVG,[SG, ML, MG, SG, SL, SG, SL, SG, ML])
         * (3KJR1907D9,[SG, SL, ML, SG, ML, LG])
         * (47620I9LOD,[LG, SL, ML, MG, SG, SL, SG, SL, SG])
         */

        // 6.接下来，我们将状态序列以窗口为2的方式依次移动，生成一个个形如((LL, MG),1)的对
        JavaPairRDD<Tuple2<String, String>, Integer> model = stateSequence.flatMapToPair(s -> {
            // 输出形式((fromState,toState),times)
            ArrayList<Tuple2<Tuple2<String, String>, Integer>> mapperOutput = new ArrayList<>();
            List<String> states = s._2;

            if (states == null) {
                return Collections.emptyIterator();
            } else {
                for (int i = 0; i < states.size() - 1; i++) {
                    String fromState = states.get(i);
                    String toState = states.get(i + 1);
                    Tuple2<String, String> k = new Tuple2<>(fromState, toState);
                    mapperOutput.add(new Tuple2<>(k, 1));
                }
            }
            return mapperOutput.iterator();
        });
        /**
         * model.saveAsTextFile("out/model");
         * ((LG,LL),1)
         * ((LL,MG),1)
         * ((MG,SG),1)
         * ((SG,SL),1)
         * ((SL,ME),1)
         * ((ME,MG),1)
         *
         */

        // 7.我们需要将这些结果组合归约，将相同的状态序列进行合并，即从(f,t),1) => ((f,t),3000)的形式。
        JavaPairRDD<Tuple2<String, String>, Integer> markovModel = model.reduceByKey((a, b) -> a + b);

        /**
         * markovModel.saveAsTextFile("/out/markov");
         * ((LL,SL),993)
         * ((MG,LL),1859)
         * ((LE,ME),25)
         * ((SL,ME),1490)
         * ((LG,ME),153)
         * ((ML,LG),3991)
         * ((ME,ME),58)
         */

        // 8.我们格式化一下输出，将其形式转变为(f,t,times)的形式
        JavaRDD<String> markovFormatModel = markovModel.map(t -> t._1._1 + "\t" + t._1._2 + "\t" + t._2);

        // 9.将结果存储到HDFS服务器，当然也可以存储到本地，这时候解析类就要使用本地文件系统的API
        String markovFormatModelStorePath = "hdfs://10.21.1.24:9000/markov/";
        markovFormatModel.saveAsTextFile(markovFormatModelStorePath);

        // 9.将最终结果进行转换，生成马尔科夫概率模型
        MarkovTableBuilder.transport(markovFormatModelStorePath);
        /** 这是一个state阶的方阵，A(ij)表示状态i到状态j的转化概率
         * 0.03318,0.02441,0.6608,0.1373,0.007937,0.02398,0.06456,0.009522,0.03832
         * 0.3842,0.02532,0.2709,0.1260,0.009590,0.01331,0.1251,0.01083,0.03477
         * 0.6403,0.02881,0.05017,0.1487,0.01338,0.008602,0.08359,0.01446,0.01196
         * 0.02081,0.002368,0.7035,0.01170,0.004638,0.1518,0.03901,0.005933,0.06030
         * 0.4309,0.02140,0.2657,0.1277,0.009697,0.02006,0.08159,0.009530,0.03344
         * 0.1847,0.01816,0.5316,0.1303,0.007175,0.02351,0.06265,0.008136,0.03379
         * 0.01847,0.004597,0.6115,0.02068,0.003091,0.1346,0.06828,0.01424,0.1245
         * 0.2250,0.01900,0.2427,0.06291,0.006335,0.03182,0.2757,0.03801,0.09847
         * 0.2819,0.01944,0.1954,0.07990,0.008015,0.03202,0.2612,0.04427,0.07781
         */

    }



    // 将迭代器转化为数组
    static List<Tuple2<Long,Integer>> toList(Iterable<Tuple2<Long,Integer>> iterable) {
        List<Tuple2<Long,Integer>> list = new ArrayList<Tuple2<Long,Integer>>();
        for (Tuple2<Long,Integer> element: iterable) {
            list.add(element);
        }
        return list;
    }

    // 按时间进行排序
    static class TupleComparatorAscending implements Comparator<Tuple2<Long, Integer>>, Serializable {
        final static TupleComparatorAscending INSTANCE = new TupleComparatorAscending();
        @Override
        public int compare(Tuple2<Long, Integer> t1, Tuple2<Long, Integer> t2) {
            // return -t1._1.compareTo(t2._1);     // sorts RDD elements descending
            return t1._1.compareTo(t2._1);         // sorts RDD elements ascending
        }
    }

    // 将一个有序的交易序列（List<Tuple2<Date,Amount>>）转化为状态序列(List<String>)，其中各个元素分别表示一个马尔科夫状态。
    static List<String> toStateSequence(List<Tuple2<Long,Integer>> list){
        // 没有足够的数据
        if(list.size() < 2){
            return null;
        }

        List<String> stateSequence = new ArrayList<>();
        // == 两两配对计算结果
        Tuple2<Long, Integer> prior = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            Tuple2<Long, Integer> current = list.get(i);

            // === 计算时间差（天），由于数据是以ms计数的，因此需要转化为天(1d = 24*60*60*1000=86400000ms)
            long dayDiff = (current._1 - prior._1) / 86400000;

            // === 获取交易额信息
            int priorAmount = prior._2;
            int currentAmount = current._2;

            // === 根据业务规则转化为字母表示
            // ==== 处理时间关系
            String dd = null;
            if(dayDiff < 30){
                dd = "S";
            }else if(dayDiff < 60){
                dd = "M";
            }else {
                dd = "L";
            }

            // ==== 处理金额关系: 使用两次交易额的比重
            String ad = null;
            if(priorAmount < 0.9 * currentAmount){
                ad = "L";
            }else if(priorAmount < 1.1 * currentAmount){
                ad = "E";
            }else{
                ad = "G";
            }

            // === 组合结果
            String element = dd + ad;
            stateSequence.add(element);
            // 大小为2的窗口前进一格
            prior = current;
        }
        return stateSequence;
    }




}
