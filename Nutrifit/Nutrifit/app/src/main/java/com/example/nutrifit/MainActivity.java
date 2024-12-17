package com.example.nutrifit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button cameraButton, userInformationButton, recommendButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInformationButton = findViewById(R.id.userInformationButton);
        cameraButton = findViewById(R.id.searchButton);
        recommendButton = findViewById(R.id.recommendButton);

        // 메인화면에서 UserInformActivity로 이동
        userInformationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserInformActivity.class);
            startActivity(intent);
        });

        // 메인화면에서 CameraActivity로 이동
        cameraButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        // 추천 버튼 클릭 시 Collabo_filter 실행
        recommendButton.setOnClickListener(v -> {
            Collabo_Filter.runRecommendation(MainActivity.this);
        });
    }
}