package com.clc.learnplatform.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.clc.learnplatform.R;
import com.clc.learnplatform.activity.ChongZhiActivity;
import com.clc.learnplatform.activity.TiYanCardActivity;
import com.clc.learnplatform.activity.VIPActivity;
import com.clc.learnplatform.activity.WeiZuoTiActivity;
import com.clc.learnplatform.activity.WenTiFanKuiActivity;
import com.clc.learnplatform.activity.XueXiCardActivity;
import com.clc.learnplatform.activity.YaoQingMaActivity;
import com.clc.learnplatform.activity.ZdmxActivity;
import com.clc.learnplatform.activity.ZhengQueryActivity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFragment extends Fragment implements View.OnClickListener {
    private View mView;

    private String openid;

    private ImageView ivHead;
    private TextView tvName;
    private TextView tvPhoneNum;
    private TextView tvID;

    private TextView tvCoinNum;//学习币数量
    private RelativeLayout rlCoinMingXi;//学习币使用明细
    private RelativeLayout rlRechange;//充值
    private RelativeLayout rlTiYanCard;//体验卡
    private TextView tvTiYanKa;
    private RelativeLayout rlStudyCard;//学习卡
    private RelativeLayout rlYaoQingMa;//我的邀请码
    private RelativeLayout rlVIP;//申请VIP
    private RelativeLayout rlZhengShuChaXun;//证书查询
    private RelativeLayout rlWenTiFanKui;//问题反馈

    private UserInfoEntity mUserInfoEntiry;
    private String DataString;

    public MyFragment(String openid, String data_string) {
        this.openid = openid;
        mUserInfoEntiry = new UserInfoEntity();
        analysisData(data_string);
        DataString = data_string;
    }

    //解析首页数据
    private void analysisData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            //解析用户信息
            String syzh = jsonObject.getString("syzh");
            JSONObject syzh_obj = new JSONObject(syzh);
            mUserInfoEntiry.ID = syzh_obj.getString("ID");
            mUserInfoEntiry.NC = syzh_obj.getString("NC");
            mUserInfoEntiry.SJH = syzh_obj.getString("SJH");
            mUserInfoEntiry.LX = syzh_obj.getString("LX");
            mUserInfoEntiry.ZHYE = syzh_obj.getInt("ZHYE");
            mUserInfoEntiry.SSS = syzh_obj.getString("SSS");
            mUserInfoEntiry.SHI = syzh_obj.getString("SHI");
            mUserInfoEntiry.ZHDLSJ = syzh_obj.getString("ZHDLSJ");
            mUserInfoEntiry.KH = syzh_obj.getString("KH");
            mUserInfoEntiry.ZCSJ = syzh_obj.getString("ZCSJ");
            mUserInfoEntiry.HEADIMGURL = syzh_obj.getString("HEADIMGURL");
            mUserInfoEntiry.GXSJ = syzh_obj.getString("GXSJ");
            mUserInfoEntiry.ZJXXXM = syzh_obj.getString("ZJXXXM");
            mUserInfoEntiry.WXCODE = syzh_obj.getString("WXCODE");
            mUserInfoEntiry.SQVIP = syzh_obj.getString("SQVIP");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_my, container, false);

        initView();
        initData();
        return mView;
    }

    private void initData() {
        Glide.with(getActivity()).load((String) mUserInfoEntiry.HEADIMGURL).into(ivHead);//显示头像
        tvName.setText(mUserInfoEntiry.NC);//显示昵称
        tvPhoneNum.setText(mUserInfoEntiry.SJH);//显示手机号码
        tvID.setText("ID:" + mUserInfoEntiry.ID);//显示用户id
        tvCoinNum.setText(String.valueOf(mUserInfoEntiry.ZHYE)+"个");

        if(mUserInfoEntiry.KH.equals("null")) {//如果没有绑定体验卡
            tvTiYanKa.setText("体验卡");
        }else{
            tvTiYanKa.setText("体验卡（已绑卡）");
        }
    }

    private void initView() {
        ivHead = mView.findViewById(R.id.iv_user_head);
        tvName = mView.findViewById(R.id.tv_user_name);
        tvPhoneNum = mView.findViewById(R.id.tv_phone_number);
        tvID = mView.findViewById(R.id.tv_id);
        tvCoinNum = mView.findViewById(R.id.tv_coin_num);
        rlCoinMingXi = mView.findViewById(R.id.rl_coin_mingxi);
        rlCoinMingXi.setOnClickListener(this);

        rlRechange = mView.findViewById(R.id.rl_recharge);
        rlRechange.setOnClickListener(this);
        rlTiYanCard = mView.findViewById(R.id.rl_card_tiyan);
        rlTiYanCard.setOnClickListener(this);
        tvTiYanKa = mView.findViewById(R.id.tv_tiyanka);
        rlStudyCard = mView.findViewById(R.id.rl_card_xuexi);
        rlStudyCard.setOnClickListener(this);
        rlYaoQingMa = mView.findViewById(R.id.rl_yaoqingma);
        rlYaoQingMa.setOnClickListener(this);
        rlVIP = mView.findViewById(R.id.rl_vip);
        rlVIP.setOnClickListener(this);
        rlZhengShuChaXun = mView.findViewById(R.id.rl_zhengshu_chaxun);
        rlZhengShuChaXun.setOnClickListener(this);
        rlWenTiFanKui = mView.findViewById(R.id.rl_wenti_fankui);
        rlWenTiFanKui.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(resultCode == 1){
                tvTiYanKa.setText("体验卡（已绑卡）");
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_coin_mingxi://学习币使用明细
                Intent intent = new Intent();
                intent.putExtra("openid",openid);
                intent.setClass(this.getActivity(), ZdmxActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_recharge://充值
                Intent intent1 = new Intent();
                intent1.putExtra("openid",openid);
                intent1.setClass(this.getActivity(), ChongZhiActivity.class);
                startActivity(intent1);
                break;
            case R.id.rl_card_tiyan://体验卡
                if(mUserInfoEntiry.KH.equals("null")){//如果没有绑定体验卡
                    Intent intent2 = new Intent();
                    intent2.putExtra("openid",openid);
                    intent2.setClass(this.getActivity(), TiYanCardActivity.class);
                    startActivityForResult(intent2,100);
                }else {
                    ToastUtil.getInstance().shortShow("一个账号只能绑定一次体验卡");
                }
                break;
            case R.id.rl_card_xuexi://学习卡
                Intent intent3 = new Intent();
                intent3.putExtra("openid",openid);
                intent3.putExtra("head",mUserInfoEntiry.HEADIMGURL);
                intent3.putExtra("name",mUserInfoEntiry.NC);
                intent3.putExtra("phone",mUserInfoEntiry.SJH);
                intent3.putExtra("coin",mUserInfoEntiry.ZHYE);
                intent3.putExtra("data_string",DataString);
                intent3.setClass(this.getActivity(), XueXiCardActivity.class);
                startActivity(intent3);
                break;
            case R.id.rl_yaoqingma://我的邀请码
                Intent intent4 = new Intent();
                intent4.putExtra("head",mUserInfoEntiry.HEADIMGURL);
                intent4.putExtra("id",mUserInfoEntiry.ID);
                intent4.setClass(this.getActivity(), YaoQingMaActivity.class);
                startActivity(intent4);
                break;
            case R.id.rl_vip://申请VIP
                Intent intent5 = new Intent();
                intent5.putExtra("openid",openid);
                intent5.setClass(this.getActivity(), VIPActivity.class);
                startActivity(intent5);
                break;
            case R.id.rl_zhengshu_chaxun://证书查询
                startActivity(new Intent(this.getActivity(), ZhengQueryActivity.class));
                break;
            case R.id.rl_wenti_fankui://问题反馈
                Intent intent6 = new Intent();
                intent6.putExtra("openid",openid);
                intent6.setClass(this.getActivity(), WenTiFanKuiActivity.class);
                startActivity(intent6);
                break;
        }
    }
}
