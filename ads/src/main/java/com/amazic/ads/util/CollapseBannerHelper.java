package com.amazic.ads.util;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class CollapseBannerHelper {
    public static ArrayList<View> listChildViews = new ArrayList<>();

    public static void getAllChildViews(ViewGroup viewGroup, String collapseTypeClose, long valueCountDownOrCountClick, Object object) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = viewGroup.getChildAt(i);
            listChildViews.add(childView);
            Log.d("CollapseBannerHelperx", "getAllChildViews: " + childView.getClass().getName());
            if (childView.getClass().getName().equals("android.widget.LinearLayout")) {
                //Case count click to collapse the banner view
                if (collapseTypeClose.equals(Admob.COUNT_CLICK)) {
                    Log.d("CollapseBannerHelper", "CASE COUNT_CLICK: " + valueCountDownOrCountClick);
                    childView.setTag(0);
                    childView.setOnClickListener(view -> {
                        childView.setTag((int) childView.getTag() + 1);
                        Log.d("CollapseBannerHelper", "COUNT_CLICK: " + (int) childView.getTag());
                        if (valueCountDownOrCountClick > 0) {
                            if ((int) childView.getTag() == valueCountDownOrCountClick) {
                                ((ViewGroup) object).setVisibility(View.GONE);
                                Log.d("CollapseBannerHelper", "CASE COUNT_DOWN setEnabled(true): " + valueCountDownOrCountClick);
                            }
                        } else {
                            ((ViewGroup) object).setVisibility(View.GONE);
                            Log.d("CollapseBannerHelper", "CASE COUNT_DOWN setEnabled(true): " + valueCountDownOrCountClick);
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
                getAllChildViews((ViewGroup) childView, collapseTypeClose, valueCountDownOrCountClick, object);
            }
        }
        //Case count down to collapse the banner view
        if (collapseTypeClose.equals(Admob.COUNT_DOWN)) {
            Log.d("CollapseBannerHelper", "CASE COUNT_DOWN: " + valueCountDownOrCountClick);
            if (valueCountDownOrCountClick > 0) {
                new Handler().postDelayed(() -> {
                    for (View view : listChildViews) {
                        view.setEnabled(true);
                    }
                    Log.d("CollapseBannerHelper", "CASE COUNT_DOWN setEnabled(true): " + valueCountDownOrCountClick);
                }, valueCountDownOrCountClick);
            } else {
                for (View view : listChildViews) {
                    view.setEnabled(true);
                }
                Log.d("CollapseBannerHelper", "CASE COUNT_DOWN setEnabled(true): " + valueCountDownOrCountClick);
            }
        }
    }

    public static ArrayList<Object> getWindowManagerViews() {
        try {
            // Get WindowManagerImpl class (can be different by Android version)
            Class<?> windowManagerImplClass = Class.forName("android.view.WindowManagerGlobal");

            // Get instance of WindowManagerGlobal
            Field sDefaultWindowManagerField = windowManagerImplClass.getDeclaredField("sDefaultWindowManager");
            sDefaultWindowManagerField.setAccessible(true);
            Object windowManager = sDefaultWindowManagerField.get(null);

            // Get current view list in WindowManager
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
