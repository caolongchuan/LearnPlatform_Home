package com.clc.learnplatform.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.clc.learnplatform.BuildConfig;
import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.fragment.HomeFragment;
import com.clc.learnplatform.fragment.JobFragment;
import com.clc.learnplatform.fragment.MapFragment;
import com.clc.learnplatform.fragment.MyFragment;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.SPUtils;
import com.clc.learnplatform.util.ToastUtil;
import com.clc.learnplatform.util.VersionUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 主Activity
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private ViewPager mViewPager;
    private RadioGroup mTabRadioGroup;

    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;

    private String openid;
    private String mDataJsonString;

    private UserInfoEntity mUserInfoEntiry;//用户的信息

    //下载更新的对话框
    private AlertDialog mDownloadDialog;
    //下载的进度条
    private ProgressBar mProgressBar;
    private boolean mIsCancel;
    private int mProgress;

    //安装包的路径
    private String file_path = "/storage/emulated/0/clc/xxpt.apk";

    /**
     * 接收消息
     */
    @SuppressLint("HandlerLeak")
    private Handler mUpdateProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // 设置进度条
                    mProgressBar.setProgress(mProgress);
                    break;
                case 2:
                    // 隐藏当前下载对话框
                    mDownloadDialog.dismiss();
                    // 安装 APK 文件
                    installAPK(file_path);
                case 3:
                    String version = msg.getData().getString("version");
                    checkVersion(version);//检查更新
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
        super.onCreate(null);
        setContentView(R.layout.activity_main);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        initUserInfo(getIntent());//解析用户信息
        initView();

        getVersionFromService();//从服务器获取版本 用于判断是否需要检查更新
    }

    /**
     * 从服务器获取版本 用于判断是否需要检查更新
     */
    private void getVersionFromService() {
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
//        sb.append("openid=")
//                .append(openid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.VERSION_URL)//获取版本号
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 获取失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "onResponse: responseInfo===" + responseInfo);
                try {
                    JSONObject jsonObject =  new JSONObject(responseInfo);
                    String version = jsonObject.getString("ver");
                    Bundle bundle = new Bundle();
                    bundle.putString("version",version);
                    Message msg = new Message();
                    msg.what = 3;
                    msg.setData(bundle);
                    mUpdateProgressHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 检查更新
     */
    private void checkVersion(String version) {
        String appVersionName = VersionUtil.getAppVersionName(this);
        if(!appVersionName.equals(version)){//如果版本不一样
            showUpdateDialog();//显示更新对话框
        }
    }

    /**
     * 点击下载弹框
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("检测到新版本");
        builder.setMessage("是否下载新的版本？");
        builder.setPositiveButton("以后再说", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("立即更新", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                showDownloadDialog();
            }
        });
        builder.show();
    }

    /**
     * 显示正在下载对话框
     */
    protected void showDownloadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("下载中");
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_progress, null);
        mProgressBar = view.findViewById(R.id.id_progress);
        builder.setView(view);

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 隐藏当前对话框
                dialog.dismiss();
                // 设置下载状态为取消
                mIsCancel = true;
            }
        });

        mDownloadDialog = builder.create();
        mDownloadDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {//禁用返回键
                    return true;
                } else {
                    return false; // 默认返回 false
                }
            }
        });
        mDownloadDialog.show();

        // 下载文件
        downloadAPK(Constants.APK_DOWNLOAD_RUL);
    }

    /**
     *开启新线程下载apk文件
    */
    private void downloadAPK(final String update_url) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mIsCancel = false;
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String sdPath = Environment.getExternalStorageDirectory() + "/";
//                      文件保存路径
                        String mSavePath = sdPath + "clc";
                        File dir = new File(mSavePath);
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        // 下载文件
                        HttpURLConnection conn = (HttpURLConnection) new URL(update_url).openConnection();
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        int length = conn.getContentLength();

                        File apkFile = new File(dir.getPath(),"xxpt");
                        if(!apkFile.exists()){
                            apkFile.mkdir();
                        }
                        String absolutePath = apkFile.getAbsolutePath();
                        File needFile = new File(absolutePath+".apk");
                        file_path = needFile.getAbsolutePath();
                        FileOutputStream fos = new FileOutputStream(needFile);

                        int count = 0;
                        byte[] buffer = new byte[1024];

                        while (!mIsCancel) {
                            int numread = is.read(buffer);
                            count += numread;
                            // 计算进度条的当前位置
                            mProgress = (int) (((float) count / length) * 100);
                            // 更新进度条
                            mUpdateProgressHandler.sendEmptyMessage(1);

                            // 下载完成
                            if (numread < 0) {
                                Message msg = new Message();
                                msg.what = 2;
                                mUpdateProgressHandler.sendMessage(msg);
                                break;
                            }
                            fos.write(buffer, 0, numread);
                        }
                        fos.close();
                        is.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /*
     * 下载到本地后执行安装
     */
    protected void installAPK(String file_path) {
        File apkFile = new File(file_path);
        if (!apkFile.exists()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
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
        mFragments.add(new MapFragment(this,openid,mUserInfoEntiry.SSS,mUserInfoEntiry.SHI));
        mFragments.add(new MyFragment(openid,mDataJsonString));
        // init view pager
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(2);//设置预加载2个页面
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
