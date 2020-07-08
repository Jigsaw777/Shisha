package com.example.shareittest

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.reflect.InvocationTargetException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket


class MainActivity : AppCompatActivity() {

    private var mReservation: WifiManager.LocalOnlyHotspotReservation? = null

    private val filePermissionRequestCode = 1
    private val wifiPermissionRequestCode = 2
    private val locationPermissionRequestCode = 3

    private lateinit var mHandler: Handler
    private lateinit var manager: WifiManager

    private var isWifiOn: Boolean = false
    private lateinit var wifiConfiguration: WifiConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        askPermissions()
    }

    private fun init() {

        manager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiConfiguration = WifiConfiguration()
        //set Hotspot's name
        wifiConfiguration.SSID = "MyDummySSID"

        mHandler = Handler(Looper.myLooper()!!, Handler.Callback {
            when (it.what) {
                AppConstants.TOGGLE_WIFI -> {
                }
                else -> {
                }
            }
            return@Callback true
        })

        send_button.setOnClickListener {
            if ((ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_WIFI_STATE
                ) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CHANGE_WIFI_STATE
                ) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
//                turnOnHotspot()
                gotoSenderActivity()
            }
        }

        receive_button.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
//                turnOnWifi()
                gotoReceiverActivity()
            }
        }
    }

    private fun gotoReceiverActivity(){
        val intent=Intent(this,ReceiverActivity::class.java)
        startActivity(intent)
    }

    private fun askPermissions() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                    == PackageManager.PERMISSION_DENIED) || (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_WIFI_STATE
            ) == PackageManager.PERMISSION_DENIED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
                ),
                wifiPermissionRequestCode
            )
        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) && (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                filePermissionRequestCode
            )
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == wifiPermissionRequestCode) {
            if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission is needed", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == filePermissionRequestCode) {
            if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission is needed", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission is needed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun turnOnHotspot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isWifiOn) {
                manager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onFailed(reason: Int) {
                        super.onFailed(reason)
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to turn on Hotspot. Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                        super.onStarted(reservation)
                        mReservation = reservation
                        isWifiOn = true
                    }

                }, Handler())
            }
        } else {
            try {
                toggleHotspot(true, manager, wifiConfiguration)
                isWifiOn = true
            } catch (e: InvocationTargetException) {
                e.cause?.printStackTrace()
            }
        }
    }

    private fun toggleHotspot(
        activate: Boolean,
        manager: WifiManager,
        wifiConfigurationL: WifiConfiguration
    ) {
        wifiConfiguration = wifiConfigurationL
        manager.javaClass.getDeclaredMethod(
            "setWifiApEnabled", WifiConfiguration::class.java,
            Boolean::class.java
        ).invoke(manager, wifiConfiguration, activate)
    }

    private fun turnOffHotspot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mReservation != null)
                isWifiOn = false
            mReservation!!.close()
        } else {
            if (isWifiOn) {
                isWifiOn = false
                toggleHotspot(false, manager, wifiConfiguration)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun turnOnWifi() {
        //use local only hotspot methods
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
//
//        }

        //use normal wifi
//        else{
//
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier = WifiNetworkSpecifier.Builder().setSsid("MyDummySSID").build()
            val request =
                NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(specifier).build()
            val connectivityManager =
                this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCallback = ConnectivityManager.NetworkCallback()
            connectivityManager.requestNetwork(request, networkCallback)
        } else {
            val hotspotsConfig = manager.configuredNetworks
            val hotspotConfig =
                hotspotsConfig.first { wifiConfiguration ->
                    wifiConfiguration.SSID.contains(
                        "AndroidShare_",
                        true
                    )
                }
            if (hotspotConfig != null) {
                manager.enableNetwork(hotspotConfig.networkId, true)
                manager.reconnect()
            }
        }
    }

    private fun gotoSenderActivity() {
        val intent = Intent(this,SenderActivity::class.java)
        startActivity(intent)
    }

}
