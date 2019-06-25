package com.example.sx.practicalassistant.bean;

/**
 * Created by SX on 2017/3/3.
 */

public class Command {
    public static final String OPENAPP="oppApp";
    public static final String PLAY="play";
    public static final String CALLPHONE="callPhone";
    public static final String SENDMESSAGE1="sendMessage1";
    public static final String SENDMESSAGE2="sendMessage2";
    public static final String  ROMANISATIONOFCHINESE1="RomanisationOfChinese1";
    public static final String  ROMANISATIONOFCHINESE2="RomanisationOfChinese2";
    public static final String CALCULATOR="calculator";
    public static final String UNKNOW=" unknow";


    String type;
    Object data;

    public Command(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
