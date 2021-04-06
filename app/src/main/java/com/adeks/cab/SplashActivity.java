package com.adeks.cab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         Thread thread = new Thread() {
             @Override
             public void run() {
                 try {
                    sleep(5000);
                 }catch (Exception ex) {
                    ex.printStackTrace();
                 }
                 finally {
                    Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                    startActivity(intent);
                 }
             }
         };
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}