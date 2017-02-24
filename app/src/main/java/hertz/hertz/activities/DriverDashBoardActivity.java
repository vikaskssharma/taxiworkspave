package hertz.hertz.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import hertz.hertz.R;
import hertz.hertz.customviews.DrawerArrowDrawable;
import hertz.hertz.fragments.BookingInfoDialogFragment;
import hertz.hertz.helpers.AppConstants;
import hertz.hertz.services.GPSTrackerService;

/**
 * Created by rsbulanon on 11/17/15.
 */
public class DriverDashBoardActivity extends BaseActivity implements OnMapReadyCallback ,
                                                                    GoogleMap.OnMarkerClickListener,
                                                                    NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.drawerLayout) DrawerLayout drawerLayout;
    @Bind(R.id.drawerIndicator) ImageView drawerIndicator;
    @Bind(R.id.navDrawer) NavigationView navDrawer;
    private GoogleMap googleMap;
    private LatLng latLng = new LatLng(0,0);
    private GeoQuery geoQuery;
    private HashMap<String,Marker> markers = new HashMap<>();
    private Circle circle;
    private Marker yourMarker;
    private BroadcastReceiver broadcastReceiver;
    private DrawerArrowDrawable drawerArrowDrawable;
    private final Handler mDrawerActionHandler = new Handler();
    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private float offset;
    private boolean flipped;
    private boolean profilePicLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);
        ButterKnife.bind(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initDrawerArrowDrawable();
        listenToChat();
        ParsePush.subscribeInBackground("Drivers", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("parse", "successfully subscribed to the broadcast channel [Drivers]");
                } else {
                    Log.e("parse", "failed to subscribe for push", e);
                }
            }
        });
        ParsePush.subscribeInBackground("D" + ParseUser.getCurrentUser().getObjectId(), new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("parse", "successfully subscribed to the broadcast channel [" + "D" + ParseUser.getCurrentUser().getObjectId() + "]");
                } else {
                    Log.e("parse", "failed to subscribe for push", e);
                }
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String data = intent.getStringExtra("com.parse.Data");
                try {
                    final JSONObject json = new JSONObject(data);
                    if (json.has("bookingStatus")) {
                        final String status = json.getJSONObject("json").getString("bookingStatus");
                        final String bookingId = json.getJSONObject("json").getString("bookingId");
                        final double latitude = json.getJSONObject("json").getDouble("latitude");
                        final double longitude = json.getJSONObject("json").getDouble("longitude");

                        if (status.equals("Pending")) {
                            showBookingMarker(bookingId, latitude, longitude);
                        } else {
                            if (markers.containsKey(bookingId)) {
                                markers.get(bookingId).remove();
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.d("push","ERROR IN PARSING JSON --> " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        final LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        mgr.registerReceiver(broadcastReceiver, new IntentFilter("broadcast_action"));

        if (!isGPSEnabled()) {
            enableGPS();
        }
    }

    private void fetchAvailableBooking() {
        Log.d("gps", "FETCH BOOKINGS");
        showCustomProgress(AppConstants.LOAD_FETCH_NEARBY_BOOKING);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Booking");
        query.whereEqualTo("status","Pending");
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                dismissCustomProgress();
                if (e == null) {
                    Log.d("gps", "BOOKING SIZE --> " + objects.size());
                    for (ParseObject o : objects) {
                        showBookingMarker(o.getObjectId(),o.getParseGeoPoint("origin").getLatitude(),
                                o.getParseGeoPoint("origin").getLongitude());
                    }
                } else {
                    Log.d("gps","failed to fetch nearby booking --> " + e.getMessage());
                    showToast(e.getMessage());
                }
            }
        });
    }

    private void showBookingMarker(String bookingId, double latitude, double longitude) {
        final Location bookingLocation = new Location("");
        bookingLocation.setLatitude(latitude);
        bookingLocation.setLongitude(longitude);

        final Location currentLocation = new Location("");
        currentLocation.setLatitude(latLng.latitude);
        currentLocation.setLongitude(latLng.longitude);

        final float distanceInMeters = currentLocation.distanceTo(bookingLocation);
        final float distanceInKiloMeter = distanceInMeters / 1000;

        if (distanceInKiloMeter > 0) {
            markers.put(bookingId, addMapMarker(googleMap, latitude, longitude, "Booking Id : " + bookingId, "", 1, false));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, GPSTrackerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        Log.d("push", "on pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("broadcast_action");
        this.registerReceiver(broadcastReceiver, filter);
        Log.d("push", "on resume");
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            GPSTrackerService.ServiceBinder binder = (GPSTrackerService.ServiceBinder) iBinder;
            if (AppConstants.GPS_TRACKER == null) {
                AppConstants.GPS_TRACKER = binder.getService();
                if (AppConstants.GPS_TRACKER.onTrackGPSListener == null) {
                    Log.d("gps","on service connected");
                    fetchAvailableBooking();
                    AppConstants.GPS_TRACKER.onTrackGPSListener = new GPSTrackerService.OnTrackGPSListener() {
                        @Override
                        public void onLocationChanged(double latitude, double longitude) {
                            Log.d("gps", "lat --> " + latitude + "  long --> " + longitude);
                            latLng = new LatLng(latitude,longitude);
                            if (googleMap != null) {
                                Log.d("gps","move camera");
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
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        googleMap.setOnMarkerClickListener(this);
        Log.d("gps", "map ready!");
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
                        fetchAvailableBooking();
                        Log.d("gps", "RESULT OK ENABLED");
                    }
                }
            }
        }
    }

    /** map marker listener */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!isNetworkAvailable()) {
            showToast(AppConstants.ERR_CONNECTION);
        } else {
            if (marker.getTitle().contains("Booking Id")) {
                final ParseQuery<ParseObject> query = ParseQuery.getQuery("Booking");
                query.include("user");
                showProgressDialog(AppConstants.LOAD_BOOKING_INFO);
                final String bookingId = marker.getTitle().replace("Booking Id : ","");
                query.getInBackground(bookingId, new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        dismissProgressDialog();
                        if (e == null) {
                            final BookingInfoDialogFragment fragment = BookingInfoDialogFragment.newInstance(object,latLng);
                            fragment.setOnAttendBookingListener(new BookingInfoDialogFragment.OnAttendBookingListener() {
                                @Override
                                public void onAttend(final String bookingId) {
                                    showCustomProgress(AppConstants.LOAD_CHECKING_BOOKING_STATUS);
                                    HashMap<String,String> params = new HashMap<>();
                                    params.put("id", bookingId);
                                    ParseCloud.callFunctionInBackground("checkBookingStatus", params,
                                            new FunctionCallback<ParseObject>() {
                                                @Override
                                                public void done(final ParseObject booking, ParseException e) {
                                                    updateCustomProgress(AppConstants.LOAD_ATTEND_BOOKING);
                                                    if (e == null) {
                                                        ParseUser.getCurrentUser().getParseObject("driver")
                                                                .fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                                                    @Override
                                                                    public void done(ParseObject driver, ParseException e) {
                                                                        booking.put("driver", ParseUser.getCurrentUser());
                                                                        booking.put("status", "Attended");
                                                                        booking.put("driverName", driver.getString("firstName")
                                                                                + " " + driver.getString("lastName"));
                                                                        booking.saveInBackground(new SaveCallback() {
                                                                            @Override
                                                                            public void done(ParseException e) {
                                                                                dismissCustomProgress();
                                                                                if (e == null) {
                                                                                    //AppConstants.FIREBASE.child(bookingId).removeValue();
                                                                                    showToast(AppConstants.OK_BOOKING_ATTENDED);
                                                                                    showAttendedBookingWindow(booking);
                                                                                } else {
                                                                                    showSweetDialog(e.getMessage(), "error");
                                                                                }
                                                                                fragment.dismiss();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                    } else {
                                                        dismissCustomProgress();
                                                        showSweetDialog(e.getMessage(), "error");
                                                        markers.get(bookingId).remove();
                                                    }
                                                }
                                            });
                                }
                            });
                            fragment.show(getFragmentManager(),"booking");
                        } else {
                            Log.d("err","failed to get booking info --> " + e.getMessage());
                            showToast(AppConstants.ERR_GET_BOOKING_INFO);
                        }
                    }
                });
                Log.d("gps", "marker --> " + marker.getTitle());
            }
        }
        return true;
    }

    private void showAttendedBookingWindow(ParseObject booking) {
        setAttendedBooking(booking);
        startActivity(new Intent(this, AttendedBookingActivity.class));
        finish();
    }

    private void initDrawerArrowDrawable() {
        drawerArrowDrawable = new DrawerArrowDrawable(getResources());
        drawerArrowDrawable.setStrokeColor(Color.WHITE);
        drawerIndicator.setImageDrawable(drawerArrowDrawable);
        drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                offset = slideOffset;
                // Sometimes slideOffset ends up so close to but not quite 1 or 0.
                if (slideOffset >= .995) {
                    flipped = true;
                    drawerArrowDrawable.setFlip(flipped);
                } else if (slideOffset <= .005) {
                    flipped = false;
                    drawerArrowDrawable.setFlip(flipped);
                }
                drawerArrowDrawable.setParameter(offset);
            }
        });

        View view = navDrawer.inflateHeaderView(R.layout.drawer_header);
        TextView tvFullName = (TextView)view.findViewById(R.id.tvFullName);
        if (AppConstants.FULL_NAME.isEmpty()) {
            AppConstants.FULL_NAME = ParseUser.getCurrentUser().getString("firstName") + " " +
                    ParseUser.getCurrentUser().getString("lastName");
        }
        if (ParseUser.getCurrentUser().getParseFile("profilePic") != null && !profilePicLoaded) {
            Log.d("profilePic","must load profile pic");
            final CircleImageView ivProfilePic = (CircleImageView)view.findViewById(R.id.ivProfilePic);
            final ProgressBar pbLoadImage = (ProgressBar)view.findViewById(R.id.pbLoadImage);
            ImageLoader.getInstance().loadImage(ParseUser.getCurrentUser().getParseFile("profilePic").getUrl(),
                    new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            pbLoadImage.setVisibility(View.VISIBLE);
                            Log.d("profilePic", "start loading");
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            profilePicLoaded = true;
                            pbLoadImage.setVisibility(View.GONE);
                            ivProfilePic.setImageBitmap(loadedImage);
                            Log.d("profilePic", "loading completed");
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
        } else {
            Log.d("profilePic","not loading anything");
        }

        tvFullName.setText(AppConstants.FULL_NAME);
        navDrawer.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(item.getItemId());
            }
        }, DRAWER_CLOSE_DELAY_MS);
        return true;
    }

    @OnClick(R.id.drawerIndicator)
    public void toggleDrawerMenu() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void navigate(int menu) {
        switch (menu) {
            case R.id.navigation_item_1:
                startActivity(new Intent(this,MyBookingsActivity.class));
                animateToLeft(this);
                break;
            case R.id.navigation_item_5:
                new SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Hertz")
                        .setContentText("Are you sure you want to logout from the app?")
                        .setConfirmText("Yes")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                ParseUser.logOut();
                                startActivity(new Intent(DriverDashBoardActivity.this, CLoginActivity.class));
                                animateToRight(DriverDashBoardActivity.this);
                                finish();
                            }
                        })
                        .setCancelText("No")
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
                break;
            default:        }
    }
}
