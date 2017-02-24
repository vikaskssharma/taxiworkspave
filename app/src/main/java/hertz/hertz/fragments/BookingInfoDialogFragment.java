package hertz.hertz.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hertz.hertz.R;
import hertz.hertz.activities.BaseActivity;
import hertz.hertz.helpers.AppConstants;
import hertz.hertz.helpers.MapHelper;
import hertz.hertz.interfaces.OnCalculateDirectionListener;
import hertz.hertz.tasks.GetDirectionsAsyncTask;

/**
 * Created by rsbulanon on 11/17/15.
 */
public class BookingInfoDialogFragment extends DialogFragment implements OnCalculateDirectionListener {

    @Bind(R.id.tvDate) TextView tvDate;
    @Bind(R.id.tvTimer) TextView tvTimer;
    @Bind(R.id.tvCustomerName) TextView tvCustomerName;
    @Bind(R.id.tvHoursToRent) TextView tvHoursToRent;
    @Bind(R.id.tvDestination) TextView tvDestination;
    @Bind(R.id.tvDistance) TextView tvDistance;
    @Bind(R.id.llParent) LinearLayout llParent;
    private View view;
    private ParseObject booking;
    private BaseActivity activity;
    private OnAttendBookingListener onAttendBookingListener;
    private float distance;
    private LatLng currentLocation;

    public static BookingInfoDialogFragment newInstance(ParseObject booking, LatLng latLng) {
        final BookingInfoDialogFragment frag = new BookingInfoDialogFragment();
        frag.booking = booking;
        frag.currentLocation = latLng;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_booking_info, null);
        ButterKnife.bind(this, view);
        activity = (BaseActivity)getActivity();
        tvCustomerName.setText("Customer Name : " + booking.getParseObject("user").getString("firstName") + " "
        +booking.getParseObject("user").getString("lastName"));
        if (booking.getNumber("hoursToRent") == null) {
            tvHoursToRent.setVisibility(View.GONE);
        } else {
            tvHoursToRent.setText("Hours to Rent : " + booking.getNumber("hoursToRent").toString()
                    + (booking.getNumber("hoursToRent").intValue() == 1 ? " Hour" : " Hours"));
        }
        LatLng origin = new LatLng(currentLocation.latitude, currentLocation.longitude);
        LatLng desti = new LatLng(booking.getNumber("destiLatitude").doubleValue(),
                                    booking.getNumber("destiLongitude").doubleValue());
        plotDirection(origin,desti);
        llParent.measure(llParent.getWidth(),llParent.getHeight());
        Log.d("height","height ---> " + llParent.getMeasuredHeight());
        tvDestination.setText(booking.getString("from"));
        tvDate.setText(activity.getSDFWithTime().format(booking.getCreatedAt()));
        final Dialog mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
                                llParent.getMeasuredHeight());
        return mDialog;
    }

    @OnClick(R.id.btnAttend)
    public void attendClient() {
        if (activity.isNetworkAvailable()) {
            onAttendBookingListener.onAttend(booking.getObjectId());
        } else {
            activity.showToast(AppConstants.ERR_CONNECTION);
        }
    }

    public interface OnAttendBookingListener {
        void onAttend(String bookingId);
    }

    public void setOnAttendBookingListener(OnAttendBookingListener onAttendBookingListener) {
        this.onAttendBookingListener = onAttendBookingListener;
    }

    private void initCountDownTimer() {
        new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long remaining = millisUntilFinished / 1000;
                tvTimer.setText("Job closes in "+ remaining +" second" + (remaining == 1 ? "s" : ""));
            }

            @Override
            public void onFinish() {
                dismiss();
            }
        }.start();
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

    @Override
    public void onCalculationBegin() {
        Log.d("dir", "begin");
        activity.showCustomProgress("Computing approximate distance from client,Please wait...");
    }

    @Override
    public void onCalculationFinished(ArrayList result) {
        Log.d("dir", "finished!");
        activity.dismissCustomProgress();
        computeDistance(result);
    }

    @Override
    public void onCalculationException(Exception e) {

    }

    public void computeDistance(ArrayList<LatLng> directionPoints) {
        initCountDownTimer();

        for (int i = 0 ; i < directionPoints.size() ; i++) {
            Location loc1 = new Location("");
            Location loc2 = new Location("");

            if (i == directionPoints.size() - 1) {
                loc1.setLatitude(directionPoints.get(i-1).latitude);
                loc1.setLongitude(directionPoints.get(i-1).longitude);

                loc2.setLatitude(directionPoints.get(i).latitude);
                loc2.setLongitude(directionPoints.get(i).longitude);
            } else {
                loc1.setLatitude(directionPoints.get(i).latitude);
                loc1.setLongitude(directionPoints.get(i).longitude);

                loc2.setLatitude(directionPoints.get(i+1).latitude);
                loc2.setLongitude(directionPoints.get(i+1).longitude);
            }
            distance += loc1.distanceTo(loc2);
        }
        tvDistance.setText(String.format("%.2f", (distance/1000))+" KM");
    }
}
