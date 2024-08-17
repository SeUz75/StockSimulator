package com.example.programmingandroid;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StockApi {
    @GET("query")
    Call<StockResponse> getIntradayData(
            @Query("function") String function,
            @Query("symbol") String symbol,
            @Query("interval") String interval,
            @Query("apikey") String apiKey
    );

    @GET("query")
    Call<StockResponse> getDailyData(
            @Query("function") String function,
            @Query("symbol") String symbol,
            @Query("apikey") String apiKey
    );
}
