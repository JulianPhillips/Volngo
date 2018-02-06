package com.example.julianparker.volngo;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class GeoFenceTransitionService extends IntentService {

    private static final String TAG = GeoFenceTransitionService.class.getSimpleName();

    public GeoFenceTransitionService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.e("onCreate: ");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.e("onHandleIntent()----------------> called with: intent = [" + intent + "]");
        // Retrieve the Geofencing intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Handling errors
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);
            return;
        }
        Intent geofenceReceive = new Intent("geofence_receive");

        // Retrieve GeoFenceTransition
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        geofenceReceive.putExtra("msg", String.valueOf(geoFenceTransition));
        LocalBroadcastManager.getInstance(this).sendBroadcast(geofenceReceive);

        // Check if the transition type
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            // Create a detail message with Geofences received
            String geofenceTransitionDetails = getGeoFenceTransitionDetails(geoFenceTransition, triggeringGeofences);
            geofenceReceive.putExtra("msg", geofenceTransitionDetails);
            LocalBroadcastManager.getInstance(this).sendBroadcast(geofenceReceive);
        }
    }

    // Create a detail message with Geofences received
    private String getGeoFenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeoFences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeoFencesList = new ArrayList<>();
        for (Geofence geofence : triggeringGeoFences) {
            triggeringGeoFencesList.add(geofence.getRequestId());
        }

        Timber.e("getGeoFenceTransitionDetails: triggeringGeoFencesList " + triggeringGeoFencesList.toString());

        String status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Entering geo fence";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            status = "Exiting geo fence";
        return status;
    }


    // Handle errors
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}