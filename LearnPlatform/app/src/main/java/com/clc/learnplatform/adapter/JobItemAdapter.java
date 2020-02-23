package com.clc.learnplatform.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.activity.JobMsgActivity;
import com.clc.learnplatform.entity.LLQZ_Entity;
import com.clc.learnplatform.util.ToastUtil;

import java.util.ArrayList;

public class JobItemAdapter extends BaseAdapter {
    private Activity mActivity;
    private ArrayList<LLQZ_Entity> mList;
    private String openid;

    public JobItemAdapter(Activity activity,ArrayList<LLQZ_Entity> list,String openid){
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mActivity, R.layout.list_job_item, null);
            holder = new ViewHolder();
            holder.tvTitlMsg = convertView.findViewById(R.id.tv_title_msg);
            holder.tvFabuTime = convertView.findViewById(R.id.tv_fabu_time);
            holder.tvSee = convertView.findViewById(R.id.tv_see);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvTitlMsg.setText(mList.get(position).e);//标题信息
        holder.tvFabuTime.setText("发布日期："+mList.get(position).g);//发布日期
        holder.tvSee.setOnClickListener(new View.OnClickListener() {//查看
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("openid",openid);
                intent.setClass(mActivity, JobMsgActivity.class);
                mActivity.startActivity(intent);
            }

        });

        return convertView;
    }

    static class ViewHolder {
        TextView tvTitlMsg;//标题信息
        TextView tvFabuTime;//发布日期
        TextView tvSee;//发布按钮
    }

}
