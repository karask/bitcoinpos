package gr.cryptocurrencies.bitcoinpos;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import gr.cryptocurrencies.bitcoinpos.database.Item;
import gr.cryptocurrencies.bitcoinpos.database.ItemHelper;
import gr.cryptocurrencies.bitcoinpos.database.PointOfSaleDb;
import gr.cryptocurrencies.bitcoinpos.database.TxStatus;
import gr.cryptocurrencies.bitcoinpos.database.UpdateDbHelper;
import gr.cryptocurrencies.bitcoinpos.network.ExchangeRates;
import gr.cryptocurrencies.bitcoinpos.network.Utilities;

import gr.cryptocurrencies.bitcoinpos.utilities.AddressValidator;
import gr.cryptocurrencies.bitcoinpos.utilities.CurrencyUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PaymentFragment extends Fragment implements View.OnClickListener, FragmentIsNowVisible {

    CoordinatorLayout coordinatorLayout;
    LinearLayout totalItemsCountLinearLayout;
    Button keypad1, keypad2, keypad3, keypad4, keypad5, keypad6, keypad7, keypad8, keypad9, keypad0, keypadDot;
    EditText productNameEditText;
    ImageButton keypadBackspace, keypadInventory, keypadClear, keypadAddToCart;
    Button requestPayment;
    ToggleButton currencyToggle;
    TextView amount, totalAmountTextView, totalItemsCountTextView, totalSecondaryAmountTextView;

    SharedPreferences mSharedPreferences;
    String mLocalCurrency;
    String mBitcoinPaymentAddress , mTestnetBitcoinPaymentAddress, mBitcoinCashPaymentAddress, mLitecoinPaymentAddress;
    String selectedAddress;
    String mSelectedCryptocurrencyToggleText;
    String mMerchantName;
    String mSelectedCryptocurrency;

    RadioGroup radioGroup;
    int selectedCrypto=0;
    boolean showSnackbar = false;

    boolean testnetStatus= false;
    boolean isTestnetAddress;
    List<Item> mItemsInCart = new ArrayList<>();
    private PointOfSaleDb mDbHelper;

    private int countCheckButtonChanges = 0;

    public PaymentFragment() {
        // Required empty public constructor
    }
    View headerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_payment, container, false);

        coordinatorLayout = (CoordinatorLayout) fragmentView.findViewById(R.id.coordinatorLayout);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        headerLayout = navigationView.getHeaderView(0);

        radioGroup = (RadioGroup) headerLayout.findViewById(R.id.radiogroup) ;
        testnetStatus = mSharedPreferences.getBoolean(getString(R.string.accept_testnet_key),false);
        RadioButton radioButtonTestnet = (RadioButton) headerLayout.findViewById(R.id.radioButtonBtcTestnet);
        if(!testnetStatus){
            radioButtonTestnet.setVisibility(View.INVISIBLE);
        }

        selectedCrypto = mSharedPreferences.getInt(getString(R.string.navigation_crypto),0);

        RadioButton radioButton = (RadioButton) radioGroup.getChildAt(selectedCrypto);
        radioButton.setChecked(true);
        setSelectedItemNavigationList(radioButton.getId());


        // get preferences
        mLocalCurrency = mSharedPreferences.getString(getString(R.string.local_currency_key), getString(R.string.default_currency_code));
        mBitcoinPaymentAddress = mSharedPreferences.getString(getString(R.string.btc_payment_address_key), "");
        mBitcoinCashPaymentAddress = mSharedPreferences.getString(getString(R.string.bch_payment_address_key),"");
        mLitecoinPaymentAddress = mSharedPreferences.getString(getString(R.string.ltc_payment_address_key),"");
        mTestnetBitcoinPaymentAddress = mSharedPreferences.getString(getString(R.string.btc_test_payment_address_key), "");


        switch (selectedCrypto){
            case 0:
                mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BTC);
                mSelectedCryptocurrencyToggleText = mSelectedCryptocurrency;
                selectedAddress = mBitcoinPaymentAddress;
                isTestnetAddress=false;
                break;
            case 1:
                mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BCH);
                mSelectedCryptocurrencyToggleText = mSelectedCryptocurrency;
                selectedAddress = mBitcoinCashPaymentAddress;
                isTestnetAddress=false;
                break;
            case 2:
                mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.LTC);
                mSelectedCryptocurrencyToggleText = mSelectedCryptocurrency;
                selectedAddress = mLitecoinPaymentAddress;
                isTestnetAddress=false;
                break;
            case 3:
                mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BTCTEST);
                mSelectedCryptocurrencyToggleText = getString(R.string.btctest);
                selectedAddress = mTestnetBitcoinPaymentAddress;
                isTestnetAddress=true;
                break;
        }

        //mLitecoinPaymentAddress = mSharedPreferences...
        //mLBitcoinCashPaymentAddress = mSharedPreferences...

        mMerchantName = mSharedPreferences.getString(getString(R.string.merchant_name_key), "");

        totalItemsCountLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.total_items_count_linear_layout);
        totalItemsCountLinearLayout.setOnClickListener(this);

        currencyToggle = (ToggleButton) fragmentView.findViewById(R.id.currencyToggleButton);

        currencyToggle.setTextOff(mSelectedCryptocurrencyToggleText);
        currencyToggle.setTextOn(mLocalCurrency);
        currencyToggle.toggle();
        currencyToggle.setOnClickListener(this);

        amount = (TextView) fragmentView.findViewById(R.id.amountTextView);
        totalAmountTextView = (TextView) fragmentView.findViewById(R.id.totalAmountTextView);
        totalItemsCountTextView = (TextView) fragmentView.findViewById(R.id.totalItemsCountTextView);
        totalSecondaryAmountTextView = (TextView) fragmentView.findViewById(R.id.totalSecondaryAmountTextView);

        if(currencyToggle.isChecked()) {

            totalSecondaryAmountTextView.setText("0 " + mSelectedCryptocurrencyToggleText.toString());//"0 BTC"
        } else {
            totalSecondaryAmountTextView.setText("0 " + mLocalCurrency.toString());
        }

        productNameEditText = (EditText) fragmentView.findViewById(R.id.productNameTextView);
        productNameEditText.setOnClickListener(this);
        productNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                productNameEditText.setCursorVisible(false);
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(productNameEditText.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        //boolean closeDrawer = false;
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                View radioButton = radioGroup.findViewById(id);
                if (mItemsInCart.size() > 0) {
                    //closeDrawer=true;
                    countCheckButtonChanges++;
                    if(countCheckButtonChanges==2){
                        DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                        drawerLayout.closeDrawers();
                        Snackbar.make(coordinatorLayout, R.string.all_items_same_currency, Snackbar.LENGTH_LONG).show();
                        countCheckButtonChanges=0;
                    }
                    //showSnackbar = true;
                    RadioButton rButton = (RadioButton) radioGroup.getChildAt(selectedCrypto);
                    rButton.setChecked(true);

                        // drawer Listener to check when drawer opens, closes etc.
//                    if (drawerLayout != null && drawerLayout instanceof DrawerLayout) {
//                        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
//                            @Override
//                            public void onDrawerSlide(View view, float v) {}
//                            @Override
//                            public void onDrawerOpened(View view) {}
//                            @Override
//                            public void onDrawerClosed(View view) {
//                                Snackbar.make(coordinatorLayout, R.string.all_items_same_currency, Snackbar.LENGTH_LONG).show();
//                            }
//                            @Override
//                            public void onDrawerStateChanged(int i) {}
//                        });
//                    }

                } else {
                    selectedCrypto = radioGroup.indexOfChild(radioButton);
                    //set background color for the selected item and for the unselected items
                    setSelectedItemNavigationList(id);


                    switch (selectedCrypto){
                        case 0:
                            mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BTC);
                            mSelectedCryptocurrencyToggleText = mSelectedCryptocurrency;
                            selectedAddress = mBitcoinPaymentAddress;
                            isTestnetAddress=false;
                            break;
                        case 1:
                            mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BCH);
                            mSelectedCryptocurrencyToggleText = mSelectedCryptocurrency;
                            selectedAddress = mBitcoinCashPaymentAddress;
                            isTestnetAddress=false;
                            break;
                        case 2:
                            mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.LTC);
                            mSelectedCryptocurrencyToggleText = mSelectedCryptocurrency;
                            selectedAddress = mBitcoinPaymentAddress;
                            isTestnetAddress=false;
                            break;
                        case 3:
                            mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BTCTEST);
                            mSelectedCryptocurrencyToggleText = getString(R.string.btctest);
                            selectedAddress = mTestnetBitcoinPaymentAddress;
                            isTestnetAddress=true;
                            break;
                    }

                    ExchangeRates exchangeRates = ExchangeRates.getInstance();
                    exchangeRates.updateExchangeRates(getActivity(), mLocalCurrency);
                    exchangeRates.updateExchangeRatesCryptocompare(getActivity(), mSelectedCryptocurrencyToggleText);

                    if (currencyToggle.isChecked()) {

                        totalSecondaryAmountTextView.setText("0 " + mSelectedCryptocurrencyToggleText);//"0 BTC"}

                    } else {

                        totalSecondaryAmountTextView.setText("0 " + mLocalCurrency.toString());
                    }

                    currencyToggle.setTextOn(mLocalCurrency);

                    currencyToggle.setTextOff(mSelectedCryptocurrencyToggleText);

                    currencyToggle.setChecked(currencyToggle.isChecked());


                }
            }
        });


        keypad1 = (Button) fragmentView.findViewById(R.id.btn_1);
        keypad1.setOnClickListener(this);
        keypad2 = (Button) fragmentView.findViewById(R.id.btn_2);
        keypad2.setOnClickListener(this);
        keypad3 = (Button) fragmentView.findViewById(R.id.btn_3);
        keypad3.setOnClickListener(this);
        keypad4 = (Button) fragmentView.findViewById(R.id.btn_4);
        keypad4.setOnClickListener(this);
        keypad5 = (Button) fragmentView.findViewById(R.id.btn_5);
        keypad5.setOnClickListener(this);
        keypad6 = (Button) fragmentView.findViewById(R.id.btn_6);
        keypad6.setOnClickListener(this);
        keypad7 = (Button) fragmentView.findViewById(R.id.btn_7);
        keypad7.setOnClickListener(this);
        keypad8 = (Button) fragmentView.findViewById(R.id.btn_8);
        keypad8.setOnClickListener(this);
        keypad9 = (Button) fragmentView.findViewById(R.id.btn_9);
        keypad9.setOnClickListener(this);
        keypad0 = (Button) fragmentView.findViewById(R.id.btn_0);
        keypad0.setOnClickListener(this);
        keypadDot = (Button) fragmentView.findViewById(R.id.btn_dot);
        keypadDot.setOnClickListener(this);
        keypadBackspace = (ImageButton) fragmentView.findViewById(R.id.btn_backspace);
        keypadBackspace.setOnClickListener(this);
        keypadInventory = (ImageButton) fragmentView.findViewById(R.id.btn_inv);
        keypadInventory.setOnClickListener(this);
        keypadClear = (ImageButton) fragmentView.findViewById(R.id.btn_clear);
        keypadClear.setOnClickListener(this);
        keypadAddToCart= (ImageButton) fragmentView.findViewById(R.id.btn_add_to_cart);
        keypadAddToCart.setOnClickListener(this);

        requestPayment = (Button) fragmentView.findViewById(R.id.request_payment);
        requestPayment.setOnClickListener(this);


        return fragmentView;
    }

    @Override
    public void onPause(){
        super.onPause();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(getString(R.string.navigation_crypto), selectedCrypto);
        editor.apply();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(Utilities.isNetworkConnectionAvailable(getActivity())) {

            ExchangeRates exchangeRates = ExchangeRates.getInstance();
            exchangeRates.updateExchangeRates(getActivity(), mLocalCurrency);
            exchangeRates.updateExchangeRatesCryptocompare(getActivity(), mSelectedCryptocurrencyToggleText);
        } else {
            // checks again and then displays... maybe do it manually without method!
            checkIfNetworkConnectionAvailable();
        }


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }


    @Override
    public void onClick(View v) {

        if(!(v instanceof EditText)) {
            productNameEditText.setCursorVisible(false);
        }

        if(v instanceof EditText) {
            productNameEditText.setCursorVisible(true);
        } else if(v instanceof LinearLayout) {
            if(mItemsInCart.size() > 0) {
                // only one clickable linear layout (items counter)
                DialogFragment myDialog = ShowItemFragment.newInstance(1, ShowItemFragment.DialogType.SHOW_CART_ITEMS_LIST, mItemsInCart);
                // for API >= 23 the title is disable by default -- we set a style that enables it
                myDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.ShowItemDialogFragment);
                myDialog.show(getFragmentManager(), getString(R.string.show_item_list_dialog_fragment_tag));
            }
        } else if(v instanceof ToggleButton) {
            // only one toggle button: currency converter

            if(mItemsInCart.size() == 0) {
                final ToggleButton currencyToggle = (ToggleButton) v;
                String currentAmount = totalAmountTextView.getText().toString();
                ExchangeRates exchangeRates = ExchangeRates.getInstance();

                if (exchangeRates.getLastUpdated() != null && exchangeRates.getLastUpdatedCryptocompare() != null && Utilities.isNetworkConnectionAvailable(getActivity()) ) {

                        if (!currencyToggle.isChecked()) {

                        // TODO since no items and amount is 0 these conversions are useless for now
                        // was local currency - convert to BTC


                            if (mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.BTC))) {
                                totalAmountTextView.setText(CurrencyUtils.getBtcFromLocalCurrency(currentAmount));
                                totalSecondaryAmountTextView.setText(CurrencyUtils.getLocalCurrencyFromBtc(currentAmount) + " " + mLocalCurrency);
                            } else {
                                totalAmountTextView.setText(CurrencyUtils.getOtherCryptosFromLocalCurrency(currentAmount));
                                totalSecondaryAmountTextView.setText(CurrencyUtils.getLocalCurrencyFromBtc(currentAmount) + " " + mLocalCurrency);
                            }


                        } else {
                        // TODO since no items and amount is 0 these conversions are useless for now
                        // was BTC - convert to local currency
                            if(mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.BTC))){
                                totalAmountTextView.setText(CurrencyUtils.getLocalCurrencyFromBtc(currentAmount));
                                totalSecondaryAmountTextView.setText(CurrencyUtils.getBtcFromLocalCurrency(currentAmount) + " " + String.valueOf(CurrencyUtils.CurrencyType.BTC));
                        }
                            else {
                                totalAmountTextView.setText(CurrencyUtils.getLocalCurrencyFromOtherCryptos(currentAmount));
                                totalSecondaryAmountTextView.setText(CurrencyUtils.getOtherCryptosFromLocalCurrency(currentAmount) + " " + mSelectedCryptocurrencyToggleText);
                            }
                        }

                } else {
                    // toggle failed -- toggle programmatically to revert
                    currencyToggle.toggle();

                    // checks network connection and then displays message with retry
                    if (!Utilities.isNetworkConnectionAvailable(getActivity())) {
                        Snackbar mesg = Snackbar.make(coordinatorLayout, R.string.network_connection_not_available_message, Snackbar.LENGTH_LONG)
                                .setAction(R.string.retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        currencyToggle.performClick();
                                    }

                                });
                        mesg.show();
                    } else {
                        // attempt to get exchange rates again
                        exchangeRates.updateExchangeRates(getActivity(), mLocalCurrency);
                        exchangeRates.updateExchangeRatesCryptocompare(getActivity(), mSelectedCryptocurrencyToggleText);
                        Toast.makeText(getActivity(), R.string.updating_exchange_rates, Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // item was already added and cannot have a different currency of another item
                Snackbar.make(coordinatorLayout, R.string.all_items_same_currency, Snackbar.LENGTH_LONG).show();
                //Toast.makeText(getContext(), R.string.all_items_same_currency, Toast.LENGTH_LONG).show();

                // toggle not allowed -- toggle programmatically to revert
                currencyToggle.toggle();
            }

        } else if(v instanceof ImageButton) {
            ImageButton imageButton = (ImageButton) v;
            switch (imageButton.getId()) {
                case  R.id.btn_backspace:
                    String currentAmount = amount.getText().toString();
                    // delete last digit
                    currentAmount = currentAmount.substring(0, currentAmount.length() - 1);
                    if (currentAmount.length() == 0)
                        amount.setText("0");
                    else
                        amount.setText(currentAmount);
                    break;
                case R.id.btn_clear:
                    amount.setText("0");
                    productNameEditText.setText("");
                    break;
                case R.id.btn_inv:
                    // check if Items db is empty
                    ItemHelper itemHelper = ItemHelper.getInstance(getActivity());
                    List<Item> allItems = itemHelper.getAll();
                    if(allItems.size() > 0) {
                        DialogFragment myDialog = ShowItemFragment.newInstance(1, ShowItemFragment.DialogType.SELECT_ITEM_LIST, allItems);
                        // for API >= 23 the title is disable by default -- we set a style that enables it
                        myDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.ShowItemDialogFragment);
                        myDialog.show(getFragmentManager(), getString(R.string.show_item_list_dialog_fragment_tag));
                    } else {
                        Snackbar mesg = Snackbar.make(coordinatorLayout, R.string.no_items_yet, Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.items), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ((MainActivity) getActivity()).getmViewPager().setCurrentItem(0);
                                    }
                                });
                        mesg.show();
                    }
                    break;
                case R.id.btn_add_to_cart:
                    if(Double.parseDouble(amount.getText().toString()) != 0) {
                        if (Utilities.isNetworkConnectionAvailable(getActivity())){
                        double cartItemAmount = CurrencyUtils.stringAmountToDouble(amount.getText().toString());
                        double cartTotalAmount = CurrencyUtils.stringAmountToDouble(totalAmountTextView.getText().toString());
                        double newCartTotalAmount = cartTotalAmount + cartItemAmount;
                        String newCartTotalAmountStr, newCartSecondaryTotalAmountStr;

                            if (currencyToggle.isChecked()) {

                                if (mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.BTC))) {
                                    newCartTotalAmountStr = CurrencyUtils.doubleAmountToString(newCartTotalAmount, CurrencyUtils.CurrencyType.LOCAL);
                                    newCartSecondaryTotalAmountStr = CurrencyUtils.getBtcFromLocalCurrency(newCartTotalAmountStr) + " " + String.valueOf(CurrencyUtils.CurrencyType.BTC);
                                } else {
                                    newCartTotalAmountStr = CurrencyUtils.doubleAmountToString(newCartTotalAmount, CurrencyUtils.CurrencyType.LOCAL);

                                    newCartSecondaryTotalAmountStr = CurrencyUtils.getOtherCryptosFromLocalCurrency(newCartTotalAmountStr) + " " + mSelectedCryptocurrencyToggleText;
                                }

                            } else {

                                if (mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.BTC))) {
                                    newCartTotalAmountStr = CurrencyUtils.doubleAmountToString(newCartTotalAmount, CurrencyUtils.CurrencyType.BTC);
                                    newCartSecondaryTotalAmountStr = CurrencyUtils.getLocalCurrencyFromBtc(newCartTotalAmountStr) + " " + mLocalCurrency;
                                } else {
                                    CurrencyUtils.CurrencyType cryptoCurrencySelected = CurrencyUtils.CurrencyType.BTC;
                                    if (mSelectedCryptocurrency.equals(CurrencyUtils.CurrencyType.BCH)) {
                                        cryptoCurrencySelected = CurrencyUtils.CurrencyType.BCH;
                                    //} else if (mSelectedCryptocurrency.equals(CurrencyUtils.CurrencyType.ETH)) {
                                    //    cryptoCurrencySelected = CurrencyUtils.CurrencyType.ETH;
                                    } else if (mSelectedCryptocurrency.equals(CurrencyUtils.CurrencyType.LTC)) {
                                        cryptoCurrencySelected = CurrencyUtils.CurrencyType.LTC;
                                    } else if (mSelectedCryptocurrency.equals(CurrencyUtils.CurrencyType.BTCTEST)) {
                                        cryptoCurrencySelected = CurrencyUtils.CurrencyType.BTCTEST;
                                    }
                                    newCartTotalAmountStr = CurrencyUtils.doubleAmountToString(newCartTotalAmount, cryptoCurrencySelected);
                                    newCartSecondaryTotalAmountStr = CurrencyUtils.getLocalCurrencyFromOtherCryptos(newCartTotalAmountStr) + " " + mLocalCurrency;
                                }

                            }


                        // is new amount amount valid?
                        if (!checkAmountAndDisplayIfError(newCartTotalAmountStr)) {
                            // valid amount - no error was displayed
                            int cartItemsCount = Integer.parseInt(totalItemsCountTextView.getText().toString());

                            totalAmountTextView.setText(newCartTotalAmountStr);
                            totalItemsCountTextView.setText(Integer.toString(cartItemsCount + 1));
                            totalSecondaryAmountTextView.setText(newCartSecondaryTotalAmountStr);

                            // create item and add to cart
                            Item item = new Item(null, productNameEditText.getText().toString(), "", cartItemAmount, 0, "", true, new Date());
                            mItemsInCart.add(item);

                            // reset amount and item name
                            amount.setText("0");
                            productNameEditText.setText("");
                        }

                        }
                        else {
                            // exchange rate is not available
                            Snackbar.make(coordinatorLayout, R.string.network_connection_not_available_message, Snackbar.LENGTH_LONG).show();
                        }
                    }
                    break;
            }

        } else {
            // it is a button!
            Button keypad = (Button) v;
            switch (keypad.getId()) {
                case R.id.btn_dot:
                    // if another dot does not exists
                    if (amount.getText().toString().indexOf(".") == -1) {
                        amount.append(".");
                    }

                    break;
                case R.id.btn_0:
                case R.id.btn_1:
                case R.id.btn_2:
                case R.id.btn_3:
                case R.id.btn_4:
                case R.id.btn_5:
                case R.id.btn_6:
                case R.id.btn_7:
                case R.id.btn_8:
                case R.id.btn_9:
                    if (amount.getText().toString().equals("0")) {
                        amount.setText(keypad.getText().toString());
                    } else {
                        String newAmount = amount.getText().toString() + keypad.getText().toString();
                        checkAmountAndDisplayIfError(newAmount);
                    }
                    break;

                case R.id.request_payment:
                    // update exchange rates for this or next payment request
                    ExchangeRates exchangeRates = ExchangeRates.getInstance();
                    exchangeRates.updateExchangeRates(getActivity(), mLocalCurrency);
                    exchangeRates.updateExchangeRatesCryptocompare(getActivity(), mSelectedCryptocurrencyToggleText);
                    //if (mBitcoinPaymentAddress.isEmpty() || !BitcoinAddressValidator.validate(mBitcoinPaymentAddress, testnetStatus)) {
                    // TODO add BCH validation
                    if ( selectedAddress.isEmpty() || (!AddressValidator.validate(selectedAddress, mSelectedCryptocurrency) && !mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.BCH)))) {

                            Snackbar mesg = Snackbar.make(coordinatorLayout, R.string.specify_valid_cryptocurrency_address_message, Snackbar.LENGTH_LONG)
                                    .setAction(getString(R.string.action_settings), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent goToSettings = new Intent(getActivity(), SettingsActivity.class);
                                            startActivity(goToSettings);
                                        }

                                    });
                        mesg.show();

                    } else if(Double.parseDouble(totalAmountTextView.getText().toString()) <= 0) {
                        Toast.makeText(getActivity(), R.string.amount_needs_to_be_positive, Toast.LENGTH_SHORT).show();
                    } else if(checkIfNetworkConnectionAvailable()) {   // check also displays toast with issue  TODO clean with else clause and simpler check

                        if(exchangeRates.getLastUpdated() != null && exchangeRates.getLastUpdatedCryptocompare() != null) {

                            String primaryAmount, secondaryAmount;
                            boolean isPrimaryAmount;
                            if (currencyToggle.isChecked()) {
                                // was local currency - convert to BTC
                                primaryAmount = totalAmountTextView.getText().toString(); //+ " " + mLocalCurrency;
                                //secondaryAmount = CurrencyUtils.getBtcFromLocalCurrency(totalAmountTextView.getText().toString()); // "(" + getBtcFromLocalCurrency(amount.getText().toString()) + " BTC)";
                                if(mSelectedCryptocurrency.equals(CurrencyUtils.CurrencyType.BTC.toString())) {
                                    secondaryAmount = CurrencyUtils.getBtcFromLocalCurrency(totalAmountTextView.getText().toString()); // "(" + getBtcFromLocalCurrency(amount.getText().toString()) + " BTC)";
                                }
                                else{
                                    secondaryAmount = CurrencyUtils.getOtherCryptosFromLocalCurrency(totalAmountTextView.getText().toString());
                                }
                                isPrimaryAmount = false;
                            } else {
                                // was BTC - convert to local currency
                                primaryAmount = totalAmountTextView.getText().toString(); // + " BTC";
                                //secondaryAmount = CurrencyUtils.getLocalCurrencyFromBtc(totalAmountTextView.getText().toString()); //"(" + getLocalCurrencyFromBtc(amount.getText().toString()) + " " + mLocalCurrency + ")";

                                if(mSelectedCryptocurrency.equals(CurrencyUtils.CurrencyType.BTC.toString()))
                                {
                                    secondaryAmount = CurrencyUtils.getLocalCurrencyFromBtc(totalAmountTextView.getText().toString()); //"(" + getLocalCurrencyFromBtc(amount.getText().toString()) + " " + mLocalCurrency + ")";
                                }
                                else{
                                    secondaryAmount = CurrencyUtils.getLocalCurrencyFromOtherCryptos(totalAmountTextView.getText().toString());
                                }

                                isPrimaryAmount = true;
                            }

                            double btcAmount = (isPrimaryAmount) ? Double.parseDouble(primaryAmount) : Double.parseDouble(secondaryAmount);

                            String exRateLocal = String.valueOf(exchangeRates.getBtcToLocalRate());
                            //query database to check if there is another tx with the same amount of BTC and for the same payment address
                            boolean statusOtherTxWithSameAmount = UpdateDbHelper.checkIfAlreadyCreatedTx(btcAmount,selectedAddress);
                            if(statusOtherTxWithSameAmount){
                                showDialogCannotCreateNewTransaction();
                            }
                            else {

                            DialogFragment myDialog = PaymentRequestFragment.newInstance(selectedAddress, mMerchantName, primaryAmount,
                                    secondaryAmount, isPrimaryAmount, mLocalCurrency, exRateLocal, convertCartItemsToString(mItemsInCart), mSelectedCryptocurrencyToggleText); //mSelectedCryptocurrency
                            // for API >= 23 the title is disable by default -- we set a style that enables it
                            myDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.RequestPaymentDialogFragment);
                            myDialog.show(getFragmentManager(), getString(R.string.request_payment_fragment_tag));
                            }

                        } else {
                            // exchange rate is not available
                            Snackbar mesg = Snackbar.make(coordinatorLayout, R.string.network_connection_not_available_message, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // programmatically click request_payment again
                                            requestPayment.performClick();
                                        }

                                    });
                            mesg.show();
                        }


                        break;
                    }

            }
        }


    }

    private void setSelectedItemNavigationList(int selectedItem){
        for(int i=0; i<radioGroup.getChildCount(); i++){
            RadioButton rb = (RadioButton) radioGroup.getChildAt(i);
            rb.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        RadioButton rb = (RadioButton) radioGroup.findViewById(selectedItem);
        rb.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    private String convertCartItemsToString(List<Item> mItemsInCart) {
        // TODO: put separator in a GenericUtils class ?
        StringBuilder idsStr = new StringBuilder();
        for(int i = 0; i < mItemsInCart.size(); i++) {
            if(i > 0)
                idsStr.append("[~|~]");
            idsStr.append(mItemsInCart.get(i).toString());
        }
        return idsStr.toString();
    }


    private boolean checkIfNetworkConnectionAvailable() {
        if(!Utilities.isNetworkConnectionAvailable(getActivity())) {
            Snackbar.make(coordinatorLayout, R.string.network_connection_not_available_message, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private boolean checkAmountAndDisplayIfError(String newAmount) {

        CurrencyUtils.CurrencyType cryptoCurrencySelected;
        if(mSelectedCryptocurrency.equals(CurrencyUtils.CurrencyType.BTC.toString())) {
            cryptoCurrencySelected = CurrencyUtils.CurrencyType.BTC;
        }
        else if(mSelectedCryptocurrency.equals(CurrencyUtils.CurrencyType.BCH.toString())) {
            cryptoCurrencySelected = CurrencyUtils.CurrencyType.BCH;
        }
        else if(mSelectedCryptocurrency.equals(CurrencyUtils.CurrencyType.BTCTEST.toString())) {
            cryptoCurrencySelected = CurrencyUtils.CurrencyType.BTCTEST;
        }
        else {
            cryptoCurrencySelected = CurrencyUtils.CurrencyType.LTC;
        }

        int error = CurrencyUtils.checkValidAmount(CurrencyUtils.stringAmountToDouble(newAmount),
                currencyToggle.isChecked() ? CurrencyUtils.CurrencyType.LOCAL : cryptoCurrencySelected);
        if (error > 0) {
            amount.setText(newAmount);
            return false;
        } else {
            if(error == -1) {
                Toast.makeText(getActivity(), R.string.amount_less_than_10000_message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.amount_has_more_decimals_than_allowed, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    @Override
    public void doWhenFragmentBecomesVisible() {
        // nothing to do when it becomes visible for now
        // TODO could update the exchange rates!
    }

    //


    private void showDialogCannotCreateNewTransaction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.other_pending_or_ongoing_transaction_title));
        builder.setMessage(getString(R.string.other_pending_or_ongoing_transaction_found_message));
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                       dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder.create();
        alert11.show();
    }

}
