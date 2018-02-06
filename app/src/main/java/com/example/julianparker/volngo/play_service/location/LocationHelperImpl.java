package com.example.julianparker.volngo.play_service.location;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.example.julianparker.volngo.play_service.base.BaseGoogleApiHelperImpl;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import timber.log.Timber;

/**
 * Created by agile-01 on 6/13/2016.
 * <p>
 * implementation of {@link LocationHelper}. notifies caller via {@link LocationHelperCallback}
 */
public class LocationHelperImpl extends BaseGoogleApiHelperImpl implements LocationHelper, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult> {

    private static long NORMAL_UPDATE_INTERVAL = 10 * 1000;
    private static long FASTEST_UPDATE_INTERVAL = 8 * 1000;
    private static int ACCURACY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    private final boolean mNeedOneTimeUpdate;
    private LocationHelperCallback mLocationHelperCallback;
    private LocationRequest mLocationRequest;
    private int mUpdateCounter;

    /**
     * constructor is private. use factory method instead
     *
     * @see #create(Context, LocationHelperCallback, boolean)
     */
    private LocationHelperImpl(@NonNull Context context, @NonNull LocationHelperCallback locationHelperCallback, boolean needOneTimeUpdate) {
        super(context);
        mLocationHelperCallback = locationHelperCallback;
        mNeedOneTimeUpdate = needOneTimeUpdate;
        mUpdateCounter = 0;
    }

    /**
     * call inside {@link Activity#onCreate(Bundle)} or {@link Fragment#onActivityCreated(Bundle)}.
     *
     * @param context                context
     * @param locationHelperCallback {@link LocationHelperCallback} implemented by activity/fragment
     * @param needOneTimeUpdate      true if needs one time update. false for repeating updates
     * @return new instance of {@link LocationHelper} implementation
     * @see LocationHelper
     */
    @NonNull
    public static LocationHelper create(Context context, LocationHelperCallback locationHelperCallback, boolean needOneTimeUpdate) {
        return new LocationHelperImpl(context, locationHelperCallback, needOneTimeUpdate);
    }

    @Override
    protected GoogleApiClient.Builder provideApiClient() {
        GoogleApiClient.Builder builder = super.provideApiClient();
        builder.addApi(LocationServices.API);
        return builder;
    }

    //region public interface methods

    /**
     * call inside {@link LocationHelperCallback#onGoogleApiClientConnected()}.
     * {@inheritDoc}
     */
    @SuppressWarnings("MissingPermission")
    @Override
    @Nullable
    public Location getLastLocation() {
        Timber.d("getLastLocation() called");
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return null;
        }

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Timber.d("getLastLocation() returned: " + lastLocation);
        return lastLocation;
    }

    /**
     * call inside {@link Activity#onCreate(Bundle)} or {@link Fragment#onActivityCreated(Bundle)}.
     * {@inheritDoc}
     */
    @Override
    public void configureUpdateFrequency(long normalUpdateInterval, long fastestUpdateInterval, boolean highAccuracy) {
        Timber.d("configureUpdateFrequency() called with: normalUpdateInterval = [" + normalUpdateInterval + "], fastestUpdateInterval = [" + fastestUpdateInterval + "], highAccuracy = [" + highAccuracy + "]");
        NORMAL_UPDATE_INTERVAL = normalUpdateInterval;
        FASTEST_UPDATE_INTERVAL = fastestUpdateInterval;
        ACCURACY = highAccuracy ? LocationRequest.PRIORITY_HIGH_ACCURACY : LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    }

    /**
     * call inside {@link LocationHelperCallback#onGoogleApiClientConnected()}.
     * {@inheritDoc}
     */
    @Override
    public void startLocationUpdates() {
        Timber.d("startLocationUpdates() called ");
        if (!mGoogleApiClient.isConnected()) {
            throw new IllegalStateException("Google Api Client not connected. call inside onGoogleApiClientConnected()");
        }

        mLocationRequest = createLocationRequest();
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .setAlwaysShow(true)
                .build();
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locationSettingsRequest);
        result.setResultCallback(this);
    }

    /**
     * call inside {@link Activity#onStop()} or {@link Fragment#onStop()}.
     * {@inheritDoc}
     */
    @Override
    public void stopLocationUpdates() {
        Timber.d("stopLocationUpdates() called");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void removeCallbacks() {
        super.removeCallbacks();
        mLocationHelperCallback = null;
    }

    /**
     * call inside {@link Activity#onActivityResult}
     * {@inheritDoc}
     */
    @Override
    public void handleActivityResult(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_OK: {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    sendLastKnownLocation();
                    requestLocationUpdates();
                } else {
                    mLocationHelperCallback.onErrorGettingLocation(new Throwable("Google api clinet is either not connected or null"));
                }
                break;
            }
            case Activity.RESULT_CANCELED: {
                mLocationHelperCallback.onUserDeniedLocationAccess();
                break;
            }
        }
    }
    //endregion

    //region private internal usage methods
    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(NORMAL_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(ACCURACY);
        return mLocationRequest;
    }

    @SuppressWarnings("MissingPermission")
    private void sendLastKnownLocation() {
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        onLocationChanged(lastKnownLocation);
    }

    @SuppressWarnings("MissingPermission")
    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, LocationHelperImpl.this);
    }
    //endregion

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        Timber.d("onResult() called with: " + "locationSettingsResult = [" + locationSettingsResult.getStatus() + "]");
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    sendLastKnownLocation();
                    requestLocationUpdates();
                } else {
                    mLocationHelperCallback.onErrorGettingLocation(new Throwable("Google api clinet is either not connected or null"));
                }
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                mLocationHelperCallback.onResolutionRequired(status);
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                mLocationHelperCallback.onErrorGettingLocation(new Throwable("Settings Change Unavailable"));
                break;
        }
    }

    //region google api client connection callbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);
        mLocationHelperCallback.onGoogleApiClientConnected();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        super.onConnectionSuspended(cause);
        mLocationHelperCallback.onErrorGettingLocation(new Throwable("Connection Suspended. cause : " + cause));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        super.onConnectionFailed(connectionResult);
        mLocationHelperCallback.onErrorGettingLocation(new Throwable("Connection Failed. connectionResult : " + connectionResult.getErrorMessage()));
    }
    //endregion

    @Override
    public void onLocationChanged(Location newLocation) {
        Timber.d("onLocationChanged() called with: " + "location = [" + newLocation + "]");
        if (newLocation == null) return;
        //if only one time update is needed then stopping further location updates after dispatching first update
        if (mNeedOneTimeUpdate && mUpdateCounter >= 1) {
            stopLocationUpdates();
        } else {
            mLocationHelperCallback.onLocationUpdated(newLocation);
        }
        mUpdateCounter++;
    }

}

