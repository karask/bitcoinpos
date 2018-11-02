package gr.cryptocurrencies.bitcoinpos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import java.util.Date;

import gr.cryptocurrencies.bitcoinpos.database.Item;
import gr.cryptocurrencies.bitcoinpos.database.ItemHelper;
import gr.cryptocurrencies.bitcoinpos.database.PointOfSaleDb;
import gr.cryptocurrencies.bitcoinpos.database.UpdateDbHelper;
import gr.cryptocurrencies.bitcoinpos.network.BlockchainInfoHelper;
import gr.cryptocurrencies.bitcoinpos.network.BlockcypherHelper;
import gr.cryptocurrencies.bitcoinpos.network.RestBitcoinHelper;
import gr.cryptocurrencies.bitcoinpos.utilities.CurrencyUtils;

public class MainActivity extends AppCompatActivity implements PaymentRequestFragment.OnFragmentInteractionListener,
                                                               AboutFragment.OnFragmentInteractionListener,
                                                               ItemFragment.OnFragmentInteractionListener,
                                                               AddItemDialogFragment.OnFragmentInteractionListener,
                                                               ItemActionListFragment.OnFragmentInteractionListener,
                                                               ShowItemFragment.OnListFragmentInteractionListener {

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;


    public ViewPager getmViewPager() {
        return mViewPager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get context for Helper classes
        new UpdateDbHelper(this);

        new BlockchainInfoHelper(this);
        new BlockcypherHelper(this);
        new RestBitcoinHelper(this);


        //this.deleteDatabase("History.db");

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
        mViewPagerAdapter.addFragment(new ItemFragment());
        mViewPagerAdapter.addFragment(new PaymentFragment());
        mViewPagerAdapter.addFragment(new HistoryFragment());
        mViewPager.setAdapter(mViewPagerAdapter);
        setSupportActionBar(mToolbar);

        /*
            TabLayout.newTab() method creates a tab view, Now a Tab view is not the view
            which is below the tabs, its the tab itself.
        */
        final TabLayout.Tab item = mTabLayout.newTab();
        final TabLayout.Tab payment = mTabLayout.newTab();
        final TabLayout.Tab history = mTabLayout.newTab();

        /*
            Setting Title text for our tabs respectively
        */
        item.setText(R.string.items);
        payment.setText(R.string.payment);
        history.setText(R.string.history);

        /*
            Adding the tab view to our tablayout at appropriate positions
            As I want home at first position I am passing home and 0 as argument to
            the tablayout and like wise for other tabs as well
        */
        mTabLayout.addTab(item, 0);
        mTabLayout.addTab(payment, 1);
        mTabLayout.addTab(history, 2);

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


        /*
            Select middle tab as default
         */
        mViewPager.setCurrentItem(1);

    }






//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        // We do that from fragments now so that they have their custom actions
//        // Thus we need to remove or have duplicate entries!
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            showAbout();
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


    private void showAbout() {
        DialogFragment myDialog = AboutFragment.newInstance();
        // for API >= 23 the title is disable by default -- we set a style that enables it
        myDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AboutFragment);
        myDialog.show(getSupportFragmentManager(), getString(R.string.about_fragment_tag));
    }




    // implements PaymentRequestFragment inner interface methods -- from PaymentRequestFragment
    public void onPaymentRequestFragmentClose(boolean isCancelled) {
        // get PaymentFragment view and update secondary amount since exchange rate might be different!
        PaymentFragment paymentFragment = (PaymentFragment) mViewPagerAdapter.instantiateItem(null, 1);

        if(isCancelled) {
            if (!paymentFragment.currencyToggle.isChecked()) {
                // was local currency - convert to BTC
                paymentFragment.totalSecondaryAmountTextView.setText(CurrencyUtils.getLocalCurrencyFromBtc(paymentFragment.totalAmountTextView.getText().toString()) + " " + paymentFragment.mLocalCurrency);
            } else {
                // was BTC - convert to local currency
                paymentFragment.totalSecondaryAmountTextView.setText(CurrencyUtils.getBtcFromLocalCurrency(paymentFragment.totalAmountTextView.getText().toString()) + " BTC");
            }
        } else {
            // TODO abstract -- duplicated in onClearCartFragmentInteraction below
            paymentFragment.mItemsInCart.clear();
            paymentFragment.totalItemsCountTextView.setText("0");
            paymentFragment.totalAmountTextView.setText("0");
            paymentFragment.totalSecondaryAmountTextView.setText("0 " + (paymentFragment.currencyToggle.isChecked() ? "BTC" : paymentFragment.mLocalCurrency));
        }

        // close dialog fragment (from Cancel or OK)
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(getString(R.string.request_payment_fragment_tag))).commit();
    }


    @Override
    // from AboutFragment
    public void onAboutOk() {
        // close dialog fragment
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(getString(R.string.about_fragment_tag))).commit();
    }

    @Override
    // from ItemFragment
    public void onListItemClickFragmentInteraction(Item item) {
        DialogFragment myDialog = ItemActionListFragment.newInstance(item.getItemId().toString());
        // for API >= 23 the title is disable by default -- we set a style that enables it
        myDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.ItemActionListDialogFragment);
        myDialog.show(getSupportFragmentManager(), getString(R.string.item_action_list_dialog_fragment_tag));
    }


    @Override
    // from AddItemDialogFragment
    public void onAddOrUpdateItemFragmentInteraction(int itemId, String itemName, double itemPrice) {
        // TODO: for now order is not implemented (always 0)
        // TODO: currently only name and price/amount can be set!

        // itemId is -1 when adding new item
        boolean isEditMode = itemId >= 0;

        // get ItemFragment's recycler view and adapter to add and display the item
        // this is a quick way of getting the fragment at position 0 (ItemFragment)
        ItemFragment itemFragment = (ItemFragment) mViewPagerAdapter.instantiateItem(null, 0);
        RecyclerView recyclerView = itemFragment.getRecyclerView();
        ItemRecyclerViewAdapter recyclerViewAdapter = (ItemRecyclerViewAdapter) recyclerView.getAdapter();

        // get DB helper for items
        ItemHelper itemHelper = ItemHelper.getInstance(getApplicationContext());

        Item item;
        if(isEditMode) {
            // get item and modify
            item = itemHelper.get(itemId);
            if(item != null) {
                item.setName(itemName);
                item.setAmount(itemPrice);

                // update in db
                itemHelper.update(item);

                // update UI
                recyclerViewAdapter.updateItem(item);
            }
        } else {
            // create new item
            item = new Item(null, itemName, "", itemPrice, 0, "", true, new Date());

            // add the item to the DB
            int newItemId = itemHelper.insert(item);

            // update the memory object with the id generated from the DB
            item.setItemId(newItemId);

            if(recyclerViewAdapter.getItemCount() == 0) {
                // first item added ever
                recyclerView.setVisibility(View.VISIBLE);
                itemFragment.getEmptyView().setVisibility(View.GONE);
            }
            recyclerViewAdapter.addItem(item);

        }

    }

    @Override
    // from AddItemDialogFragment
    public void onCancelItemFragmentInteraction() {
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(getString(R.string.add_item_dialog_fragment_tag))).commit();
    }

    @Override
    // from ItemActionListFragment
    public void onEditItemAction(int id) {
        // get item from database
        ItemHelper itemHelper = ItemHelper.getInstance(getApplicationContext());
        Item itemToUpdate = itemHelper.get(id);
        if(itemToUpdate != null) {
            DialogFragment myDialog = AddItemDialogFragment.newInstance(id, itemToUpdate.getName(), itemToUpdate.getAmount());
            // for API >= 23 the title is disable by default -- we set a style that enables it
            myDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AddItemDialogFragment);
            myDialog.show(getSupportFragmentManager(), getString(R.string.add_item_dialog_fragment_tag));
        }
    }

    @Override
    // from ItemActionListFragment
    public void onDeleteItemAction(int id) {
        ItemHelper itemHelper = ItemHelper.getInstance(getApplicationContext());
        if(itemHelper.delete(id)) {
            // update UI as well (get ItemFragment's recycler view and adapter
            ItemFragment itemFragment = (ItemFragment) mViewPagerAdapter.instantiateItem(null, 0);
            RecyclerView recyclerView = itemFragment.getRecyclerView();
            ItemRecyclerViewAdapter recyclerViewAdapter = (ItemRecyclerViewAdapter) recyclerView.getAdapter();
            if(recyclerViewAdapter.getItemCount() == 1) {
                // if last item is deleted
                recyclerView.setVisibility(View.GONE);
                itemFragment.getEmptyView().setVisibility(View.VISIBLE);
            }
            recyclerViewAdapter.removeItem(id);
        }
    }


    @Override
    // from ShowItemFragment  (select from inventory mode)
    public void onShowItemSelectionFragmentInteraction(Item item) {
        // get PaymentFragment view and clear cart and update UI
        PaymentFragment paymentFragment = (PaymentFragment) mViewPagerAdapter.instantiateItem(null, 1);

        // close ShowItemFragment
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(getString(R.string.show_item_list_dialog_fragment_tag))).commit();

        // update PaymentFragment edit texts
        paymentFragment.productNameEditText.setText(item.getName());
        paymentFragment.amount.setText(String.valueOf(item.getAmount()));
    }

    @Override
    // from ShowItemFragment  (show cart mode)
    public void onClearCartFragmentInteraction() {
        // get PaymentFragment view and clear cart and update UI
        PaymentFragment paymentFragment = (PaymentFragment) mViewPagerAdapter.instantiateItem(null, 1);
        // TODO abstract -- duplicated in onPaymentRequestFragmentClose above
        paymentFragment.mItemsInCart.clear();
        paymentFragment.totalItemsCountTextView.setText("0");
        paymentFragment.totalAmountTextView.setText("0");
        paymentFragment.totalSecondaryAmountTextView.setText("0 " + (paymentFragment.currencyToggle.isChecked() ? "BTC" : paymentFragment.mLocalCurrency));
    }
}
