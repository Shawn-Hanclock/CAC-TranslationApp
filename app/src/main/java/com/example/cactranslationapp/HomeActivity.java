package com.example.cactranslationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {
    private MaterialButton toMainBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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