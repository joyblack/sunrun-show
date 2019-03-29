package com.sunrun.movieshow.bean;

public class NBCBasketData {
    private Integer id;
    private String outlook;
    private String temperature;
    private String humidity;
    private String wind;

    public NBCBasketData() {
    }

    public NBCBasketData(Integer id, String outlook, String temperature, String humidity, String wind) {
        this.id = id;
        this.outlook = outlook;
        this.temperature = temperature;
        this.humidity = humidity;
        this.wind = wind;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOutlook() {
        return outlook;
    }

    public void setOutlook(String outlook) {
        this.outlook = outlook;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    @Override
    public String toString() {
        return id +"," + outlook + "," + temperature + "," + humidity + "," + wind;
    }
}
