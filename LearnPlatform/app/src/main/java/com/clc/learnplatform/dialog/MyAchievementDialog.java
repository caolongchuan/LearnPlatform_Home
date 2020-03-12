package com.clc.learnplatform.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.WDCJ_Entity;

/**
 * 我的成绩
 */
public class MyAchievementDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = "MyAchievementDialog";
    //在构造方法里提前加载了样式
    private Context context;//上下文
    private int layoutResID;//布局文件id
    private WDCJ_Entity mWdcj;//我的成绩

    private TextView tvHighScroe;//历史最高分
    private TextView tvPjScroe;//平均分数
    private TextView tvMnksTime;//模拟考试次数
    private TextView tvJgTime;//及格次数
    private TextView tvRightLv;//正确率
    private TextView tvWdwTime;//限时内未答完次数

    public MyAchievementDialog(Context context, WDCJ_Entity wdcj) {
        super(context, R.style.MyAchievementDialog);//加载dialog的样式
        this.context = context;
        this.layoutResID = R.layout.dialog_achievement;
        this.mWdcj = wdcj;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null){
            Log.i(TAG, "onCreate: savedInstanceState != null");
        }else{
            Log.i(TAG, "onCreate: saveInstanceState == null");
        }

        //提前设置Dialog的一些样式
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);//设置dialog显示居中
        //dialogWindow.setWindowAnimations();设置动画效果
        setContentView(layoutResID);

        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth() * 4 / 5;// 设置dialog宽度为屏幕的4/5
        lp.height = display.getHeight() * 3 / 5;
        getWindow().setAttributes(lp);
        setCanceledOnTouchOutside(true);//点击外部Dialog消失

        tvHighScroe = findViewById(R.id.tv_hi_scroe);
        tvPjScroe = findViewById(R.id.tv_pingjun_scroe);
        tvMnksTime = findViewById(R.id.tv_mnks_time);
        tvJgTime = findViewById(R.id.tv_jige_time);
        tvRightLv = findViewById(R.id.tv_right_lv);
        tvWdwTime = findViewById(R.id.tv_wdw_time);

        tvHighScroe.setText(String.valueOf(mWdcj.LSGF)+"分");
        tvPjScroe.setText(String.valueOf(mWdcj.PJF)+"分");
        tvMnksTime.setText(String.valueOf(mWdcj.MNCS)+"次");
        tvJgTime.setText(String.valueOf(mWdcj.JGCS)+"次");
        tvRightLv.setText(String.valueOf(mWdcj.ZQL)+"%");
        tvWdwTime.setText(String.valueOf(mWdcj.WWCCS)+"次");
    }


    @Override
    public void onClick(View v) {
        dismiss();//注意：我在这里加了这句话，表示只要按任何一个控件的id,弹窗都会消失，不管是确定还是取消。
    }

}
