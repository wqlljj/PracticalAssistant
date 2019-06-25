package com.example.sx.practicalassistant.bean;

/**
 * Created by cloud on 2018/10/30.
 */

public class EventBean {
    public static final int TYPE_MUSIC=0;
    int type;
    String msg;
    Object data;

    public EventBean(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public EventBean(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "EventBean{" +
                "type=" + type +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
