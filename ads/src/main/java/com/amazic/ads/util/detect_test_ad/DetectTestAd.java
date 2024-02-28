package com.amazic.ads.util.detect_test_ad;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class DetectTestAd {
    public static String testAd = "Test Ad";
    private boolean showAds = false;
    public static DetectTestAd INSTANCE;

    public static DetectTestAd getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DetectTestAd();
        }
        return INSTANCE;
    }

    public void setShowAds(boolean showAds, Context context) {
        this.showAds = showAds;
        SharedPreferences.Editor editor = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).edit();
        editor.putBoolean(testAd, !showAds);
        editor.apply();
    }

    public boolean isTestAd(Context context) {
        return context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).getBoolean(testAd, false);
    }

    public Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public String imageToText(Bitmap bitmap, Context context, IOnImageToTextSuccess iOnImageToTextSuccess) {
        SharedPreferences.Editor editor = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).edit();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context.getApplicationContext()).build();
        Frame imageFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        String imageText = "";
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
            imageText = imageText + "" + textBlock.getValue();
        }
        Log.d("TAG", "imageToText: " + imageText);
        if (imageText.toLowerCase().contains(testAd.toLowerCase()) && !this.showAds) {
            editor.putBoolean(testAd, true);
            if (editor.commit()) {
                iOnImageToTextSuccess.onImageToTextSuccess();
            }
        } else {
            iOnImageToTextSuccess.onImageToTextSuccess();
        }
        return imageText;
    }

    public interface IOnImageToTextSuccess {
        void onImageToTextSuccess();
    }
}
