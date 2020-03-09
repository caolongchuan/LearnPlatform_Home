package com.clc.learnplatform.pager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.clc.learnplatform.R;
import com.clc.learnplatform.activity.ChongZhiActivity;
import com.clc.learnplatform.activity.LsxxActivity;
import com.clc.learnplatform.adapter.HomeItemAdapter;
import com.clc.learnplatform.dialog.ChoiceItemDialog;
import com.clc.learnplatform.entity.KHZL_Entity;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.fragment.HomeFragment;
import com.clc.learnplatform.util.SPUtils;
import com.clc.learnplatform.util.ToastUtil;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeMainPager implements View.OnClickListener {
    private Fragment mFragment;
    private View mView;
    private String openid;

    private ArrayList<String> list_path;//图片路径

    private ImageView mHeadIcon;//头像
    private TextView mNickName;//昵称
    private TextView mPhoneNumber;//手机号码
    private TextView mLearnCoin;//学习币
    private TextView mRecharge;//充值
    private Banner mBanner;//轮播图
    private TextView mSeeAll;//查看全部历史学习
    private View mLine1;//分割线
    private TextView mLatelyStudyName;//最近学习项目名称
    private TextView mLatelyStudySign;//最近学习项目的代号
    private TextView mStudied;//继续学习按钮
    private TextView mCertificateClass;//操作证的分类
    private TextView mChange;//切换

    private ListView mLVItem;
    private HomeItemAdapter mHomeItemAdapter;
    private ArrayList<String> mHomeItemName;//项目的一级分类

    private UserInfoEntity mUserInfoEntiry;//用户的信息
    private KHZL_Entity mKhzlEntity;//默认证书种类
    private ArrayList<KHZL_Entity> mKHZL_List;//证书种类list
    private KSXM_Entity mZJXXXM;//最近学习项目
    private ArrayList<KSXM_Entity> mKsxmList;//项目list

    private String mDataString;//首页传过来的数据

    public HomeMainPager(Fragment fragment, String openid, String data_jsonString) {
        mFragment = fragment;
        this.openid = openid;
        // TODO 动态添加布局(xml方式)
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);       //LayoutInflater inflater1=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //      LayoutInflater inflater2 = getLayoutInflater();
        LayoutInflater inflater = LayoutInflater.from(mFragment.getContext());
        mView = inflater.inflate(R.layout.fragment_home_main, null);
        mView.setLayoutParams(lp);

        mDataString = data_jsonString;
        analysisData(data_jsonString);//解析首页数据
        initView();
        initData();
        initBanner();
    }

    //解析首页数据
    private void analysisData(String responseInfo) {
        //放图片地址的集合
        list_path = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            //解析用户信息
            String syzh = jsonObject.getString("syzh");
            JSONObject syzh_obj = new JSONObject(syzh);
            mUserInfoEntiry = new UserInfoEntity();
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
            //解析当前默认的证书种类
            mKhzlEntity = new KHZL_Entity();
            String khzl = jsonObject.getString("khzl");
            JSONObject khzl_obj = new JSONObject(khzl);
            mKhzlEntity.ID = khzl_obj.getString("ID");
            mKhzlEntity.JGFS = khzl_obj.getInt("JGFS");
            mKhzlEntity.KSFZ = khzl_obj.getInt("KSFZ");
            mKhzlEntity.MF = khzl_obj.getInt("MF");
            mKhzlEntity.NAME = khzl_obj.getString("NAME");
            mKhzlEntity.ZT = khzl_obj.getString("ZT");
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
            //解析最近学习项目
            mZJXXXM = new KSXM_Entity();
            String zjxxxm = jsonObject.getString("zjxxxm");
            JSONObject zjxxxm_obj = new JSONObject(zjxxxm);
            mZJXXXM.ID = zjxxxm_obj.getString("ID");
            mZJXXXM.BZ = zjxxxm_obj.getString("BZ");
            mZJXXXM.CTL = zjxxxm_obj.getInt("CTL");
            mZJXXXM.DM = zjxxxm_obj.getString("DM");
            mZJXXXM.MNXH = zjxxxm_obj.getInt("MNXH");
            mZJXXXM.NAME = zjxxxm_obj.getString("NAME");
            mZJXXXM.STXH = zjxxxm_obj.getInt("STXH");
            mZJXXXM.ZLID = zjxxxm_obj.getString("ZLID");
            mZJXXXM.ZT = zjxxxm_obj.getString("ZT");
            mZJXXXM.ZTL = zjxxxm_obj.getInt("ZTL");
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
            //轮播图图片
            JSONArray images = jsonObject.getJSONArray("images");
            for(int i=0;i<images.length();i++){
                String string = images.getString(i);
                list_path.add(string);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 初始化轮播图
     */
    private void initBanner() {
        //设置内置样式，共有六种可以点入方法内逐一体验使用。
        //        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        //设置图片加载器，图片加载器在下方
        mBanner.setImageLoader(new MyLoader());
        //设置图片网址或地址的集合
        mBanner.setImages(list_path);
        //设置轮播的动画效果，内含多种特效，可点入方法内查找后内逐一体验
        mBanner.setBannerAnimation(Transformer.Default);
//        //设置轮播图的标题集合
//        mBanner.setBannerTitles(list_title);
        //设置轮播间隔时间
        mBanner.setDelayTime(3000);
        //设置是否为自动轮播，默认是“是”。
        mBanner.isAutoPlay(true);
        //设置指示器的位置，小点点，左中右。
        mBanner.setIndicatorGravity(BannerConfig.CENTER)
                //以上内容都可写成链式布局，这是轮播图的监听。比较重要。方法在下面。
                .setOnBannerListener(new OnBannerListener() {
                    @Override
                    public void OnBannerClick(int position) {
                        Toast.makeText(mFragment.getContext(), "点击了第" + position + "个", Toast.LENGTH_SHORT).show();
                    }
                })
                //必须最后调用的方法，启动轮播图。
                .start();
    }

    private void initData() {
        Glide.with(mFragment.getContext()).load((String) mUserInfoEntiry.HEADIMGURL).into(mHeadIcon);//显示头像
        mNickName.setText(mUserInfoEntiry.NC);//显示昵称
        mPhoneNumber.setText(mUserInfoEntiry.SJH);//显示手机号码
        mLearnCoin.setText(String.valueOf(mUserInfoEntiry.ZHYE));//显示学习币数量
        //根据解析出来的数据判断是否有最近学习
        if (mUserInfoEntiry.ZJXXXM != null && mUserInfoEntiry.ZJXXXM.equals("null")) {//没有最近学习项目 先将其隐藏
            mLatelyStudyName.setVisibility(View.GONE);
            mLatelyStudySign.setVisibility(View.GONE);
            mStudied.setVisibility(View.GONE);
            mLine1.setVisibility(View.GONE);
        } else {
            mLatelyStudyName.setVisibility(View.VISIBLE);//最近学习的项目名称
            mLatelyStudyName.setText(mZJXXXM.NAME);
            mLatelyStudySign.setVisibility(View.VISIBLE);//最近学习的项目代码
            mLatelyStudySign.setText(mZJXXXM.DM);
            mStudied.setVisibility(View.VISIBLE);//
            mLine1.setVisibility(View.VISIBLE);
        }
        mCertificateClass.setText(mKhzlEntity.NAME);//证书种类
        mHomeItemName = new ArrayList<>();
        //全部项目
        for (int i = 0; i < mKsxmList.size(); i++) {
            String bz = mKsxmList.get(i).BZ;
            int j = 0;
            for (; j < mHomeItemName.size(); j++) {
                if (bz != null && bz.equals(mHomeItemName.get(j))) {
                    break;
                }
            }
            if (j>=mHomeItemName.size()){
                mHomeItemName.add(mKsxmList.get(i).BZ);
            }
        }

        mHomeItemAdapter = new HomeItemAdapter(mFragment.getActivity(), mHomeItemName,mKsxmList,openid,mDataString);
        mLVItem.setAdapter(mHomeItemAdapter);
    }

    private void initView() {
        mHeadIcon = mView.findViewById(R.id.iv_user_head);//头像
        mNickName = mView.findViewById(R.id.tv_user_name);//昵称
        mPhoneNumber = mView.findViewById(R.id.tv_phone_number);//手机号码
        mLearnCoin = mView.findViewById(R.id.tv_learn_b);//学习币
        mRecharge = mView.findViewById(R.id.btn_recharge);//充值
        mRecharge.setOnClickListener(this);
        mBanner = mView.findViewById(R.id.banner);//轮播图
        mSeeAll = mView.findViewById(R.id.tv_see_all);//查看全部
        mSeeAll.setOnClickListener(this);
        mLine1 = mView.findViewById(R.id.line1);//分割线
        mLatelyStudyName = mView.findViewById(R.id.tv_recently_item_name);//最近学习的项目名称
        mLatelyStudySign = mView.findViewById(R.id.tv_recently_item_type);//最近学习的项目的代号
        mStudied = mView.findViewById(R.id.tv_goto_studied);//继续学习
        mStudied.setOnClickListener(this);
        mCertificateClass = mView.findViewById(R.id.tv_item);//操作证的分类
        mChange = mView.findViewById(R.id.tv_item_change);//切换
        mChange.setOnClickListener(this);
        mLVItem = mView.findViewById(R.id.lv_item);//学习项目分类
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_recharge://充值
                Intent intent1 = new Intent();
                intent1.putExtra("openid",openid);
                intent1.putExtra("YHID",mUserInfoEntiry.ID);
                intent1.setClass(mFragment.getContext(), ChongZhiActivity.class);
                mFragment.startActivity(intent1);
                break;
            case R.id.tv_see_all://查看全部
                Intent intent = new Intent();
                intent.putExtra("openid",openid);
                intent.putExtra("data_string",mDataString);
                intent.setClass(mFragment.getActivity(), LsxxActivity.class);
                mFragment.startActivityForResult(intent,200);
                break;
            case R.id.tv_goto_studied://继续学习
                ((HomeFragment) mFragment).gotoStudiedPager(mZJXXXM.ID);
                break;
            case R.id.tv_item_change://切换
                String zszl_name = mCertificateClass.getText().toString();
                ChoiceItemDialog cid = new ChoiceItemDialog(
                        mFragment.getContext(), mKHZL_List, zszl_name, new ChoiceItemDialog.SeleListener() {
                    @Override
                    public void sele(KHZL_Entity ke) {
                        ToastUtil.getInstance().shortShow("切换完成回调");

                    }
                });
                cid.show();
                break;
        }
    }

    //设置最近学习项目
    public void setZuijingStudy(KSXM_Entity ke){
        mZJXXXM = ke;
        mLatelyStudyName.setText(mZJXXXM.NAME);
        mLatelyStudySign.setText(mZJXXXM.DM);
    }

    //设置金币值
    public void setCoin(Context context) {
        int coin_num = (int) SPUtils.get(context, "COIN_NUM", 0);
        mLearnCoin.setText(String.valueOf(coin_num));
    }

    //自定义的图片加载器 轮播图使用
    private class MyLoader extends ImageLoader {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context).load((String) path).into(imageView);
        }
    }

    public View getmView() {
        return mView;
    }

}
