package hertz.hertz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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
public class ReservationsActivity extends BaseActivity {

    @Bind(R.id.rvReservations) RecyclerView rvReservations;
    private ArrayList<ParseObject> reservations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);
        ButterKnife.bind(this);
        MyReservationsAdapter adapter = new MyReservationsAdapter(this,reservations);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
        rvReservations.setAdapter(scaleAdapter);
        rvReservations.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnTxClickListener(new MyReservationsAdapter.OnTxClickListener() {
            @Override
            public void onSelected(ParseObject parseObject) {
                setAttendedBooking(parseObject);
                startActivity(new Intent(ReservationsActivity.this, MyBookingReviewActivity.class));
                animateToLeft(ReservationsActivity.this);
            }
        });
        loadBookings();
    }

    private void loadBookings() {
        showCustomProgress(AppConstants.LOAD_FETCH_BOOKING);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Booking");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        ArrayList<String> condition = new ArrayList<>();
        query.include("driver");
        query.include("driver.driver");
        condition.add("Pending");
        condition.add("Attended");
        query.whereContainedIn("status",condition);
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
