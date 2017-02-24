package hertz.hertz.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by rsbulanon on 11/11/15.
 */
public class GPSTrackerService extends Service implements LocationListener {

    private final IBinder mBinder = new ServiceBinder();
    public OnTrackGPSListener onTrackGPSListener = null;

    /** flag for GPS status */
    private boolean isGPSEnabled = false;

    /** flag for network status */
    private boolean isNetworkEnabled = false;

    private Location location;
    private double latitude; //
    private double longitude; // longitude

    /** The minimum distance to change Updates in meters */
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 3; // 10 meters

    /** The minimum time between updates in milliseconds */
    //private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private static final long MIN_TIME_BW_UPDATES = 5000; // 1 minute

    /** Declaring a Location Manager */
    protected LocationManager locationManager;

    public GPSTrackerService() {}

    public Location getLocation() {
        Log.d("gps", "get location called");
        try {
            locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

            /** getting GPS status */
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            /** getting network status */
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                /** First get location from Network Provider */
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("gps", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            if (onTrackGPSListener != null) {
                                onTrackGPSListener.onLocationChanged(latitude,longitude);
                            }
                        } else {
                            if (onTrackGPSListener != null) {
                                onTrackGPSListener.onGetLocationFailed("network");
                            }
                        }
                    } else {
                        Log.d("gps","location manager is null");
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        if (locationManager != null) {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                locationManager.requestLocationUpdates(
                                        LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,
                                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            }
                        }
                        Log.d("gps", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                if (onTrackGPSListener != null) {
                                    onTrackGPSListener.onLocationChanged(latitude,longitude);
                                }
                            } else {
                                if (onTrackGPSListener != null) {
                                    onTrackGPSListener.onGetLocationFailed("gps");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("gps","error --> " + e.toString());
            if (onTrackGPSListener != null) {
                onTrackGPSListener.onGetLocationException(e);
            }
        }
        return location;
    }

    /** Stop using GPS listener */
    public void stopUsingGPS() {
        if(locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(GPSTrackerService.this);
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (onTrackGPSListener != null) {
            onTrackGPSListener.onLocationChanged(location.getLatitude(),location.getLongitude());
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public interface OnTrackGPSListener {
        void onLocationChanged(double latitude, double longitude);
        void onGetLocationFailed(String provider);
        void onGetLocationException(Exception e);
    }

    public class ServiceBinder extends Binder {
        public GPSTrackerService getService() {
            return GPSTrackerService.this;
        }
    }

    public void startGPSTracker() {
        Log.d("gps", "Start getting of GPS");
        getLocation();
    }
}
