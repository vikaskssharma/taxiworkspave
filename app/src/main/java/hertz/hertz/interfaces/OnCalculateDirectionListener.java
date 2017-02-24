package hertz.hertz.interfaces;


import java.util.ArrayList;

/**
 * Created by rsbulanon on 11/16/15.
 */
public interface OnCalculateDirectionListener {

    void onCalculationBegin();

    void onCalculationFinished(ArrayList result);

    void onCalculationException(Exception e);
}
