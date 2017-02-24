package hertz.hertz.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import hertz.hertz.BuildConfig;

/**
 * Created by rsbulanon on 11/25/15.
 */
public class MyPushNotificationReceiver extends ParsePushBroadcastReceiver {

    private NotificationManager mNotificationManager;
    private int notification_id = 0;

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        /** remove notification from notification bar */
        Notification n = super.getNotification(context, intent);
        notification_id = intent.getExtras().getInt("NOTIFICATION_TYPE");
        final String data = intent.getStringExtra("com.parse.Data");
        Log.d("push","data --> " + data);
        try {
            final JSONObject json = new JSONObject(data);
            if (json.has("bookingStatus")) {
                final String status = json.getJSONObject("json").getString("bookingStatus");
                if (status.equals("Attended")) {
                    if (!BuildConfig.FLAVOR.equals("booking")) {
                        mNotificationManager.cancel(notification_id);
                        Log.d("push", "must cancel notification");
                    } else {
                        mNotificationManager.notify(notification_id, n);
                        Log.d("push", "booking app must show notification");
                    }
                } else {
                    mNotificationManager.notify(notification_id, n);
                    Log.d("push", "notification must show");
                }
            } else {
                /** chat notification */
                mNotificationManager.notify(notification_id, n);
            }
            intent.setAction("broadcast_action");
            LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(context);
            mgr.sendBroadcast(intent);
        } catch (JSONException e) {
            Log.d("push", "error in parsing json --> " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mNotificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        super.onReceive(context, intent);
    }

/*
    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        intent.setAction("broadcast_action");
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(context);
        mgr.sendBroadcast(intent);
    }*/
}
