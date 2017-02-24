package hertz.hertz.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;

import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import hertz.hertz.R;
import hertz.hertz.helpers.AppConstants;

public class COpeningScreenActivity extends BaseActivity {

    @Bind(R.id.llSignUp) LinearLayout llSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copening_screen);
        ButterKnife.bind(this);
        if (getResources().getString(R.string.app_name).equals("Hertz Driver App")) {
            llSignUp.setVisibility(View.INVISIBLE);
        }
        if (!isNetworkAvailable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(AppConstants.ERR_CONNECTION)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void onClick(View view) {
        switch(view.getId()){
            case R.id.bLogin:
                /** check if there's still a logged in parse user */
                if (ParseUser.getCurrentUser() != null) {
                    if (ParseUser.getCurrentUser().getString("userRole").equals("driver")) {
                        startActivity(new Intent(this, DriverDashBoardActivity.class));
                    } else if (ParseUser.getCurrentUser().getString("userRole").equals("superadmin")) {
                        startActivity(new Intent(this, SuperAdminActivity.class));
                    } else {
                        /** skip login screen and proceed to home screen */
                        startActivity(new Intent(this, HomeActivity.class));
                    }
                    animateToLeft(this);
                    finish();
                } else {
                    /** if there's none, redirect user to login screen */
                    startActivity(new Intent(this, CLoginActivity.class));
                }
                animateToLeft(this);
                break;
            case R.id.bSignUp:
                startActivity(new Intent(this, CRegistrationActivity.class));
                animateToLeft(this);
                break;
        }
    }
}
