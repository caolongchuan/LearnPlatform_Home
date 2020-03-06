package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.ST_Entity;
import com.clc.learnplatform.entity.ZDA_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.ToastUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 搜答案
 */
public class SerchAnswerActivity extends AppCompatActivity implements View.OnClickListener {
    public static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final String TAG = "--SerchAnswerActivity--";

    private String openid;
    private String xmid;

    private ImageView mBack;
    private TextView mTishi1;//剩余或者消耗
    private TextView mSYTime;//剩余次数
    private TextView mTishi2;//
    private EditText mSearchText;
    private ImageView mSearchIcon;
    private TextView mMsg;
    private ListView mListView;
    private MyAdapter mAdapter;

    private ZDA_Entity mZdaEntity;
    private ArrayList<ST_Entity> mStList;

    private int mSearchTime = 0;//记录搜索次数

    private AlertDialog alertDialog;//等待对话框
    private boolean isBindingCard;//标示是否有绑定学习卡

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0x01://搜索完毕
                    alertDialog.dismiss();
                    //判断有没有搜索到内容
                    if(mStList.size()>0){
                        mMsg.setVisibility(View.GONE);
                        //更新剩余次数
                        if(isBindingCard){
                            getShengYuTime();//从服务器获取剩余次数
                        }else{
                            mSearchTime++;
                        }
                    }else{
                        mMsg.setVisibility(View.VISIBLE);
                    }
                    mAdapter.notifyDataSetChanged();
                    break;
                case 0x02://获取剩余次数
                    Bundle data = message.getData();
                    String sycs = data.getString("sycs");
                    mSYTime.setText(sycs);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serch_answer);
        //隐藏标题栏,有效
        getSupportActionBar().hide();
        Intent intent = getIntent();
        this.openid = intent.getStringExtra("openid");
        this.xmid = intent.getStringExtra("xmid");
        this.isBindingCard = intent.getBooleanExtra("bind_crad",false);

        initView();
        initData();
    }

    /**
     * 从服务器获取剩余次数
     */
    private void getShengYuTime() {
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        String xzda = mSearchText.getText().toString().trim();
        sb.append("openid=").append(openid).append("&xmid=").append(xmid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.XX_TOST_URL)//搜答案
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//成功
                        JSONObject jsonObject1 = new JSONObject(responseInfo);
                        JSONObject zda = jsonObject1.getJSONObject("zda");
                        String sycs = zda.getString("SYCS");
                        Bundle bundle = new Bundle();
                        bundle.putString("sycs",sycs);
                        Message msg = new Message();
                        msg.what = 0x02;
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });    }

    private void initData() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("正在检索数据，请稍候...")
                .create();

        if(isBindingCard){
            mTishi1.setText("剩余：");
            mTishi2.setText("次");
            getShengYuTime();//从服务器获取剩余次数
        }else{
            mTishi1.setText("消耗：");
            mSYTime.setText("1");
            mTishi2.setText("个学习币/次");
        }
    }

    private void initView() {
        mBack = findViewById(R.id.iv_back);
        mBack.setOnClickListener(this);
        mTishi1 = findViewById(R.id.tv_tishi1);
        mSYTime = findViewById(R.id.tv_surplus_time);//剩余次数
        mTishi2 = findViewById(R.id.tv_tishi2);
        mSearchText = findViewById(R.id.tv_search);
        mSearchIcon = findViewById(R.id.iv_search);
        mSearchIcon.setOnClickListener(this);
        mListView = findViewById(R.id.lv_list);
        mMsg = findViewById(R.id.iv_no_data);

        mZdaEntity = new ZDA_Entity();
        mStList = new ArrayList<>();
        mAdapter = new MyAdapter();
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back://返回
                Intent intent = new Intent();
                intent.putExtra("mSearchTime",mSearchTime);
                setResult(1, intent);
                finish();
                break;
            case R.id.iv_search://执行搜索
                //先判断是否够四个字
                String s = mSearchText.getText().toString();
                if(s.length()<4){
                    ToastUtil.getInstance().shortShow("请输入至少4个汉字");
                }else{
                    alertDialog.show();
                    mMsg.setVisibility(View.GONE);
                    mStList.clear();
                    mAdapter.notifyDataSetChanged();
                    doSearch();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent();
            intent.putExtra("mSearchTime",mSearchTime);
            setResult(1, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //执行搜索
    private void doSearch() {
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        String xzda = mSearchText.getText().toString().trim();
        sb.append("openid=").append(openid).append("&xmid=").append(xmid).append("&xzda=").append(xzda);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.XX_TOST_URL)//搜答案
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//成功
                        analysisData(responseInfo);//解析数据
                        Message msg = new Message();
                        msg.what = 0x01;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //解析数据
    private void analysisData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            JSONObject zda = jsonObject.getJSONObject("zda");//找答案
            mZdaEntity.ID = zda.getString("ID");
            mZdaEntity.XMID = zda.getString("XMID");
            mZdaEntity.YHID = zda.getString("YHID");
            mZdaEntity.SYCS = zda.getString("SYCS");
            mZdaEntity.GXSJ = zda.getString("GXSJ");
            mZdaEntity.TJSJ = zda.getString("TJSJ");
            JSONArray stlist = jsonObject.getJSONArray("stlist");
            for (int i = 0; i < stlist.length(); i++) {
                JSONObject st = stlist.getJSONObject(i);
                ST_Entity se = new ST_Entity();
                se.ID = st.getString("ID");
                se.TM = st.getString("TM");
                se.TP = st.getString("TP");
                se.TX = st.getString("TX");
                se.TXID = st.getString("TXID");
                se.XX = st.getString("XX");
                se.XZDA = st.getString("XZDA");
                se.ZQDA = st.getString("ZQDA");
                mStList.add(se);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class MyAdapter extends BaseAdapter {
        char[] cc = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};

        @Override
        public int getCount() {
            return mStList.size();
        }

        @Override
        public Object getItem(int i) {
            return mStList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            final ViewHolder holder;
            String[] split = mStList.get(i).XX.split("\\^");

            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.list_search_item, null);
                holder = new ViewHolder();
                holder.tv_TM = convertView.findViewById(R.id.tv_tm);
                holder.iv_TM = convertView.findViewById(R.id.iv_tm);
                holder.ll_XX = convertView.findViewById(R.id.ll_xx);
                holder.tv_ZQDA = convertView.findViewById(R.id.tv_zqda);

                holder.linearLayouts = new LinearLayout[10];
                holder.textViews = new TextView[10];
                holder.imageViews = new ImageView[10];
                for (int j = 0; j < 10; j++) {
                    holder.linearLayouts[j] = new LinearLayout(SerchAnswerActivity.this);
                    holder.linearLayouts[j].setOrientation(LinearLayout.HORIZONTAL);
                    holder.linearLayouts[j].setGravity(Gravity.CENTER_VERTICAL);
                    holder.linearLayouts[j].setPadding(0, 10, 0, 10);
                    LinearLayout.LayoutParams ll_lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    holder.linearLayouts[j].setLayoutParams(ll_lp);
                    holder.textViews[j] = new TextView(SerchAnswerActivity.this);
                    holder.imageViews[j] = new ImageView(SerchAnswerActivity.this);
                    LinearLayout.LayoutParams iv_lp = new LinearLayout.LayoutParams(60, 60);
                    holder.imageViews[j].setLayoutParams(iv_lp);
                    holder.linearLayouts[j].addView(holder.textViews[j]);
                    holder.linearLayouts[j].addView(holder.imageViews[j]);
                    holder.linearLayouts[j].setVisibility(View.GONE);
                    holder.ll_XX.addView(holder.linearLayouts[j]);
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tv_TM.setText(mStList.get(i).TM.trim());
            if (mStList.get(i).TP.equals("null")) {
                holder.iv_TM.setVisibility(View.GONE);
            } else {
                holder.iv_TM.setVisibility(View.VISIBLE);
                Glide.with(SerchAnswerActivity.this).load(mStList.get(i).TP).into(holder.iv_TM);
            }
            holder.tv_ZQDA.setText("正确答案：" + mStList.get(i).ZQDA);

            for (int j = 0; j < 10; j++) {
                if (j < split.length) {
                    holder.linearLayouts[j].setVisibility(View.VISIBLE);
                    String split1 = split[j].trim().replace("/n", "");
                    if (split1.contains("http")) {
                        holder.textViews[j].setText(cc[j] + "、");
                        holder.imageViews[j].setVisibility(View.VISIBLE);
                        String s = StringUtils.substringAfter(split1, "'");
                        String s1 = StringUtils.substringBefore(s, "'");
                        Glide.with(SerchAnswerActivity.this).load(s1).into(holder.imageViews[j]);
                    } else {
                        holder.imageViews[j].setVisibility(View.GONE);
                        holder.textViews[j].setText(cc[j] + "、" + split1);
                    }
                } else {
                    holder.linearLayouts[j].setVisibility(View.GONE);
                }
            }
            return convertView;
        }


        class ViewHolder {
            LinearLayout ll_XX;
            LinearLayout[] linearLayouts;
            TextView[] textViews;
            ImageView[] imageViews;
            TextView tv_TM;
            ImageView iv_TM;
            TextView tv_ZQDA;
        }

    }

}
