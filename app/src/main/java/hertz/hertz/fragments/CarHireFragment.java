package hertz.hertz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.rey.material.widget.Spinner;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import hertz.hertz.R;
import hertz.hertz.activities.CarHireActivity;
import hertz.hertz.adapters.ViewPagerAdapter;

/**
 * Created by rsbulanon on 1/11/16.
 */
public class CarHireFragment extends Fragment {

    @Bind(R.id.autoComplete) AutoCompleteTextView autoComplete;
    @Bind(R.id.tvCarModel) TextView tvCarModel;
    @Bind(R.id.tvPlateNo) TextView tvPlateNo;
    @Bind(R.id.tvCapacity) TextView tvCapacity;
    @Bind(R.id.tvRate) TextView tvRate;
    @Bind(R.id.viewPager) ViewPager viewPager;
    @Bind(R.id.spnrRate) Spinner spnrRate;
    @Bind(R.id.tvExcessRate) TextView tvExcessRate;
    @Bind(R.id.tvType) TextView tvType;
    private CarHireActivity activity;
    private String type;

    public static CarHireFragment newInstance(String type) {
        CarHireFragment fragment = new CarHireFragment();
        fragment.type = type;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_car_hire,container,false);
        ButterKnife.bind(this,view);
        initSpinner();
        ArrayList<String> items = new ArrayList<>();
        ArrayList<Fragment> cars = new ArrayList<>();
        activity = (CarHireActivity)getActivity();
        for (ParseObject c : activity.getForHireCars(type)) {
            cars.add(CarFragment.newInstance(c));
            items.add(c.getString("carModel"));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.row_spinner, items);
        autoComplete.setThreshold(1);
        autoComplete.setAdapter(adapter);
        viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), cars));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                final ParseObject car = activity.getForHireCars(type).get(position);
                setItems(car);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setItems(activity.getForHireCars(type).get(0));
        return view;
    }

    private void initSpinner() {
        ArrayList<String> items = new ArrayList<>();
        items.add("Rate per 3 Hours");
        items.add("Rate per 10 Hours");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.row_spinner, items);
        adapter.setDropDownViewResource(R.layout.row_spinner_dropdown);
        spnrRate.setAdapter(adapter);
        spnrRate.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                ParseObject car = activity.getForHireCars(type).get(viewPager.getCurrentItem());
                if (position == 0) {
                    tvRate.setText("Rate : " + activity.getDecimalFormatter()
                            .format(car.getNumber("ratePer3Hours").doubleValue()));
                } else {
                    tvRate.setText("Rate : " + activity.getDecimalFormatter()
                            .format(car.getNumber("ratePer10Hours").doubleValue()));
                }
            }
        });
    }

    private void setItems(final ParseObject car) {
        tvCarModel.setText(car.getString("carModel") + "\n" + car.getString("description"));
        tvCapacity.setText("Capacity : " + car.getNumber("capacity").intValue());
        String plateNo = car.getString("plateNo");
        tvPlateNo.setText("Plate No : " + (plateNo == null ? "Not Available" :
                plateNo.isEmpty() ? "Not Available" : plateNo));
        tvExcessRate.setText("Excess Rate : " + activity.getDecimalFormatter()
                .format(car.getNumber("excessRate").doubleValue()));
        tvRate.setText("Rate : " + activity.getDecimalFormatter()
                .format(car.getNumber("ratePer3Hours").doubleValue()));
        tvType.setText("Mode : " + car.getString("mode"));
    }
}
