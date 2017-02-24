package hertz.hertz.activities;

/**
 * Created by Alyana on 9/19/2015.
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import hertz.hertz.R;

public class CSplashScreenActivity extends BaseActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                Intent i = new Intent(CSplashScreenActivity.this, COpeningScreenActivity.class);
                //Intent i = new Intent(CSplashScreenActivity.this, AvailableDriversActivity.class);
                startActivity(i);
                animateToLeft(CSplashScreenActivity.this);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}