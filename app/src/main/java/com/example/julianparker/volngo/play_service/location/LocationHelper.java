package com.example.julianparker.volngo.play_service.location;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.Nullable;

import com.example.julianparker.volngo.play_service.base.BaseGoogleApiHelper;


/**
 * Created by agile-01 on 6/13/2016.
 * <p>
 * helper for fused location api
 */
public interface LocationHelper extends BaseGoogleApiHelper {

    /**
     * fetches last known location
     *
     * @return last known location. null if not found.
     */
    @Nullable
    Location getLastLocation();

    /**
     * configures location update intervals and accuracy.
     *
     * @param normalUpdateInterval  normal update interval. default is 10000ms.
     * @param fastestUpdateInterval fastest update interval that app can receive. should be greater than normal. default is 8000ms.
     * @param highAccuracy          true if high accuracy required. false if only balanced power accuracy needed. default is balanced power.
     *                              For best practices, check battery and choose accuracy accordingly. Otherwise high accuracy will drain battery.
     */
    void configureUpdateFrequency(long normalUpdateInterval, long fastestUpdateInterval, boolean highAccuracy);


    /**
     * processes resolution activity result
     *
     * @param resultCode activity result code
     * @see LocationHelperCallback#onResolutionRequired
     */
    void handleActivityResult(int resultCode);

    /**
     * checks location settings and request location updates<br/>
     * gives callback {@link LocationHelperCallback#onResolutionRequired} if system need to ask for resolution settings<br/>
     * updates location as defined in {@link #configureUpdateFrequency}<br/>
     * and gives callback {@link LocationHelperCallback#onLocationUpdated} whenever location is updated<br/>
     * <p>
     * CALL INSIDE {@link LocationHelperCallback#onGoogleApiClientConnected()}
     *
     * @throws IllegalStateException if google api client is not connected
     */
    void startLocationUpdates();


    /**
     * stops location updates and removes location update callbacks<br/>
     * call inside {@link Activity#onStop()} to avoid leaks
     */
    void stopLocationUpdates();


}
