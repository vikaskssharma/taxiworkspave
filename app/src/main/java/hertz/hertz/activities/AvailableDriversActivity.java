package hertz.hertz.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.cocosw.bottomsheet.BottomSheet;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

import hertz.hertz.R;
import hertz.hertz.helpers.AppConstants;
import hertz.hertz.services.GPSTrackerService;

/**
 * Created by rsbulanon on 11/23/15.
 */
public class AvailableDriversActivity extends BaseActivity implements OnMapReadyCallback ,
                                                                        GoogleMap.OnMarkerClickListener {

    private GoogleMap googleMap;
    private LatLng latLng = new LatLng(0,0);
    private GeoQuery geoQuery;
    private HashMap<String,Marker> markers = new HashMap<>();
    private Circle circle;
    private Marker yourMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_drivers);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (!isGPSEnabled()) {
            enableGPS();
        }
        listenToChat();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, GPSTrackerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            GPSTrackerService.ServiceBinder binder = (GPSTrackerService.ServiceBinder) iBinder;
            if (AppConstants.GPS_TRACKER == null) {
                AppConstants.GPS_TRACKER = binder.getService();
                if (AppConstants.GPS_TRACKER.onTrackGPSListener == null) {
                    AppConstants.GPS_TRACKER.onTrackGPSListener = new GPSTrackerService.OnTrackGPSListener() {
                        @Override
                        public void onLocationChanged(double latitude, double longitude) {
                            Log.d("gps", "lat --> " + latitude + "  long --> " + longitude);
                            latLng = new LatLng(latitude,longitude);
                            if (googleMap != null) {
                                moveCamera(googleMap,latLng);
                            }
                        }

                        @Override
                        public void onGetLocationFailed(String provider) {
                            Log.d("gps","failed to get using ---> " + provider);
                        }

                        @Override
                        public void onGetLocationException(Exception e) {
                            Log.d("gps","Exception --> " + e.toString());
                        }
                    };
                    AppConstants.GPS_TRACKER.startGPSTracker();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        animateToLeft(this);
    }

    private void moveCamera(GoogleMap googleMap, LatLng latLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        if (yourMarker != null) {
            yourMarker.remove();
        }
        if (circle != null) {
            circle.remove();
            circle = null;
        }
        circle = googleMap.addCircle(drawMarkerWithCircle(5000, googleMap, latLng));
        yourMarker = addMapMarker(googleMap, latLng.latitude, latLng.longitude, "You're currently here", "", -1, true);
        if (geoQuery == null) {
            initGeoQuery();
        }
        geoQuery.setCenter(new GeoLocation(latLng.latitude, latLng.longitude));
        geoQuery.setRadius(5);
    }

    private void initGeoQuery() {
        geoQuery = AppConstants.GEOFIRE.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 5);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("geo", "key --> " + key + " location --> " + location.latitude + "," + location.longitude);
                if (key.contains("Avail")) {
                    markers.put(key, addMapMarker(googleMap, location.latitude, location.longitude,
                            key, "", R.drawable.car_marker, false));
                }
            }

            @Override
            public void onKeyExited(String key) {
                Log.d("geo", "umexit na");
                markers.remove(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("geo", "gumalaw key --> " + markers.get(key).getTitle());
                if (key.contains("Avail")) {
                    animateMarker(googleMap, markers.get(key), new LatLng(location.latitude, location.longitude), false);
                }
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("geo", "geo query ready!");
            }

            @Override
            public void onGeoQueryError(FirebaseError error) {
                Log.d("geo", "firebase error --> " + error.getMessage());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.d("marker", "marker clicked --> " + marker.getTitle());
        if (marker.getTitle().contains("Driver")) {
            marker.setTitle(marker.getTitle().replace("Avail", ""));
            marker.hideInfoWindow();
            new BottomSheet.Builder(this).title(marker.getTitle()).sheet(R.menu.menu_avail_driver)
                    .listener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case R.id.action_driver_info:
                                    Log.d("marker", "driver info");
                                    break;
                                case R.id.action_rent_car:
                                    break;
                            }
                        }
                    }).show();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.REQUEST_ENABLE_GPS) {
            if (resultCode == RESULT_CANCELED) {
                if (!isGPSEnabled()) {
                    enableGPS();
                } else {
                    if (AppConstants.GPS_TRACKER != null) {
                        AppConstants.GPS_TRACKER.startGPSTracker();
                        Log.d("gps", "RESULT OK ENABLED");
                    }
                }
            }
        }
    }
}
