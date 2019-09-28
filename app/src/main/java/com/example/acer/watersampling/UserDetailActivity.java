package com.example.acer.watersampling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.acer.watersampling.bean.Msg;
import com.example.acer.watersampling.bean.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.XMLFormatter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class UserDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private LocationClient locationClient;
    private TextView currentPositionText,userName_textView;
    private Button myTask_currentPlace_button,myTools_button,myAllTask_button,getTask_button;
    private ListView listView;
//    员工id
    private String userId;
//    经纬度
    private double latitude,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        locationClient = new LocationClient(getApplication());
        locationClient.registerLocationListener(new MyLocationListener());
        currentPositionText = findViewById(R.id.currentPosition);
        userName_textView = findViewById(R.id.userName_textView);
        myTask_currentPlace_button = findViewById(R.id.myTask_currentPlace_button);
        myAllTask_button = findViewById(R.id.myAllTask_button);
        myTools_button = findViewById(R.id.myTools_button);
        getTask_button = findViewById(R.id.getTask_button);

        myTask_currentPlace_button.setOnClickListener(this);
        myAllTask_button.setOnClickListener(this);
        myTools_button.setOnClickListener(this);
        getTask_button.setOnClickListener(this);
//        获取定位信息
        requestLocation();
        showUsersInfo();

    }

    private void showUsersInfo() {
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        Msg msg = new Gson().fromJson(data, Msg.class);
        Object obg = msg.getData().get("user");
        String user_str = new Gson().toJson(obg);
        User user = new Gson().fromJson(user_str, User.class);
        userName_textView.setText(user.getUserName());
        userId = user.getUserId();
    }

    private void requestLocation() {
        initLocation();
        locationClient.start();
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            查看在该地的任务
            case R.id.myTask_currentPlace_button:
                Intent current_intent = new Intent(UserDetailActivity.this, CurrentPlaceActivity.class);
                current_intent.putExtra("userId",userId);
                current_intent.putExtra("latitude",latitude);
                current_intent.putExtra("longitude",longitude);
                startActivity(current_intent);
                break;

            case R.id.myAllTask_button:
                Intent intent = new Intent(UserDetailActivity.this, AllTasksActivity.class);
                intent.putExtra("userId",userId);
                startActivity(intent);
                break;
//            查看所需要的全部工具
            case R.id.myTools_button:
                showAllTools();
                break;
//                领取任务
            case R.id.getTask_button:
                Log.i("tag","aaaaaaaaaaaa");
                startActivity(new Intent(UserDetailActivity.this,GetTaskActivity.class));
                break;


        }
    }
    private void showAllTools(){
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://10.0.1.38:8080/water_sampling/task/getAllTools?userId=" + userId)
                .get()
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                Log.i("tag",s+"getAllTools");
                Msg msg = new Gson().fromJson(s, Msg.class);
                Object totalBottles = msg.getData().get("totalBottles");
                String bottleTypeMap_str = new Gson().toJson(msg.getData().get("bottleTypeMap"));
                Log.i("tag",bottleTypeMap_str+":"+"getAllTools");
                Map<String, Integer> bottleTypeMap_map = JSON.parseObject(bottleTypeMap_str, new TypeReference<Map<String, Integer>>() {
                });
                Log.i("tag",bottleTypeMap_str+":getAllTools"+bottleTypeMap_map.toString());
                String reagentTypeMap_str = new Gson().toJson(msg.getData().get("reagentTypeMap"));
                Map<String, Integer> reagentTypeMap_map = JSON.parseObject(reagentTypeMap_str, new TypeReference<Map<String, Integer>>() {
                });
                final View view = View.inflate(UserDetailActivity.this, R.layout.view_tools, null);
               TextView bottleTypeNum_textView_tools = view.findViewById(R.id.bottleTypeNum_textView_tools);
               TextView bottles_tools = view.findViewById(R.id.bottles_tools);
               TextView reagentTextView = view.findViewById(R.id.reagent_textView_tools);
                StringBuffer bottleType_stringBuffer = new StringBuffer();
                for (Map.Entry<String, Integer> entry:bottleTypeMap_map.entrySet()){
                    bottleType_stringBuffer.append(entry.getKey()+"：\t"+entry.getValue()+"\n");
                }
                bottles_tools.setText("总共需要的瓶子个数："+String.valueOf(totalBottles));
                StringBuffer reagentType_stringBuffer = new StringBuffer();
                for (Map.Entry<String, Integer> entry:reagentTypeMap_map.entrySet()){
                    reagentType_stringBuffer.append(entry.getKey()+"：\t"+entry.getValue()+"\n");
                }
                reagentTextView.setText(reagentType_stringBuffer.toString());
                bottleTypeNum_textView_tools.setText(bottleType_stringBuffer.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(UserDetailActivity.this)
                                .setTitle("您所需要的瓶子和试剂：")
                                .setView(view)
                                .setPositiveButton("确定",null)
                                .show();
                    }
                });


            }
        });

    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    currentPosition.append("纬度：").append(location.getLatitude()).
                            append("\n");
                    currentPosition.append("经线：").append(location.getLongitude()).
                            append("\n");
                    currentPosition.append("城市：").append(location.getCity()).
                            append("\n");
                    currentPosition.append("区：").append(location.getDistrict()).
                            append("\n");
                    currentPosition.append("街道：").append(location.getStreet()).
                            append("\n");
                    currentPosition.append("详细地址：").append(location.getAddrStr()).
                            append("\n");
                    currentPosition.append("定位方式：");
                    if (location.getLocType() == BDLocation.TypeGpsLocation) {
                        currentPosition.append("GPS");
                    } else if (location.getLocType() ==
                            BDLocation.TypeNetWorkLocation) {
                        currentPosition.append("网络");
                    }
                    currentPositionText.setText(currentPosition);
                }
            });
        }
    }

}
