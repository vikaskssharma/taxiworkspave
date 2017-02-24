package hertz.hertz.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import hertz.hertz.R;
import hertz.hertz.adapters.PlaceAutocompleteAdapter;
import hertz.hertz.customviews.DrawerArrowDrawable;
import hertz.hertz.fragments.ChatDialogFragment;
import hertz.hertz.fragments.DriverInfoDialogFragment;
import hertz.hertz.helpers.AppConstants;
import hertz.hertz.helpers.MapHelper;
import hertz.hertz.interfaces.OnCalculateDirectionListener;
import hertz.hertz.tasks.GetDirectionsAsyncTask;

/**
 * Created by rsbulanon on 11/13/15.
 */
public class HomeActivity extends BaseActivity implements OnMapReadyCallback,
                                                    NavigationView.OnNavigationItemSelectedListener,
                                                    OnCalculateDirectionListener,
                                                    GoogleApiClient.OnConnectionFailedListener {

    @Bind(R.id.drawerLayout) DrawerLayout drawerLayout;
    @Bind(R.id.drawerIndicator) ImageView drawerIndicator;
    @Bind(R.id.navDrawer) NavigationView navDrawer;
    @Bind(R.id.autoOrigin) AutoCompleteTextView autoOrigin;
    @Bind(R.id.autoDestination) AutoCompleteTextView autoDestination;
    @BindColor(R.color.metro_teal) int metro_teal;
    private static final LatLngBounds BOUNDS_NCR = new LatLngBounds(
            new LatLng(14.393448, 120.842743), new LatLng(14.766915, 121.135254));
    private GoogleApiClient googleApiClient;
    private DrawerArrowDrawable drawerArrowDrawable;
    private final Handler mDrawerActionHandler = new Handler();
    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private float offset;
    private boolean flipped;
    private GoogleMap map;
    private Place placeOrigin = null;
    private Place placeDesti = null;
    private PlaceAutocompleteAdapter mAdapter;
    private String selectedPlace;
    private boolean plotExisting = false;
    private boolean profilePicLoaded;
    private boolean isChatWindowOpen;
    private boolean isListeningToRoom;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        initDrawerArrowDrawable();
        initGoogleClient();
        if (AppConstants.FULL_NAME.isEmpty()) {
            AppConstants.FULL_NAME = ParseUser.getCurrentUser().getString("firstName") + " " +
                    ParseUser.getCurrentUser().getString("lastName");
        }
        Log.d("user", "on create --> " + AppConstants.FULL_NAME);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ParsePush.subscribeInBackground("Client", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("push", "successfully subscribed to the broadcast channel [Clients]");
                } else {
                    Log.e("push", "failed to subscribe for push", e);
                }
            }
        });
        ParsePush.subscribeInBackground("C" + ParseUser.getCurrentUser().getObjectId(), new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("push", "successfully subscribed to the broadcast channel [" + "C" + ParseUser.getCurrentUser().getObjectId() + "]");
                } else {
                    Log.e("push", "failed to subscribe for push", e);
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
                        if (status.equals("Attended")) {
                            showCustomProgress(AppConstants.LOAD_FETCH_DRIVER_INFO);
                            final String driverId = json.getJSONObject("json").getString("driverId");
                            ParseQuery<ParseUser> driver = ParseUser.getQuery();
                            driver.include("driver");
                            driver.include("driver.car");
                            driver.getInBackground(driverId, new GetCallback<ParseUser>() {
                                @Override
                                public void done(ParseUser driver, ParseException e) {
                                    dismissCustomProgress();
                                    if (e == null) {
                                        final DriverInfoDialogFragment fragment = DriverInfoDialogFragment
                                                .newInstance(driver.getParseObject("driver"));
                                        fragment.show(getSupportFragmentManager(),"driver");
                                    } else {
                                        showSweetDialog(e.getMessage(),"error");
                                    }
                                }
                            });
                        }
                    } else {
                        showChatWindow(data);
                    }
                } catch (JSONException e) {
                    Log.d("push","ERROR IN PARSING JSON --> " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        final LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        mgr.registerReceiver(broadcastReceiver, new IntentFilter("broadcast_action"));
    }

    private void initGoogleClient() {
        googleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .addOnConnectionFailedListener( this )
                .build();
        mAdapter = new PlaceAutocompleteAdapter(this, googleApiClient, BOUNDS_NCR, null);
        autoOrigin.setAdapter(mAdapter);
        autoOrigin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AutocompletePrediction item = mAdapter.getItem(position);
                final String placeId = item.getPlaceId();
                selectedPlace = "origin";
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        });

        autoDestination.setAdapter(mAdapter);
        autoDestination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AutocompletePrediction item = mAdapter.getItem(position);
                final String placeId = item.getPlaceId();
                selectedPlace = "destination";
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        });
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
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setScrollGesturesEnabled(true);
        final LatLng latLng = new LatLng( 14.696123, 121.117771);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    @OnClick(R.id.drawerIndicator)
    public void toggleDrawerMenu() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void plotDirection(LatLng origin, LatLng desti) {
        GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
        asyncTask.execute(findDirections(origin.latitude, origin.longitude,
                desti.latitude, desti.longitude, MapHelper.MODE_DRIVING));
    }

    public Map<String, String> findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode) {
        Log.d("map", "find direction");
        Map<String, String> map = new HashMap<>();
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);
        return map;
    }

    public PolylineOptions handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {
        PolylineOptions rectLine = new PolylineOptions().width(5).color(metro_teal);
        for(int i = 0 ; i < directionPoints.size() ; i++) {
            rectLine.add(directionPoints.get(i));
        }
        return rectLine;
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(menuItem.getItemId());
            }
        }, DRAWER_CLOSE_DELAY_MS);
        return true;
    }

    private void navigate(int menu) {
        switch (menu) {
            case R.id.navigation_item_1:
                startActivity(new Intent(this,ReservationsActivity.class));
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
                                startActivity(new Intent(HomeActivity.this, CLoginActivity.class));
                                animateToRight(HomeActivity.this);
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
            default:
        }
    }

    @Override
    public void onCalculationBegin() {
        Log.d("dir", "begin");
        showCustomProgress("Calculating direction, Please wait...");
    }

    @Override
    public void onCalculationFinished(ArrayList result) {
        Log.d("dir", "finished!");
        dismissCustomProgress();
        map.addPolyline(handleGetDirectionsResult(result));
    }

    @Override
    public void onCalculationException(Exception e) {
        dismissCustomProgress();
        showToast(e.toString());
        Log.d("dir", "on error --> " + e.toString());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void showPlaceMarker(Place place) {
        if (placeOrigin != null && placeDesti != null && plotExisting) {
            map.clear();
            if (selectedPlace.equals("origin")) {
                addMarker(placeDesti, BitmapDescriptorFactory.HUE_GREEN);
            } else {
                addMarker(placeOrigin, BitmapDescriptorFactory.HUE_RED);
            }
        }
        addMarker(place, (selectedPlace.equals("origin") ? BitmapDescriptorFactory.HUE_RED :
                BitmapDescriptorFactory.HUE_GREEN));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 10));
        map.animateCamera(CameraUpdateFactory.zoomTo(14));
        if (placeOrigin != null && placeDesti != null) {
            plotExisting = true;
            Log.d("plot","must plot direction --> " + placeOrigin.getLatLng().toString() + "  "
            + placeDesti.getLatLng().toString());
            plotDirection(placeOrigin.getLatLng(), placeDesti.getLatLng());
        } else {
            Log.d("plot", "not able to plot");
        }
    }

    private void addMarker(Place place, float color) {
        map.addMarker(new MarkerOptions().position(new LatLng(place.getLatLng().latitude,
                place.getLatLng().longitude)).title(place.getName().toString())
                .snippet(place.getAddress().toString()).icon(BitmapDescriptorFactory
                        .defaultMarker(color))).showInfoWindow();
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.d("places", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            if (selectedPlace.equals("origin")) {
                placeOrigin = places.get(0);
                showPlaceMarker(placeOrigin);
            } else {
                placeDesti = places.get(0);
                showPlaceMarker(placeDesti);
            }
            //places.release();
        }
    };

    @OnClick(R.id.btnBook)
    public void bookNow() {
        if (!isNetworkAvailable()) {
            showToast(AppConstants.ERR_CONNECTION);
        } else if (placeOrigin == null) {
            showSweetDialog("Please select your origin location","warning",false,null,null);
        } else if (placeDesti == null) {
            showSweetDialog("Please select your destination location","warning",false,null,null);
        } else {
            /** create booking in parse */
            /** create booking in parse */
            ParseGeoPoint origin = new ParseGeoPoint();
            origin.setLatitude(placeOrigin.getLatLng().latitude);
            origin.setLongitude(placeOrigin.getLatLng().longitude);

            getBooking().setOrigin(origin);
            getBooking().setUser(ParseUser.getCurrentUser());
            getBooking().setTo(placeDesti.getName().toString());
            getBooking().setFrom(placeOrigin.getName().toString());
            getBooking().setDestiLatitude(placeDesti.getLatLng().latitude);
            getBooking().setDestiLongitude(placeDesti.getLatLng().longitude);
            getBooking().setStatus("Pending");
            getBooking().setBookedBy(ParseUser.getCurrentUser().getString("firstName") + " " +
                    ParseUser.getCurrentUser().getString("lastName"));
            autoDestination.setText("");
            autoOrigin.setText("");
            placeDesti = null;
            placeOrigin = null;
            map.clear();
            startActivity(new Intent(HomeActivity.this, CReservationActivity.class));
            animateToLeft(HomeActivity.this);
        }
    }

    private void showChatWindow(final String data) {
        if (!isChatWindowOpen) {
            isChatWindowOpen = true;
            try {
                JSONObject obj = new JSONObject(data);
                final String room = obj.getJSONObject("json").getString("room");
                final String sender = obj.getJSONObject("json").getString("sender");
                final String senderName = obj.getJSONObject("json").getString("senderName");
                ChatDialogFragment chat = ChatDialogFragment.newInstance(room, sender, senderName);
                chat.setOnDismissListener(new ChatDialogFragment.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        isChatWindowOpen = false;
                        if (!isListeningToRoom) {
                            isListeningToRoom = true;
                            listenToRoom(room, data);
                        }
                    }
                });
                chat.show(getFragmentManager(), "chat");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("push", "error --> " + e.toString());
            }
        }
    }

    private void listenToRoom(final String room, final String data) {
        AppConstants.FIREBASE.child("Chat").child(room).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                showChatWindow(data);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
