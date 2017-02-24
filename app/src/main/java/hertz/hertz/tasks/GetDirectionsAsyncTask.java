package hertz.hertz.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Map;

import hertz.hertz.helpers.MapHelper;
import hertz.hertz.interfaces.OnCalculateDirectionListener;


/**
 * Created by rsbulanon on 9/16/15.
 */
public class GetDirectionsAsyncTask extends AsyncTask<Map<String, String>, Object, ArrayList>
{
    public static final String USER_CURRENT_LAT = "user_current_lat";
    public static final String USER_CURRENT_LONG = "user_current_long";
    public static final String DESTINATION_LAT = "destination_lat";
    public static final String DESTINATION_LONG = "destination_long";
    public static final String DIRECTIONS_MODE = "directions_mode";
    private Exception exception;
    private OnCalculateDirectionListener onCalculateDirectionListener;

    public GetDirectionsAsyncTask(OnCalculateDirectionListener onCalculateDirectionListener) {
        super();
        this.onCalculateDirectionListener = onCalculateDirectionListener;
    }

    public void onPreExecute() {
        this.onCalculateDirectionListener.onCalculationBegin();
    }

    @Override
    public void onPostExecute(ArrayList result) {
        if (exception == null) {
            this.onCalculateDirectionListener.onCalculationFinished(result);
        } else {
            Log.d("map", "erroorr --> " + exception.toString());
            this.onCalculateDirectionListener.onCalculationException(exception);
        }
    }

    @Override
    protected ArrayList doInBackground(Map<String, String>... params) {
        Map<String, String> paramMap = params[0];
        try {
            LatLng fromPosition = new LatLng(Double.valueOf(paramMap.get(USER_CURRENT_LAT)) , Double.valueOf(paramMap.get(USER_CURRENT_LONG)));
            LatLng toPosition = new LatLng(Double.valueOf(paramMap.get(DESTINATION_LAT)) , Double.valueOf(paramMap.get(DESTINATION_LONG)));
            MapHelper md = new MapHelper();
            Document doc = md.getDocument(fromPosition, toPosition, paramMap.get(DIRECTIONS_MODE));
            ArrayList directionPoints = md.getDirection(doc);
            return directionPoints;
        } catch (Exception e) {
            exception = e;
            return null;
        }
    }

}
