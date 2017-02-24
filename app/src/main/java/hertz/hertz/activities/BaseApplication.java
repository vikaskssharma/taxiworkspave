package hertz.hertz.activities;

import android.app.Application;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

import hertz.hertz.R;
import hertz.hertz.helpers.AppConstants;
import hertz.hertz.model.Booking;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


/**
 * Created by rsbulanon on 11/11/15.
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        AppConstants.FIREBASE = new Firebase(AppConstants.BASE_URL);
        AppConstants.GEOFIRE = new GeoFire(AppConstants.FIREBASE);

        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(Booking.class);
        Parse.initialize(this, AppConstants.PARSE_APP_ID, AppConstants.PARSE_CLIENT_KEY);
        ParseUser.enableRevocableSessionInBackground();

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(this);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app
        ImageLoader.getInstance().init(config.build());

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/gotham-rounded-book.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}
