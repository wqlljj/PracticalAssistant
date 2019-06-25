package com.example.sx.practicalassistant.view.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.sx.practicalassistant.R;
import com.example.sx.practicalassistant.bean.HelpBean;

import java.util.ArrayList;

/**
 * Created by wqlljj on 2017/3/6.
 */

public class HelpRVAdapter extends BaseRVAdapter<HelpBean> {
    private final ArrayList<HelpBean> list;

    public HelpRVAdapter(Context context, @NonNull ArrayList<HelpBean> list, @IdRes int[] layouts) {
        super(context, list, layouts);
        this.list = list;
    }

    @Override
    public int getItemLayoutType(HelpBean helpBean) {
        return helpBean.getType();
    }

    @Override
    public ViewHolder createVH(View view, int index) {
        return new ViewHolder(view,index);
    }

    @Override
    public void onBindViewHolder(BaseRVAdapter.ViewHolder holder, int position) {
        ((ViewHolder) holder).content.setText(list.get(position).getContent());
    }

    public class ViewHolder extends BaseRVAdapter.ViewHolder{
        private final TextView content;
        //index:布局索引
        public ViewHolder(View itemView, int index) {
            super(itemView);
            content = ((TextView) itemView.findViewById(R.id.content));
        }
    }
}
