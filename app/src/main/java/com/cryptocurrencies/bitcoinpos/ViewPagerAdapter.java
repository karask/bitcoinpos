package com.cryptocurrencies.bitcoinpos;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by kostas on 14/6/2016.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {


        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return new PaymentFragment();
            }
            else {
                return new HistoryFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;           // As there are only 2 Tabs
        }

}

