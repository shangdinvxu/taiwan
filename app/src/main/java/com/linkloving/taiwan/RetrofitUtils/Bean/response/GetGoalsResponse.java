package com.linkloving.taiwan.RetrofitUtils.Bean.response;

/**
 * Created by Daniel.Xu on 2017/2/21.
 */

public class GetGoalsResponse {


    /**
     * Id : 1420
     * NotififyWhenReached : true
     * Value : 24240
     * Date : 2017-02-21T00:00:00Z
     * Direction : Less
     * Threshold : 1
     * Parameter : person.posture.sitting
     * Window : Day
     */

    private int Id;
    private boolean NotififyWhenReached;
    private int Value;
    private String Date;
    private String Direction;
    private double Threshold;
    private String Parameter;
    private String Window;

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public boolean isNotififyWhenReached() {
        return NotififyWhenReached;
    }

    public void setNotififyWhenReached(boolean NotififyWhenReached) {
        this.NotififyWhenReached = NotififyWhenReached;
    }

    public int getValue() {
        return Value;
    }

    public void setValue(int Value) {
        this.Value = Value;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String Date) {
        this.Date = Date;
    }

    public String getDirection() {
        return Direction;
    }

    public void setDirection(String Direction) {
        this.Direction = Direction;
    }

    public double getThreshold() {
        return Threshold;
    }

    public void setThreshold(double Threshold) {
        this.Threshold = Threshold;
    }

    public String getParameter() {
        return Parameter;
    }

    public void setParameter(String Parameter) {
        this.Parameter = Parameter;
    }

    public String getWindow() {
        return Window;
    }

    public void setWindow(String Window) {
        this.Window = Window;
    }
}
