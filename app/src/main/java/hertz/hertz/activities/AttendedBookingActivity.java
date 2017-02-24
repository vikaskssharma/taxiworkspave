package hertz.hertz.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import hertz.hertz.R;
import hertz.hertz.fragments.ChatDialogFragment;
import hertz.hertz.helpers.MapHelper;
import hertz.hertz.interfaces.OnCalculateDirectionListener;
import hertz.hertz.tasks.GetDirectionsAsyncTask;

/**
 * Created by rsbulanon on 11/13/15.
 */
public class AttendedBookingActivity extends BaseActivity implements OnMapReadyCallback,
                                                    OnCalculateDirectionListener,
                                                    GoogleApiClient.OnConnectionFailedListener {

    @Bind(R.id.tvFullName) TextView tvFullName;
    @Bind(R.id.tvContactNo) TextView tvContactNo;
    @Bind(R.id.ivProfilePic) CircleImageView ivProfilePic;
    @Bind(R.id.pbLoadImage) ProgressBar pbLoadImage;
    @BindColor(R.color.metro_teal) int metro_teal;
    private GoogleApiClient googleApiClient;
    private GoogleMap map;
    private boolean profilePicLoaded;
    private LatLng origin;
    private LatLng desti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attended_booking);
        ButterKnife.bind(this);
        initGoogleClient();
        listenToChat();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final ParseObject booking = getAttendedBooking();
        if (booking.getParseObject("user").getString("mobileNo") != null) {
            tvContactNo.setText(getAttendedBooking().getParseObject("user").getString("mobileNo"));
        }
        tvFullName.setText(booking.getParseObject("user").getString("firstName") + " " +
                            booking.getParseObject("user").getString("lastName"));
        if (booking.getParseObject("user").getParseFile("profilePic") != null && !profilePicLoaded) {
            Log.d("profilePic","must load profile pic");
            ImageLoader.getInstance().loadImage(booking.getParseObject("user").getParseFile("profilePic").getUrl(),
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
        origin = new LatLng(booking.getParseGeoPoint("origin").getLatitude(), booking.getParseGeoPoint("origin").getLongitude());
        desti = new LatLng(booking.getNumber("destiLatitude").doubleValue(), booking.getNumber("destiLongitude").doubleValue());
        plotDirection(origin,desti);
    }

    private void initGoogleClient() {
        googleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .addOnConnectionFailedListener( this )
                .build();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLng(desti));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));
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
    public void onCalculationBegin() {
        Log.d("dir", "begin");
        showCustomProgress("Calculating direction, Please wait...");
    }

    @Override
    public void onCalculationFinished(ArrayList result) {
        Log.d("dir", "finished!");
        dismissCustomProgress();
        addMarker(origin, getBooking().getString("from"), BitmapDescriptorFactory.HUE_GREEN);
        addMarker(desti, getBooking().getString("to"), BitmapDescriptorFactory.HUE_RED);
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

    private void addMarker(LatLng latLng, String title, float color) {
        map.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude))
                .title(title).snippet("").icon(BitmapDescriptorFactory.defaultMarker(color)))
                .showInfoWindow();
    }

    @OnClick(R.id.btnCall)
    public void dialClient() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + tvContactNo.getText().toString()));
        startActivity(intent);
    }

    @OnClick(R.id.btnChat)
    public void sendChat() {
        final ChatDialogFragment fragment = ChatDialogFragment
                .newInstance(getAttendedBooking().getObjectId(),
                        "C"+getAttendedBooking().getParseUser("user").getObjectId(),
                        tvFullName.getText().toString());
        fragment.setOnDismissListener(new ChatDialogFragment.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
        fragment.show(getFragmentManager(),"chat");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        animateToRight(this);
    }
}
