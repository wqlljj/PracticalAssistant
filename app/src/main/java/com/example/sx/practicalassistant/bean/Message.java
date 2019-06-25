package com.example.sx.practicalassistant.bean;

/**
 * Created by wqlljj on 2017/3/4.
 */

public class Message {
    String number;
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String content;

    public Message() {
    }

    public Message(String name,String content, String number) {
        this.content = content;
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getContent() {
        return content;
    }
    public void clear(){
        name="";
        number="";
        content="";
    }

    public void setContent(String content) {
        this.content = content;
    }
}
