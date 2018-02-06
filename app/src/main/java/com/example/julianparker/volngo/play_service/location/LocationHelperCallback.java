package com.example.julianparker.volngo.play_service.location;


import android.app.Activity;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.api.Status;

/**
 * Created by agile-01 on 6/13/2016.
 * <p>
 * listener for {@link LocationHelper} updates
 * should be implemented by caller {@link Activity} or {@link Fragment}
 */
public interface LocationHelperCallback {

    /**
     * called when google api clinet is connected
     */
    void onGoogleApiClientConnected();

    /**
     * if system needs to ask for change in location settings.
     * call {@link Status#startResolutionForResult}
     */
    void onResolutionRequired(Status locationSettingsStatus);

    /**
     * called when location updates received
     * <p>
     * if one time update is true, will give only last known location
     *
     * @param newLocation new location
     */
    void onLocationUpdated(@Nullable Location newLocation);

    /**
     * called whenever any error occurred
     *
     * @param exception contains description about the cause
     */
    void onErrorGettingLocation(Throwable exception);

    /**
     * called when user denied in location settings dialog
     */
    void onUserDeniedLocationAccess();

}
