package com.speedata.uhf;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.speedata.uhf.bean.Msg;
import com.speedata.uhf.bean.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private EditText et_login_username,et_login_password;
    private Button bt_username_login,bt_qrcode_login;
    //        设置最大重连接次数
    private int maxConnectTimes = 3;
//    设置当前连接次数
    private int currentConnextTimes = 0;
    private OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20,TimeUnit.SECONDS)
            .build();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        BGAQRCodeUtil.setDebug(true);
        bt_username_login = findViewById(R.id.bt_username_login);
        bt_qrcode_login = findViewById(R.id.bt_qrcode_login);
       et_login_username = findViewById(R.id.et_login_username);
       et_login_password = findViewById(R.id.et_login_password);
//       自动登录
//        autoLogin();
    }

    private void positionPermission(){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()) {
            String [] permissions =permissionList.toArray(new String[permissionList.
                    size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
    }
    //    用户名登录
    public void loginByName(View view){
//        在重连之前禁用按钮
        bt_username_login.setEnabled(false);

        final String username = et_login_username.getText().toString();
        final String password = et_login_password.getText().toString();
        Log.i("tag",username+":"+password);
        FormBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        final Request request = new Request.Builder()
                .url("http://10.0.1.38:8080/water_sampling/user/loginByName")
                .post(formBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("tag","失败了");
                Log.i("tag",e.getClass().getName());
                if ((e instanceof SocketTimeoutException || e instanceof ConnectException) && currentConnextTimes < maxConnectTimes){
                    currentConnextTimes++;
                    okHttpClient.newCall(request).enqueue(this);
                    Log.i("currentConnextTimes","currentConnextTimes:"+currentConnextTimes);
                    if (currentConnextTimes == maxConnectTimes){
                      runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              Toast.makeText(MainActivity.this,"网络质量不好，请检查您的网络",Toast.LENGTH_SHORT).show();
//                              finish();
                              bt_username_login.setEnabled(true);
                              currentConnextTimes = 0;
                          }
                      });
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
//                得到msg对象
                Msg msg = new Gson().fromJson(data, Msg.class);
                Object user = msg.getData().get("user");
                String s = new Gson().toJson(user);
                User user1 = new Gson().fromJson(s, User.class);
                Log.i("tag",msg.toString());
                if (msg.getCode() == 200){
//                登录成功
//                    将用户名和密码保存到sp中
                    SharedPreferences sharedPreferences = getSharedPreferences("user_login", MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
//                    保存用户名和密码
                    edit.putString("username",username);
                    edit.putString("password",password);
//                    保存userId和密码
                    edit.putString("userId",String.valueOf(user1.getUserId()));
                    edit.apply();
//                    启动UserDetailActivity
                    Intent intent = new Intent(MainActivity.this, UserDetailActivity.class);
                    intent.putExtra("data",data);
//                  登录成功结束登录界面
                    startActivity(intent);
                    finish();
                }else if (msg.getCode() == 500){
//                 登录失败
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"用户名或密码错误",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

    }
//    扫码登录
    public void loginByQrcode(View view){
        bt_qrcode_login.setEnabled(false);
        Intent intent = new Intent(MainActivity.this, ScanQrCodeActivity.class);
        startActivityForResult(intent,0x100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x100 && resultCode == 0x100){
//        登录成功
            Intent intent = new Intent(MainActivity.this, UserDetailActivity.class);
            intent.putExtra("data",data.getStringExtra("data"));
            //登录成功结束本界面
            startActivity(intent);
//            finish();
        }else if (requestCode == 0x100 && resultCode == 0x101){
//            登录失败
            Log.i("tag","进到这里来了");
            bt_qrcode_login.setEnabled(true);
            Toast.makeText(MainActivity.this,"您暂未得到授权,无法登陆系统",Toast.LENGTH_SHORT).show();
        }
    }

    //    自动登录
    private void autoLogin(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_login",MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        String password = sharedPreferences.getString("password", null);
        if (username != null && password != null){
            FormBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .build();
            final Request request = new Request.Builder()
                    .url("http://10.0.1.38:8080/water_sampling/user/loginByName")
                    .post(formBody)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if ((e instanceof SocketTimeoutException || e instanceof ConnectException) && currentConnextTimes < maxConnectTimes){
                        currentConnextTimes++;
                        okHttpClient.newCall(request).enqueue(this);
                        if (currentConnextTimes == maxConnectTimes){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"网络质量不好，请检查您的网络",Toast.LENGTH_SHORT).show();
//                              finish();
                                    bt_username_login.setEnabled(true);
                                    currentConnextTimes = 0;
                                }
                            });
                        }
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String data = response.body().string();
//                得到msg对象
                    Msg msg = new Gson().fromJson(data, Msg.class);
                    Object user = msg.getData().get("user");
                    String s = new Gson().toJson(user);
                    User user1 = new Gson().fromJson(s, User.class);
                    Log.i("tag",msg.toString());
                    if (msg.getCode() == 200){

//                    启动UserDetailActivity
                        Intent intent = new Intent(MainActivity.this, UserDetailActivity.class);
                        intent.putExtra("data",data);
                        startActivity(intent);
//                        登录成功结束登录界面
                        finish();
                    }
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
//        requestCodeQRCodePermissions();
        positionPermission();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) { Toast.makeText(this, "必须同意所有权限才能使用本程序",
                                Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
//        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    /*@AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }*/

}
