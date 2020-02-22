package com.clc.learnplatform.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.clc.learnplatform.R;
import com.clc.learnplatform.activity.StudiedActivity;
import com.clc.learnplatform.entity.KSXM_Entity;

import java.util.ArrayList;

public class HomeItemSonAdapter extends BaseAdapter {
    private Activity mActivity;
    private ArrayList<KSXM_Entity> mList;
    private String openid;

    public HomeItemSonAdapter(Activity activity, ArrayList<KSXM_Entity> list,String openid){
        mActivity = activity;
        mList = list;
        this.openid = openid;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mActivity, R.layout.list_home_item_detial, null);
            holder = new ViewHolder();
            holder.tvItemName = convertView.findViewById(R.id.tv_item_detial_name);
            holder.tvItemSign = convertView.findViewById(R.id.tv_item_detial_sign);
            holder.lvStudied = convertView.findViewById(R.id.tv_goto_studied);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvItemName.setText(mList.get(position).NAME);
        holder.tvItemSign.setText(mList.get(position).DM);
        holder.lvStudied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //学习
                Intent intent = new Intent();
                intent.putExtra("openid", openid);
                intent.putExtra("xmid",mList.get(position).ID);
                intent.setClass(mActivity, StudiedActivity.class);
                mActivity.startActivityForResult(intent,100);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView tvItemName;
        TextView tvItemSign;
        TextView lvStudied;
    }
}
