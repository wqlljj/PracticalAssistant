package com.example.sx.practicalassistant.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sx.practicalassistant.R;
import com.example.sx.practicalassistant.app.BaseContants;
import com.example.sx.practicalassistant.bean.ChatMessage;
import com.example.sx.practicalassistant.bean.Command;
import com.example.sx.practicalassistant.bean.Response;
import com.example.sx.practicalassistant.customview.VoiceSpectrum;
import com.example.sx.practicalassistant.service.BootReceiver;
import com.example.sx.practicalassistant.service.MediaButtonReceiver;
import com.example.sx.practicalassistant.utils.CommandParse;
import com.example.sx.practicalassistant.utils.OperateUtils;
import com.example.sx.practicalassistant.utils.VoiceUtils;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;


/**
 * Created by wqlljj on 2017/3/3.
 */

public class VoiceActivity extends BaseActivity implements View.OnClickListener, MediaButtonReceiver.OnMediaButtonListening {
    private VoiceUtils voiceUtils;
    private VoiceSpectrum spectrum_voice;
    private Animation voiceAnim;
    private Button btn_asr;
    private Button btn_rec_1;
    private Button btn_rec_2;
    private TextView result;
    private EditText input;
    private View rly_input;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_layout);
        MediaButtonReceiver.register(this);
        voiceUtils = VoiceUtils.getIntance(this);
        voiceUtils.setmRecognizerListener(mRecognizerListener);
        initView();
        initData();
        voiceUtils.startListening();
        Intent intent =new Intent(this, BootReceiver.class);
        intent.setAction("com.example.sx.practicalassistant.keepalive");
        sendBroadcast(intent);
    }
    private void initView() {
        findViewById(R.id.viewPager_bt).setVisibility(View.GONE);


        // 频谱
        spectrum_voice = (VoiceSpectrum) findViewById(R.id.spectrum_voice);
        btn_asr = (Button) findViewById(R.id.btn_asr);
        btn_rec_1 = (Button) findViewById(R.id.btn_rec_1);
        btn_rec_2 = (Button) findViewById(R.id.btn_rec_2);
        btn_asr.setVisibility(View.VISIBLE);
        result = ((TextView) findViewById(R.id.result));
        input = ((EditText) findViewById(R.id.input));
        rly_input = findViewById(R.id.rly_input);

        findViewById(R.id.commit).setOnClickListener(this);
        findViewById(R.id.close).setOnClickListener(this);
        btn_asr.setOnClickListener(this);
        spectrum_voice.setOnClickListener(this);
    }
    private void initData() {
        voiceAnim = AnimationUtils.loadAnimation(this, R.anim.voice_rotate);
        voiceAnim.setInterpolator(new LinearInterpolator());
        showVoice(1);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_asr:// 话筒
                if (btn_asr.getVisibility() == View.VISIBLE) {
                    voiceUtils.startListening();
                }
                break;
            case R.id.spectrum_voice:// 频谱
                if (spectrum_voice.getVisibility() == View.VISIBLE) {
                    voiceUtils.stopListening();
                    showVoice(1);
                }
                break;
            case R.id.commit:
                commandParse(input.getText().toString().trim());
                break;
            default:
                finish();
                break;
        }
    }
    private String TAG="TEST";
    private VoiceUtils.MRecognizerListener mRecognizerListener=new VoiceUtils.MRecognizerListener(){
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            Log.e(TAG, "onVolumeChanged: ");
            spectrum_voice.updateVisualizer(bytes);
        }

        @Override
        public void onBeginOfSpeech() {
            Log.e(TAG, "onBeginOfSpeech: " );
            showVoice(3);
        }

        @Override
        public void onEndOfSpeech() {
            Log.e(TAG, "onEndOfSpeech: " );
            showVoice(2);
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            Log.e(TAG, "onResult: "+ recognizerResult.getResultString());
            String result = voiceUtils.printResult(recognizerResult);
            if (b) {
                showVoice(1);
                commandParse(result);
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            Log.e(TAG, "onError: " );
            showVoice(1);
        }
    };
    String type=Command.UNKNOW;
    private void commandParse(String content){
        result.setText(content);
        Command command = CommandParse.parse(content);
        type=command.getType();
        Response response = OperateUtils.operateCommand(command, this);
        if(type.equals(Command.SENDMESSAGE1)||type.equals(Command.ROMANISATIONOFCHINESE2)||(type.equals(Command.CALCULATOR)&&response.getFlag()==Response.SUCCESS)){
            result.setText(response.getShowContent());
        }
        if(type.equals(Command.ROMANISATIONOFCHINESE2)&&response.getFlag()==Response.SUCCESS){
            InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
            //关闭输入框
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            input.setText("");
            rly_input.setVisibility(View.GONE);
        }
        voiceUtils.startSpeak(TextUtils.isEmpty(response.getSpeakContent())?response.getShowContent():response.getSpeakContent());
        if(response.getFlag()==Response.FINISH){
            finish();
        }
        if(type.equals(Command.ROMANISATIONOFCHINESE1)){
            rly_input.setVisibility(View.VISIBLE);
        }
    }
    private void showVoice(int opration) {
        switch (opration) {
            case 1:// 话筒
                if (voiceAnim != null) {
                    btn_rec_1.clearAnimation();
                }
                btn_asr.setVisibility(View.VISIBLE);
                btn_rec_1.setVisibility(View.GONE);
                btn_rec_2.setVisibility(View.GONE);
                spectrum_voice.setVisibility(View.GONE);
                break;
            case 2:// 识别中
                btn_asr.setVisibility(View.GONE);
                btn_rec_1.setVisibility(View.VISIBLE);
                btn_rec_2.setVisibility(View.VISIBLE);
                spectrum_voice.setVisibility(View.GONE);
                if (voiceAnim != null) {
                    btn_rec_1.startAnimation(voiceAnim);
                }
                break;
            case 3:// 频谱
                if (voiceAnim != null) {
                    btn_rec_1.clearAnimation();
                }
                btn_asr.setVisibility(View.GONE);
                btn_rec_1.setVisibility(View.GONE);
                btn_rec_2.setVisibility(View.GONE);
                spectrum_voice.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        voiceUtils.stopListening();
        voiceUtils.stopSpeak();
        MediaButtonReceiver.unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void clickHandle(KeyEvent event) {
        Log.e(TAG, "clickHandle: " +lifeState );
        if(lifeState == BaseContants.ONRESUME) {
            voiceUtils.startListening();
        }
    }
}
