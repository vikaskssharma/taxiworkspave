package hertz.hertz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.firebase.geofire.GeoLocation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hertz.hertz.R;
import hertz.hertz.adapters.ViewPagerAdapter;
import hertz.hertz.fragments.CarFragment;
import hertz.hertz.fragments.HoursOfRentalDialogFragment;
import hertz.hertz.helpers.AppConstants;

public class CReservationActivity extends BaseActivity {

    @Bind(R.id.rbPickUp) RadioButton rbPickUp;
    @Bind(R.id.rbCarHire) RadioButton rbCarHire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creservation);
        ButterKnife.bind(this);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Car");
        query.whereEqualTo("status", "available");
        query.orderByAscending("carModel");
        showCustomProgress(AppConstants.LOAD_FETCH_AVAILABLE_CARS);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                dismissCustomProgress();
                if (e == null) {
                    rbPickUp.setChecked(true);
                    for (ParseObject car : objects) {
                        if (car.getString("purpose").equals("For Hire")) {
                            getForHireCars().add(car);
                        } else {
                            getForPickUpCars().add(car);
                        }
                    }
                } else {
                    showSweetDialog(e.getMessage(), "error");
                }
            }
        });
    }

    @OnClick(R.id.btnContinue)
    public void continueBooking() {
        if (rbCarHire.isChecked()) {
            startActivity(new Intent(this, CarHireActivity.class));
            animateToLeft(this);
        } else {
            getBooking().setTxType("pickup");
            createBooking();
        }
    }

    private void createBooking() {
        showCustomProgress(AppConstants.LOAD_CREATING_BOOKING);
        getBooking().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dismissCustomProgress();
                if (e == null) {
                    showToast(AppConstants.OK_BOOKING_CREATED);
                    /** create booking in fire base */
                    AppConstants.GEOFIRE.setLocation(getBooking().getObjectId(),
                            new GeoLocation(getBooking().getOrigin().getLatitude(),
                                    getBooking().getOrigin().getLongitude()));
                    clearBooking();
                    startActivity(new Intent(CReservationActivity.this, AvailableDriversActivity.class));
                    animateToLeft(CReservationActivity.this);
                    finish();
                } else {
                    showSweetDialog(e.getMessage(), "error");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        animateToRight(this);
    }

    @OnClick(R.id.ivNavBack)
    public void close() {
        onBackPressed();
    }
}
