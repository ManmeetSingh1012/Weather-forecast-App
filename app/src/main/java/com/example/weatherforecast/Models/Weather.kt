package com.example.weatherforecast.Models

import java.io.Serializable

data class Weather(
    val id:Int,
    val main:String,
    val discrp:String,
    val icon:String
):Serializable
