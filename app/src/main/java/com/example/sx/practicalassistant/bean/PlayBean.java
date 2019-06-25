package com.example.sx.practicalassistant.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by wqlljj on 2017/3/5.
 */

public class PlayBean implements Serializable{
    public final static String VIDEO_TYPE="video/";
    public final static String AUDIO_TYPE="audio/MP3";

    private String fileName="";
    private String type;
    private HashMap<String,String> path_name=new HashMap<>();

    public PlayBean() {
    }

    public PlayBean(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public HashMap<String,String> getPaths() {
        return path_name;
    }
    public void removePath(String path){
         path_name.remove(path);
    }
    public void clearPath(){
         path_name.clear();
    }
    public void addPath(String path,String name) {
        path_name.put(path,name);
    }

    @Override
    public String toString() {
        return "PlayBean{" +
                "fileName='" + fileName + '\'' +
                ", type='" + type + '\'' +
                ", path_name=" + path_name +
                '}';
    }
}
