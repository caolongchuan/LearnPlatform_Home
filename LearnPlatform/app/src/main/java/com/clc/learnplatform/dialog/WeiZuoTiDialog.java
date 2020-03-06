package com.clc.learnplatform.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.clc.learnplatform.R;

public class WeiZuoTiDialog extends Dialog {
    private Context context;
    private int layoutResID;//布局文件id

    private TextView tvMsg;
    private TextView tvMnksTime;
    private TextView tvLtl;
    private TextView tvRightLv;

    private TextView tv1;
    private TextView tv2;
    private TextView tv3;

    private int MNCSBZ;//规定模拟考试次数
    private int ZQLBZ;//规定正确率
    private int LXLBZ;//规定练体率

    private int mMnksTime;//模拟考试次数
    private int mLtl;//练题率
    private int mRightLv;//正确率

    public WeiZuoTiDialog(Context context, int mncsbz, int zqlbz, int lxlbz, int mnks_time, int ltl, int right_lv) {
        super(context, R.style.WeiZuoTiDialog);//加载dialog的样式
        this.context = context;
        this.layoutResID = R.layout.dialog_weizuoti;

        this.MNCSBZ = mncsbz;
        this.ZQLBZ = zqlbz;
        this.LXLBZ = lxlbz;

        this.mMnksTime = mnks_time;
        this.mLtl = ltl;
        this.mRightLv = right_lv;
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
        lp.height = display.getHeight() * 2 / 5;
        getWindow().setAttributes(lp);
        setCanceledOnTouchOutside(true);//点击外部Dialog消失

        tv1 = findViewById(R.id.tv_1);
        tv1.setText("模拟考试次数满" + MNCSBZ + "次");
        tv2 = findViewById(R.id.tv_2);
        tv2.setText("练题率" + LXLBZ + "%");
        tv3 = findViewById(R.id.tv_3);
        tv3.setText("正确率" + ZQLBZ + "%");

        tvMsg = findViewById(R.id.tv_msg);
        tvMnksTime = findViewById(R.id.tv_mnks_sign);
        tvLtl = findViewById(R.id.tv_ltl_sign);
        tvRightLv = findViewById(R.id.tv_rightlv_sign);

        if (mMnksTime > MNCSBZ) {
            tvMnksTime.setText("已达标");
            tvMnksTime.setBackground(context.getResources().getDrawable(R.drawable.shape_yidabiao_bg));
        }
        if (mLtl > LXLBZ) {
            tvLtl.setText("已达标");
            tvLtl.setBackground(context.getResources().getDrawable(R.drawable.shape_yidabiao_bg));
        }
        if (mRightLv > ZQLBZ) {
            tvRightLv.setText("已达标");
            tvRightLv.setBackground(context.getResources().getDrawable(R.drawable.shape_yidabiao_bg));
        }

        String sss = "您当前的模拟考试次数为" + mMnksTime + "次，练题率为" + mLtl + "%，正确率为" + mRightLv + "%";
        tvMsg.setText(sss);
    }
}
