package com.example.acer.watersampling;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetTaskActivity extends AppCompatActivity {
    private ListView listView;
    private Button select_task_button;
    private OkHttpClient okHttpClient;
    private List<String> taskNameList = new ArrayList<>();
    private List<String> taskBottleTypeList = new ArrayList<>();
    private List<String> taskReagentList = new ArrayList<>();
    private  List<Map<String,String>> mapList;
    private TaskSelectAdapter taskSelectAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_task);
        listView = findViewById(R.id.get_task_listView);
        taskSelectAdapter = new TaskSelectAdapter(this);
        select_task_button = findViewById(R.id.select_task_button);
        getAllUnSelectedTasks();

//        选定任务
        select_task_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> checkBoxList = taskSelectAdapter.getCheckBoxList();
                View view = View.inflate(GetTaskActivity.this, R.layout.view_taskname_showselected, null);
                LinearLayout linearLayout = view.findViewById(R.id.showSelected_linearLayout);
                for (String str:checkBoxList){
                    TextView textView = new TextView(GetTaskActivity.this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(10,10,10,10);
                    textView.setLayoutParams(layoutParams);
                    textView.setPadding(20,20,20,20);
                    textView.setTextSize(15);
                    textView.setText(str);
                    linearLayout.addView(textView);
                }
                new AlertDialog.Builder(GetTaskActivity.this)
                        .setTitle("您已选择的采样点：")
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                保存到数据库
                                SharedPreferences sharedPreferences = getSharedPreferences("user_login", MODE_PRIVATE);
                                String userId = sharedPreferences.getString("userId", "NotFond");
                                FormBody.Builder builder = new FormBody.Builder();
                                builder.add("userId",userId);
                                RequestBody requestBody = builder.build();
                                Request request = new Request.Builder()
                                        .url("http://10.0.1.38:8080/water_sampling/task/saveSelectedTasks")
                                        .post(requestBody)
                                        .build();
                                okHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Toast.makeText(GetTaskActivity.this,"网络连接错误，请稍等",Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        Toast.makeText(GetTaskActivity.this,"选择成功",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("重选",null)
                        .show();
            }
        });
//        查看该地点的任务详情(弹出对话框)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*CheckBox checkbox = view.findViewById(R.id.select_task_checBox);
              Toast.makeText(GetTaskActivity.this,checkbox.isSelected()+"aaaa",Toast.LENGTH_SHORT).show();*/

                View view1 = View.inflate(GetTaskActivity.this, R.layout.view_task_detail, null);
                TextView placeName_textView_view = view1.findViewById(R.id.placeName_textView_view);
                placeName_textView_view.setText(taskNameList.get(position));
                TextView bottleType_textView_view = view1.findViewById(R.id.bottleType_textView_view);
                bottleType_textView_view.setText(taskBottleTypeList.get(position));
                TextView reagent_textView_view = view1.findViewById(R.id.reagent_textView_view);
                reagent_textView_view.setText(taskReagentList.get(position));
                new AlertDialog.Builder(GetTaskActivity.this)
                      .setTitle("任务详情")
                      .setPositiveButton("确定",null)
                      .setView(view1)
                      .show();
            }
        });

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            taskSelectAdapter.setTaskList(taskNameList);
            listView.setAdapter(taskSelectAdapter);
        }
    };

    private void getAllUnSelectedTasks() {
        okHttpClient = new OkHttpClient();

        final Request request = new Request.Builder()
                .url("http://10.0.1.38:8080/water_sampling/task/getAllUnSelectedTasks")
                .get()
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.code() == 200){
                        String allTasks = response.body().string();
                        Log.i("taskName",allTasks);
                        mapList = JSONArray.parseObject(allTasks,List.class);

                        for (Map<String,String> map:mapList){
                            taskNameList.add(map.get("placeName"));
                            taskBottleTypeList.add(map.get("bottleType"));
                            taskReagentList.add(map.get("reagent"));
                        }
                        Log.i("taskName",taskNameList.toString()+"ssssssssssssss");
                        handler.sendEmptyMessage(0x001);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                    Toast.makeText(GetTaskActivity.this,"网络请求失败，请检查您的网络",Toast.LENGTH_SHORT).show();
                }
            }
        }).start();


    }
}
