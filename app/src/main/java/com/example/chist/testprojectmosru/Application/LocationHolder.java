package com.example.chist.testprojectmosru.Application;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

/**
 * Created by chist on 3/10/17.
 */

public class LocationHolder {
    private double lastX;
    private double lastY;
    private LocationManager locationManager;
    private long lastTimeUpdate;
    private boolean isWiFiEnabled = false;
    private boolean isGpsEnabled = false;
    private static LocationHolder INSTANCE;
    public static final int validDeltaTime = 60000; // время в течении которого будут подтягиваться gps координаты
    public static final String EMPTYGPS = "emptygps";
    public static String onGPSUpdate = null;

    public boolean isWiFiEnabled() {
        return isWiFiEnabled;
    }

    public boolean isGpsEnabled() {
        return isGpsEnabled;
    }

    public double getLastX() {
        return lastX;
    }

    public double getLastY() {
        return lastY;
    }

    public long getLastTimeUpdate() {
        return lastTimeUpdate;
    }

    private LocationHolder(LaunchApplication app) {
        if (INSTANCE != null) {
            throw new IllegalAccessError("You couldn't create second instance of Singleton");
        }
        initLocationManager(app);
    }

    public static LocationHolder getInstance(@Nullable LaunchApplication app) {
        if (INSTANCE == null) {
            INSTANCE = new LocationHolder(app);
        }
        return INSTANCE;
    }

    private void initLocationManager(LaunchApplication context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, context);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                context);
        checkEnabled();
    }

    public void checkEnabled() {
        isGpsEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        isWiFiEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void removeManagerUpdates(LocationListener listener) {
        locationManager.removeUpdates(listener);
    }

    public void showLocation(Context context, Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            updateLocation(context, location);
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            updateLocation(context, location);
        }
    }

    public void showLocation(Context context, String provider) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        showLocation(context, locationManager.getLastKnownLocation(provider));
    }

    public void updateLocation(Context context, Location location) {
        if (location == null)
            return;
        lastX = location.getLatitude();
        lastY = location.getLongitude();
        lastTimeUpdate = location.getTime();
        context.getContentResolver().notifyChange(Utils.getGeoDataUriAdapter(context), null);
    }
}
