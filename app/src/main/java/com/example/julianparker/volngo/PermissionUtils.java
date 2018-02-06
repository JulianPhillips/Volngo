package com.example.julianparker.volngo;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by agile-03 on 1/25/2016.
 * <p>
 * Usage :
 * To check whether permission is granted or not  :
 * call <pre>{@link #checkMissingPermission}</pre>
 * <p>
 * To check whether user denied or accepted asked permission :
 * call <pre>{@link #checkGrantResults}</pre> in onRequestPermissionResult()
 */
public class PermissionUtils {


    /**
     * checks whether given permissions are granted or not. Requests non-granted permission to user.
     *
     * @param fragment       this method is only for using in fragments. pass reference to fragment here
     * @param permissionCode unique constant to check later in {@link Fragment#onRequestPermissionsResult}
     * @param permissions    pass multiple permissions here
     * @return true if all permissions are granted, false otherwise.
     */
    public boolean checkMissingPermission(Fragment fragment, int permissionCode, String... permissions) {
        List<String> revokedPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //iterating over permission, if not granted then add to our list and ask permission to user
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(fragment.getActivity(), permission) == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Permission " + permission + " is granted");
                } else {
                    Timber.i("Permission " + permission + " is revoked");
                    revokedPermissions.add(permission); //to ask only non-granted permissions
                }
            }
            //if all permissions are granted, list of revoked permissions will be empty
            if (revokedPermissions.isEmpty()) {
                return true;
            } else {
                fragment.requestPermissions(revokedPermissions.toArray(new String[revokedPermissions.size()]), permissionCode);
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * checks whether given permissions are granted or not. Requests non-granted permission to user.
     *
     * @param activity       this method is only for using in activity. pass reference to activity here
     * @param permissionCode unique constant to check later in {@link AppCompatActivity#onRequestPermissionsResult}
     * @param permissions    pass multiple permissions here
     * @return true if all permissions are granted, false otherwise.
     */
    public boolean checkMissingPermission(AppCompatActivity activity, int permissionCode, String... permissions) {
        List<String> revokedPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //iterating over permission, if not granted then add to our list and ask permission to user
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Permission " + permission + " is granted");
                } else {
                    Timber.i("Permission " + permission + " is revoked");
                    revokedPermissions.add(permission); //to ask only non-granted permissions
                }
            }
            //if all permissions are granted, list of revoked permissions will be empty
            if (revokedPermissions.isEmpty()) {
                return true;
            } else {
                activity.requestPermissions(revokedPermissions.toArray(new String[revokedPermissions.size()]), permissionCode);
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * this methods checks grant result. i.e whether user pressed allow or deny.
     * Must be called in  onRequestPermissionsResult()
     *
     * @param grantResults grant results got in onRequestPermissionsResult
     * @return true if user pressed allow for all permissions, false otherwise
     */
    public boolean checkGrantResults(@NonNull int[] grantResults) {
        boolean isGranted = true;
        for (int grantResult : grantResults) { //iterate though grant results for asked permissions
            if (grantResult != PackageManager.PERMISSION_GRANTED) { //if any asked permission is not granted
                isGranted = false;
                break;
            }
        }
        Timber.i("checkGrantResults() returned: " + isGranted);
        return isGranted;
    }
}
