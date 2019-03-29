package com.sunrun.movieshow.algorithm.secondsort;

import java.io.Serializable;

public class Student implements Serializable{


    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", major='" + major + '\'' +
                ", score=" + score +
                '}';
    }

    public Student() {
    }

    public Student(String id, String major, Double score) {
        this.id = id;
        this.major = major;
        this.score = score;
    }

    private String id;
    private String major;
    private Double score;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
