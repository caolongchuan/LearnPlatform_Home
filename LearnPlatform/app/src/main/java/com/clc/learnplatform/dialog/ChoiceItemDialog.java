package com.clc.learnplatform.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.adapter.HomeItemAdapter;
import com.clc.learnplatform.adapter.HomeItemSonAdapter;
import com.clc.learnplatform.entity.KHZL_Entity;
import com.clc.learnplatform.entity.KSXM_Entity;

import java.util.ArrayList;

/**
 * 选择证书种类的对话框
 */
public class ChoiceItemDialog extends Dialog {
    //在构造方法里提前加载了样式
    private Context context;//上下文
    private int layoutResID;//布局文件id
    private ArrayList<KHZL_Entity> mKHZL_List;//证书种类list

    private ListView mListView;
    private MyAdapter mAdapter;

    public SeleListener seleListener;

    public ChoiceItemDialog(Context context,ArrayList<KHZL_Entity> list,SeleListener seleListener) {
        super(context, R.style.ChoiceItemDialog);//加载dialog的样式
        this.context = context;
        this.mKHZL_List = list;
        this.layoutResID = R.layout.dialog_choice_item;
        this.seleListener = seleListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //提前设置Dialog的一些样式
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);//设置dialog显示居中
        //dialogWindow.setWindowAnimations();设置动画效果
        setContentView(layoutResID);

        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth() * 4 / 5;// 设置dialog宽度为屏幕的4/5
        lp.height = display.getHeight() * 1 / 5;
        getWindow().setAttributes(lp);
        setCanceledOnTouchOutside(true);//点击外部Dialog消失

        mListView = findViewById(R.id.lv_list);
        mAdapter = new MyAdapter();
        mListView.setAdapter(mAdapter);
    }


    public class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mKHZL_List.size();
        }

        @Override
        public Object getItem(int i) {
            return mKHZL_List.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.list_item_choice, null);
                holder = new ViewHolder();
                holder.tvItemName = convertView.findViewById(R.id.tv_item_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvItemName.setText(mKHZL_List.get(i).NAME);
            holder.tvItemName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null!=seleListener){
                        seleListener.sele(mKHZL_List.get(i));
                    }
                    ChoiceItemDialog.this.dismiss();
                }
            });
            return convertView;
        }

        class ViewHolder {
            TextView tvItemName;
        }
    }

    //选择监听接口
    public interface SeleListener {
        public void sele(KHZL_Entity ke);
    }

}
