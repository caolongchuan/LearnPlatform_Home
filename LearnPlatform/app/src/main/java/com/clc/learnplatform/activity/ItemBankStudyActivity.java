package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.XMFL_Entity;
import com.clc.learnplatform.entity.ZSD_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.SPUtils;
import com.clc.learnplatform.util.TTSUtils;
import com.clc.learnplatform.util.TimeUtil;
import com.clc.learnplatform.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.clc.learnplatform.activity.LoginActivity.FORM_CONTENT_TYPE;

/**
 * 题库学习
 */
public class ItemBankStudyActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "ItemBankStudyActivity";
    private final String SP_KEY = "tkxx_";

    private String openid;
    private int flid;//分类id

    private ImageView ivBack;

    private TextView mItemName;//项目名称
    private TextView mItemSign;//项目代码
    private TextView mItemClass;//分类名称
    private TextView mCurrPager;//当前页号
    private TextView mTotalPager;//总页数
    private ListView mListView;
    private TextView mPreviousPage;//上一页
    private TextView mNextPager;//下一页

    private boolean mPlaying;//播放标志
    private boolean mRun;//运行标示
    private ImageView mBitPlayStop;//播放停止 (大图）
    private ImageView mPlayStop;//播放停止
    private ProgressBar mProgressBar;//进度条
    private TextView mCurrTime;//当前播放时间
    private TextView mTotalTime;//全部时间

    private int mCurrentPager;//当前页号
    private String mtotalPager;//总页数
    private int mcurrTime;//已经播放时间（秒）
    private int mtotalTime;//全部所用时间（秒）

    private XMFL_Entity mXmflEntiy;//项目分类
    private KSXM_Entity mKsxmEntity;//项目类
    private ArrayList<ZSD_Entity> mZsdList;//知识点list
    private ItemBankDetialAdapter mAdapter;

    private StringBuilder mAllZsdString;//本页所有知识点String

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0x01://初始完成
                    updateUI();
                    break;
                case 0x02://更新时间
                    mCurrTime.setText(TimeUtil.getTimeString(mcurrTime));
                    break;
                case 0x03://自动进入下一页获取数据成功
                    updateUI();
                    mPlaying = true;
                    mPlayStop.setImageResource(R.mipmap.icon_stop);
                    TTSUtils.getInstance().resume();
                    if (!mRun) {
                        mRun = true;
                    }
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_bank_study);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");
        flid = intent.getIntExtra("flid", 0);

        initView();
        getDataFromService(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRun) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    if (mcurrTime >= mtotalTime) {
//                        mRun = false;
//                        break;
//                    }
                    if (mPlaying) {
                        mcurrTime = mcurrTime + 1;
                        Message msg = new Message();
                        msg.what = 0x02;
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    //从服务器获取数据
    private void getDataFromService(final boolean play) {
        mZsdList.clear();
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&flid=")
                .append(flid)
                .append("&pageNo=")
                .append(mCurrentPager);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.ITEM_BANK_URL)
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 获取题库学习数据失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "autoSignIn.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//失败
                        String message = jsonObject.getString("message");
                    } else if (error.equals("false")) {//成功
                        JSONObject fl = jsonObject.getJSONObject("fl");//项目分类
                        mXmflEntiy.ID = fl.getInt("ID");
                        mXmflEntiy.XMID = fl.getString("XMID");
                        mXmflEntiy.FLNC = fl.getString("FLNC");
                        mXmflEntiy.CTS = fl.getInt("CTS");
                        mXmflEntiy.ZTL = fl.getInt("ZTL");
                        mXmflEntiy.ZFZ = fl.getInt("ZFZ");
                        mXmflEntiy.ZT = fl.getString("ZT");
                        mXmflEntiy.XSFZ = fl.getInt("XSFZ");
                        mXmflEntiy.ZZDTS = fl.getInt("ZZDTS");
                        mXmflEntiy.ZZDXH = fl.getInt("ZZDXH");
                        JSONObject ksxm = jsonObject.getJSONObject("ksxm");//项目
                        mKsxmEntity.ID = ksxm.getString("ID");
                        mKsxmEntity.NAME = ksxm.getString("NAME");
                        mKsxmEntity.DM = ksxm.getString("DM");
                        mKsxmEntity.BZ = ksxm.getString("BZ");
                        mKsxmEntity.ZT = ksxm.getString("ZT");
                        mKsxmEntity.ZLID = ksxm.getString("ZLID");
                        mKsxmEntity.ZTL = ksxm.getInt("ZTL");
                        mKsxmEntity.MNXH = ksxm.getInt("MNXH");
                        mKsxmEntity.CTL = ksxm.getInt("CTL");
                        mKsxmEntity.STXH = ksxm.getInt("STXH");
                        mKsxmEntity.SXRQ = ksxm.getInt("SXRQ");
                        JSONArray zsdlist = jsonObject.getJSONArray("zsdlist");
                        for (int i = 0; i < zsdlist.length(); i++) {
                            JSONObject zs_obj = zsdlist.getJSONObject(i);
                            ZSD_Entity ze = new ZSD_Entity();
                            ze.ID = zs_obj.getString("ID");
                            ze.FLID = zs_obj.getString("FLID");
                            ze.NR = zs_obj.getString("NR");
                            ze.PLSX = zs_obj.getInt("PLSX");
                            mZsdList.add(ze);
                        }

                        mtotalPager = jsonObject.getString("pageCount");
                        if (play) {
                            Message msg = new Message();
                            msg.what = 0x03;
                            mHandler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = 0x01;
                            mHandler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        mItemName = findViewById(R.id.tv_item_name);
        mItemSign = findViewById(R.id.tv_item_sign);
        mItemClass = findViewById(R.id.tv_item_class);
        mListView = findViewById(R.id.lv_item_bank_detial);
        mCurrPager = findViewById(R.id.tv_cuttent_page);
        mTotalPager = findViewById(R.id.tv_total_page);
        mPreviousPage = findViewById(R.id.tv_previous_page);
        mPreviousPage.setOnClickListener(this);
        mNextPager = findViewById(R.id.tv_next_page);
        mNextPager.setOnClickListener(this);
        mBitPlayStop = findViewById(R.id.iv_big_play_stop);
        mBitPlayStop.setOnClickListener(this);
        mPlayStop = findViewById(R.id.iv_play_stop);
        mPlayStop.setOnClickListener(this);
        mProgressBar = findViewById(R.id.sb_play_where);
        mCurrTime = findViewById(R.id.tv_cuttent_play_time);
        mTotalTime = findViewById(R.id.tv_cuttent_total_time);

        mcurrTime = 0;
        mCurrentPager = (int) SPUtils.get(getApplicationContext(),
                SP_KEY + flid,1);//初始化当前页数 用flid作为key
        mXmflEntiy = new XMFL_Entity();
        mKsxmEntity = new KSXM_Entity();
        mZsdList = new ArrayList<>();
        mAdapter = new ItemBankDetialAdapter();
        mListView.setAdapter(mAdapter);
        mRun = true;
    }

    private void updateUI() {
        mItemName.setText(mKsxmEntity.NAME);
        mItemSign.setText(mKsxmEntity.DM);
        mItemClass.setText(mXmflEntiy.FLNC);
        mCurrPager.setText(String.valueOf(mCurrentPager));
        mTotalPager.setText(mtotalPager);

        mAdapter.notifyDataSetChanged();
        mListView.setSelection(0);

        mAllZsdString = null;
        mAllZsdString = new StringBuilder();
        for (int i = 0; i < mZsdList.size(); i++) {
            mAllZsdString.append("。" + ((mCurrentPager - 1) * 5 + i + 1) + "。");
            mAllZsdString.append(mZsdList.get(i).NR);
        }

        mtotalTime = (int) (mAllZsdString.toString().length() * Constants.TIME_XISHU);//全部时间
        mTotalTime.setText(TimeUtil.getTimeString(mtotalTime));
        mcurrTime = 0;
        mCurrTime.setText(TimeUtil.getTimeString(mcurrTime));
        mProgressBar.setProgress(0);
        mPlayStop.setImageResource(R.mipmap.icon_play);

        TTSUtils.getInstance().stop();
        TTSUtils.getInstance().speak(mAllZsdString.toString(), new TTSUtils.TTSUtileLisenning() {
            @Override
            public void SpeakProgress(int i, int i1, int i2) {
                mProgressBar.setProgress(i);
            }

            @Override
            public void Completed() {
                mcurrTime = 0;
                mCurrTime.setText(TimeUtil.getTimeString(mcurrTime));
                mProgressBar.setProgress(0);
                TTSUtils.getInstance().stop();
                if (mCurrentPager == Integer.valueOf(mtotalPager)) {
                    ToastUtil.getInstance().shortShow("已经是最后一页");
                } else {
                    mCurrentPager++;
                    getDataFromService(true);
                }
            }
        });
        mPlaying = false;
        TTSUtils.getInstance().pause();//初始化后先暂停
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back://返回
                finish();
                break;
            case R.id.tv_previous_page://上一页
                TTSUtils.getInstance().stop();
                if (mCurrentPager == 1) {
                    ToastUtil.getInstance().shortShow("已经是第一页");
                } else {
                    mCurrentPager--;
                    getDataFromService(false);
                }
                break;
            case R.id.tv_next_page://下一页
                TTSUtils.getInstance().stop();
                if (mCurrentPager == Integer.valueOf(mtotalPager)) {
                    ToastUtil.getInstance().shortShow("已经是最后一页");
                } else {
                    mCurrentPager++;
                    getDataFromService(false);
                }
                break;
            case R.id.iv_big_play_stop://大播放
                if (mPlaying) {
                    mPlaying = false;
                    mPlayStop.setImageResource(R.mipmap.icon_play);
                    TTSUtils.getInstance().pause();
                } else {
                    mPlaying = true;
                    mPlayStop.setImageResource(R.mipmap.icon_stop);
                    TTSUtils.getInstance().resume();
                }
                if (!mRun) {
                    mRun = true;
                }
                break;
            case R.id.iv_play_stop://小播放
                if (mPlaying) {
                    mPlaying = false;
                    mPlayStop.setImageResource(R.mipmap.icon_play);
                    TTSUtils.getInstance().pause();
                } else {
                    mPlaying = true;
                    mPlayStop.setImageResource(R.mipmap.icon_stop);
                    TTSUtils.getInstance().resume();
                }
                if (!mRun) {
                    mRun = true;
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRun = false;//关闭线程
        //关闭时将当前页数保存起来 用于下次进入时直接定位页数
        SPUtils.put(getApplicationContext(),SP_KEY + flid,mCurrentPager);
        TTSUtils.getInstance().stop();
    }

    public class ItemBankDetialAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mZsdList.size();
        }

        @Override
        public Object getItem(int position) {
            return mZsdList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(ItemBankStudyActivity.this, R.layout.item_bank_list, null);
                holder = new ViewHolder();
                holder.tvId = convertView.findViewById(R.id.tv_id);
                holder.tvDetial = convertView.findViewById(R.id.tv_detial);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvId.setText(String.valueOf(mZsdList.get(position).PLSX));
            holder.tvDetial.setText(mZsdList.get(position).NR);
            return convertView;
        }

        class ViewHolder {
            TextView tvId;
            TextView tvDetial;
        }

    }
}
