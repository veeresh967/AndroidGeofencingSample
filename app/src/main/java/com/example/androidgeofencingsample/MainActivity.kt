package com.example.androidgeofencingsample
import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
class MainActivity : AppCompatActivity(), OnCompleteListener<Void>, OnMapReadyCallback {

    private var mGeofencingClient: GeofencingClient? = null
    private val MY_PERMISSIONS_REQUEST_LOCATION = 42
    private var mGeofenceList: ArrayList<Geofence>? = null
    private var mGeofencePendingIntent: PendingIntent? = null
    private val TAG = "VEEEE"
    private lateinit var map: GoogleMap
    var listPlaces = ArrayList<MyLocation>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        if (ContextCompat.checkSelfPermission( this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
        } else {
            listPlaces.add(MyLocation("Place1",13.0104834, 77.6613728))
            listPlaces.add(MyLocation("Bhagini restaurant",13.0191, 77.65554))
            listPlaces.add(MyLocation("Indrnagar Truffels", 12.97837, 77.64084))
            listPlaces.add(MyLocation("Bhagmane Techpark", 12.97934, 77.65789))
            addGeofences()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG,"on Map ready Triggered")
        map = googleMap
        if(map != null && this::map.isInitialized) {
            Log.d(TAG,"Maps loaded")
            for (location: MyLocation in listPlaces){

                val latlng = LatLng(
                    location.latitude,
                    location.longitude
                )

                val circleOptions = CircleOptions()
                    .center(LatLng(latlng.latitude, latlng.longitude))
                    .radius(500.0)
                    .strokeColor(Color.RED)
                    .fillColor(Color.TRANSPARENT)

                val zoomLevel = 12f

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomLevel))
                map.addMarker(MarkerOptions().position(latlng))
                map.addCircle(circleOptions)

            }
        }

    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    addGeofences()
                } else {

                }
                return
            }
        }
    }

    private fun addGeofences() {
        Log.d(TAG,"add geo fence called");
        mGeofenceList = ArrayList<Geofence>()

        listPlaces.add(MyLocation("Place1",13.0104834, 77.6613728))
       // listPlaces.add(MyLocation("Bhagini restaurant",13.0191, 77.65554))
       // listPlaces.add(MyLocation("Indrnagar Truffels", 12.97837, 77.64084))
        //listPlaces.add(MyLocation("Bhagmane Techpark", 12.97934, 77.65789))
      //  listPlaces.add(MyLocation("Place4", 63.336492, -7.438219))
       // listPlaces.add(MyLocation("Place5", 63.383416, -7.491321))

        for (location: MyLocation in listPlaces) {
            mGeofenceList?.add(
                Geofence.Builder()
                    .setRequestId(location.key)
                    .setCircularRegion(
                        location.latitude,
                        location.longitude,
                        2000f
                    )
                    .setNotificationResponsiveness(1000)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setLoiteringDelay(10000)
                    .build()
            )


        }


        createGeofencingClient()
    }

    private fun createGeofencingClient() {
        mGeofencePendingIntent = null

        mGeofencingClient = LocationServices.getGeofencingClient(this@MainActivity)

        createGeoFencePendingIntent()?.let { mGeofencePendingIntent ->
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mGeofencingClient?.addGeofences(createGeofencingRequest(), mGeofencePendingIntent)
                ?.addOnCompleteListener(this)
        }
    }

    override fun onComplete(task: Task<Void>) {
        if (task.isSuccessful) {
            Toast.makeText(this, "Geofencing Successful", Toast.LENGTH_SHORT).show()
        } else {
            val errorMessage = task.exception?.let { MyGeofenceErrorMessages.getErrorString(this, it) }
            if (errorMessage != null) {
                Log.e(TAG, errorMessage)
            }
        }
    }

    private fun createGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
        builder.addGeofences(mGeofenceList)
        return builder.build()
    }

    private fun createGeoFencePendingIntent(): PendingIntent? {

        mGeofencePendingIntent?.let {
            return it
        }

        val intent = Intent(this, MyGeofenceTransitionsIntentService::class.java)
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return mGeofencePendingIntent

    }

}
