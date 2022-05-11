package com.example.weatherforecast.Models

import java.io.Serializable

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
):Serializable
// why serialiZable to use in diff format
