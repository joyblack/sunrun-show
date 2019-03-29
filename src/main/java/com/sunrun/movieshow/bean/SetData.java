package com.sunrun.movieshow.bean;

import java.io.Serializable;

public class SetData implements Serializable {
    private String a;
    private String b;

    public SetData() {
    }

    public SetData(String a, String b) {
        this.a = a;
        this.b = b;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return "SetData{" +
                "a='" + a + '\'' +
                ", b='" + b + '\'' +
                '}';
    }
}
