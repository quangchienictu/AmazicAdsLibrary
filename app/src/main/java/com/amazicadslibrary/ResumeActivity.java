package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.amazic.ads.util.manager.open_app.OpenAppCallback;
import com.amazic.ads.util.manager.open_app.AdOpenAppManager;

public class ResumeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume);
        Button button = findViewById(R.id.tv_next_screen);
        button.setOnClickListener(v -> {
            AdOpenAppManager.getInstance().showAd(this, new OpenAppCallback(){
                @Override
                public void onNextAction() {
                    super.onNextAction();
                    finish();
                }
            });
        });
    }
}