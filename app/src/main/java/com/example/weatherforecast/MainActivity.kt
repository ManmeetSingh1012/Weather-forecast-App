package com.example.weatherforecast

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.weatherforecast.Models.WeatherResponse
import com.example.weatherforecast.Network.NetworkService
import com.example.weatherforecast.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    var binding:ActivityMainBinding?=null
    // fused location is for location provider
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var dialog:Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mFusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        if(!isloctaionenabled())
        {

            dialog()
        }
        else {
            locationPermission()

        }




    }



    private fun dialog() {
        val dialog= AlertDialog.Builder(this)
        dialog.setMessage("Please on GPS Service to get weather updates")
        dialog.setPositiveButton("Settings"){
                dialgo,_->
            try{
                // this will take us directly to settings page

                //this will be intent or allow us to go over there
                val intent= Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)

            }catch (e: ActivityNotFoundException)
            {
                e.printStackTrace()
            }
        }

        dialog.setNegativeButton("Cancel"){ dialog,_->dialog.dismiss()}
        dialog.create().show()
    }

    private fun isloctaionenabled():Boolean
    {
        // this will help us to get the geographical location from system
        val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun locationPermission()
    {
        Dexter.withContext(this).
        withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
            .withListener(object :MultiplePermissionsListener{
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    requestlocation()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    dialog2()
                }

            }).onSameThread().check()
    }

    // this will request location
    @SuppressLint("MissingPermission")
    private  fun requestlocation()
    {
        val request=LocationRequest()
        request.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
        Looper.myLooper()?.let {
            mFusedLocationProviderClient.requestLocationUpdates(
                request,locationCallback, it
            )
        }


    }
    // this will give the location
    private val locationCallback=object :LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            val lastlocation:Location=result.lastLocation
            val lati=lastlocation.latitude
            val longi=lastlocation.longitude




            Log.i("lat","$lati")
            Log.i("long","$longi")
            isNetworkAva(lati,longi)
        }
    }

    private fun isNetworkAva(lati:Double,longi:Double)
    {
        if(constants.isnetworkavailable(this))
        {
            val retrofit:Retrofit=Retrofit.Builder().baseUrl(constants.Base_url).
            addConverterFactory(GsonConverterFactory
                .create()).build()

            // this will complete the link or we can say that we have made a service to request data
            // call<T> where T represent as a sucessful response body type
            val service:NetworkService=retrofit.create(NetworkService::class.java)
            // here matric will give us the data in metric units
             val listcall:Call<WeatherResponse> = service.getweather(lati,longi,constants.API,constants.metric)

            progress_bar()
            listcall.enqueue(object :Callback<WeatherResponse>{
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if(response.isSuccessful){
                        dismissProgress()
                        val weatherlist: WeatherResponse? =response.body()
                        displaydata(weatherlist!!)
                        Log.i("data","$weatherlist")

                    }
                    else
                    {
                        val rc=response.code()
                        when(rc){
                            404 -> Log.e("error","Not found")
                            400 -> Log.e("error","Bad network")
                            else ->{
                                Log.e("Errror ","genric error")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    dismissProgress()
                    Log.e("ERrron",t.message.toString())
                }

            })
        }
        else
        {
            Toast.makeText(this,"Network is not avalable",Toast.LENGTH_SHORT).show()
        }
    }

    private fun dialog2() {
        val dialog= AlertDialog.Builder(this)
        dialog.setTitle("Permission For GPS Service ?")
        dialog.setMessage("Permission Required to acess the GPS Service ,Go to settings and allow us to use GPS Service")
        dialog.setPositiveButton("Settings"){
                dialgo,_->
            try{
                // this will take us directly to settings page

                //this will be intent or allow us to go over there
                val intent= Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri=Uri.fromParts("package",packageName,null)
                intent.data=uri
                startActivity(intent)

            }catch (e: ActivityNotFoundException)
            {
                e.printStackTrace()
            }
        }

        dialog.setNegativeButton("Cancel"){ dialog,_->dialog.dismiss()}
        dialog.create().show()
    }

    //progress_bar
    private fun progress_bar()
    {
        dialog=Dialog(this)
        dialog!!.setContentView(R.layout.progress_bar)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    private fun dismissProgress()
    {
        if(dialog!=null)
        {
            dialog!!.dismiss()
        }
    }

    // this is used to display the data
    @SuppressLint("SetTextI18n")
    private fun displaydata(weahterlist:WeatherResponse)
    {
        binding?.CTY?.text=weahterlist.name
        binding?.speed?.text=weahterlist.wind.speed.toString()+"km/h"
        binding?.humidity?.text=weahterlist.main.humidity.toString()+"%"
        binding?.feelslike?.text=weahterlist.main.feels_like.toString()+"°C"
        binding?.pressure?.text=weahterlist.main.pressure.toString()+"mbar"
        binding?.temp?.text=weahterlist.main.temp.toString()
        binding?.rise?.text = unixTime(weahterlist.sys.sunrise)
        binding?.set?.text = unixTime(weahterlist.sys.sunset)
        // why bez sometimes we can get multiple weather
        for( i in weahterlist.weather.indices)
        {
            when (weahterlist.weather[i].icon) {
                "01d" -> binding?.weatherPhoto?.setImageResource(R.drawable.sunny)
                "02d" -> binding?.weatherPhoto?.setImageResource(R.drawable.cloud)
                "03d" -> binding?.weatherPhoto?.setImageResource(R.drawable.cloud)
                "04d" -> binding?.weatherPhoto?.setImageResource(R.drawable.cloud)
                "04n" -> binding?.weatherPhoto?.setImageResource(R.drawable.cloud)
                "10d" -> binding?.weatherPhoto?.setImageResource(R.drawable.rain)
                "11d" -> binding?.weatherPhoto?.setImageResource(R.drawable.storm)
                "13d" -> binding?.weatherPhoto?.setImageResource(R.drawable.snowflake)
                "01n" -> binding?.weatherPhoto?.setImageResource(R.drawable.cloud)
                "02n" -> binding?.weatherPhoto?.setImageResource(R.drawable.cloud)
                "03n" -> binding?.weatherPhoto?.setImageResource(R.drawable.cloud)
                "10n" -> binding?.weatherPhoto?.setImageResource(R.drawable.cloud)
                "11n" -> binding?.weatherPhoto?.setImageResource(R.drawable.rain)
                "13n" -> binding?.weatherPhoto?.setImageResource(R.drawable.snowflake)
            }
        }


    }

    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("HH:mm")
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menus,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId)
        {
            R.id.refresh_itm->{
                requestlocation()
                true
            }

            R.id.exit-> {
                if(isloctaionenabled())
                {
                   locationPermission()
                }
                Log.i("check","passed")
                true

            }
            else ->super.onOptionsItemSelected(item)
        }

    }
}




