package com.clc.learnplatform.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

public class TTSUtils implements InitListener, SynthesizerListener {
    private static final String TAG = "TTSUtils";

    private Context mContext;
    private static volatile TTSUtils instance = null;
    private boolean isInitSuccess = false;
    private SpeechSynthesizer mTts;

    private TTSUtileLisenning ttsUtileLisenning;

    private TTSUtils() {
    }

    //单例模式
    public static TTSUtils getInstance() {
        if (instance == null) {
            synchronized (TTSUtils.class) {
                if (instance == null) {
                    instance = new TTSUtils();
                }
            }
        }
        return instance;
    }

    // 初始化合成对象
    public void init(Context context) {
        //判断进程是否已启动，初始化多次会报错
        //个人遇到问题：极光推送引入后，不加该条件回报错
//        if (CourseUtils.resultProcess("com.zhanghai.ttsapp")) {
//
//        }
        mContext = context;
        mTts = SpeechSynthesizer.createSynthesizer(mContext, this);
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 设置在线云端
        mTts.setParameter(SpeechConstant.ENGINE_TYPE,
                SpeechConstant.TYPE_CLOUD);

        // 设置发音人--发音人选择--具体见values-string
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoqi");

        // 设置发音语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        // 设置音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        // 设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "100");
        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        // 设置音频保存路径，需要申请WRITE_EXTERNAL_STORAGE权限，如不需保存注释该行代码
//        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,"./sdcard/iflytek.pcm");
        Log.i("zhh", "--初始化成完成-");
        isInitSuccess = true;
    }

    //
    public void speak(String msg, TTSUtileLisenning tt) {
        ttsUtileLisenning = tt;
        if (isInitSuccess) {
            mTts.startSpeaking(msg, this);
            mTts.pauseSpeaking();
        }
    }

    //开始
    public void resume() {
        if (isInitSuccess) {
            mTts.resumeSpeaking();
        }
    }

    //暂停
    public void pause() {
        if (isInitSuccess) {
            mTts.pauseSpeaking();
        }
    }

    //停止
    public void stop() {
        if (isInitSuccess) {
            mTts.stopSpeaking();
        }
    }

    @Override
    public void onInit(int i) {

    }

    @Override
    public void onSpeakBegin() {
        Log.i(TAG, "onSpeakBegin: 开始了");
    }

    @Override
    public void onBufferProgress(int i, int i1, int i2, String s) {

    }

    @Override
    public void onSpeakPaused() {

    }

    @Override
    public void onSpeakResumed() {

    }

    @Override
    public void onSpeakProgress(int i, int i1, int i2) {
        Log.i(TAG, "onSpeakProgress: i=" + i);
        Log.i(TAG, "onSpeakProgress: i1=" + i1);
        Log.i(TAG, "onSpeakProgress: i2=" + i2);

        ttsUtileLisenning.SpeakProgress(i, i1, i2);
    }

    @Override
    public void onCompleted(SpeechError speechError) {
        ttsUtileLisenning.Completed();
    }

    @Override
    public void onEvent(int i, int i1, int i2, Bundle bundle) {

    }

    public interface TTSUtileLisenning {
        void SpeakProgress(int i, int i1, int i2);
        void Completed();
    }

}
