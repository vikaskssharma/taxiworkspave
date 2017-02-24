package hertz.hertz.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hertz.hertz.R;
import hertz.hertz.activities.BaseActivity;
import hertz.hertz.helpers.AppConstants;

/**
 * Created by rsbulanon on 11/17/15.
 */
public class ResendForgotDialogFragment extends DialogFragment {

    @Bind(R.id.tvHeader) TextView tvHeader;
    @Bind(R.id.etEmail) EditText etEmail;
    private View view;
    private String header;
    private BaseActivity activity;
    private OnRequestListener onRequestListener;

    public static ResendForgotDialogFragment newInstance(String header) {
        ResendForgotDialogFragment frag = new ResendForgotDialogFragment();
        frag.header = header;
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
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_resend_forgot, null);
        ButterKnife.bind(this, view);
        tvHeader.setText(header);
        activity = (BaseActivity)getActivity();
        final Dialog mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        return mDialog;
    }

    @OnClick(R.id.btnProceed)
    public void proceed() {
        String email = etEmail.getText().toString();
        if (!activity.isNetworkAvailable()) {
            activity.showToast(AppConstants.ERR_CONNECTION);
        } else if (email.isEmpty()) {
            activity.setError(etEmail, AppConstants.WARN_FIELD_REQUIRED);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            activity.setError(etEmail, AppConstants.WARN_INVALID_EMAIL_FORMAT);
        } else {
            onRequestListener.onRequest(email);
        }
    }

    public interface OnRequestListener {
        void onRequest(String email);
    }

    public void setOnRequestListenern(OnRequestListener onRequestListener) {
        this.onRequestListener = onRequestListener;
    }
}
