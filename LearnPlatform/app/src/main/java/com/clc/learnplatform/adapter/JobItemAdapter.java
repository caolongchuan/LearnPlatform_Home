package com.clc.learnplatform.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.activity.JobMsgActivity;
import com.clc.learnplatform.entity.QZXX_Entity;
import com.clc.learnplatform.entity.ZPXX_Entity;
import com.clc.learnplatform.entity.ZYLB_Entity;

import java.util.ArrayList;

public class JobItemAdapter extends BaseAdapter {
    private Activity mActivity;
    private ArrayList<ZPXX_Entity> mZpxxEntity;//招聘信息集合
    private ArrayList<QZXX_Entity> mQzxxEntity;//求职信息集合
    private ArrayList<ZYLB_Entity> mZylbEntity;//职业类别集合
    private String openid;
    private String qy;//区域

    private boolean showZpOrQz;//用于判断是显示招聘信息还是求职信息

    public JobItemAdapter(Activity activity,ArrayList<ZYLB_Entity> list,ArrayList<ZPXX_Entity> list1,
                          ArrayList<QZXX_Entity> list2, String openid,String qy){
        mActivity = activity;
        mZylbEntity = list;
        mZpxxEntity = list1;
        mQzxxEntity = list2;
        this.openid = openid;
        this.qy = qy;
    }


    @Override
    public int getCount() {
        if(!showZpOrQz){
            return mZpxxEntity.size();
        }else{
            return mQzxxEntity.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if(!showZpOrQz){
            return mZpxxEntity.get(position);
        }else{
            return mQzxxEntity.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
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
        if(!showZpOrQz){//招聘
            holder.tvTitlMsg.setText(mZpxxEntity.get(position).BT);//标题信息
            holder.tvFabuTime.setText("发布日期："+mZpxxEntity.get(position).TJSJ);//发布日期
            holder.tvSee.setOnClickListener(new View.OnClickListener() {//查看
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("openid",openid);
                    intent.putExtra("qy",qy);
                    intent.putExtra("zp_qz","0");//标示是招聘 (1是求职)
                    intent.putExtra("bt",mZpxxEntity.get(position).BT);
                    intent.putExtra("fbsj",mZpxxEntity.get(position).TJSJ);
                    for(int i=0;i<mZylbEntity.size();i++){
                        if(mZpxxEntity.get(position).ZWLB.equals(mZylbEntity.get(i).id)){
                            intent.putExtra("zwlb",mZylbEntity.get(i).value);
                        }
                    }

                    intent.putExtra("zprs",mZpxxEntity.get(position).ZPRS);
                    intent.putExtra("szcs",mZpxxEntity.get(position).SSS+" "+mZpxxEntity.get(position).SHI);
                    intent.putExtra("lxdh",mZpxxEntity.get(position).SJH);
                    intent.putExtra("zwms",mZpxxEntity.get(position).ZWMS);

                    intent.setClass(mActivity, JobMsgActivity.class);
                    mActivity.startActivity(intent);
                }
            });
        }else{//求职
            holder.tvTitlMsg.setText(mQzxxEntity.get(position).BT);//标题信息
            holder.tvFabuTime.setText("发布日期："+mQzxxEntity.get(position).TJSJ);//发布日期
            holder.tvSee.setOnClickListener(new View.OnClickListener() {//查看
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("openid",openid);
                    intent.putExtra("qy",qy);
                    intent.putExtra("zp_qz","1");//标示是求职 (0是招聘)
                    intent.putExtra("bt",mQzxxEntity.get(position).BT);
                    intent.putExtra("fbsj",mQzxxEntity.get(position).TJSJ);
                    for(int i=0;i<mZylbEntity.size();i++){
                        if(mZpxxEntity.get(position).ZWLB.equals(mZylbEntity.get(i).id)){
                            intent.putExtra("zwlb",mZylbEntity.get(i).value);
                        }
                    }
                    intent.putExtra("szcs",mQzxxEntity.get(position).SSS+" "+mQzxxEntity.get(position).SHI);
                    intent.putExtra("lxdh",mQzxxEntity.get(position).SJH);
                    intent.putExtra("jlms",mQzxxEntity.get(position).JLMS);
                    intent.putExtra("qzyx",mQzxxEntity.get(position).QZYX);


                    intent.setClass(mActivity, JobMsgActivity.class);
                    mActivity.startActivity(intent);
                }
            });
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvTitlMsg;//标题信息
        TextView tvFabuTime;//发布日期
        TextView tvSee;//发布按钮
    }

    /**
     * 更新数据
     * @param ZpxxEntity
     * @param QzxxEntity
     */
    public void updataData(ArrayList<ZPXX_Entity> ZpxxEntity, ArrayList<QZXX_Entity> QzxxEntity) {
        mZpxxEntity = ZpxxEntity;
        mQzxxEntity = QzxxEntity;
    }

    /**
     * 设置显示招聘信息还是求职信息
     * @param zporqz
     */
    public void setShowZpOrQz(boolean zporqz){
        showZpOrQz = zporqz;
        notifyDataSetChanged();
    }

    public boolean getShowZpOrQz(){
        return showZpOrQz;
    }

}
