package com.amazic.ads.util;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class CollapseBannerHelper {
    public static ArrayList<View> listChildViews = new ArrayList<>();

    public static void getAllChildViews(ViewGroup viewGroup, String collapseTypeClose, long valueCountDownOrCountClick) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = viewGroup.getChildAt(i);
            listChildViews.add(childView);
            Log.d("CollapseBannerHelperx", "getAllChildViews: " + childView.getClass().getName());
            if (childView.getClass().getName().equals("android.widget.LinearLayout")) {
                //Case count click to collapse the banner view
                if (collapseTypeClose.equals(Admob.COUNT_CLICK)) {
                    Log.d("CollapseBannerHelper", "CASE COUNT_CLICK: " + valueCountDownOrCountClick);
                    ((LinearLayout) childView).setTag(0);
                    ((LinearLayout) childView).setOnClickListener(view -> {
                        ((LinearLayout) childView).setTag((int) ((LinearLayout) childView).getTag() + 1);
                        Log.d("CollapseBannerHelper", "COUNT_CLICK: " + (int) ((LinearLayout) childView).getTag());
                        if (valueCountDownOrCountClick > 0) {
                            if ((int) ((LinearLayout) childView).getTag() == valueCountDownOrCountClick) {
                                for (View viewChild : listChildViews) {
                                    if (viewChild.getClass().getName().equals("android.widget.RelativeLayout")) {
                                        viewChild.setVisibility(View.GONE);
                                        Log.d("CollapseBannerHelper", "CASE COUNT_DOWN setEnabled(true): " + valueCountDownOrCountClick);
                                        break;
                                    }
                                }
                            }
                        } else {
                            for (View viewChild : listChildViews) {
                                if (viewChild.getClass().getName().equals("android.widget.RelativeLayout")) {
                                    viewChild.setVisibility(View.GONE);
                                    Log.d("CollapseBannerHelper", "CASE COUNT_DOWN setEnabled(true): " + valueCountDownOrCountClick);
                                    break;
                                }
                            }
                        }
                    });
                } else if (collapseTypeClose.equals(Admob.COUNT_DOWN)) {
                    childView.setEnabled(false);
                }
            }
            int viewId = childView.getId();
            String viewIdString = viewId != View.NO_ID ?
                    viewGroup.getResources().getResourceEntryName(viewId) : "No ID";
            if (childView instanceof ViewGroup) {
                Log.d("CollapseBannerHelper", "ViewGroup: " + childView.getClass().getName() + ", ID: " + viewIdString + "\n------------------------------------");
            } else {
                Log.d("CollapseBannerHelper", "View: " + childView.getClass().getName() + ", ID: " + viewIdString);
            }
            if (childView instanceof ViewGroup) {
                getAllChildViews((ViewGroup) childView, collapseTypeClose, valueCountDownOrCountClick);
            }
        }
        //Case count down to collapse the banner view
        if (collapseTypeClose.equals(Admob.COUNT_DOWN)) {
            Log.d("CollapseBannerHelper", "CASE COUNT_DOWN: " + valueCountDownOrCountClick);
            if (valueCountDownOrCountClick > 0) {
                new Handler().postDelayed(() -> {
                    for (View view : listChildViews) {
                        view.setEnabled(true);
                        Log.d("CollapseBannerHelper", "CASE COUNT_DOWN setEnabled(true): " + valueCountDownOrCountClick);
                    }
                }, valueCountDownOrCountClick);
            } else {
                for (View view : listChildViews) {
                    view.setEnabled(true);
                    Log.d("CollapseBannerHelper", "CASE COUNT_DOWN setEnabled(true): " + valueCountDownOrCountClick);
                }
            }
        }
    }

    public static ArrayList<Object> getWindowManagerViews() {
        try {
            // Lấy WindowManagerImpl class (có thể thay đổi tuỳ phiên bản Android)
            Class<?> windowManagerImplClass = Class.forName("android.view.WindowManagerGlobal");

            // Lấy instance của WindowManagerGlobal
            Field sDefaultWindowManagerField = windowManagerImplClass.getDeclaredField("sDefaultWindowManager");
            sDefaultWindowManagerField.setAccessible(true);
            Object windowManager = sDefaultWindowManagerField.get(null);

            // Lấy danh sách các view hiện tại trong WindowManager
            Field mViewsField = windowManagerImplClass.getDeclaredField("mViews");
            mViewsField.setAccessible(true);
            ArrayList<Object> views = (ArrayList<Object>) mViewsField.get(windowManager);

            return views;

        } catch (Exception e) {
            Log.d("TAGvvv", "getWindowManagerViews: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
