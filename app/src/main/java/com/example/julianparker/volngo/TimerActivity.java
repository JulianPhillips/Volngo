package com.example.julianparker.volngo;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.julianparker.volngo.play_service.location.LocationHelper;
import com.example.julianparker.volngo.play_service.location.LocationHelperCallback;
import com.example.julianparker.volngo.play_service.location.LocationHelperImpl;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class TimerActivity extends AppCompatActivity implements OnMapReadyCallback, LocationHelperCallback {

    private static final int REQUEST_LOCATION = 45;
    TextView textView;

    Button start, pause, reset, lap;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    private GoogleMap map;
    private LocationHelper mLocationHelper;

    Handler handler;
    int Hours, Seconds, Minutes, MilliSeconds;
    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);
            Hours = Minutes / 60;

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            textView.setText(Hours + ": " + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handler.postDelayed(this, 0);
        }

    };
    ListView listView;

    String[] ListElements = new String[]{};
    String UserId;
    List<String> ListElementsArrayList;
    String Place;
    ArrayAdapter<String> adapter;
    private FirebaseDatabase Database;
    private FirebaseAuth auth;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeractivity);
        bundle = getIntent().getExtras();

        if (bundle != null) {
            UserId = bundle.getString("User");
            Place = bundle.getString("Place");
        } else {
            UserId = auth.getCurrentUser().getUid();
            Toast.makeText(this, "Bundle aint working", Toast.LENGTH_LONG).show();
        }


        auth = FirebaseAuth.getInstance();


        textView = (TextView) findViewById(R.id.textView);
        start = (Button) findViewById(R.id.button);
        pause = (Button) findViewById(R.id.button2);
        reset = (Button) findViewById(R.id.button3);
        lap = (Button) findViewById(R.id.button4);
        listView = (ListView) findViewById(R.id.listview1);

        handler = new Handler();

        ListElementsArrayList = new ArrayList<String>(Arrays.asList(ListElements));

        adapter = new ArrayAdapter<String>(TimerActivity.this,
                android.R.layout.simple_list_item_1,
                ListElementsArrayList
        );

        listView.setAdapter(adapter);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StartTime = SystemClock.uptimeMillis();
                handler.postDelayed(runnable, 0);

                reset.setEnabled(false);

            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimeBuff += MillisecondTime;

                handler.removeCallbacks(runnable);

                reset.setEnabled(true);
                goToDataBase();

            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MillisecondTime = 0L;
                StartTime = 0L;
                TimeBuff = 0L;
                UpdateTime = 0L;
                Seconds = 0;
                Minutes = 0;
                Hours = 0;
                MilliSeconds = 0;

                textView.setText("00:00:00:00");

                ListElementsArrayList.clear();

                adapter.notifyDataSetChanged();
            }
        });

        lap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListElementsArrayList.add(textView.getText().toString());
                adapter.notifyDataSetChanged();
            }
        });


        Database = FirebaseDatabase.getInstance();
        mLocationHelper = LocationHelperImpl.create(this, this, false);
        mLocationHelper.connectApiClient();
        initGMaps();
    }

    // Initialize GoogleMaps
    private void initGMaps() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        drawGeofence();
    }

    public void goToDataBase() {


        if (auth.getCurrentUser().getDisplayName() != null || auth.getCurrentUser().getDisplayName().length() > 0) {
            String name = auth.getCurrentUser().getDisplayName();
            String data = "at " + Place + " " + name + " Volunteered for: " + textView.getText().toString();
            DatabaseReference myRef = Database.getReference("User");
            myRef.child(UserId).push().setValue(data);
            Intent intent = new Intent(TimerActivity.this, Logger.class);
            startActivity(intent);
            finish();


        } else {
            String data = "at " + Place + " User Volunteered for: " + textView.getText().toString();
            DatabaseReference myRef = Database.getReference("User");
            myRef.child(UserId).push().setValue(data);
            Intent intent = new Intent(TimerActivity.this, Logger.class);
            startActivity(intent);
            finish();

        }

    }

    @Override
    protected void onDestroy() {
        mLocationHelper.stopLocationUpdates();
        mLocationHelper.removeCallbacks();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOCATION:
                mLocationHelper.handleActivityResult(resultCode);
                break;
        }

    }


    private void drawGeofence() {
        if (getIntent().hasExtra(Consts.LOCATION)) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(((LatLng) getIntent().getParcelableExtra(Consts.LOCATION)))
                    .strokeColor(Color.argb(50, 70, 70, 70))
                    .fillColor(Color.argb(100, 150, 150, 150))
                    .radius(Consts.GEOFENCE_RADIUS);
            map.addCircle(circleOptions);
        }
    }

    @Override
    public void onGoogleApiClientConnected() {
        mLocationHelper.startLocationUpdates();
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
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if (map != null) {
            // Remove the anterior marker
            if (locationMarker != null)
                locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            float zoom = Consts.ZOOM_RATIO;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }
}