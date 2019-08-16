package com.client.cx.wflowerclient.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author cx 任务类
 * @class describe
 * @time 2019/8/15 16:15
 */
public class Task implements Serializable {
    private int num;//任务编号
    private int type;//任务类型
    private List<Integer> days;//任务重复日期
    private int yield;//喷水量
    private int hour;
    private int minute;
    private int sec;
    private int time;//喷洒时间

    private static final int MOUTH_EXE = 1;//按月执行
    private static final int MOUTH_INEXE = 3;//按月执行,但任务挂起
    private static final int WEEK_EXE = 2;//按周执行
    private static final int WEEK_INEXE = 4;//按周执行，但任务挂起

    public Task() {
    }

    public Task(int num, int type, List<Integer> days, int yield, int hour, int minute, int time) {
        this.num = num;
        this.type = type;
        this.days = days;
        this.yield = yield;
        this.hour = hour;
        this.minute = minute;
        this.time = time;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Integer> getDays() {
        return days;
    }

    public void setDays(List<Integer> days) {
        this.days = days;
    }

    public int getYield() {
        return yield;
    }

    public void setYield(int yield) {
        this.yield = yield;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    //获取重复日期信息
    public String getRepMsg() {
        String repMsg = "";
        if (type == MOUTH_EXE || type == MOUTH_INEXE) {
            repMsg = "每月：";
        } else {
            repMsg = "每周：";
        }
        if (days != null && days.size() > 0 ) {
            for (int i = 0; i < days.size() - 1; i++) {
                repMsg += days.get(i) + "、";
            }
            repMsg += days.get(days.size() - 1);
        }

        return repMsg;
    }
}
