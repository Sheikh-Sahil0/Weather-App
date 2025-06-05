package com.example.SkyCast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("weather")
    Call<SkyCast> getWeatherData(
            @Query("q") String city,
            @Query("appid") String appid,
            @Query("units") String units
    );
}
