package hertz.hertz.activities;

import android.content.Intent;
import android.os.Bundle;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import hertz.hertz.R;
import hertz.hertz.helpers.AppConstants;

/**
 * Created by rsbulanon on 12/6/15.
 */
public class SuperAdminActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);
        ButterKnife.bind(this);
        if (isNetworkAvailable()) {
            if (getAvailableCars().size() == 0) {
                fetchCars();
            }
        } else {
            showSweetDialog(AppConstants.ERR_CONNECTION,"error");
        }
    }

    private void fetchCars() {
        ParseQuery<ParseObject> car = ParseQuery.getQuery("Car");
        car.orderByAscending("carModel");
        car.whereEqualTo("markedAsDeleted", false);
        showCustomProgress(AppConstants.LOAD_FETCH_CAR);
        car.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                dismissCustomProgress();
                if (e == null) {
                    getAvailableCars().clear();
                    getAvailableCars().addAll(objects);
                }
            }
        });
    }

    @OnClick(R.id.btnCar)
    public void manageCards() {
        startActivity(new Intent(this, CarManagementActivity.class));
        animateToLeft(this);
    }

    @OnClick(R.id.btnDriver)
    public void manageDrivers() {
        if (getAvailableCars().size() > 0) {
            startActivity(new Intent(this,DriverManagementActivity.class));
            animateToLeft(this);
        }
    }

    @OnClick(R.id.btnLogout)
    public void logout() {
        showCustomProgress(AppConstants.LOAD_LOGOUT);
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    startActivity(new Intent(SuperAdminActivity.this,CLoginActivity.class));
                    finish();
                    animateToRight(SuperAdminActivity.this);
                }
            }
        });
    }
}
