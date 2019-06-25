package com.example.sx.practicalassistant.bean;

/**
 * Created by wqlljj on 2017/3/4.
 */

public class Response {
    public static final int UNFINISH=0;
    public static final int FINISH=1;
    public static final int FAIL=2;
    public static final int SUCCESS=3;

    String showContent;
    String speakContent;
    int flag;

    public Response() {
    }

    public Response(String showContent, String speakContent,int flag) {
        this.showContent = showContent;
        this.speakContent = speakContent;
        this.flag=flag;
    }
    public void clear(){
        showContent="";
        speakContent="";
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getShowContent() {
        return showContent;
    }

    public void setShowContent(String showContent) {
        this.showContent = showContent;
    }

    public String getSpeakContent() {
        return speakContent;
    }

    public void setSpeakContent(String speakContent) {
        this.speakContent = speakContent;
    }
}
