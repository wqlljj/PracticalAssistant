package com.example.sx.practicalassistant.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.example.sx.practicalassistant.app.BaseApplication;
import com.example.sx.practicalassistant.bean.PlayBean;
import com.example.sx.practicalassistant.db.WordsDB;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ContactManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;


/**
 * Created by SX on 2017/3/2.
 */

public class VoiceUtils {
    private static VoiceUtils voiceUtils;
    private static  Context mContext;
    private SpeechSynthesizer mTts;
    private SpeechRecognizer mIat;
    private MRecognizerListener mRecognizerListener;
    private MSynthesizerListener mSynthesizerListener;
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private static String TAG="VoiceUtils";
    private LexiconListener lexiconListenertemp;

    public void setmRecognizerListener(MRecognizerListener mRecognizerListener) {
        this.mRecognizerListener = mRecognizerListener;
    }
    public void setmSynthesizerListener(MSynthesizerListener mSynthesizerListener) {
        this.mSynthesizerListener = mSynthesizerListener;
    }
    public static String Contact_KEY="contact";
    public static String AUDIO_KEY= "audio";
    public static String VIDEO_KEY="video";
    public static String CUSTOM_KEY="custom";
    public static String APPNAME_KEY="appname";
    private int ret;
    private static String name="";
    private static String words="";
    //获取联系人监听器。  
    private ContactManager.ContactListener mContactListener= new ContactManager.ContactListener(){
        @Override
        public void onContactQueryFinish(String contactInfos,boolean changeFlag){
            //指定引擎类型  
            mIat.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD);
            mIat.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
            name="contact";
            words=contactInfos;
            words= words.replaceAll("\\n",",");
            if(words.length()==0){
                words+="暂无联系人";
            }
            Log.e(TAG, "关键词: "+words );
            ret=mIat.updateLexicon("contact",contactInfos,lexiconListenertemp==null?lexiconListener:lexiconListenertemp);
            if(ret!= ErrorCode.SUCCESS){
                Log.d(TAG,"上传联系人失败："+ret);
                startSpeak("上传联系人失败");
            }
        }};
    //上传联系人监听器。  
    private LexiconListener lexiconListener=new VoiceLexiconListener();
    public static  class VoiceLexiconListener implements   LexiconListener{
        private final SharedPreferences sharedPreferences;

        public VoiceLexiconListener() {
            sharedPreferences = BaseApplication.getContext().getSharedPreferences("practicalassistant", Context.MODE_PRIVATE);
        }

        @Override
        public void onLexiconUpdated(String lexiconId,SpeechError error){
            if(error!=null){
                Log.d(TAG,"上传"+error.getMessage()+"  "+error.getErrorCode());
//                startSpeak("上传失败");
            }else{
                Log.d(TAG, "上传成功！");
//                startSpeak("上传成功");
                ContentValues values = new ContentValues();
                values.put("name",name);
                values.put("words",words);
                WordsDB wordsDB = WordsDB.getIntance(mContext);
                switch (name){
                    case "contact":
                        sharedPreferences.edit().putLong(VoiceUtils.Contact_KEY,System.currentTimeMillis()).apply();
                        break;
                    case PlayBean.AUDIO_TYPE:
                        sharedPreferences.edit().putLong(VoiceUtils.AUDIO_KEY,System.currentTimeMillis()).apply();
                        break;
                    case PlayBean.VIDEO_TYPE:
                        sharedPreferences.edit().putLong(VoiceUtils.VIDEO_KEY,System.currentTimeMillis()).apply();
                        break;
                }
                Cursor cursor = wordsDB.find(new String[]{"words"}, "name = ?", new String[]{name});
                if(cursor.moveToFirst()){
                    wordsDB.update(values,"name = ?",new String[]{name});
                }else{
                    wordsDB.insert(values);
                }
            }
        }
    }

    public VoiceUtils(Context context) {
        mContext = context;
        initSpeechSynthesizer();
        initSpeechRecognizer();
    }
    public static VoiceUtils getIntance(Context context){
        if(voiceUtils==null)
        voiceUtils = new VoiceUtils(context);
        return voiceUtils;
    }
//   {
//     "userword":
//              [
//               { "name" : "default" , "words" : [ "默认词条1", "默认词条2" ] },
//               { "name" : "词表名称1", "words": [ "词条1的第一个词", "词条1的第二个词"] },
//               { "name" : "词表名称2", "words": [ "词条2的第一个词", "词条2的第二个词"] }
//      ]
//}
    public void uploadWords(HashMap<String,HashSet<String>> allWord,LexiconListener lexiconListener) throws JSONException {
        Set<String> keys = allWord.keySet();
        JSONArray userword = new JSONArray();
        for (String name : keys) {
            JSONObject jsonObject = new JSONObject();
            JSONArray words = new JSONArray();
            HashSet<String> arrayList = allWord.get(name);
            for (String w : arrayList) {
                words.put(w);
            }
            if(words.length()==0){
                words.put("词表为空");
            }
            this.name=name;
            this.words=words.toString();
            this.words=this.words.replaceAll("(\\[|\"|\\])","");
            Log.e(TAG, "关键词: "+this.words );
            jsonObject.put("name",name);
            jsonObject.put("words",words);
            userword.put(jsonObject);
        }
        JSONObject content = new JSONObject();
        content.put("userword",userword);
        //上传用户词表，userwords为用户词表文件。  
//        String contents = "您所定义的用户词表内容";  
        mIat.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
//指定引擎类型  
        mIat.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD);
        ret=mIat.updateLexicon("userword",content.toString(),lexiconListener==null?VoiceUtils.this.lexiconListener:lexiconListener);
        if(ret!=ErrorCode.SUCCESS){
            Log.d(TAG,"上传用户词表失败："+ret);
        }
    }
    public void uploadcontact(LexiconListener lexiconListener){
        lexiconListenertemp = lexiconListener;
        ContactManager mgr= ContactManager.createManager(mContext, mContactListener);
        //异步查询联系人接口，通过onContactQueryFinish接口回调  
        mgr.asyncQueryAllContactsName();
    }
    private void initSpeechSynthesizer() {
        // 1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        mTts = SpeechSynthesizer.createSynthesizer(mContext, null);
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");// 设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "40");// 设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "50");// 设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 设置云端
        // 如果不需要保存合成音频，注释该行代码

    }
    private void initSpeechRecognizer() {
        mIat = SpeechRecognizer.createRecognizer(mContext, null);
        // 2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        // mIat.setParameter(SpeechConstant.VAD_BOS,"80000ms");//音频前面部分最长静音时长。
        mIat.setParameter(SpeechConstant.VAD_EOS, "1500ms");//音频后面部分最长静音时长。
        mIat.setParameter(SpeechConstant.ASR_PTT,"0");//不代标点
    }

    public int startListening(){
        if(mTts.isSpeaking()){
            stopSpeak();
        }
        if(mIat.isListening()){
            mIat.stopListening();
        }
        return mIat.startListening(mRecognizerListener);
    }
    public void stopListening(){
        if(mIat.isListening())
        mIat.stopListening();
    }
    public int startSpeak(String content){
        if(mIat.isListening()){
            stopListening();
        }
        if (mTts.isSpeaking())
            mTts.stopSpeaking();
        return mTts.startSpeaking(content,mSynthesizerListener);
    }
    public void stopSpeak(){
        mTts.stopSpeaking();
    }
    public String printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        System.out.println("resultBuffer.toString()" + resultBuffer.toString());
        return resultBuffer.toString();
    }
    public static class MRecognizerListener implements RecognizerListener {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
//            Log.e(TAG, "onVolumeChanged: ");
//            spectrum_voice.updateVisualizer(bytes);
        }

        @Override
        public void onBeginOfSpeech() {
//            Log.e(TAG, "onBeginOfSpeech: " );
//            showVoice(3);
        }

        @Override
        public void onEndOfSpeech() {
//            Log.e(TAG, "onEndOfSpeech: " );
//            showVoice(2);
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
//            Log.e(TAG, "onResult: " );
//            showVoice(1);
        }

        @Override
        public void onError(SpeechError speechError) {
//            Log.e(TAG, "onError: " );
//            showVoice(1);
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    }
    public static class MSynthesizerListener implements SynthesizerListener {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };
}
