package hertz.hertz.fragments;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseObject;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import hertz.hertz.R;
import hertz.hertz.activities.HomeActivity;

/**
 * Created by rsbulanon on 12/30/15.
 */
public class DriverInfoDialogFragment extends DialogFragment {

    @Bind(R.id.tvDriverName) TextView tvDriverName;
    @Bind(R.id.tvContactNo) TextView tvContactNo;
    @Bind(R.id.tvCarModel) TextView tvCarModel;
    @Bind(R.id.tvPlateNo) TextView tvPlateNo;
    @Bind(R.id.ivProfilePic) CircleImageView ivProfilePic;
    @Bind(R.id.pbLoadImage) ProgressBar pbLoadImage;
    private View view;
    private ParseObject driver;
    private HomeActivity activity;

    public static DriverInfoDialogFragment newInstance(ParseObject driver) {
        final DriverInfoDialogFragment frag = new DriverInfoDialogFragment();
        frag.driver = driver;
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
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_driver_assigned, null);
        ButterKnife.bind(this, view);
        activity = (HomeActivity)getActivity();
        tvDriverName.setText(driver.getString("firstName") + " " + driver.getString("lastName"));
        tvContactNo.setText(driver.getString("mobileNo"));
        tvCarModel.setText(driver.getParseObject("car").getString("carModel"));
        tvPlateNo.setText(driver.getParseObject("car").getString("plateNo"));
        ImageLoader.getInstance().loadImage(driver.getParseFile("profilePic").getUrl(),
                new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        pbLoadImage.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        pbLoadImage.setVisibility(View.GONE);
                        ivProfilePic.setImageBitmap(loadedImage);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
        final Dialog mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        return mDialog;
    }
}
