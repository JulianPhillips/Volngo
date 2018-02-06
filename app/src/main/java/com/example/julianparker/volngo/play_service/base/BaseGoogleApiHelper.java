package com.example.julianparker.volngo.play_service.base;

import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by agile on 12/8/16.
 * <p>
 * base class for all google api client related helpers
 */
public interface BaseGoogleApiHelper {

    /**
     * builds and connects google api client
     */
    void connectApiClient();

    @Nullable
    GoogleApiClient getGoogleApiClient();

    /**
     * disconnects google api client and removes connection callbacks<br/>
     */
    void disconnectApiClient();

    /**
     * remove callbacks and listeners
     * Do not forget to call this to avoid leaks
     */
    void removeCallbacks();


}
