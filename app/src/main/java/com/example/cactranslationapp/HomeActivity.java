package com.example.cactranslationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {
    private MaterialButton toMainBtn;
    private ImageView backgroundGlobe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        backgroundGlobe = findViewById(R.id.globeBack_imgV);
        backgroundGlobe.setColorFilter(Color.argb(200, 255, 255, 255));
        toMainBtn = findViewById(R.id.toMainActivity_btn);
        toMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMain();
            }
        });
    }

    public void openMain()
    {
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
    }
}