package com.example.sx.practicalassistant.view.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sx.practicalassistant.R;
import com.example.sx.practicalassistant.bean.HelpBean;
import com.example.sx.practicalassistant.view.adapter.HelpRVAdapter;

import java.util.ArrayList;


public class HelpFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private HelpRVAdapter helpRVAdapter;

    public HelpFragment() {
        // Required empty public constructor
    }

    public static HelpFragment newInstance() {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_help, container, false);
        init();
        return view;
    }

    private void init() {
        recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerview));
        ArrayList<HelpBean> list = new ArrayList<>();
        list.add(new HelpBean("打开应用",HelpBean.TITLE));
        list.add(new HelpBean("打开+应用名（打开酷狗音乐）",HelpBean.DETAIL));
        list.add(new HelpBean("打电话",HelpBean.TITLE));
        list.add(new HelpBean("打电话给+联系人名（打电话给小明）",HelpBean.DETAIL));
        list.add(new HelpBean("呼叫+手机号（呼叫10086）",HelpBean.DETAIL));
        list.add(new HelpBean("发短信",HelpBean.TITLE));
        list.add(new HelpBean("第一步：发短信给+手机号或联系人名",HelpBean.DETAIL));
        list.add(new HelpBean("第二步：说出短信内容",HelpBean.DETAIL));
        list.add(new HelpBean("播放音视频",HelpBean.TITLE));
//        list.add(new HelpBean("播放音乐",HelpBean.DETAIL));
        list.add(new HelpBean("播放+音乐名/音乐名中关键字（播放放过自己）",HelpBean.DETAIL));
        list.add(new HelpBean("播放音乐+音乐名/音乐名中关键字（播放音乐放过）",HelpBean.DETAIL));
//        list.add(new HelpBean("播放视频",HelpBean.DETAIL));
        list.add(new HelpBean("播放视频+视频名/关键字（播放视频猛龙）",HelpBean.DETAIL));
        list.add(new HelpBean("生字查询",HelpBean.TITLE));
        list.add(new HelpBean("第一步：生字查询",HelpBean.DETAIL));
        list.add(new HelpBean("第二步：输入要查询的生字，确认/发送",HelpBean.DETAIL));
        list.add(new HelpBean("快速计算",HelpBean.TITLE));
        list.add(new HelpBean("计算+四则表达式（支持小数，但不支持括号和优先级）",HelpBean.DETAIL));
        list.add(new HelpBean("桌面语音的启用",HelpBean.TITLE));
        list.add(new HelpBean("请在桌面小部件里查找“安卓助手语音”，并添加桌面",HelpBean.DETAIL));
        helpRVAdapter = new HelpRVAdapter(getContext(), list,new int[]{R.layout.help_item,R.layout.help_detail});
        recyclerView.setAdapter(helpRVAdapter);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
                c.drawColor(Color.parseColor("#ff99cccc"));
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(5, 3,5,0);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
