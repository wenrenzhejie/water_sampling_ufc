package com.example.acer.watersampling;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.EditText;
import android.widget.Toast;

import com.example.acer.watersampling.bean.Msg;
import com.example.acer.watersampling.bean.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private EditText et_login_username,et_login_password;
    private OkHttpClient okHttpClient = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        BGAQRCodeUtil.setDebug(true);
       et_login_username = findViewById(R.id.et_login_username);
       et_login_password = findViewById(R.id.et_login_password);
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
        final String username = et_login_username.getText().toString();
        final String password = et_login_password.getText().toString();
        Log.i("tag",username+":"+password);
        FormBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url("http://10.0.1.38:8080/water_sampling/user/loginByName")
                .post(formBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("tag","失败了");
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
                    edit.putString(username,password);
//                    保存userId和密码
                    edit.putString("userId",String.valueOf(user1.getUserId()));
                    edit.apply();
//                    启动UserDetailActivity
                    Intent intent = new Intent(MainActivity.this, UserDetailActivity.class);
                    intent.putExtra("data",data);
                    startActivity(intent);
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
        startActivity(new Intent(MainActivity.this,ScanQrCodeActivity.class));
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
