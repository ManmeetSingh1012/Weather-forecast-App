package com.example.weatherforecast

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast

object constants {

    const val API:String="73cb78dc737be136486eb4d30cca973b"
    // base url basically use to make a network call
    const val Base_url="https://api.openweathermap.org/data/"
    const val metric:String="metric"

    // this func is for checking network
    fun isnetworkavailable(context: Context):Boolean
    {
        val connectivityManager=context.
                getSystemService(Context.CONNECTIVITY_SERVICE)
        as ConnectivityManager

        // if the phone version is more than the version M or 23
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M)
        {
            // it will get network and check it if it is not avaible then it return false
            val network=connectivityManager.activeNetwork?:return false
            // network capabilites
            val activeNetwork=connectivityManager.getNetworkCapabilities(network)?:return false

            return when{
                // for the capabilites
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->true
                else -> false
            }
        }
        // this method can be used upto api 29 not above than this
        else
        {
            val nteworkinfo=connectivityManager.activeNetworkInfo
            return nteworkinfo!=null && nteworkinfo.isConnectedOrConnecting
        }


    }

}