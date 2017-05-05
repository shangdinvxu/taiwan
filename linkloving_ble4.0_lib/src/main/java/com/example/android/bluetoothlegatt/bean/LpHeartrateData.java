package com.example.android.bluetoothlegatt.bean;

/**
 * Created by Daniel.Xu on 2016/10/17.
 */

public class LpHeartrateData {
    public LpHeartrateData() {
    }


    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getFakeMaxRate() {
        return fakeMaxRate;
    }

    public void setFakeMaxRate(int fakeMaxRate) {
        this.fakeMaxRate = fakeMaxRate;
    }

    public int getFakeAvgRate() {
        return fakeAvgRate;
    }

    public void setFakeAvgRate(int fakeAvgRate) {
        this.fakeAvgRate = fakeAvgRate;
    }

    public int getMaxRate() {
        return MaxRate;
    }

    public void setMaxRate(int maxRate) {
        MaxRate = maxRate;
    }

    public int getAvgRate() {
        return avgRate;
    }

    public void setAvgRate(int avgRate) {
        this.avgRate = avgRate;
    }

    public LpHeartrateData(int startTime, int fakeMaxRate, int fakeAvgRate, int maxRate, int avgRate) {
        this.startTime = startTime;
        this.fakeMaxRate = fakeMaxRate;
        this.fakeAvgRate = fakeAvgRate;
        MaxRate = maxRate;
        this.avgRate = avgRate;
    }

    @Override
    public String toString() {
        return "LpHeartrateData{" +
                "startTime=" + startTime +
                ", fakeMaxRate=" + fakeMaxRate +
                ", fakeAvgRate=" + fakeAvgRate +
                ", MaxRate=" + MaxRate +
                ", avgRate=" + avgRate +
                '}';
    }

    private int startTime ;
    private int fakeMaxRate ;
    private int fakeAvgRate ;
    private int MaxRate ;
    private int avgRate ;

}
