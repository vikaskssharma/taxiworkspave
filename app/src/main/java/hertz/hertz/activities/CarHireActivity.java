package hertz.hertz.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseObject;
import com.rey.material.widget.Spinner;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hertz.hertz.R;
import hertz.hertz.adapters.TabAdapter;
import hertz.hertz.adapters.ViewPagerAdapter;
import hertz.hertz.fragments.CarFragment;
import hertz.hertz.fragments.CarHireFragment;

/**
 * Created by rsbulanon on 1/9/16.
 */
public class CarHireActivity extends BaseActivity {

    @Bind(R.id.viewPager) ViewPager viewPager;
    @Bind(R.id.tabLayout) TabLayout tabLayout;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private ArrayList<String> tabNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_hire);
        ButterKnife.bind(this);
        initViewPager();
    }

    private void initViewPager() {
        /** set up viewpager and tab layout */
        fragments.add(CarHireFragment.newInstance("Car/Sedan"));
        fragments.add(CarHireFragment.newInstance("SUV/MiniVan"));

        tabNames.add("Car/Sedan");
        tabNames.add("SUV/MiniVan");

        viewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), fragments, tabNames));
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(2);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        animateToRight(this);
    }

    @OnClick(R.id.ivNavBack)
    public void close() {
        onBackPressed();
    }
}
