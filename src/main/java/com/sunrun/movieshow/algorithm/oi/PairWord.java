package com.sunrun.movieshow.algorithm.oi;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 参考PairOfStrings类进行设计，大数据工具类都需要实现WritableComparable接口
 */
public class PairWord implements WritableComparable<PairWord> {
    private String leftElement;
    private String rightElement;


    // == constructor
    public PairWord() {
    }

    public PairWord(String leftElement, String rightElement) {
        set(leftElement, rightElement);
    }

    // == getter and setter
    public String getLeftElement() {
        return leftElement;
    }

    public void setLeftElement(String leftElement) {
        this.leftElement = leftElement;
    }

    public String getRightElement() {
        return rightElement;
    }

    public void setRightElement(String rightElement) {
        this.rightElement = rightElement;
    }

    /**
     * set 方法
     * @param left
     * @param right
     */
    public void set(String left, String right) {
        leftElement = left;
        rightElement = right;
    }

    // == 序列化反序列化(Writable接口)
    /**
     * 序列化
     * @param out
     * @throws IOException
     */
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(leftElement);
        out.writeUTF(rightElement);
    }

    /**
     * 反序列化，注意顺序和序列化的顺序保持一致
     * @param in
     * @throws IOException
     */
    @Override
    public void readFields(DataInput in) throws IOException {
        leftElement = in.readUTF();
        rightElement = in.readUTF();
    }

    // == 实现结构 java.lang.comparable,和序列化接口加起来，刚好是WritableComparable的内容

    /**
     * 左词优先排序
     * @param pairWord
     * @return
     */
    @Override
    public int compareTo(PairWord pairWord) {
        String pl = pairWord.getLeftElement();
        String pr = pairWord.getRightElement();
        if (leftElement.equals(pl)) {
            return rightElement.compareTo(pr);
        }
        return leftElement.compareTo(pl);
    }

    // == 检查两个pair是否相等
    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }

        if(!(obj instanceof PairWord)){
            return false;
        }

        PairWord other = (PairWord) obj;

        return leftElement.equals(other.getLeftElement()) && rightElement.equals(other.getRightElement());
    }

    // == hashcode生成
    @Override
    public int hashCode() {
        return leftElement.hashCode() + rightElement.hashCode();
    }

    // == toString 方法(关系到最后的文件输出)
    @Override
    public String toString() {
        return "(" + leftElement + "," + rightElement + ")";
    }

    // == 克隆方法
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new PairWord(this.leftElement,this.rightElement);
    }
}
