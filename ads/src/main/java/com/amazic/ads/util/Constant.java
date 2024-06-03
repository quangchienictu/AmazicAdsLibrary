package com.amazic.ads.util;

import com.google.android.gms.ads.LoadAdError;

public class Constant {
    public static final LoadAdError NO_INTERNET_ERROR = new LoadAdError(-100, "No Internet", "local", null, null);
    public static final LoadAdError AD_NOT_AVAILABLE_ERROR = new LoadAdError(-200, "Ad Not Available", "local", null, null);
    public static final LoadAdError AD_NOT_HAVE_ID = new LoadAdError(-300, "Not have id", "local", null, null);
}
