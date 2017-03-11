package com.example.chist.testprojectmosru.Application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;


import com.facebook.FacebookSdk;
import com.vk.sdk.VKSdk;
import com.vk.sdk.util.VKUtil;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by 1 on 27.02.2017.
 */
public class LaunchApplication extends Application implements Application.ActivityLifecycleCallbacks, LocationListener{

    public static final String APP_PREFERENCES = "global_prefs";
    public static String PACKAGE_NAME;
    private static boolean isActivityVisible;
    private LocationHolder locationHolder;

    public boolean isActivityVisible() {
        return isActivityVisible;
    }

    // Activity public callbacks
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        isActivityVisible = true;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        isActivityVisible = false;
    }

    // Application callbacks
    @Override
    public void onCreate() {
        super.onCreate();
        PACKAGE_NAME = getPackageName();
        locationHolder = LocationHolder.getInstance(this);
        VKSdk.initialize(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        locationHolder.removeManagerUpdates(this);
    }

    public SharedPreferences getAppPrefs() {
        return getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
    }

    @Override
    public void onLocationChanged(Location location) {
        locationHolder.showLocation(this, location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        locationHolder.checkEnabled();
    }

    @Override
    public void onProviderEnabled(String provider) {
        locationHolder.checkEnabled();
        locationHolder.showLocation(this, provider);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            //Log.d("GPS","GPS  "+ String.valueOf(status));
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            //Log.d("GPS", "WIFI " +  String.valueOf(status));
        }
    }

    public void removeManagerUpdates(){

    }

    public boolean hasEmptyGPSInBase() {
        SharedPreferences prefs = getAppPrefs();
        return prefs.contains(LocationHolder.EMPTYGPS);
    }

    public void putFlagEmptyGPS() {
        SharedPreferences prefs = getAppPrefs();
        prefs.edit().putString(LocationHolder.EMPTYGPS, "anything text").commit();
        return;
    }

    // Dangerous high-load method. It's not recommended for use
    // Working only with network
    public String getCountryPlaceFromCoords(double MyLat, double MyLong) {
        String result = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(MyLat, MyLong, 1);
            result +=  addresses.get(0).getAddressLine(0) + "\n";
            result +=  addresses.get(0).getAddressLine(1) + "\n";
            result +=  addresses.get(0).getAddressLine(2);
        } catch (IOException e) {
            e.printStackTrace();
            result = "STRANGE PLACE";
        }
        return result;
    }
}
