package com.clc.learnplatform.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.fragment.HomeFragment;
import com.clc.learnplatform.fragment.JobFragment;
import com.clc.learnplatform.fragment.MapFragment;
import com.clc.learnplatform.fragment.MyFragment;
import com.clc.learnplatform.util.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 主Activity
 */
public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private RadioGroup mTabRadioGroup;

    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;

    private String openid;
    private String mDataJsonString;

    private UserInfoEntity mUserInfoEntiry;//用户的信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        initUserInfo(getIntent());//解析用户信息
        initView();
    }

    //解析用户信息
    private void initUserInfo(Intent intent) {
        openid = intent.getStringExtra("openid");
        mDataJsonString = intent.getStringExtra("data_json_string");

        try {
            JSONObject jsonObject = new JSONObject(mDataJsonString);
            //解析用户信息
            String syzh = jsonObject.getString("syzh");
            JSONObject syzh_obj = new JSONObject(syzh);
            mUserInfoEntiry = new UserInfoEntity();
            mUserInfoEntiry.ID = syzh_obj.getString("ID");
            mUserInfoEntiry.NC = syzh_obj.getString("NC");
            mUserInfoEntiry.SJH = syzh_obj.getString("SJH");
            mUserInfoEntiry.LX = syzh_obj.getString("LX");
            mUserInfoEntiry.ZHYE = syzh_obj.getInt("ZHYE");//账户余额 也就是学习币值
            //每次在刚打开app时将学习币保存到sp里边
            SPUtils.put(this, "COIN_NUM", mUserInfoEntiry.ZHYE);

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

    private void initView() {
        // find view
        mViewPager = findViewById(R.id.fragment_vp);
        mTabRadioGroup = findViewById(R.id.tab_bar);
        // init fragment
        mFragments = new ArrayList<>(4);
        mFragments.add(new HomeFragment(openid,mDataJsonString));
        mFragments.add(new JobFragment(this,openid,mUserInfoEntiry));
        mFragments.add(new MapFragment(this,openid));
        mFragments.add(new MyFragment(openid,mDataJsonString));
        // init view pager
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mAdapter);
        // register listener
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mTabRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case 1:
                KSXM_Entity ke = new KSXM_Entity();
                ke.ID = data.getStringExtra("zjxx_id");
                ke.NAME = data.getStringExtra("zjxx_name");
                ke.DM = data.getStringExtra("zjxx_dm");
                ke.BZ = data.getStringExtra("zjxx_bz");
                ke.ZT = data.getStringExtra("zjxx_zt");
                ke.ZLID = data.getStringExtra("zjxx_zlid");
                ke.ZTL = data.getIntExtra("zjxx_ztl", 0);
                ke.MNXH = data.getIntExtra("zjxx_mnxh", 0);
                ke.CTL = data.getIntExtra("zjxx_ctl", 0);
                ke.STXH = data.getIntExtra("zjxx_stxh", 0);
                ke.SXRQ = data.getIntExtra("zjxx_sxrq", 0);
                int coin = data.getIntExtra("coin", 0);

                ((HomeFragment)mFragments.get(0)).setZuijingStudy(ke);
                break;
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.removeOnPageChangeListener(mPageChangeListener);
    }

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i).getId() == checkedId) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    };

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            RadioButton radioButton = (RadioButton) mTabRadioGroup.getChildAt(position);
            radioButton.setChecked(true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> mList;

        public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.mList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return this.mList == null ? null : this.mList.get(position);
        }

        @Override
        public int getCount() {
            return this.mList == null ? 0 : this.mList.size();
        }

    }

    //替换fragment
    public void replace(int id, Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(id, fragment)
                .commitAllowingStateLoss();
    }

}
