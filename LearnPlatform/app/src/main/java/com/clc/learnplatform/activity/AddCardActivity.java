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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.dialog.ChoiceItemDialog;
import com.clc.learnplatform.dialog.ChoiceItemNameDialog;
import com.clc.learnplatform.entity.KHZL_Entity;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.ToastUtil;

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
 * 添加学习卡
 */
public class AddCardActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "AddCardActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String openid;
    private AlertDialog alertDialog;//等待对话框
    private UserInfoEntity mUser = null;

    private ImageView ivBack;
    private RelativeLayout rlItemClass;
    private RelativeLayout rlItem;
    private TextView tvItemClass;
    private TextView tvItem;
    private EditText etCardNum;
    private EditText etCardPassword;
    private Button btnOk;

    private ImageView ivClearCard;
    private ImageView ivClearPassword;

    private String mDataJsonString;
    private ArrayList<KHZL_Entity> mKHZL_List;//证书种类list
    private ArrayList<KSXM_Entity> mKsxmList;//项目list

    private String xmid = "";

    private String isMypage;//用来标示是否是我的页面传过来的数据 00标示是 01标示从项目学习页面过来的数据
    private String mKSXM_Name;//预制数据 项目名称 （只在是项目学习页面转过来时使用）
    private String mKhzl_Name;//预制数据 证书种类名称 （只在是项目学习页面转过来时使用）

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0x01://获取完了数据
                    alertDialog.dismiss();
                    ToastUtil.getInstance().shortShow("绑定成功");
                    break;
                case 0x02://绑定失败
                    alertDialog.dismiss();
                    Bundle data = message.getData();
                    String msg = data.getString("msg");
                    ToastUtil.getInstance().shortShow(msg);
                    break;
                case 0x03:
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");
        mDataJsonString = intent.getStringExtra("data_string");
        isMypage = intent.getStringExtra("ismypage");
        if(isMypage!=null){
            if(isMypage.equals("01")){//如果是从项目学习页面转过来的
                mKSXM_Name = intent.getStringExtra("ksxm_name");
                mKhzl_Name = intent.getStringExtra("khzl_name");
            }else if(isMypage.equals("00")){//如果是从我的页面转过来
                mKSXM_Name = "";
                mKhzl_Name = "";
            }
        }

        initView();
        initData();
    }

    /**
     *根据证书种类id获取项目list
     * @param zlid 证书种类id
     */
    private void getXmList(String zlid){
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("zlid=")
                .append(zlid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.YH_GETXM_URL)//绑定学习卡页切换证书种类，查询此种类下的项目
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 获取数据失败");
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

                    } else if (error.equals("false")) {//成功
//                        mKHZL_List.clear();
//                        JSONArray zllist = jsonObject.getJSONArray("zllist");
//                        for(int i=0;i<zllist.length();i++){
//                            JSONObject zl = zllist.getJSONObject(i);
//                            KHZL_Entity ke = new KHZL_Entity();
//                            ke.ID = zl.getString("ID");
//                            ke.NAME = zl.getString("NAME");
//                            ke.JGFS = zl.getInt("JGFS");
//                            ke.MF = zl.getInt("MF");
//                            mKHZL_List.add(ke);
//                        }
                        mKsxmList.clear();
                        JSONArray xmlist = jsonObject.getJSONArray("xmlist");
                        for (int i=0;i<xmlist.length();i++){
                            JSONObject xm = xmlist.getJSONObject(i);
                            KSXM_Entity ke = new KSXM_Entity();
                            ke.ID = xm.getString("ID");
                            ke.NAME = xm.getString("NAME");
                            ke.DM = xm.getString("DM");
                            ke.BZ = xm.getString("BZ");
                            ke.ZT = xm.getString("ZT");
                            ke.ZLID = xm.getString("ZLID");
                            ke.ZTL = xm.getInt("ZTL");
                            ke.MNXH = xm.getInt("MNXH");
                            ke.CTL = xm.getInt("CTL");
                            ke.STXH = xm.getInt("STXH");
                            mKsxmList.add(ke);
                        }

                        Message msg = new Message();
                        msg.what = 0x03;
                        mHandler.sendMessage(msg);//通知UI线程更新界面
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void initData() {
        if(isMypage!=null && isMypage.equals("01")){//如果是项目学习页面跳转过来的 就将项目名称与证书种类预制上
            tvItemClass.setText(mKhzl_Name);
            tvItem.setText(mKSXM_Name);
        }
        try {
            JSONObject jsonObject = new JSONObject(mDataJsonString);
            //解析证书种类list证书种类list
            JSONArray zllist = jsonObject.getJSONArray("zllist");
            mKHZL_List = new ArrayList<>();
            for (int i = 0; i < zllist.length(); i++) {
                KHZL_Entity ke = new KHZL_Entity();
                JSONObject jsonObject1 = zllist.getJSONObject(i);
                ke.ID = jsonObject1.getString("ID");
                ke.JGFS = jsonObject1.getInt("JGFS");
                ke.KSFZ = jsonObject1.getInt("KSFZ");
                ke.MF = jsonObject1.getInt("MF");
                ke.NAME = jsonObject1.getString("NAME");
                ke.ZT = jsonObject1.getString("ZT");
                mKHZL_List.add(ke);
            }
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

    private void initView() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("请稍候...")
                .create();
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        rlItemClass = findViewById(R.id.rl_1);
        rlItemClass.setOnClickListener(this);
        rlItem = findViewById(R.id.rl_2);
        rlItem.setOnClickListener(this);
        tvItemClass = findViewById(R.id.tv_item_class);
        tvItem = findViewById(R.id.tv_item_name);
        btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(this);
        etCardNum = findViewById(R.id.et_card_num);
        etCardPassword = findViewById(R.id.et_card_password);
        ivClearCard = findViewById(R.id.iv_delete_text1);
        ivClearCard.setOnClickListener(this);
        ivClearPassword = findViewById(R.id.iv_delete_password);
        ivClearPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back://返回
                finish();
                break;
            case R.id.rl_1://选择项目种类
                String item_class = tvItemClass.getText().toString();
                ChoiceItemDialog cid = new ChoiceItemDialog(
                        this, mKHZL_List, item_class,new ChoiceItemDialog.SeleListener() {
                    @Override
                    public void sele(KHZL_Entity ke) {
                        tvItemClass.setText(ke.NAME);
                        getXmList(ke.ID);
                    }
                });
                cid.show();
                break;
            case R.id.rl_2://选择项目
                String name = tvItem.getText().toString();
                ChoiceItemNameDialog cind = new ChoiceItemNameDialog(
                        this, mKsxmList,name, new ChoiceItemNameDialog.SeleListener() {
                    @Override
                    public void sele(KSXM_Entity ke) {
                        tvItem.setText(ke.NAME);
                        xmid = ke.ID;
                    }
                });
                cind.show();
                break;
            case R.id.btn_ok://确定
                doOk();
                break;
            case R.id.iv_delete_text1:
                etCardNum.setText("");
                break;
            case R.id.iv_delete_password:
                etCardPassword.setText("");
                break;
        }


    }

    private void doOk() {
        String cardNum = etCardNum.getText().toString();
        if (cardNum.equals("")) {
            ToastUtil.getInstance().shortShow("卡号不能为空");
            return;
        }
        String cardPassword = etCardPassword.getText().toString();
        if (cardPassword.equals("")) {
            ToastUtil.getInstance().shortShow("卡密不能为空");
            return;
        }
        alertDialog.show();
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&KH=")
                .append(cardNum)
                .append("&MM=")
                .append(cardPassword)
                .append("&xmid=")
                .append(xmid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.BDXXK_URL)//绑定学习卡
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 绑定失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "autoSignIn.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//绑定失败
                        String message = jsonObject.getString("message");
                        Message msg = new Message();
                        msg.what = 0x02;
                        Bundle bundle = new Bundle();
                        bundle.putString("msg", message);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);//通知UI线程更新界面
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//绑定成功
                        analysisData(responseInfo);//解析数据
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
            JSONObject syzh = jsonObject.getJSONObject("syzh");
            mUser = new UserInfoEntity();
            mUser.ID = syzh.getString("ID");
            mUser.NC = syzh.getString("NC");
            mUser.SJH = syzh.getString("SJH");
            mUser.LX = syzh.getString("LX");
            mUser.ZHYE = syzh.getInt("ZHYE");
            mUser.SSS = syzh.getString("SSS");
            mUser.SHI = syzh.getString("SHI");
            mUser.ZHDLSJ = syzh.getString("ZHDLSJ");
            mUser.KH = syzh.getString("KH");
            mUser.ZCSJ = syzh.getString("ZCSJ");
            mUser.HEADIMGURL = syzh.getString("HEADIMGURL");
            mUser.GXSJ = syzh.getString("GXSJ");
            mUser.ZJXXXM = syzh.getString("ZJXXXM");
            mUser.WXCODE = syzh.getString("WXCODE");
            mUser.SQVIP = syzh.getString("SQVIP");

            Message msg = new Message();
            msg.what = 0x01;
            mHandler.sendMessage(msg);//通知UI线程更新界面
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
