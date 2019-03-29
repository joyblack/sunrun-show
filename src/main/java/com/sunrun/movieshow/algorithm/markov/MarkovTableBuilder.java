package com.sunrun.movieshow.algorithm.markov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 输入数据生成马尔科夫状态表
 */
public class MarkovTableBuilder {
    // 状态的数量
    private int numberOfState;

    private int scale;

    // 状态表
    private double[][] table = null;

    // 状态序列
    private Map<String, Integer> states = null;


    public MarkovTableBuilder(int numberOfState, int scale){
        this(numberOfState);
        this.scale = scale;
    }

    private MarkovTableBuilder(int numberOfState){
        this.numberOfState = numberOfState;
        table = new double[numberOfState][numberOfState];
        initStates();
    }

    // 初始化state状态
    private void initStates(){
        states = new HashMap<String, Integer>();
        states.put("SL", 0);
        states.put("SE", 1);
        states.put("SG", 2);
        states.put("ML", 3);
        states.put("ME", 4);
        states.put("MG", 5);
        states.put("LL", 6);
        states.put("LE", 7);
        states.put("LG", 8);
    }

    // 将状态信息添加到状态表
    public void add(StateItem item){
        // 获取该状态对应的角标
        int row = states.get(item.fromState);
        int column = states.get(item.toState);
        table[row][column] = item.count;
    }

    public void normalize() {
        //
        // 拉普拉斯校正:一般通过将所有的计数+1来进行。see: http://cs.nyu.edu/faculty/davise/ai/bayesText.html
        for (int r = 0; r < numberOfState; r++) {
            boolean gotZeroCount = false;
            for (int c = 0; c < numberOfState; c++) {
                if(table[r][c] == 0) {
                    gotZeroCount = true;
                    break;
                }
            }
            if (gotZeroCount) {
                for (int c = 0; c < numberOfState; c++) {
                    table[r][c] += 1;
                }
            }
        }
        // normalize
        for (int r = 0; r < numberOfState; r++) {
            double rowSum = getRowSum(r);
            for (int c = 0; c < numberOfState; c++) {
                table[r][c] = table[r][c] / rowSum;
            }
        }
    }

    // 获取rowNumber行的累加结果
    public double getRowSum(int rowNumber) {
        double sum = 0.0;
        for (int column = 0; column < numberOfState; column++) {
            sum += table[rowNumber][column];
        }
        return sum;
    }

    /**
     * 存储状态表:作为示例，只是将结果输出即可。
     */
    private void persist() {
        for (int row = 0; row < numberOfState; row++) {
            StringBuilder builder = new StringBuilder();
            for (int column = 0; column < numberOfState; column++) {
                double element = table[row][column];
                builder.append(String.format("%.4g", element));
                if (column < (numberOfState - 1)) {
                    builder.append(",");
                }
            }
            System.out.println(builder.toString());
        }
    }



    public static void transport(String markovFormatDataPath){
        List<StateItem> items = ReadDataFromHDFS.readDirectory(markovFormatDataPath);
        MarkovTableBuilder markovTableBuilder = new MarkovTableBuilder(9);
        for (StateItem item : items) {
            markovTableBuilder.add(item);
        }

        // 归一化数据
        markovTableBuilder.normalize();

        // 存储马尔科夫状态表
        markovTableBuilder.persist();

    }


}
