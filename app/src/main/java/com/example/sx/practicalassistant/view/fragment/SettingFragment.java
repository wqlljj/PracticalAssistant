package com.example.sx.practicalassistant.view.fragment;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.sx.practicalassistant.R;
import com.example.sx.practicalassistant.app.BaseApplication;
import com.example.sx.practicalassistant.app.BaseContants;
import com.example.sx.practicalassistant.bean.PlayBean;
import com.example.sx.practicalassistant.db.WordsDB;
import com.example.sx.practicalassistant.utils.AudioUtils;
import com.example.sx.practicalassistant.utils.VoiceUtils;
import com.example.sx.practicalassistant.view.activity.UserWordActivity;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.SpeechError;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment implements View.OnClickListener, AudioUtils.ScanListening {


    private View view;
    private TextView scan_audio_time;
    private TextView scan_video_time;
    private TextView upload_audio_time;
    private TextView upload_video_time;
    private TextView upload_contact_time;
    private Button scan_audio_bt;
    private Button scan_video_bt;
    private Button upload_audio_bt;
    private Button upload_video_bt;
    private Button upload_contact_bt;
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private VoiceUtils voiceUtils;
    private Intent intent;
    private SharedPreferences sharedPreferences;
    private Button start_hello_bt;
    private View start_hello;
    private TextView upload_appname_time;
    private Button upload_appname_bt;

    public SettingFragment() {
        // Required empty public constructor
    }
    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        voiceUtils = VoiceUtils.getIntance(getContext());
        initView();
        initData();
        Log.e(TAG, "onCreateView: "+ Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"\n"+
                Environment.getExternalStorageDirectory().getAbsolutePath());
        return view;
    }

    private void initData() {
        scan_audio_bt.setOnClickListener(this);
        scan_video_bt.setOnClickListener(this);
        upload_audio_bt.setOnClickListener(this);
        upload_video_bt.setOnClickListener(this);
        upload_contact_bt.setOnClickListener(this);
        start_hello_bt.setOnClickListener(this);
        upload_appname_bt.setOnClickListener(this);
        view.findViewById(R.id.upload_contact).setOnClickListener(this);
        view.findViewById(R.id.upload_audio).setOnClickListener(this);
        view.findViewById(R.id.upload_video).setOnClickListener(this);
        view.findViewById(R.id.custom_userwords).setOnClickListener(this);
        view.findViewById(R.id.upload_appname).setOnClickListener(this);
        start_hello.setOnClickListener(this);
        sharedPreferences = getActivity().getSharedPreferences(BaseContants.SHARE_NAME, Context.MODE_PRIVATE);
        upload_audio_time.setText(sdf.format(new Date(sharedPreferences.getLong(VoiceUtils.AUDIO_KEY, 0l))));
        upload_video_time.setText(sdf.format(new Date(sharedPreferences.getLong(VoiceUtils.VIDEO_KEY, 0l))));
        upload_contact_time.setText(sdf.format(new Date(sharedPreferences.getLong(VoiceUtils.Contact_KEY, 0l))));
        upload_appname_time.setText(sdf.format(new Date(sharedPreferences.getLong(VoiceUtils.APPNAME_KEY, 0l))));
        scan_audio_time.setText(sdf.format(new Date(sharedPreferences.getLong(PlayBean.AUDIO_TYPE, 0l))));
        scan_video_time.setText(sdf.format(new Date(sharedPreferences.getLong(PlayBean.VIDEO_TYPE, 0l))));
        setStart_Holle();
    }

    private void initView() {
        scan_audio_time = ((TextView) view.findViewById(R.id.scan_audio_time));
        scan_video_time = ((TextView) view.findViewById(R.id.scan_video_time));
        upload_audio_time = ((TextView) view.findViewById(R.id.upload_audio_time));
        upload_video_time = ((TextView) view.findViewById(R.id.upload_video_time));
        upload_contact_time = ((TextView) view.findViewById(R.id.upload_contact_time));
        upload_appname_time = ((TextView) view.findViewById(R.id.upload_appname_time));
        scan_audio_bt = (Button) view.findViewById(R.id.scan_audio_bt);
        upload_contact_bt =(Button) view.findViewById(R.id.upload_contact_bt);
        upload_video_bt = (Button)view.findViewById(R.id.upload_video_bt);
        upload_audio_bt = (Button)view.findViewById(R.id.upload_audio_bt);
        upload_appname_bt = (Button)view.findViewById(R.id.upload_appname_bt);
        scan_video_bt = (Button)view.findViewById(R.id.scan_video_bt);
        start_hello_bt = (Button)view.findViewById(R.id.start_hello_bt);
        start_hello = view.findViewById(R.id.start_hello);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.scan_audio_bt:
                scan_audio_bt.setText("扫描中");
                scan_audio_bt.setEnabled(false);
                scan_video_bt.setEnabled(false);
                AudioUtils.scanFile("",PlayBean.AUDIO_TYPE,getContext(),this);
                break;
            case R.id.scan_video_bt:
                scan_video_bt.setText("扫描中");
                scan_audio_bt.setEnabled(false);
                scan_video_bt.setEnabled(false);
                AudioUtils.scanFile("",PlayBean.VIDEO_TYPE,getContext(),this);
                break;
            case R.id.upload_audio_bt:
                upload_audio_bt.setText("上传中");
                upload_audio_bt.setEnabled(false);
                upload_video_bt.setEnabled(false);
                upload_contact_bt.setEnabled(false);
                upload_appname_bt.setEnabled(false);
                BaseApplication.getBaseApplication().getWord(PlayBean.AUDIO_TYPE,lexiconListener);
                break;
            case R.id.upload_video_bt:
                upload_video_bt.setText("上传中");
                upload_audio_bt.setEnabled(false);
                upload_video_bt.setEnabled(false);
                upload_contact_bt.setEnabled(false);
                upload_appname_bt.setEnabled(false);
                BaseApplication.getBaseApplication().getWord(PlayBean.VIDEO_TYPE,lexiconListener);
                break;
            case R.id.upload_contact_bt:
                upload_contact_bt.setText("上传中");
                upload_audio_bt.setEnabled(false);
                upload_video_bt.setEnabled(false);
                upload_contact_bt.setEnabled(false);
                upload_appname_bt.setEnabled(false);
                voiceUtils.uploadcontact(lexiconListener);
                break;
            case R.id.upload_appname_bt:
                upload_appname_bt.setText("上传中");
                upload_appname_bt.setEnabled(false);
                upload_audio_bt.setEnabled(false);
                upload_video_bt.setEnabled(false);
                upload_contact_bt.setEnabled(false);
                uploadAppName();
                break;
            case R.id.upload_audio:
                intent = new Intent(getContext(), UserWordActivity.class);
                intent.putExtra("title","音频关键词");
                intent.putExtra("key",PlayBean.AUDIO_TYPE);
                getActivity().startActivity(intent);
                break;
            case R.id.upload_video:
                intent = new Intent(getContext(), UserWordActivity.class);
                intent.putExtra("title","视频关键词");
                intent.putExtra("key",PlayBean.VIDEO_TYPE);
                getActivity().startActivity(intent);
                break;
            case R.id.upload_contact:
                intent = new Intent(getContext(), UserWordActivity.class);
                intent.putExtra("title","通讯录");
                intent.putExtra("key",VoiceUtils.Contact_KEY);
                getActivity().startActivity(intent);
                break;
            case R.id.custom_userwords:
                intent = new Intent(getContext(), UserWordActivity.class);
                intent.putExtra("title","自定义用户关键词");
                intent.putExtra("key",VoiceUtils.CUSTOM_KEY);
                getActivity().startActivity(intent);
                break;
            case R.id.start_hello:
                intent = new Intent(getContext(), UserWordActivity.class);
                intent.putExtra("title","启动问候语");
                intent.putExtra("key",BaseContants.CUSTOM_HELLO);
                getActivity().startActivity(intent);
                break;
            case R.id.upload_appname:
                intent = new Intent(getContext(), UserWordActivity.class);
                intent.putExtra("title","应用名");
                intent.putExtra("key",VoiceUtils.APPNAME_KEY);
                getActivity().startActivity(intent);
                break;
            case R.id.start_hello_bt:
                if(start_hello_bt.getText().equals("开启")){
                    BaseContants.ISOPEN_STARTHELLO =false;
                }else{
                    BaseContants.ISOPEN_STARTHELLO =true;
                }
                    setStart_Holle();
                break;
        }
    }
    HashSet<String> appNames=null;
    private void uploadAppName() {
        PackageManager packageManager = getContext().getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);
        appNames=new HashSet<>();
        for (ApplicationInfo installedApplication : installedApplications) {
            String appname = installedApplication.loadLabel(packageManager).toString();
            appNames.add(appname);
        }
        HashMap<String,HashSet<String>> words=new HashMap<>();
        words.put(VoiceUtils.APPNAME_KEY,appNames);
        try {
            VoiceUtils.getIntance(getContext()).uploadWords(words,lexiconListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setStart_Holle(){
        start_hello_bt.setText(BaseContants.ISOPEN_STARTHELLO ?"开启":"关闭");
        start_hello.setClickable(BaseContants.ISOPEN_STARTHELLO);
        sharedPreferences.edit().putBoolean(BaseContants.START_HELLO,BaseContants.ISOPEN_STARTHELLO).apply();
    }
    private String TAG="TEST";
    //上传监听器。  
    private LexiconListener lexiconListener=new WordLexiconListener();
    public class WordLexiconListener extends VoiceUtils.VoiceLexiconListener{
        @Override
        public void onLexiconUpdated(String lexiconId,SpeechError error){
            super.onLexiconUpdated(lexiconId,error);
            String content="";
            if(error!=null){
                Log.d(TAG,"上传"+error.toString());
                voiceUtils.startSpeak("上传失败");
                content="上传失败";
            }else{
                Log.d(TAG, "上传成功！");
                voiceUtils.startSpeak("上传成功");
                content="上传成功";
            }
            final String finalContent = content;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    update_time(finalContent);
                }
            });
        }
    }
    private void update_time(String content){
        int id=0;
        if(upload_audio_bt.getText().equals("上传中")){
            id=upload_audio_bt.getId();
        }else if(upload_video_bt.getText().equals("上传中")){
            id=upload_video_bt.getId();
        }else if(upload_contact_bt.getText().equals("上传中")){
            id=upload_contact_bt.getId();
        }else if(upload_appname_bt.getText().equals("上传中")){
            id=upload_appname_bt.getId();
        }
        switch (id){
            case R.id.upload_audio_bt:
                upload_audio_bt.setText(content);
                sharedPreferences.edit().putLong(VoiceUtils.AUDIO_KEY,System.currentTimeMillis()).apply();
                upload_audio_time.setText(sdf.format(new Date(System.currentTimeMillis())));
                upload_audio_bt.setEnabled(true);
                upload_video_bt.setEnabled(true);
                upload_contact_bt.setEnabled(true);
                upload_appname_bt.setEnabled(true);
                break;
            case R.id.upload_video_bt:
                upload_video_bt.setText(content);
                sharedPreferences.edit().putLong(VoiceUtils.VIDEO_KEY,System.currentTimeMillis()).apply();
                upload_video_time.setText(sdf.format(new Date(System.currentTimeMillis())));
                upload_audio_bt.setEnabled(true);
                upload_video_bt.setEnabled(true);
                upload_contact_bt.setEnabled(true);
                upload_appname_bt.setEnabled(true);
                break;
            case R.id.upload_contact_bt:
                upload_contact_bt.setText(content);
                sharedPreferences.edit().putLong(VoiceUtils.Contact_KEY,System.currentTimeMillis()).apply();
                upload_contact_time.setText(sdf.format(new Date(System.currentTimeMillis())));
                upload_audio_bt.setEnabled(true);
                upload_video_bt.setEnabled(true);
                upload_contact_bt.setEnabled(true);
                upload_appname_bt.setEnabled(true);
                break;
            case R.id.upload_appname_bt:
                upload_appname_bt.setText(content);
                sharedPreferences.edit().putLong(VoiceUtils.APPNAME_KEY,System.currentTimeMillis()).apply();
                upload_appname_time.setText(sdf.format(new Date(System.currentTimeMillis())));
                upload_audio_bt.setEnabled(true);
                upload_video_bt.setEnabled(true);
                upload_contact_bt.setEnabled(true);
                upload_appname_bt.setEnabled(true);
                ContentValues values = new ContentValues();
                values.put("name",VoiceUtils.APPNAME_KEY);
                StringBuilder sb=new StringBuilder();
                for (String appName : appNames) {
                    sb.append(appName+",");
                }
                String appnames = sb.substring(0, sb.length() - 1);
                values.put("words",appnames);
                WordsDB.getIntance(getContext()).insert(values);
                appNames.clear();
                break;
        }
    }
    @Override
    public void finsh(String type, final Long time) {
        if(type.equals(PlayBean.VIDEO_TYPE)) {
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    scan_video_bt.setText("扫描完成");
                    sharedPreferences.edit().putLong(PlayBean.VIDEO_TYPE,time).apply();
                    scan_video_time.setText(sdf.format(new Date(time)));
                    scan_audio_bt.setEnabled(true);
                    scan_video_bt.setEnabled(true);
                    }
                });
        }else {
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    scan_audio_bt.setText("扫描完成");
                    sharedPreferences.edit().putLong(PlayBean.AUDIO_TYPE,time).apply();
                    scan_audio_time.setText(sdf.format(new Date(time)));
                    scan_audio_bt.setEnabled(true);
                    scan_video_bt.setEnabled(true);
                }
            });
        }
    }
}
