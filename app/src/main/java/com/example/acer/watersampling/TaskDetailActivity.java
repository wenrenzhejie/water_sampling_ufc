package com.example.acer.watersampling;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TaskDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        final Intent intent = getIntent();
        TextView bottleType_textView = findViewById(R.id.bottleType_textView);
        TextView reagent_textView = findViewById(R.id.reagent_textView);

        bottleType_textView.setText(intent.getStringExtra("bottleType"));
        reagent_textView.setText(intent.getStringExtra("reagent"));

        Button searchQrcode= findViewById(R.id.searchQrcode);
        Button searchRFIC= findViewById(R.id.searchRFIC);
//        监听扫描二维码
        searchQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(TaskDetailActivity.this, ScanBottleActivity.class);
                intent1.putExtra("placeName",intent.getStringExtra("placeName"));
                intent1.putExtra("bottleType",intent.getStringExtra("bottleType"));
                intent1.putExtra("reagent",intent.getStringExtra("reagent"));
                startActivity(intent1);
            }
        });

//        监听查找RFIC

    }
}
