package com.example.sx.practicalassistant.bean;

/**
 * Created by wqlljj on 2017/3/6.
 */

public class HelpBean {
    public static int TITLE=0;
    public static int DETAIL=1;
    String content;
    int type;

    public HelpBean() {
    }

    public HelpBean(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
