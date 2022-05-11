package com.example.weatherforecast.Network


import com.example.weatherforecast.Models.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkService {

    // why we have made this interface bec to complete our link to get data

    // get will help to get data
    @GET("2.5/weather")
    fun getweather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("appid") appid:String?,
        @Query("units") units:String

    ):Call<WeatherResponse> // helps to get the data

}