package com.cryptocurrencies.bitcoinpos;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements PaymentRequestFragment.OnFragmentInteractionListener {

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
            Assigning view variables to their respective view in xml
            by findViewByID method
        */
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        /*
            Creating Adapter and setting that adapter to the viewPager
            setSupportActionBar method takes the toolbar and sets it as
            the default action bar thus making the toolbar work like a normal
            action bar.
        */
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(new PaymentFragment());
        mViewPagerAdapter.addFragment(new HistoryFragment());
        mViewPager.setAdapter(mViewPagerAdapter);
        setSupportActionBar(mToolbar);

        /*
            TabLayout.newTab() method creates a tab view, Now a Tab view is not the view
            which is below the tabs, its the tab itself.
        */
        final TabLayout.Tab payment = mTabLayout.newTab();
        final TabLayout.Tab history = mTabLayout.newTab();

        /*
            Setting Title text for our tabs respectively
        */
        payment.setText(R.string.payment);
        history.setText(R.string.history);

        /*
            Adding the tab view to our tablayout at appropriate positions
            As I want home at first position I am passing home and 0 as argument to
            the tablayout and like wise for other tabs as well
        */
        mTabLayout.addTab(payment, 0);
        mTabLayout.addTab(history, 1);

        /*
            TabTextColor sets the color for the title of the tabs, passing a ColorStateList here makes
            tab change colors in different situations such as selected, active, inactive etc

            TabIndicatorColor sets the color for the indiactor below the tabs
        */
        mTabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.tab_selector));
        //mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.indicator));

        /*
            Adding a onPageChangeListener to the mViewPager
            We add the PageChangeListener and pass a TabLayoutPageChangeListener so that Tabs Selection
            changes when a viewpager page changes.
        */
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        /*
            Adding another listener to act when a fragment becomes visible!
         */
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // assumes that FragmentIsNowVisible interface is implemented in all fragments!
                FragmentIsNowVisible fragment = (FragmentIsNowVisible) mViewPagerAdapter.getItem(position);
                if(fragment != null)
                    fragment.doWhenFragmentBecomesVisible();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        /*
            Adding a onTabSelectedListener to the mTabLayout
            We add the TabSelectedListener and pass a ViewPagerOnTabSelectedListener so that mViewPager
            Selection changes when a tab layout changes.
        */
        mTabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        } else if(id == R.id.action_settings) {
            showSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        Intent goToSettings = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(goToSettings);

    }




    // implements PaymentRequestFragment inner interface methods
    public void onPaymentCancellation() {
        // close dialog fragment
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(getString(R.string.request_payment_fragment_tag))).commit();
    }


}
