package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.ZDMX_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 学习币使用明细
 */
public class ZdmxActivity extends AppCompatActivity implements View.OnClickListener {
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final String TAG = "ZdmxActivity";

    private String openid;
    private ArrayList<ZDMX_Entity> mZdmxList;
    private MyAdapter mAdapter;

    private TextView tvData;
    private TextView tvXiaoHao;
    private TextView tvChongzhi;
    private ImageView ivBack;
    private RelativeLayout mChoiceData;
    private ListView mListView;

    private String rq;//日期
    private int cz;//充值
    private int xh;//消耗

    private AlertDialog alertDialog;//等待对话框
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0x01://从服务器获取到数据解析后更新
                    alertDialog.dismiss();
                    tvXiaoHao.setText(String.valueOf(xh));
                    tvChongzhi.setText(String.valueOf(cz));
                    mAdapter.notifyDataSetChanged();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zdmx);
        //隐藏标题栏,有效
        getSupportActionBar().hide();
        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");

        initView();
        //获取当前时间
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String rq = sdf.format(d);
        tvData.setText(rq.replaceAll("-","年") + "月");
        getDataFromService(rq);
    }

    private void getDataFromService(String rq) {
        alertDialog.show();
        mZdmxList.clear();//清除原有数据
        mAdapter.notifyDataSetChanged();

        //获取账单明细数据
        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&rq=")
                .append(rq);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(Constants.ZDMX_URL)
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: 获取账单明细数据失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.d(TAG, "onResponse: " + responseInfo);
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    String error = jsonObject.getString("error");
                    if (error.equals("false")) {//获取项目学习数据成功
                        Log.d(TAG, "onResponse: 获取账单明细数据成功");
                        nalysisData(responseInfo);//解析数据
                        Message msg = new Message();
                        msg.what = 0x01;
                        mHandler.sendMessage(msg);
                    } else if (error.equals("true")) {//获取项目学习数据失败
                        String message = jsonObject.getString("message");
                        Log.d(TAG, "onResponse: 获取账单明细数据失败--失败信息是：" + message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //解析数据
    private void nalysisData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            rq = jsonObject.getString("rq");
            cz = jsonObject.getInt("cz");
            xh = jsonObject.getInt("xh");
            JSONArray mxlist = jsonObject.getJSONArray("mxlist");
            for (int i = 0; i < mxlist.length(); i++) {
                ZDMX_Entity ze = new ZDMX_Entity();
                JSONObject zdmx = mxlist.getJSONObject(i);
                ze.DDH = zdmx.getString("DDH");
                ze.FPH = zdmx.getString("FPH");
                ze.FY = zdmx.getInt("FY");
                ze.ID = zdmx.getString("ID");
                ze.LX = zdmx.getString("LX");
                ze.SFKP = zdmx.getString("SFKP");
                ze.SM = zdmx.getString("SM");
                ze.YHID = zdmx.getString("YHID");
                ze.ZFSJ = zdmx.getString("ZFSJ");
                ze.ZFZT = zdmx.getString("ZFZT");
                mZdmxList.add(ze);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("正在加载数据...")
                .create();
        mZdmxList = new ArrayList<>();
        mAdapter = new MyAdapter();

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        tvData = findViewById(R.id.tv_data);
        tvXiaoHao = findViewById(R.id.tv_xiaohao);
        tvChongzhi = findViewById(R.id.tv_chongzhi);
        mChoiceData = findViewById(R.id.rl_choice_data);
        mChoiceData.setOnClickListener(this);
        mListView = findViewById(R.id.lv_list);
        mListView.setAdapter(mAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back://返回
                finish();
                break;
            case R.id.rl_choice_data://选择日期
                // 初始化日期
                final Calendar calendar = Calendar.getInstance();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH);
                int dd = calendar.get(Calendar.DAY_OF_MONTH);

                final int[] Year = new int[1];
                final int[] Month = new int[1];
                Year[0] = yy;
                Month[0] = mm +1 ;

                DatePickerDialog dlg = new DatePickerDialog(new ContextThemeWrapper(ZdmxActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar), null, yy, mm, dd) {
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        LinearLayout mSpinners = (LinearLayout) findViewById(getContext().getResources().getIdentifier("android:id/pickers", null, null));
                        if (mSpinners != null) {
                            NumberPicker mMonthSpinner = (NumberPicker) findViewById(getContext().getResources().getIdentifier("android:id/month", null, null));
                            NumberPicker mYearSpinner = (NumberPicker) findViewById(getContext().getResources().getIdentifier("android:id/year", null, null));
                            mSpinners.removeAllViews();
                            if (mMonthSpinner != null) {
                                mSpinners.addView(mMonthSpinner);
                            }
                            if (mYearSpinner != null) {
                                mSpinners.addView(mYearSpinner);
                            }
                        }
                        View dayPickerView = findViewById(getContext().getResources().getIdentifier("android:id/day", null, null));
                        if (dayPickerView != null) {
                            dayPickerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onDateChanged(DatePicker view, int year, int month, int day) {
                        super.onDateChanged(view, year, month, day);
                        setTitle("请选择日期");
                        Year[0] = year;
                        Month[0] = month + 1;
                    }
                };
                dlg.setTitle("请选择日期");
                //添加时间框的确定按钮，其实setButton是AlertDialog中的方法，DatePickerDialog继承过来的
                dlg.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Month[0]<10){
                            tvData.setText(Year[0] + "年0" + Month[0] + "月");
                        }else{
                            tvData.setText(Year[0] + "年" + Month[0] + "月");
                        }
                        if(Month[0]<10){
                            getDataFromService(Year[0] + "-0" + Month[0]);
                        }else{
                            getDataFromService(Year[0] + "-" + Month[0]);
                        }
                    }
                });
//                //添加时间框的取消按钮
//                dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //将文本内容置为空
//                    }
//                });
                dlg.show();

                break;
        }
    }


    public class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mZdmxList.size();
        }

        @Override
        public Object getItem(int i) {
            return mZdmxList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(ZdmxActivity.this, R.layout.list_zhangdan_mingxi, null);
                holder = new ViewHolder();
                holder.tvItemName = convertView.findViewById(R.id.tv_item_name);
                holder.tvData = convertView.findViewById(R.id.tv_data);
                holder.lvNum = convertView.findViewById(R.id.tv_num);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvItemName.setText(mZdmxList.get(i).SM);
            holder.tvData.setText(mZdmxList.get(i).ZFSJ);
            if (mZdmxList.get(i).LX.equals("00")) {//充值
                holder.lvNum.setText("+" + mZdmxList.get(i).FY);
            } else if (mZdmxList.get(i).LX.equals("01")) {//消耗
                holder.lvNum.setText("-" + mZdmxList.get(i).FY);
            }

            return convertView;
        }

        class ViewHolder {
            TextView tvItemName;
            TextView tvData;
            TextView lvNum;
        }
    }

}
