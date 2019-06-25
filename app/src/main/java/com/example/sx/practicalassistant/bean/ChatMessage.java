package com.example.sx.practicalassistant.bean;

import java.net.PortUnreachableException;

/**
 * Created by SX on 2017/2/23.
 */

public class ChatMessage {
    private int type;//左右
    private int contentType;

    private String time;
    private  Object data;

    public ChatMessage(int type, String time, Object data) {
        this.type = type;
        this.time = time;
        this.data = data;
    }

    public ChatMessage(int type, Object data) {
        this.type = type;
        this.data = data;
    }
    public int getContentType() {
        return contentType;
    }
    public void setContentType(int contentType) {
        this.contentType = contentType;
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    public static class Type{
        public static final int CHAT_LEFT=0;
        public static final int CHAT_RGIHT=1;
    }
    public static class ContentType{
        public static final int CONTENT_STRING=0;
    }
}
