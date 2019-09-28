package com.example.acer.watersampling;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CurrentPlaceActivity extends AppCompatActivity {
    private ListView listView;
    private OkHttpClient okHttpClient;
    private List<String> taskNameList = new ArrayList<>();
    private  List<Map<String,String>> mapList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_place);
        listView = findViewById(R.id.current_tasks_listView);
        final Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        double latitude = intent.getDoubleExtra("latitude",0);
        double longitude = intent.getDoubleExtra("longitude",0);
        getAllTasks(userId,latitude,longitude);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> map = mapList.get(position);
                Intent intent1 = new Intent(CurrentPlaceActivity.this,TaskDetailActivity.class);
                intent1.putExtra("id",map.get("id"));
                intent1.putExtra("placeName",map.get("placeName"));
                intent1.putExtra("bottleType",map.get("bottleType"));
                intent1.putExtra("reagent",map.get("reagent"));
                startActivity(intent1);

            }
        });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            listView.setAdapter(new TaskAdapter(CurrentPlaceActivity.this,taskNameList));
        }
    };

    private void getAllTasks(String userId,double latitude,double longitude) {

        okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://10.0.1.38:8080/water_sampling/task/getCurrentTasksByUserIdAndLocation?userId="+userId+"&longitude="+latitude+"&latitude="+longitude)
                .get()
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.code() == 200){
                        String allTasks = response.body().string();
                        Log.i("current",allTasks);
                        mapList = JSONArray.parseObject(allTasks,List.class);

                        for (Map<String,String> map:mapList){
                            taskNameList.add(map.get("placeName"));
                        }
                        handler.sendEmptyMessage(0x001);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                    Toast.makeText(CurrentPlaceActivity.this,"网络请求失败，请检查您的网络",Toast.LENGTH_SHORT).show();
                }
            }
        }).start();


    }
}
