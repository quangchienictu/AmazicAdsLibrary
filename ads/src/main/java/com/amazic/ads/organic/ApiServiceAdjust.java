package com.amazic.ads.organic;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiServiceAdjust {
    @GET("inspect_device")
    Call<AdjustOutputModel> sendData(
            @Query("advertising_id") String advertisingId,
            @Query("app_token") String appToken,
            @Header("Authorization") String authHeader
    );
}
