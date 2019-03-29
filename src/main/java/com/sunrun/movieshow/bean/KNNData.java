package com.sunrun.movieshow.bean;

public class KNNData {
    private Double px;
    private Double py;

    public KNNData() {
    }

    public KNNData(Double px, Double py) {
        this.px = px;
        this.py = py;
    }

    public Double getPx() {
        return px;
    }

    public void setPx(Double px) {
        this.px = px;
    }

    public Double getPy() {
        return py;
    }

    public void setPy(Double py) {
        this.py = py;
    }
}
