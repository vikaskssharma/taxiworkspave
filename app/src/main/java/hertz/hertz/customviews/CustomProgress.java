package hertz.hertz.customviews;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;


import butterknife.Bind;
import butterknife.ButterKnife;
import hertz.hertz.R;

/**
 * Created by rsbulanon on 11/18/15.
 */
public class CustomProgress extends DialogFragment {

    @Bind(R.id.tvLoadingMessage) TextView tvMessage;
    private View view;
    private String message;

    public static CustomProgress newInstance(String message) {
        CustomProgress frag = new CustomProgress();
        frag.message = message;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //getDialog().getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.fragment_custom_loading, null);
        ButterKnife.bind(this, view);
        tvMessage.setText(message);
        final Dialog mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        return mDialog;
    }

    public void updateMessage(String message) {
        tvMessage.setText(message);
    }

}
