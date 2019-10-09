package com.example.rfic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IUHFService iuhfService = UHFManager.getUHFService(this);
        iuhfService.OpenDev();
    }
}
