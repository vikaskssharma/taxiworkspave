package hertz.hertz.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseObject;
import com.rey.material.app.Dialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import hertz.hertz.R;
import hertz.hertz.customviews.CustomProgress;
import hertz.hertz.fragments.ChatDialogFragment;
import hertz.hertz.helpers.AppConstants;
import hertz.hertz.model.Booking;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by rsbulanon on 11/11/15.
 */
public class BaseActivity extends AppCompatActivity {

    private SweetAlertDialog sweetAlertDialog;
    private ProgressDialog pDialog;
    private CustomProgress customProgress;
    private static ArrayList<ParseObject> availableCars = new ArrayList<>();
    private static Booking booking = new Booking();
    private static ParseObject attendedBooking;
    private BroadcastReceiver broadcastReceiver;
    private boolean isChatWindowOpen;
    private boolean isListeningToRoom;
    private static ArrayList<ParseObject> forHireCars = new ArrayList<>();
    private static ArrayList<ParseObject> forPickUpCars = new ArrayList<>();

    public void listenToChat() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String data = intent.getStringExtra("com.parse.Data");
                try {
                    final JSONObject json = new JSONObject(data);
                    Log.d("push","BASE ON RECEIVED DATA! --> " + json);
                    showChatWindow(data);
                } catch (JSONException e) {
                    Log.d("push","ERROR IN PARSING JSON --> " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        final LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        mgr.registerReceiver(broadcastReceiver, new IntentFilter("broadcast_action"));
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

    private void showChatWindow(final String data) {
        if (!isChatWindowOpen) {
            isChatWindowOpen = true;
            try {
                Log.d("push","BASE --> chat window must open!");
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
                            listenToRoom(room,data);
                        }
                    }
                });
                chat.show(getFragmentManager(), "chat");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("push","BASE error --> " + e.toString());
            }
        }
    }

    public void showProgressDialog(String message) {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(message);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        pDialog.dismiss();
    }

    public void updateProgressDialog(String message) {
        pDialog.setMessage(message);
    }

    public void animateToRight(Activity activity) {
        activity.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    public void animateToLeft(Activity activity) {
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    public void animateToUp(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
    }

    public void animateToBottom(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
    }

    public void animateFadeIn(Activity activity) {
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void animateFadeOut(Activity activity) {
        activity.overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void setError(TextView textView, String errorMsg) {
        textView.setError(errorMsg);
        textView.requestFocus();
    }

    /**
     * check network connection availability
     */
    public boolean isNetworkAvailable() {
        boolean isConnected = false;
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            isConnected = true;
        } else {
            NetworkInfo mData = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mData == null) {
                isConnected = false;
            } else {
                boolean isDataEnabled = mData.isConnected();
                isConnected = isDataEnabled ? true : false;
            }
        }
        return isConnected;
    }

    /** check if GPS is enabled */
    public boolean isGPSEnabled() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        return true;
    }

    /** Date formatter */
    public SimpleDateFormat getDateFormatter() { return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); }

    /** get simple date formatter */
    public SimpleDateFormat getSDFWithTime() { return  new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm a"); }

    public SimpleDateFormat getSDF() { return  new SimpleDateFormat("EEE, MMM dd, yyyy"); }

    /** Decimal formatter */
    public DecimalFormat getDecimalFormatter() { return new DecimalFormat("###,###.00"); }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public void showSweetDialog(String message, String type) {
        int dialogType = 0;
        if (type.equals("error")) {
            dialogType = SweetAlertDialog.ERROR_TYPE;
        } else if (type.equals("warning")) {
            dialogType = SweetAlertDialog.WARNING_TYPE;
        } else if (type.equals("success")) {
            dialogType = SweetAlertDialog.SUCCESS_TYPE;
        }
        new SweetAlertDialog(this,dialogType)
                .setTitleText("Hertz")
                .setContentText(message)
                .setConfirmText("Close")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                }).show();
    }

    public void showSweetDialog(String message, String type, final boolean finish,
                                final Activity activity, final String direction) {
        int dialogType = 0;
        if (type.equals("error")) {
            dialogType = SweetAlertDialog.ERROR_TYPE;
        } else if (type.equals("warning")) {
            dialogType = SweetAlertDialog.WARNING_TYPE;
        } else if (type.equals("success")) {
            dialogType = SweetAlertDialog.SUCCESS_TYPE;
        }
        new SweetAlertDialog(this,dialogType)
                .setTitleText("Hertz")
                .setContentText(message)
                .setConfirmText("Close")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        if (finish) {
                            finish();
                            if (direction.equals("left")) {
                                animateToLeft(activity);
                            } else {
                                animateToRight(activity);
                            }
                        }
                    }
                }).show();
    }

    public void updateSweetProgressDialog(String message) {
        sweetAlertDialog.setContentText(message);
    }

    public void dismissSweetProgressDialog() {
        sweetAlertDialog.dismiss();
    }

    public String getText(TextView view) {
        return view.getText().toString();
    }

    public Marker addMapMarker(GoogleMap map, double lat, double longi, String title,
                             String snippet, int icon, boolean showInfo) {
        Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(lat, longi)).
                title(title).snippet(snippet).icon(
                icon == -1 ? BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                : icon == 1 ? BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                : BitmapDescriptorFactory.fromResource(icon)));
        if (showInfo) {
            marker.showInfoWindow();
        }
        return marker;
    }

    public void enableGPS() {
        final Dialog mDialog = new Dialog(this);
        mDialog.title("Enable GPS")
                .contentView(R.layout.dialog_enable_gps)
                .titleColor(ContextCompat.getColor(this, R.color.metro_yellow))
                .positiveAction("OK")
                .positiveActionTextColor(ContextCompat.getColor(this, R.color.metro_yellow))
                .positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        if (!enabled) {
                            mDialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, AppConstants.REQUEST_ENABLE_GPS);
                        }
                    }
                })
                .cancelable(false)
                .show();
    }

    public CircleOptions drawMarkerWithCircle(double radius, GoogleMap map, LatLng position) {
        return new CircleOptions().center(position)
                .radius(radius).fillColor(AppConstants.MAP_CIRCLE_SHADE_COLOR)
                .strokeColor(AppConstants.MAP_CIRCLE_STROKE_COLOR).strokeWidth(0);
    }

    public void showCustomProgress(String message) {
        if (customProgress == null) {
            customProgress = CustomProgress.newInstance(message);
            customProgress.setCancelable(false);
            customProgress.show(getSupportFragmentManager(),"load");
        }
    }

    public void updateCustomProgress(String message) {
        if (customProgress == null) {
            customProgress.updateMessage(message);
        }
    }

    public void dismissCustomProgress() {
        if (customProgress != null) {
            customProgress.dismiss();
            customProgress = null;
        }
    }

    public void animateMarker(GoogleMap map, final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    Log.d("geo", "animate");
                    handler.postDelayed(this, 16);
                } else {
                    marker.setVisible(!hideMarker);
                }
            }
        });
    }

    public ArrayList<ParseObject> getAvailableCars() {
        return availableCars;
    }

    public void setAvailableCars(ArrayList<ParseObject> availableCars) {
        this.availableCars = availableCars;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public Booking getBooking() {
        return booking;
    }

    public void clearBooking() {
        booking = null;
        booking = new Booking();
    }

    public static ParseObject getAttendedBooking() {
        return attendedBooking;
    }

    public static void setAttendedBooking(ParseObject attendedBooking) {
        BaseActivity.attendedBooking = attendedBooking;
    }

    public ArrayList<ParseObject> getForHireCars() {
        return forHireCars;
    }

    public ArrayList<ParseObject> getForPickUpCars() {
        return forPickUpCars;
    }

    public ArrayList<ParseObject> getForHireCars(final String type) {
        ArrayList<ParseObject> cars = new ArrayList<>();
        for (ParseObject o : forHireCars) {
            if (o.getString("type").equals(type)) {
                cars.add(o);
            }
        }
        return cars;
    }
}
