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
public class LaunchApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public static final String APP_PREFERENCES = "global_prefs";
    public static final String EMPTYGPS = "emptygps";
    public static int validDeltaTime = 60000; // время в течении которого будут подтягиваться gps координаты

    private static boolean isActivityVisible;

    private boolean isWiFiEnabled = false;
    private boolean isGpsEnabled = false;
    private double lastX;
    private double lastY;
    private long lasttimeUpdate;
    private  LocationListener locationListener;
    private   LocationManager locationManager;
    public String onGPSUpdate = null;

    public double getLastX() { return lastX; }
    public double getLastY() { return lastY; }
    public long   getLasttimeUpdate() { return lasttimeUpdate; }
    public boolean isWiFiEnabled() { return isWiFiEnabled;}
    public boolean isGpsEnabled() { return isGpsEnabled; }
    public boolean isActivityVisible() {
        return isActivityVisible;
    }

    {
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                showLocation(location);
            }

            @Override
            public void onProviderDisabled(String provider) {
                checkEnabled();
            }

            @Override
            public void onProviderEnabled(String provider) {
                checkEnabled();
                showLocation(locationManager.getLastKnownLocation(provider));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    //Log.d("GPS","GPS  "+ String.valueOf(status));
                } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                    //Log.d("GPS", "WIFI " +  String.valueOf(status));
                }
            }
        };
    }

    private static LaunchApplication _instance;
    {
        _instance = this;
    }
    public static LaunchApplication getInstance() {
        return _instance;
    }

    // Activity public callbacks
    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivityResumed(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override public void onActivityDestroyed(Activity activity) {}

    @Override public void onActivityStarted(Activity activity) {
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
        VKSdk.initialize(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
        checkEnabled();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        locationManager.removeUpdates(locationListener);
    }

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            updateLocation(location);
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            updateLocation(location);
        }
    }

    private void updateLocation(Location location) {
        if (location == null)
            return ;
        lastX =  location.getLatitude();
        lastY  = location.getLongitude();
        lasttimeUpdate = location.getTime();
        this.getContentResolver().notifyChange(Utils.getGeoDataUriAdapter(),null);
    }

    private void checkEnabled() {
        isGpsEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        isWiFiEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static SharedPreferences getAppPrefs() {
        return getInstance().getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
    }


    public static boolean hasEmptyGPSInBase() {
        SharedPreferences prefs = getAppPrefs();
            return prefs.contains(EMPTYGPS);
    }

    public static void putFlagEmptyGPS() {
        SharedPreferences prefs = getAppPrefs();
        prefs.edit().putString(EMPTYGPS, "anything text").commit();
        return;
    }

    // Dangerous high-load method. It's not recommended for use
    // Working only with network
    public static String getCountryPlaceFromCoords(double MyLat, double MyLong) {
        String result = "";
        Geocoder geocoder = new Geocoder(_instance, Locale.getDefault());
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
