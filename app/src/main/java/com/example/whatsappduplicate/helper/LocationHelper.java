package com.example.whatsappduplicate.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationCallbackListener listener;

    public interface LocationCallbackListener {
        void onLocationResult(Location location);
    }

    public LocationHelper(Context context) {
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = createLocationRequest();
        locationCallback = createLocationCallback();
    }

    private LocationRequest createLocationRequest() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000); // Update interval in milliseconds
        request.setFastestInterval(5000); // Fastest update interval in milliseconds
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return request;
    }

    private LocationCallback createLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    notifyLocationUpdate(location);
                }
            }
        };
    }

    public void startLocationUpdates(LocationCallbackListener listener) {
        this.listener = listener;

        // Check if the app has location permissions
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            // You should request permission from the user
            // Handle the permission request in your activity or fragment
            // You can use ActivityCompat.requestPermissions() to request permissions
        }
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void notifyLocationUpdate(Location location) {
        // Notify the registered listener when a new location is available.
        if (listener != null) {
            listener.onLocationResult(location);
        }
    }
}
