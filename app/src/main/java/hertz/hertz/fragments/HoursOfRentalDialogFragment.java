package hertz.hertz.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hertz.hertz.R;
import hertz.hertz.activities.BaseActivity;
import hertz.hertz.helpers.AppConstants;

/**
 * Created by rsbulanon on 1/4/16.
 */
public class HoursOfRentalDialogFragment extends DialogFragment {

    @Bind(R.id.etHours) EditText etHours;
    private View view;
    private BaseActivity activity;
    private OnRentalHoursListener onRentalHoursListener;

    public static HoursOfRentalDialogFragment newInstance() {
        final HoursOfRentalDialogFragment frag = new HoursOfRentalDialogFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_hours_of_rental, null);
        ButterKnife.bind(this, view);
        activity = (BaseActivity)getActivity();
        final Dialog mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        return mDialog;
    }

    public interface OnRentalHoursListener {
        void onDesiredRentalHours(int hours);
    }

    public void setOnRentalHoursListener(OnRentalHoursListener onRentalHoursListener) {
        this.onRentalHoursListener = onRentalHoursListener;
    }

    @OnClick(R.id.btnProceed)
    public void proceed() {
        final String hours = etHours.getText().toString();
        if (hours.isEmpty()) {
            activity.setError(etHours, AppConstants.WARN_FIELD_REQUIRED);
        } else if (Integer.valueOf(hours) < 1) {
            activity.setError(etHours, AppConstants.WARN_RENTAL_HORUS);
        } else {
            onRentalHoursListener.onDesiredRentalHours(Integer.valueOf(hours));
        }
    }
}
