package hertz.hertz.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by rsbulanon on 11/18/15.
 */
public class TabAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> fragments;
    private ArrayList<String> tabNames;

    public TabAdapter(FragmentManager fm, ArrayList<Fragment> fragments, ArrayList<String> tabNames) {
        super(fm);
        this.fragments = fragments;
        this.tabNames = tabNames;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabNames.get(position);
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
