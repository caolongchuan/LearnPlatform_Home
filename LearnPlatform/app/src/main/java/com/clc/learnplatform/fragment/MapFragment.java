package com.clc.learnplatform.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.route.BaiduMapRoutePlan;
import com.baidu.mapapi.utils.route.RouteParaOption;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.KSJG_Entity;
import com.clc.learnplatform.entity.SHENG_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.CityLocationUtil;
import com.clc.learnplatform.util.QyZwlbUtil;
import com.clc.learnplatform.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapFragment extends Fragment implements View.OnClickListener {
    public static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final String TAG = "--MapFragment--";

    private Activity mActivty;
    private View mView;

    private String openid;
    private String SSS;//省
    private String SHI;//市

    private BaiduMap mBaiduMap;
    private MapView mMapView = null;
    private LocationClient mLocationClient;

    private TextView tvCityName;//选择城市 显示城市名称
    private TextView tvZhanKai;//展开与收起按钮
    private LinearLayout llAddr;
    private TextView tvTiShi;//无数据提示
    private ListView lvAddr;
    private MyAdapter mAdapter;

    private boolean isFirstLocation;
    private double myLatitude;//我的位置
    private double myLongitude;//我的位置

    private ArrayList<KSJG_Entity> mKsjgList; //考试机构list

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x01:
                    String msg1 = msg.getData().getString("msg");
                    ToastUtil.getInstance().shortShow(msg1);
                    break;
                case 0x02:
                    mAdapter.notifyDataSetChanged();
                    mardAddr();
                    if(mKsjgList.size()>0){//有地图数据
                        tvTiShi.setVisibility(View.GONE);
                        lvAddr.setVisibility(View.VISIBLE);
                    }else{//没有地图数据
                        tvTiShi.setVisibility(View.VISIBLE);
                        lvAddr.setVisibility(View.GONE);
                    }
                    break;
            }
            return false;
        }
    });

    public MapFragment(Activity activity, String openid,String sss,String shi) {
        mActivty = activity;
        this.openid = openid;
        SSS = sss;
        SHI = shi;
        mKsjgList = new ArrayList<>();
    }

    //从服务器获取考试结构列表
    private void getKsjgListFromService(String shi) {
        mKsjgList.clear();
        mAdapter.notifyDataSetChanged();
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        if(shi == null){
            sb.append("openid=")
                    .append(openid);
        }else{
            sb.append("openid=")
                    .append(openid)
                    .append("&shi=")
                    .append(shi);
        }
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.KSJG_URL)
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
                Log.i(TAG, "autoSignIn.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//失败
                        String message = jsonObject.getString("message");
                        Message msg = new Message();
                        msg.what = 0x01;
                        Bundle bundle = new Bundle();
                        bundle.putString("msg", message);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//成功
                        analysisData(responseInfo);
                        Message msg = new Message();
                        msg.what = 0x02;
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
            JSONArray ksjglist = jsonObject.getJSONArray("ksjglist");
            for (int i = 0; i < ksjglist.length(); i++) {
                JSONObject ksjg = ksjglist.getJSONObject(i);
                KSJG_Entity ke = new KSJG_Entity();
                ke.ID = ksjg.getString("ID");
                ke.NAME = ksjg.getString("NAME");
                ke.SHI = ksjg.getString("SHI");
                ke.DZ = ksjg.getString("DZ");
                ke.DH = ksjg.getString("DH");
                ke.JD = ksjg.getString("JD");
                ke.WD = ksjg.getString("WD");
                ke.GXSJ = ksjg.getString("GXSJ");
                mKsjgList.add(ke);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在地图上添加Marker，并显示
     */
    public void mardAddr() {
        for (int i = 0; i < mKsjgList.size(); i++) {
            //定义Maker坐标点
            LatLng point = new LatLng(Double.valueOf(mKsjgList.get(i).WD), Double.valueOf(mKsjgList.get(i).JD));
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.mipmap.icon_marka);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);


        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        isFirstLocation = true;

        mView = inflater.inflate(R.layout.fragment_map, container, false);
        initView();
        getKsjgListFromService(null);
        initData();

        tvCityName.setText(SHI);
        double[] coordinate = CityLocationUtil.getCoordinate(getContext(), SSS, SHI);
        if(coordinate!=null){
            setlatilong2Center(mBaiduMap,coordinate[1],coordinate[0],true);
        }

        return mView;
    }

    private void initView() {
        //获取地图控件引用
        mMapView = (MapView) mView.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        tvCityName = mView.findViewById(R.id.tv_city);
        tvCityName.setOnClickListener(this);
        tvZhanKai = mView.findViewById(R.id.tv_zhankai);
        tvZhanKai.setOnClickListener(this);
        lvAddr = mView.findViewById(R.id.lv_addr);
        mAdapter = new MyAdapter();
        lvAddr.setAdapter(mAdapter);
        tvTiShi = mView.findViewById(R.id.tv_tishi);
        llAddr = mView.findViewById(R.id.ll_addr);
    }

    private void initData() {
        mBaiduMap.setMyLocationEnabled(true);
        //定位初始化
        mLocationClient = new LocationClient(mActivty);
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        //设置locationClientOption
        mLocationClient.setLocOption(option);
        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_city://选择城市
                ArrayList<SHENG_Entity> mShengEntity = QyZwlbUtil.getInstance().getShengList();
                final List<String> options1Items = new ArrayList<>();
                final List<List<String>> options2Items = new ArrayList<>();
                for (int i = 0; i < mShengEntity.size(); i++) {
                    options1Items.add(mShengEntity.get(i).value);
                    List<String> temp = new ArrayList<>();
                    for (int j = 0; j < mShengEntity.get(i).childs.size(); j++) {
                        temp.add(mShengEntity.get(i).childs.get(j).value);
                    }
                    options2Items.add(temp);
                }
                //条件选择器
                OptionsPickerView pvOptions = new OptionsPickerBuilder(mActivty, new OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3, View v) {
                        //返回的分别是三个级别的选中位置
                        tvCityName.setText(options2Items.get(options1).get(option2));
                        //设置选择城市后将该城市设置为中心点
                        //设置并显示中心点
                        double[] coordinate = CityLocationUtil.getCoordinate(getContext(),
                                options1Items.get(options1), options2Items.get(options1).get(option2));
                        if(coordinate!=null){
                            setlatilong2Center(mBaiduMap,coordinate[1],coordinate[0],true);
                        }
                        //根据城市名获取地址列表
                        getKsjgListFromService(options2Items.get(options1).get(option2));
                    }
                }).build();
                pvOptions.setTitleText("选择城市");
                pvOptions.setPicker(options1Items, options2Items);
                pvOptions.show();
                break;
            case R.id.tv_zhankai://展开与收起
                if (llAddr.getVisibility() == View.GONE) {
                    llAddr.setVisibility(View.VISIBLE);
                    tvZhanKai.setText("收起");
                } else {
                    llAddr.setVisibility(View.GONE);
                    tvZhanKai.setText("展开");
                }
                break;
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            //这个判断是为了防止每次定位都重新设置中心点和marker
            if (isFirstLocation) {
                isFirstLocation = false;
                myLatitude = location.getLatitude();
                myLongitude = location.getLongitude();
            }

        }

        BDLocation db = new BDLocation();
    }

    /**
     * 设置中心点和添加marker (根据百度地图获取到的位置）
     *
     * @param map
     * @param bdLocation
     * @param isShowLoc
     */
    public void setPosition2Center(BaiduMap map, BDLocation bdLocation, Boolean isShowLoc) {
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                .direction(bdLocation.getRadius()).latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();
        map.setMyLocationData(locData);

        if (isShowLoc) {
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f);
            map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }

    /**
     * 设置中心点和添加marker (根据经纬度标识的位置）
     *
     * @param map
     * @param isShowLoc
     */
    public void setlatilong2Center(BaiduMap map, double latitude, double longitude, Boolean isShowLoc) {
        if (isShowLoc) {
            LatLng ll = new LatLng(latitude, longitude);
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(15.0f);
            map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mKsjgList.size();
        }

        @Override
        public Object getItem(int position) {
            return mKsjgList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (null == convertView) {
                convertView = View.inflate(mActivty, R.layout.list_ksjg_item, null);
                holder = new ViewHolder();
                holder.rlMain = convertView.findViewById(R.id.rl_main);
                holder.tvAddrName = convertView.findViewById(R.id.tv_addr_name);
                holder.tvDaoHang = convertView.findViewById(R.id.tv_daohang);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvAddrName.setText(mKsjgList.get(position).NAME);

            holder.rlMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setlatilong2Center(mBaiduMap,
                            Double.valueOf(mKsjgList.get(position).WD), Double.valueOf(mKsjgList.get(position).JD), true);
                }
            });
            holder.tvDaoHang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog
                            .Builder(mActivty).setTitle("")
                            .setMessage("确定要跳转到百度地图进行导航吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //ToDo: 你想做的事情

                                    //定义起终点坐标
                                    LatLng startPoint = new LatLng(myLatitude, myLongitude);
                                    LatLng endPoint = new LatLng(Double.valueOf(mKsjgList.get(position).WD), Double.valueOf(mKsjgList.get(position).JD));

                                    //构建RouteParaOption参数以及策略
                                    //也可以通过startName和endName来构造
                                    RouteParaOption paraOption = new RouteParaOption()
                                            .startPoint(startPoint)
                                            .endPoint(endPoint)
                                            .busStrategyType(RouteParaOption.EBusStrategyType.bus_recommend_way);
                                    //调起百度地图
                                    try {
                                        BaiduMapRoutePlan.openBaiduMapTransitRoute(paraOption, mActivty);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    //调起结束时及时调用finish方法以释放相关资源
                                    BaiduMapRoutePlan.finish(mActivty);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //ToDo: 你想做的事情
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();

                }
            });

            return convertView;
        }

        class ViewHolder {
            RelativeLayout rlMain;
            TextView tvAddrName;
            TextView tvDaoHang;
        }

    }

}
