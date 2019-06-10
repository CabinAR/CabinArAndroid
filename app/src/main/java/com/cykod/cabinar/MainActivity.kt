package com.cykod.cabinar

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var listView:ListView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    private var listItems: ArrayList<CabinSpace> = ArrayList<CabinSpace>()

    private lateinit var apiClient: ApiClient

    private var apiToken: String? = null

    val ASK_FOR_LOCATION_PERMISSION = 12

    private lateinit var sharedPref:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.titlebar))

        sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        apiToken = sharedPref.getString("api_token",null)

        apiClient = ApiClient(applicationContext, apiToken)

        listView = findViewById<ListView>(R.id.spacelist)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val adapter = SpaceListAdapter(this, listItems)
        listView.adapter = adapter

        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.refresh)
        swipeRefreshLayout.setOnRefreshListener {
            grabLocation()
        }


        val context = this
        listView.setOnItemClickListener { _, _, position, _ ->
            val space = listItems[position]
            val detailIntent = SpaceViewActivity.newIntent(context, space)
            startActivity(detailIntent)
        }




        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    ASK_FOR_LOCATION_PERMISSION)

            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ASK_FOR_LOCATION_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    grabLocation()
                } else {
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_options -> {
            // User chose the "Settings" item, show the app settings UI...
            val detailIntent = OptionsActivity.newIntent(this)
            startActivity(detailIntent)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        grabLocation()
    }

    fun grabLocation() {
        swipeRefreshLayout.isRefreshing = true
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED) {
            getSpaces(null, null)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if(location != null) {
                    getSpaces(location.latitude, location.longitude )
                } else {
                    getSpaces(null, null)
                }
            }
        }
    }


    fun getSpaces(latitude: Double?, longitude: Double?) {
        apiClient.getSpaces(latitude,longitude) { spaces, message ->
            swipeRefreshLayout.isRefreshing = false
            if(spaces != null) {
                listItems.clear()
                for (space in spaces) {
                    listItems.add(space)
                }
                val adapter = listView.adapter as SpaceListAdapter
                adapter.notifyDataSetChanged()

            } else {

            }
        }
    }
}
