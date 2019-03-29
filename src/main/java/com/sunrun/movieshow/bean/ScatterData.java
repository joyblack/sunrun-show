package com.sunrun.movieshow.bean;

import java.util.List;

public class ScatterData {
    private String type;
    private List<Double[]> data;
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScatterData() {
    }

    public ScatterData(String type, List<Double[]> data,String name) {
        this.type = type;
        this.data = data;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double[]> getData() {
        return data;
    }

    public void setData(List<Double[]> data) {
        this.data = data;
    }
}
