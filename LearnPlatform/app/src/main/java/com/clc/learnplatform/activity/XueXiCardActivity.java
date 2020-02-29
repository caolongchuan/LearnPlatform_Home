package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.LXK_Entity;
import com.clc.learnplatform.global.Constants;

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
 * 学习卡
 */
public class XueXiCardActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "XueXiCardActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String DataString;
    private ArrayList<KSXM_Entity> mKsxmList;//项目list
    private String openid;
    private String head;
    private String name;
    private String phone;
    private int coin;

    private ImageView ivBack;
    private TextView tvMsg;
    private ImageView tvHead;
    private TextView tvName;
    private TextView tvPhone;
    private TextView tvCoin;

    private ArrayList<LXK_Entity> mLxkList;
    private ListView lvListView;
    private MyAdapter mAdapter;

    private TextView mRecharge;//充值
    private Button btnAdd;


    private AlertDialog alertDialog;//等待对话框


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0x01://获取完了数据
                    alertDialog.dismiss();
                    mAdapter.notifyDataSetChanged();
                    if (mLxkList.size() > 0) {
                        tvMsg.setVisibility(View.GONE);
                        lvListView.setVisibility(View.VISIBLE);
                    } else {
                        tvMsg.setVisibility(View.VISIBLE);
                        lvListView.setVisibility(View.GONE);
                    }

                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xue_xi_card);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");
        head = intent.getStringExtra("head");
        name = intent.getStringExtra("name");
        phone = intent.getStringExtra("phone");
        coin = intent.getIntExtra("coin", 0);
        DataString = intent.getStringExtra("data_string");

        initView();
        initData();
        getDataFromService();
    }

    private void initData() {
        try {
            JSONObject jsonObject = new JSONObject(DataString);
            //解析项目列表
            mKsxmList = new ArrayList<>();
            JSONArray ksxmlist = jsonObject.getJSONArray("ksxmlist");
            for (int i = 0; i < ksxmlist.length(); i++) {
                JSONObject ksxm_obj = ksxmlist.getJSONObject(i);
                KSXM_Entity ke = new KSXM_Entity();
                ke.ID = ksxm_obj.getString("ID");
                ke.BZ = ksxm_obj.getString("BZ");
                ke.CTL = ksxm_obj.getInt("CTL");
                ke.DM = ksxm_obj.getString("DM");
                ke.MNXH = ksxm_obj.getInt("MNXH");
                ke.NAME = ksxm_obj.getString("NAME");
                ke.STXH = ksxm_obj.getInt("STXH");
                ke.ZLID = ksxm_obj.getString("ZLID");
                ke.ZT = ksxm_obj.getString("ZT");
                ke.ZTL = ksxm_obj.getInt("ZTL");
                mKsxmList.add(ke);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //从服务器获取数据
    private void getDataFromService() {
        alertDialog.show();
        mLxkList.clear();
        mAdapter.notifyDataSetChanged();

        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.MNKS_URL)//模拟考试接口
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 获取学习卡数据失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "autoSignIn.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//获取学习卡数据失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//获取学习卡数据成功
                        analysisData(responseInfo);//解析数据
                        Message msg = new Message();
                        msg.what = 0x01;
                        mHandler.sendMessage(msg);//通知UI线程更新界面
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
            JSONArray xxklist = jsonObject.getJSONArray("xxklist");
            for (int i = 0; i < xxklist.length(); i++) {
                JSONObject xxk = xxklist.getJSONObject(i);
                LXK_Entity le = new LXK_Entity();
                le.KH = xxk.getString("KH");
                le.MM = xxk.getString("MM");
                le.ZT = xxk.getString("ZT");
                le.BDSJ = xxk.getString("BDSJ");
                le.XXB = xxk.getInt("XXB");
                le.YHID = xxk.getString("YHID");
                le.LX = xxk.getString("LX");
                le.ZDTGY = xxk.getString("ZDTGY");
                le.YXTS = xxk.getInt("YXTS");
                le.XMID = xxk.getString("XMID");
                le.YXQ = xxk.getString("YXQ");
                le.STCS = xxk.getInt("STCS");
                mLxkList.add(le);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("正在加载数据...")
                .create();
        mLxkList = new ArrayList<>();
        mAdapter = new MyAdapter();
        lvListView = findViewById(R.id.lv_list);
        lvListView.setAdapter(mAdapter);

        tvHead = findViewById(R.id.iv_user_head);
        Glide.with(this).load((String) head).into(tvHead);//显示头像
        tvName = findViewById(R.id.tv_user_name);
        tvName.setText(name);
        tvPhone = findViewById(R.id.tv_phone_number);
        tvPhone.setText(phone);
        tvCoin = findViewById(R.id.tv_learn_b);
        tvCoin.setText(String.valueOf(coin));

        tvMsg = findViewById(R.id.tv_jilu);
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        mRecharge = findViewById(R.id.btn_recharge);
        mRecharge.setOnClickListener(this);
        btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back://返回
                finish();
                break;
            case R.id.btn_recharge://充值
                Intent intent1 = new Intent();
                intent1.putExtra("openid",openid);
                intent1.setClass(this, ChongZhiActivity.class);
                startActivity(intent1);
                break;
            case R.id.btn_add://添加学习卡
                Intent intent = new Intent();
                intent.putExtra("openid",openid);
                intent.putExtra("data_string",DataString);
                intent.setClass(this, AddCardActivity.class);
                startActivity(intent);
                break;
        }
    }

    public class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mLxkList.size();
        }

        @Override
        public Object getItem(int i) {
            return mLxkList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(XueXiCardActivity.this, R.layout.list_xuexi_card, null);
                holder = new ViewHolder();
                holder.tvItemName = convertView.findViewById(R.id.tv_item_name);
                holder.tvSearchTime = convertView.findViewById(R.id.tv_search_time);
                holder.tvYouXiaoQi = convertView.findViewById(R.id.tv_youxiaoqi);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            for(int j=0;j<mKsxmList.size();j++){
                if(mKsxmList.get(i).ID.equals(mLxkList.get(i).XMID)){
                    holder.tvItemName.setText(mKsxmList.get(i).NAME);
                }
            }
            holder.tvSearchTime.setText(mLxkList.get(i).STCS);
            holder.tvYouXiaoQi.setText(mLxkList.get(i).YXQ);

            return convertView;
        }

        class ViewHolder {
            TextView tvItemName;
            TextView tvSearchTime;
            TextView tvYouXiaoQi;
        }
    }


}
