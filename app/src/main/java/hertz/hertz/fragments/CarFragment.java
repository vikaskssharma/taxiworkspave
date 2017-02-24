package hertz.hertz.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import hertz.hertz.R;
import hertz.hertz.activities.BaseActivity;

/**
 * Created by rsbulanon on 1/4/16.
 */
public class CarFragment extends Fragment {

    @Bind(R.id.pbLoadImage) ProgressBar pbLoadImage;
    @Bind(R.id.ivCarImage) ImageView ivCarImage;
    private ParseObject car;
    private BaseActivity activity;

    public static CarFragment newInstance(ParseObject car) {
        CarFragment fragment = new CarFragment();
        fragment.car = car;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_select_car,container,false);
        ButterKnife.bind(this, view);
        activity = (BaseActivity)getActivity();

        if (car.getParseFile("carImage") == null) {
            pbLoadImage.setVisibility(View.GONE);
            ivCarImage.setVisibility(View.VISIBLE);
            Log.d("carImage", "CAR IMAGE IS NULL");
        } else {
            ImageLoader.getInstance().loadImage(car.getParseFile("carImage").getUrl(),new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    pbLoadImage.setVisibility(View.VISIBLE);
                    Log.d("carImage", "IMAGE LOADING START");
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    pbLoadImage.setVisibility(View.GONE);
                    ivCarImage.setVisibility(View.VISIBLE);
                    Log.d("carImage", "IMAGE LOADING FAILED");
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    pbLoadImage.setVisibility(View.GONE);
                    ivCarImage.setVisibility(View.VISIBLE);
                    ivCarImage.setImageBitmap(loadedImage);
                    Log.d("carImage", "IMAGE LOADING COMPLETE");
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }
        return view;
    }
}
