package hertz.hertz.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hertz.hertz.R;
import hertz.hertz.adapters.CarsAdapter;
import hertz.hertz.fragments.AddCarDialogFragment;
import hertz.hertz.helpers.AppConstants;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

/**
 * Created by rsbulanon on 12/6/15.
 */
public class CarManagementActivity extends BaseActivity {

    @Bind(R.id.rvCars) RecyclerView rvCars;
    @Bind(R.id.swipeRefresh) SwipeRefreshLayout swipeRefresh;
    private ArrayList<ParseObject> cars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_management);
        ButterKnife.bind(this);

        if (getAvailableCars().size() > 0) {
            cars.addAll(getAvailableCars());
        } else {
            getCars();
        }

        CarsAdapter adapter = new CarsAdapter(this,cars);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
        rvCars.setAdapter(scaleAdapter);
        rvCars.setLayoutManager(new LinearLayoutManager(this));
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCars();
            }
        });
    }

    public void getCars() {
        if (!swipeRefresh.isRefreshing()) {
            showCustomProgress(AppConstants.LOAD_FETCH_CAR);
        }
        ParseQuery<ParseObject> car = ParseQuery.getQuery("Car");
        car.orderByAscending("carModel");
        car.whereEqualTo("markedAsDeleted",false);
        car.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (!swipeRefresh.isRefreshing()) {
                    dismissCustomProgress();
                }
                if (e == null) {
                    cars.clear();
                    cars.addAll(objects);
                    getAvailableCars().clear();
                    getAvailableCars().addAll(cars);
                    rvCars.getAdapter().notifyDataSetChanged();
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                } else {
                    showSweetDialog(e.getMessage(),"error");
                }
            }
        });
    }

    @OnClick(R.id.btnAddCar)
    public void addCar() {
        final AddCarDialogFragment fragment = AddCarDialogFragment.newInstance(null);
        fragment.setOnAddCarListener(new AddCarDialogFragment.OnAddCarListener() {
            @Override
            public void onSuccessful() {
                fragment.dismiss();
                showSweetDialog(AppConstants.OK_NEW_CAR_ADDED, "success");
                getCars();
            }

            @Override
            public void onFailed(ParseException e) {
                fragment.dismiss();
                showSweetDialog(e.getMessage(), "error");
            }
        });
        fragment.show(getFragmentManager(),"add car");
    }

    public void removeCarFromList(int index) {
        cars.remove(index);
        rvCars.getAdapter().notifyDataSetChanged();
    }
}
