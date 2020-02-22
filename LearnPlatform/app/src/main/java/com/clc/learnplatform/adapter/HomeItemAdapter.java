package com.clc.learnplatform.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.KSXM_Entity;

import java.util.ArrayList;

public class HomeItemAdapter extends BaseAdapter {
    private Activity mActivity;
    private ArrayList<String> mList;
    private ArrayList<KSXM_Entity> mKsxmList;//项目list
    private String openid;

    private ArrayList<HomeItemSonAdapter> mSonAdapter;//子listview的adapter list
    private boolean[] showControl;//表明对应的item是否需要展开

    public HomeItemAdapter(Activity activity, ArrayList<String> list, ArrayList<KSXM_Entity> list1,String openid) {
        mActivity = activity;
        mList = list;
        mKsxmList = list1;
        this.openid = openid;

        mSonAdapter = new ArrayList<>();
        for(int i=0;i<mList.size();i++){
            String bz = mList.get(i);
            ArrayList<KSXM_Entity> kel = new ArrayList<>();
            for (int j = 0; j < mKsxmList.size(); j++) {
                if (bz != null && bz.equals(mKsxmList.get(j).BZ)) {
                    kel.add(mKsxmList.get(j));
                }
            }

            HomeItemSonAdapter hisa = new HomeItemSonAdapter(mActivity, kel,openid);
            mSonAdapter.add(hisa);
        }

        showControl = new boolean[mList.size()];//此时数组中的默认值都是false
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
            convertView = View.inflate(mActivity, R.layout.list_home_item, null);
            holder = new ViewHolder();
            holder.llItemMain = convertView.findViewById(R.id.ll_home_item_main);
            holder.tvItemName = convertView.findViewById(R.id.tv_item_name);
            holder.lvPersonList = convertView.findViewById(R.id.lv_son);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.llItemMain.setTag(position);//为点击的区域设置Tag为position
        if (showControl[position]) {//加载item的时候查看是否需要打开或隐藏
            holder.lvPersonList.setVisibility(View.VISIBLE);
        } else {
            holder.lvPersonList.setVisibility(View.GONE);
        }
        holder.llItemMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int) v.getTag();//获取点击位置对应的tag值
                if (showControl[tag]) {//改变数组中对应的值
                    showControl[tag] = false;
                } else {
                    showControl[tag] = true;
                }
               notifyDataSetChanged();//重新加载
            }
        });
        holder.tvItemName.setText(mList.get(position));
        holder.lvPersonList.setAdapter(mSonAdapter.get(position));
        setListViewHeightBasedOnChildren(holder.lvPersonList); // 执行该方法获取listview高度

        return convertView;
    }

    static class ViewHolder {
        LinearLayout llItemMain;
        TextView tvItemName;
        ListView lvPersonList;
    }

    /**
     * 在ListView中嵌套ListView时确保能将子ListView的项全部显示出来
     * @param listView
     */
    private void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }


}
