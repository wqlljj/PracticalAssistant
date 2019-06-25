package com.example.sx.practicalassistant.view.fragment;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.sx.practicalassistant.app.BaseApplication;
import com.example.sx.practicalassistant.app.BaseContants;
import com.example.sx.practicalassistant.bean.ChatMessage;
import com.example.sx.practicalassistant.bean.Response;
import com.example.sx.practicalassistant.customview.VoiceSpectrum;
import com.example.sx.practicalassistant.service.MediaButtonReceiver;
import com.example.sx.practicalassistant.utils.CommandParse;
import com.example.sx.practicalassistant.utils.DateUtil;
import com.example.sx.practicalassistant.utils.JsoupUtil;
import com.example.sx.practicalassistant.utils.LogUtils;
import com.example.sx.practicalassistant.utils.OperateUtils;
import com.example.sx.practicalassistant.utils.ToastUtil;
import com.example.sx.practicalassistant.utils.VoiceUtils;
import com.example.sx.practicalassistant.view.activity.MainActivity;
import com.example.sx.practicalassistant.view.adapter.HomeRVAdapter;
import com.example.sx.practicalassistant.view.adapter.ViewPagerAdapter;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by SX on 2017/2/22.
 */

public class HomeFragment extends Fragment implements View.OnClickListener, MediaButtonReceiver.OnMediaButtonListening {

    private Context mContext;
    private View view;
    private KeyguardManager mKeyguardManager;
    private String initContent;
    private ViewPager viewPager;
    private View voiceView;
    private View textView;
    private TextView tv_down;
    private RelativeLayout llt_image;
    private TextView tv_tag;
    private ImageView iv_image;
//    private TextView tv_reg;
    private VoiceSpectrum spectrum_voice;
    private Animation voiceAnim;
    private Button btn_asr;
    private Button btn_rec_1;
    private Button btn_rec_2;
    private ImageView btn_send_message;
    private EditText et_content;
    private RecyclerView recyclerView;
    private HomeRVAdapter homeRVAdapter;
    private String TAG="HomeFragment";
    private VoiceUtils voiceUtils;
    private boolean isVoice;


    public HomeFragment newInstance() {
        HomeFragment mFragment = new HomeFragment();
        return mFragment;
    }

    public HomeFragment newInstance(String content) {
        HomeFragment mFragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("Content", content);
        //将Bundle设置为fragment的参数
        mFragment.setArguments(args);
        return mFragment;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    /**
     * 动态注册广播
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        mKeyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        MediaButtonReceiver.register(this);
        voiceUtils = VoiceUtils.getIntance(this.getContext());
        voiceUtils.setmRecognizerListener(mRecognizerListener);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(BaseContants.SHARE_NAME, Context.MODE_PRIVATE);
        if(BaseContants.ISOPEN_STARTHELLO=sharedPreferences.getBoolean(BaseContants.START_HELLO,true))
        voiceUtils.startSpeak(sharedPreferences.getString(BaseContants.CUSTOM_HELLO,BaseContants.DEFULT_STARTHELLO));
        initView();
        initData();
//        initListener();
        if(getArguments()!=null && getArguments().getString("Content") !=null){
            initContent = getArguments().getString("Content");
        }
        return view;
    }

    private void initData() {
        homeRVAdapter = new HomeRVAdapter(this.getActivity(),new ArrayList<ChatMessage>(),new int[]{R.layout.chat_left_item,R.layout.chat_right_item});
        recyclerView.setAdapter(homeRVAdapter);
        //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener

        voiceAnim = AnimationUtils.loadAnimation(mContext, R.anim.voice_rotate);
        voiceAnim.setInterpolator(new LinearInterpolator());
        showVoice(1);
    }

    private void initView() {
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        voiceView = LayoutInflater.from(mContext).inflate(R.layout.part_voice, null);
        textView = LayoutInflater.from(mContext).inflate(R.layout.part_text, null);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter();
        ArrayList<View> views = new ArrayList<>();
        views.add(voiceView);
        views.add(textView);
        viewPagerAdapter.setViews(views);
        viewPager.setAdapter(viewPagerAdapter);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        tv_down = (TextView) view.findViewById(R.id.tv_down);
        llt_image = (RelativeLayout) view.findViewById(R.id.llt_image);
        tv_tag = (TextView) view.findViewById(R.id.tv_tag);
        iv_image = (ImageView) view.findViewById(R.id.iv_iamge);


        // 频谱
        spectrum_voice = (VoiceSpectrum) voiceView.findViewById(R.id.spectrum_voice);
        btn_asr = (Button) voiceView.findViewById(R.id.btn_asr);
        btn_rec_1 = (Button) voiceView.findViewById(R.id.btn_rec_1);
        btn_rec_2 = (Button) voiceView.findViewById(R.id.btn_rec_2);
        btn_send_message = (ImageView) textView.findViewById(R.id.btn_send_message);
        et_content = (EditText) textView.findViewById(R.id.et_content);
        et_content.setOnKeyListener(onKey);
        btn_asr.setVisibility(View.VISIBLE);

        btn_send_message.setOnClickListener(this);
        btn_asr.setOnClickListener(this);
        spectrum_voice.setOnClickListener(this);
        et_content.setOnClickListener(this);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.e(TAG, "onPageScrolled: "+position );
            }

            @Override
            public void onPageSelected(int position) {
                Log.e(TAG, "onPageSelected: "+position );
                isVoice = position==0;
                InputMethodManager imm = (InputMethodManager)HomeFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(position==0&&imm.isActive()){
                    //关闭输入框
                    imm.hideSoftInputFromWindow(HomeFragment.this.getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        String tag="凉凉";
                        HashMap<String,String> map = JsoupUtil.getMp3s("http://www.ytmp3.cn/?search="+tag, "http://www.ytmp3.cn/", tag);
                        Set<String> keySet = map.keySet();
                        for (String s : keySet) {
                            Log.e(TAG, "run: "+s+"  "+map.get(s) );
                        }
                        for (String s : keySet) {
                            String path = JsoupUtil.getSongUrl(map.get(s));
                            Log.e(TAG, "run: "+s+"  "+path );
                        }
                    }
                }.start();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.e(TAG, "onPageScrollStateChanged: "+state );

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MediaButtonReceiver.unregister(this);
    }
    int lifeState=0;
    @Override
    public void onResume() {
        super.onResume();
        lifeState=BaseContants.ONRESUME;
    }

    @Override
    public void onPause() {
        lifeState=BaseContants.ONPAUSE;
        super.onPause();
    }

    View.OnKeyListener onKey = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(keyCode==KeyEvent.KEYCODE_ENTER&&event.getAction()==KeyEvent.ACTION_DOWN) {
                String content = et_content.getText().toString().trim();
                LogUtils.i("---------------content--------------" + content);
                //ToastUtil.showLongDefault(mContext, "onkey" + "content");
                if (TextUtils.isEmpty(content)) {
                    ToastUtil.showLongDefault(mContext, "您输入的内容为空。");
                } else {
                    et_content.setTag(true);
//                    addMessage(1, content);
                    et_content.setText("");

                }
                return true;
            }
            return false;
        }
    };
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_message:// 文本的发送
                String content = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    ToastUtil.showLongDefault(mContext, getString(R.string.content_null));
                } else {
                    commandParse(content);
                    et_content.setText("");
                }
                //bugly测试
//                CrashReport.testJavaCrash();
                break;
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
            case R.id.et_content:
                break;
            default:
                break;
        }
    }
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
            Log.e(TAG, "onError: "+speechError.getMessage()+"   "+speechError.getErrorCode() );
            showVoice(1);
        }
    };

    private void addMessage(int type,String result) {
        ChatMessage chatMessage = new ChatMessage(type, DateUtil.getCurrentTimeStr(), result);
        homeRVAdapter.addData(chatMessage);
    }
    private void commandParse(String content){
        addMessage(ChatMessage.Type.CHAT_RGIHT,content);
        Response response = OperateUtils.operateCommand(CommandParse.parse(content), MainActivity.mainActivity);
        addMessage(ChatMessage.Type.CHAT_LEFT,response.getShowContent());
        voiceUtils.startSpeak(TextUtils.isEmpty(response.getSpeakContent())?response.getShowContent():response.getSpeakContent());
    }

    @Override
    public void clickHandle(KeyEvent event) {
        Log.e(TAG, "clickHandle: " +lifeState );
        if(lifeState==BaseContants.ONRESUME&&!isVoice) {
            voiceUtils.startListening();
        }
    }
}
