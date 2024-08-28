package com.drawingapps.tracedrawing.drawingsketch.drawingapps;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.manager.open_app.OpenAppBuilder;
import com.amazic.ads.util.manager.open_app.OpenAppCallback;
import com.amazic.ads.util.manager.open_app.AdOpenAppManager;
import com.ardrawing.tracedrawing.drawingsketch.drawingapps.R;

public class ResumeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume);
        Button button = findViewById(R.id.tv_next_screen);

        OpenAppBuilder builder = new OpenAppBuilder(this)
                .setId(AdmobApi.getInstance().getListIDByName("appopen_resume"))
                .setCallback(new OpenAppCallback(){
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        finish();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        Toast.makeText(ResumeActivity.this, "success", Toast.LENGTH_SHORT).show();
                    }
                });
        AdOpenAppManager adOpenAppManager = new AdOpenAppManager();
        adOpenAppManager.setBuilder(builder);
        adOpenAppManager.loadAd();
        button.setOnClickListener(v -> {
            adOpenAppManager.showAd(this);
        });
    }
}