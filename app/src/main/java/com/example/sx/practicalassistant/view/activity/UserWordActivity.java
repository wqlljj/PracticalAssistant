package com.example.sx.practicalassistant.view.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sx.practicalassistant.R;
import com.example.sx.practicalassistant.app.BaseContants;
import com.example.sx.practicalassistant.db.WordsDB;
import com.example.sx.practicalassistant.utils.VoiceUtils;
import com.example.sx.practicalassistant.view.adapter.WordsLVAdapter;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.util.UserWords;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UserWordActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView listView;
    private String key;
    private String TAG="UserWordActivity";
    private WordsLVAdapter lvAdapter;
    private EditText add_Word;
    private boolean isAdd=false;
    private EditText startHello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_word);
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        key = intent.getStringExtra("key");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(TextUtils.isEmpty(title)?"关键词":title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        if(key.equals(BaseContants.CUSTOM_HELLO)){
            findViewById(R.id.content_user_word).setVisibility(View.GONE);
            findViewById(R.id.custom_starthello).setVisibility(View.VISIBLE);
            startHello.setText(getSharedPreferences("practicalassistant",Context.MODE_PRIVATE).getString(BaseContants.CUSTOM_HELLO,"欢迎使用安卓助手"));
        }else {
            inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            lvAdapter = new WordsLVAdapter();
            lvAdapter.addWords(getWords(key));
            listView.setAdapter(lvAdapter);
            if (key.equals(VoiceUtils.CUSTOM_KEY)) {
                listView.setOnItemClickListener(this);
            }
        }
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(key.equals(VoiceUtils.CUSTOM_KEY))
        getMenuInflater().inflate(R.menu.custom_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_add:
                lvAdapter.addWord("");
                add_Word.setVisibility(View.VISIBLE);
                index=lvAdapter.getCount()-1;
                break;
            case R.id.action_upload:
                HashMap<String, HashSet<String>> allWords = new HashMap<>();
                HashSet<String> words=new HashSet<>();
                ArrayList<String> lvAdapterWords = lvAdapter.getWords();
                words.addAll(lvAdapterWords);
                allWords.put(VoiceUtils.CUSTOM_KEY,words);
                isAdd=false;
                try {
                    VoiceUtils.getIntance(this).uploadWords(allWords,new VoiceUtils.VoiceLexiconListener(){
                        @Override
                        public void onLexiconUpdated(String lexiconId, SpeechError error) {
                            super.onLexiconUpdated(lexiconId, error);
                            if(error!=null){
                                Log.d(TAG,"上传"+error.toString());
                                VoiceUtils.getIntance(UserWordActivity.this).startSpeak("上传失败");
                            }else {
                                Log.d(TAG, "上传成功！");
                                VoiceUtils.getIntance(UserWordActivity.this).startSpeak("上传成功");
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        listView = ((ListView) findViewById(R.id.listView));
        add_Word = ((EditText) findViewById(R.id.word));
        add_Word.setOnClickListener(this);


        startHello = ((EditText) findViewById(R.id.custom_hello));
        findViewById(R.id.save_starthello).setOnClickListener(this);
    }
    private String[] getWords(String key){
        Cursor cursor = WordsDB.getIntance(this).find(new String[]{"words"}, "name = ?", new String[]{key});
        String[] split=null;
        if(cursor!=null&&cursor.moveToFirst()){
            String words = cursor.getString(cursor.getColumnIndex("words"));
            split = words.split(",");
        }
        return split;
    }
    InputMethodManager inputMethodManager=null;
    int index=0;
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        add_Word.setVisibility(View.VISIBLE);
        index=i;
        onClick(add_Word);
    }
    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_DOWN){
            if(add_Word.getVisibility()==View.VISIBLE){
                String word = add_Word.getText().toString().trim();
                if(!TextUtils.isEmpty(word)) {
                    lvAdapter.update(index, word);
                    isAdd=true;
                }
                add_Word.setVisibility(View.GONE);
                return true;
            }else if(isAdd){
                if((System.currentTimeMillis()-exitTime)>2000){
                    Toast.makeText(getApplicationContext(),"有未上传的新词！请先提交或再按一次退出",Toast.LENGTH_SHORT).show();
                    exitTime=System.currentTimeMillis();
                } else{
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.word:
                // 接受软键盘输入的编辑文本或其它视图  
                inputMethodManager.showSoftInput(add_Word,InputMethodManager.SHOW_FORCED);
                break;
            case R.id.save_starthello:
                getSharedPreferences("practicalassistant",Context.MODE_PRIVATE).edit().putString(BaseContants.CUSTOM_HELLO,startHello.getText().toString().trim()).apply();
                 Toast.makeText(getApplicationContext(),"保存成功",Toast.LENGTH_LONG).show();
                break;
        }
    }
}
