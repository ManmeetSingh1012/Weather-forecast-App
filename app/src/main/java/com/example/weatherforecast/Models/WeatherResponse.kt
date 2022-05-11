package com.example.weatherforecast.Models

import java.io.Serializable

data class WeatherResponse(
    val coord:Coord,
    val weather:List<Weather>,
    val base:String,
    val visbility:Int,
    val wind:Wind,
    val clouds:Clouds,
    val dt:Int,
    val sys:Sys,
    val id:Int,
    val name:String,
    val cod:Int,
    val main:Main
):Serializable
