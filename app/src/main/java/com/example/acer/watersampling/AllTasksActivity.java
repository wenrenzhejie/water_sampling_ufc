package com.example.acer.watersampling;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AllTasksActivity extends AppCompatActivity {
    private ListView listView;
    private OkHttpClient okHttpClient;
    private List<String> taskNameList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_task);
        listView = findViewById(R.id.tasks_listView);
        getAllTasks(getIntent().getStringExtra("userId"));
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            listView.setAdapter(new TaskAdapter(AllTasksActivity.this,taskNameList));

        }
    };

    private void getAllTasks(String userId) {
        okHttpClient = new OkHttpClient();

        final Request request = new Request.Builder()
                .url("http://192.168.123.4:8080/water_sampling/task/getAllTasksByUserId?userId=" + userId)
                .get()
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.code() == 200){
                        String allTasks = response.body().string();
                        Log.i("taskName",allTasks+"mmmmmmmmmmmmmm");
                        JsonArray jsonElements = new Gson().fromJson(allTasks, JsonArray.class);
                        for (int i=0;i<jsonElements.size();i++){
                            taskNameList.add(new Gson().fromJson(jsonElements.get(i),String.class));
                        }
                        handler.sendEmptyMessage(0x001);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                    Toast.makeText(AllTasksActivity.this,"网络请求失败，请检查您的网络",Toast.LENGTH_SHORT).show();
                }
            }
        }).start();


    }
}
