package hertz.hertz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import hertz.hertz.R;
import hertz.hertz.adapters.MyReservationsAdapter;
import hertz.hertz.helpers.AppConstants;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

/**
 * Created by rsbulanon on 12/26/15.
 */
public class MyBookingsActivity extends BaseActivity {

    @Bind(R.id.tvHeader) TextView tvHeader;
    @Bind(R.id.rvReservations) RecyclerView rvReservations;
    private ArrayList<ParseObject> reservations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);
        ButterKnife.bind(this);
        tvHeader.setText("Your Bookings");
        listenToChat();
        MyReservationsAdapter adapter = new MyReservationsAdapter(this,reservations);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
        rvReservations.setAdapter(scaleAdapter);
        rvReservations.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnTxClickListener(new MyReservationsAdapter.OnTxClickListener() {
            @Override
            public void onSelected(ParseObject parseObject) {
                setAttendedBooking(parseObject);
                startActivity(new Intent(MyBookingsActivity.this, AttendedBookingActivity.class));
                animateToLeft(MyBookingsActivity.this);
            }
        });
        loadBookings();
    }

    private void loadBookings() {
        showCustomProgress(AppConstants.LOAD_FETCH_BOOKING);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Booking");
        query.whereEqualTo("driver", ParseUser.getCurrentUser());
        query.whereEqualTo("status","Attended");
        query.include("user");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                dismissCustomProgress();
                if (e == null) {
                    reservations.clear();
                    reservations.addAll(objects);
                    rvReservations.getAdapter().notifyDataSetChanged();
                } else {
                    showSweetDialog(e.getMessage(),"error");
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
}
