package com.ecandle.example.geoweather.Interface;

import com.ecandle.example.geoweather.Model.ResponseData;
import com.ecandle.example.geoweather.Model.WeatherData;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by johannfjs on 21/10/16.
 * Modifed by jtomaylla on 20/11/16
 */

public interface HelperWS {
    @POST
    Call<ResponseData> getWeather(@Url String url);

    @POST
    Call<WeatherData> getWeatherData(@Url String url);
}
