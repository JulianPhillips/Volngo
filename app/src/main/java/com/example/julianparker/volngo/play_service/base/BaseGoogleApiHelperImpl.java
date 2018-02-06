package com.example.julianparker.volngo.play_service.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import timber.log.Timber;


/**
 * Created by agile on 12/8/16.
 * <p>
 * implementation of {@link BaseGoogleApiHelper}
 */
public class BaseGoogleApiHelperImpl implements BaseGoogleApiHelper, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final Context mContext;
    protected GoogleApiClient mGoogleApiClient;

    /**
     * call inside {@link Activity#onCreate(Bundle)} or {@link Fragment#onActivityCreated(Bundle)}.
     *
     * @param context context
     */
    protected BaseGoogleApiHelperImpl(@NonNull Context context) {
        mContext = context;
    }

    /**
     * derived classes will provide configuration for google api client builder
     *
     * @return builder configured according to needs of derived class
     */
    protected GoogleApiClient.Builder provideApiClient() {
        return new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this);
    }

    //region public interface methods

    /**
     * call inside {@link Activity#onStart()} or {@link Fragment#onStart()}.
     * {@inheritDoc}
     */
    @Override
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public void connectApiClient() {
        Timber.i("connectApiClient() called ");
        mGoogleApiClient = provideApiClient().build();
        mGoogleApiClient.connect();
    }


    /**
     * call inside {@link Activity#onStop()} or {@link Fragment#onStop()}.
     * {@inheritDoc}
     */
    @Override
    public void disconnectApiClient() {
        Timber.i("disconnectApiClient() called");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }


    @Nullable
    @Override
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * call inside {@link Activity#onDestroy()}  or {@link Fragment#onDestroyView()}.
     * {@inheritDoc}
     */
    @CallSuper
    @Override
    public void removeCallbacks() {
        Timber.i("removeCallbacks: ");
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnectionCallbacksRegistered(this))
                mGoogleApiClient.unregisterConnectionCallbacks(this);

            if (mGoogleApiClient.isConnectionFailedListenerRegistered(this))
                mGoogleApiClient.unregisterConnectionFailedListener(this);
        }
    }
    //endregion

    //region google api client connection callbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.i("onConnected() called with: " + "bundle = [" + bundle + "]");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Timber.i("onConnectionSuspended() called with: " + "cause = [" + cause + "]");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.i("onConnectionFailed() called with: " + "connectionResult = [" + connectionResult + "]");
    }
    //endregion


}
