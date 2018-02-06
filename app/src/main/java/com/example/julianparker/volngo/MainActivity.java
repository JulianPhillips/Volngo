//package com.example.julianparker.placepicker;

package com.example.julianparker.volngo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.julianparker.volngo.play_service.location.LocationHelper;
import com.example.julianparker.volngo.play_service.location.LocationHelperCallback;
import com.example.julianparker.volngo.play_service.location.LocationHelperImpl;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener, LocationHelperCallback, ResultCallback<Status> {
    private static final String TAG = "MainActivity";
    private final static int MY_PERMISSION_FINE_LOCATION = 101;
    private final static int PLACE_PICKER_REQUEST = 1;
    private static final int REQUEST_LOCATION = 12;
    private static final int GEOFENCE_REQ_CODE = 454;
    //    private static final float GEOFENCE_RADIUS = 100f;
    TextView placeNameText;
    TextView placeAddressText;
    WebView attributionText;
    Button getPlaceButton;
    TextView UserId;
    private GoogleMap map;

    private TextView tvStatus;

    private LocationHelper mLocationHelper;
    private final BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            Toast.makeText(context, intent.getStringExtra("msg"), Toast.LENGTH_SHORT).show();
            tvStatus.setText(intent.getStringExtra("msg"));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle bundle = getIntent().getExtras();
        String e = "ERROR";
        if (bundle != null) {
            e = bundle.getString("User");
        }

        mLocationHelper = LocationHelperImpl.create(this, this, false);
        requestPermission();
        UserId = (TextView) findViewById(R.id.TheUser);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        UserId.setText(e);
        placeNameText = (TextView) findViewById(R.id.tvPlaceName);
        placeAddressText = (TextView) findViewById(R.id.tvPlaceAddress);
        attributionText = (WebView) findViewById(R.id.wvAttribution);
        getPlaceButton = (Button) findViewById(R.id.btGetPlace);
        getPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    Intent intent = builder.build(MainActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        registerReceiver(mGeoFenceReceiver, new IntentFilter("geofence_receive"));
        initGMaps();
    }


    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
            }
        } else {
            mLocationHelper.connectApiClient();
        }
    }

    public void goToTimer(View view) {
        if (placeNameText.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Choose a place first!", Toast.LENGTH_LONG).show();
        } else {
            Intent myIntent = new Intent(MainActivity.this, TimerActivity.class);
            myIntent.putExtra("Place", placeNameText.getText().toString());
            myIntent.putExtra("User", UserId.getText().toString());

            if (geoFenceMarker != null && geoFenceMarker.getPosition() != null) {
                myIntent.putExtra(Consts.LOCATION, geoFenceMarker.getPosition());
            }
            startActivity(myIntent);
        }

    }

    public void goToLog(View view) {
        Intent intent = new Intent(MainActivity.this, Logger.class);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_FINE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "This app requires location permissions to be granted", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    mLocationHelper.connectApiClient();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(MainActivity.this, data);
                    placeNameText.setText(place.getName());
                    placeAddressText.setText(place.getAddress());
                    if (place.getAttributions() == null) {
                        attributionText.loadData("no attribution", "text/html; charset=utf-8", "UFT-8");
                    } else {
                        attributionText.loadData(place.getAttributions().toString(), "text/html; charset=utf-8", "UFT-8");
                    }
                    markerForGeofence(place.getLatLng());
                    startGeofence(place.getId());
                }
                break;

            case REQUEST_LOCATION:
                mLocationHelper.handleActivityResult(resultCode);
                break;
        }

    }

    public void goToProfile(View view) {
        Intent i = new Intent(MainActivity.this, ProfileScreen.class);
        startActivity(i);
    }

    // Initialize GoogleMaps
    private void initGMaps() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    // Callback called when Map is touched
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick(" + latLng + ")");
    }

    // Callback called when Marker is touched
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
        return false;
    }

    @Override
    public void onGoogleApiClientConnected() {
        mLocationHelper.startLocationUpdates();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mGeoFenceReceiver);
        mLocationHelper.stopLocationUpdates();
        mLocationHelper.removeCallbacks();
        super.onDestroy();
    }

    @Override
    public void onResolutionRequired(Status locationSettingsStatus) {
        try {
            locationSettingsStatus.startResolutionForResult(this, REQUEST_LOCATION);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationUpdated(@Nullable Location newLocation) {
        Timber.d("onLocationUpdated() called with: newLocation = [" + newLocation + "]");
        if (newLocation == null) return;
        markerLocation(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
    }

    @Override
    public void onErrorGettingLocation(Throwable exception) {
        exception.printStackTrace();
    }

    @Override
    public void onUserDeniedLocationAccess() {
        Toast.makeText(this, "Location access is required!", Toast.LENGTH_SHORT).show();
    }

    private Marker locationMarker;

    // Create a Location Marker
    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if (map != null) {
            // Remove the anterior marker
            if (locationMarker != null)
                locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
        }
    }

    private Marker geoFenceMarker;

    // Create a marker for the geofence creation
    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if (map != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();
            geoFenceMarker = map.addMarker(markerOptions);
            float zoom = Consts.ZOOM_RATIO;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent;

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(this, GeoFenceTransitionService.class);
        geoFencePendingIntent = PendingIntent.getService(
                getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geoFencePendingIntent;
    }

    // Add the created GeofenceRequest to the device's monitoring list
    @SuppressWarnings("MissingPermission")
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        LocationServices.GeofencingApi.addGeofences(
                mLocationHelper.getGoogleApiClient(),
                request,
                createGeofencePendingIntent()
        ).setResultCallback(this);
    }

    // Start Geofence creation process
    private void startGeofence(String id) {
        Log.i(TAG, "startGeofence()");
        if (geoFenceMarker != null) {
            Geofence geofence = createGeofence(id, geoFenceMarker.getPosition(), Consts.GEOFENCE_RADIUS);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            drawGeofence();
        } else {
            // inform about fail
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;

    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(Consts.GEOFENCE_RADIUS);
        geoFenceLimits = map.addCircle(circleOptions);
    }

    // Create a Geofence
    private Geofence createGeofence(String id, LatLng latLng, float radius) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();
    }
}