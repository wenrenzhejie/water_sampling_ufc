package com.example.acer.watersampling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.acer.watersampling.bean.Msg;
import com.example.acer.watersampling.bean.User;
import com.google.gson.Gson;

import java.util.logging.XMLFormatter;


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

                break;
//                领取任务
            case R.id.getTask_button:
                Log.i("tag","aaaaaaaaaaaa");
                startActivity(new Intent(UserDetailActivity.this,GetTaskActivity.class));
                break;


        }
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
