package hertz.hertz.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import butterknife.OnClick;
import hertz.hertz.R;
import hertz.hertz.adapters.DriversAdapter;
import hertz.hertz.fragments.AddDriverDialogFragment;
import hertz.hertz.helpers.AppConstants;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

/**
 * Created by rsbulanon on 12/30/15.
 */
public class DriverManagementActivity extends BaseActivity {

    @Bind(R.id.rvDrivers) RecyclerView rvDrivers;
    @Bind(R.id.swipeRefresh) SwipeRefreshLayout swipeRefresh;
    private ArrayList<ParseObject> drivers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_management);
        ButterKnife.bind(this);
        DriversAdapter adapter = new DriversAdapter(this,drivers);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
        rvDrivers.setAdapter(scaleAdapter);
        rvDrivers.setLayoutManager(new LinearLayoutManager(this));
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(false);
            }
        });
        getDrivers();
    }

    public void getDrivers() {
        showCustomProgress(AppConstants.LOAD_FETCH_ALL_DRIVERS);
        final ParseQuery<ParseObject> status = ParseQuery.getQuery("Driver");
        status.whereEqualTo("status", "active");

        final ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereMatchesQuery("driver", status);
        query.whereEqualTo("userRole", "driver");
        query.include("driver");
        query.include("car");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> dri, ParseException e) {
                if (e == null) {
                    ParseQuery<ParseObject> car = ParseQuery.getQuery("Car");
                    car.orderByAscending("carModel");
                    car.whereEqualTo("markedAsDeleted", false);
                    car.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                dismissCustomProgress();
                                getAvailableCars().clear();
                                getAvailableCars().addAll(objects);
                                drivers.clear();
                                drivers.addAll(dri);
                                rvDrivers.getAdapter().notifyDataSetChanged();
                            }
                        }
                    });
                } else {
                    showSweetDialog(e.getMessage(), "error");
                }
            }
        });
    }

    @OnClick(R.id.btnAddDriver)
    public void addDriver() {
        final AddDriverDialogFragment fragment = AddDriverDialogFragment.newInstance(null);
        fragment.setOnAddDriverListener(new AddDriverDialogFragment.OnAddDriverListener() {

            @Override
            public void onAddUpdateStart() {
                showCustomProgress(AppConstants.LOAD_CREATE_DRIVER);
            }

            @Override
            public void onNewDriverAdded(final ParseUser newDriver) {
                ParseQuery<ParseObject> car = ParseQuery.getQuery("Car");
                car.orderByAscending("carModel");
                car.whereEqualTo("markedAsDeleted", false);
                car.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            getAvailableCars().clear();
                            getAvailableCars().addAll(objects);
                            dismissCustomProgress();
                            fragment.dismiss();
                            drivers.add(0, newDriver);
                            rvDrivers.getAdapter().notifyDataSetChanged();
                            showToast(AppConstants.OK_NEW_DRIVER_ADDED);
                        }
                    }
                });
            }

            @Override
            public void onDriverRecordUpdated() {
                ParseQuery<ParseObject> car = ParseQuery.getQuery("Car");
                car.orderByAscending("carModel");
                car.whereEqualTo("markedAsDeleted", false);
                car.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            getAvailableCars().clear();
                            getAvailableCars().addAll(objects);
                            dismissCustomProgress();
                            fragment.dismiss();
                        }
                    }
                });
            }

            @Override
            public void onAddDriverFailed(ParseException e) {
                dismissCustomProgress();
                fragment.dismiss();
                if (e.getCode() == ParseException.EMAIL_TAKEN) {
                    showSweetDialog(AppConstants.ERR_EMAIL_TAKEN, "error");
                } else {
                    showSweetDialog(e.getMessage(), "error");
                }
            }
        });
        fragment.show(getSupportFragmentManager(),"driver");
    }
}
