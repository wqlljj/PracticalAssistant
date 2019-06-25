package com.example.sx.practicalassistant.utils;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by cloud on 2018/11/2.
 */

public class JsoupUtil {
    private static String TAG = "JsoupUtil";

    public static HashMap<String,String> getMp3s(String url,String baseUrl, String word){
        HashMap<String,String> list = new HashMap<>();
        try {
            Document document = Jsoup.connect(url).get();
            Elements select = document.select("div.ser_3");
//            Log.e(TAG, "getData: select "+select.toString());
            Elements ul = select.select("ul");
            int size = ul.size();
//            Log.e(TAG, "getData: size = "+size );
            for (int i = 0; i < size; i++) {
                Element element = ul.get(i);
//                Log.e(TAG, "getData: element = "+element.toString() );
                Elements li = element.select("li");
//                Log.e(TAG, "getData: li = "+li.size() );
                for (int j = 0; j < li.size(); j++) {
                    Elements item = li.get(j).select("a");
//                    Log.e(TAG, "getData: item = "+item );
                    String text = item.text();
                    if(text.contains(word)){
                        String path = item.attr("href");
                        Log.i(TAG, "getData: href  "+path );
                        list.put(text,baseUrl+path);
                    }else{
//                        Log.e(TAG, "getData: "+text+"  "+word );
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String getSongUrl(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            Elements script = document.getElementsByClass("play-left");
//            Log.e(TAG, "getSongUrl: script "+script.toString() );
//            Elements select = script.select("table");
            String select = script.select("input").attr("value");
            Log.i(TAG, "getSongUrl: "+select );
            return select;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
