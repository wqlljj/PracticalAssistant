package com.example.sx.practicalassistant.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sx.practicalassistant.R;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by wqlljj on 2017/3/12.
 */

public class WordsLVAdapter extends BaseAdapter {
    ArrayList<String> words=new ArrayList<>();
    private int layoutId= R.layout.textview;
    public WordsLVAdapter() {
    }

    public WordsLVAdapter(ArrayList<String> words) {
        this.words = words;
    }
    public void addWords(String[] words){
        if(words!=null) {
            for (String word : words) {
                if (!this.words.contains(word))
                    this.words.add(word);
            }
            notifyDataSetChanged();
        }
    }
    public void update(int index,String word){
        words.remove(index);
        words.add(index,word);
        notifyDataSetChanged();
    }
    public void addWord(String word){
        if(!words.contains(word)) {
            words.add(word);
            notifyDataSetChanged();
        }
    }

    public ArrayList<String> getWords() {
        return words;
    }

    @Override
    public int getCount() {
        return words.size();
    }

    @Override
    public Object getItem(int i) {
        return words.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            view=LayoutInflater.from(viewGroup.getContext()).inflate(layoutId,null);
        }
        ((TextView) view).setText(words.get(i));
        return view;
    }
}
