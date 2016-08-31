package gr.cryptocurrencies.bitcoinpos;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import gr.cryptocurrencies.bitcoinpos.database.Item;
import gr.cryptocurrencies.bitcoinpos.database.ItemHelper;
import gr.cryptocurrencies.bitcoinpos.network.ExchangeRates;
import gr.cryptocurrencies.bitcoinpos.network.Utilities;
import gr.cryptocurrencies.bitcoinpos.utilities.BitcoinUtils;
import gr.cryptocurrencies.bitcoinpos.utilities.CurrencyUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kostas on 14/6/2016.
 */
public class PaymentFragment extends Fragment implements View.OnClickListener, FragmentIsNowVisible {

    CoordinatorLayout coordinatorLayout;
    LinearLayout totalItemsCountLinearLayout;
    Button keypad1, keypad2, keypad3, keypad4, keypad5, keypad6, keypad7, keypad8, keypad9, keypad0, keypadDot;
    EditText productNameEditText;
    ImageButton keypadBackspace, keypadInventory, keypadClear, keypadAddToCart;
    Button requestPayment;
    ToggleButton currencyToggle;
    TextView amount;
    TextView totalAmountTextView;
    TextView totalItemsCountTextView;

    SharedPreferences mSharedPreferences;
    String mLocalCurrency;
    String mBitcoinPaymentAddress;
    String mMerchantName;

    List<Item> mItemsInCart = new ArrayList<>();

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // get preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mLocalCurrency = mSharedPreferences.getString(getString(R.string.local_currency_key), getString(R.string.default_currency_code));
        mBitcoinPaymentAddress = mSharedPreferences.getString(getString(R.string.payment_address_key), "");
        mMerchantName = mSharedPreferences.getString(getString(R.string.merchant_name_key), "");

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_payment, container, false);

        coordinatorLayout = (CoordinatorLayout) fragmentView.findViewById(R.id.coordinatorLayout);

        totalItemsCountLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.total_items_count_linear_layout);
        totalItemsCountLinearLayout.setOnClickListener(this);

        amount = (TextView) fragmentView.findViewById(R.id.amountTextView);
        totalAmountTextView = (TextView) fragmentView.findViewById(R.id.totalAmountTextView);
        totalItemsCountTextView = (TextView) fragmentView.findViewById(R.id.totalItemsCountTextView);

        currencyToggle = (ToggleButton) fragmentView.findViewById(R.id.currencyToggleButton);
        currencyToggle.setTextOff("BTC");
        currencyToggle.setTextOn(mLocalCurrency);
        currencyToggle.toggle();
        currencyToggle.setOnClickListener(this);

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(Utilities.isNetworkConnectionAvailable(getContext())) {
            ExchangeRates exchangeRates = ExchangeRates.getInstance();
            exchangeRates.updateExchangeRates(getContext(), mLocalCurrency);
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

                if (exchangeRates.getLastUpdated() != null) {
                    if (currentAmount != "0") {
                        if (!currencyToggle.isChecked()) {
                            // was local currency - convert to BTC
                            totalAmountTextView.setText(CurrencyUtils.getBtcFromLocalCurrency(currentAmount));
                        } else {
                            // was BTC - convert to local currency
                            totalAmountTextView.setText(CurrencyUtils.getLocalCurrencyFromBtc(currentAmount));
                        }
                    }
                } else {
                    // toggle failed -- toggle programmatically to revert
                    currencyToggle.toggle();

                    // checks network connection and then displays message with retry
                    if (!Utilities.isNetworkConnectionAvailable(getContext())) {
                        Snackbar mesg = Snackbar.make(coordinatorLayout, R.string.network_connection_not_available_message, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        currencyToggle.performClick();
                                    }

                                });
                        mesg.show();
                    } else {
                        // attempt to get exchange rates again
                        exchangeRates.updateExchangeRates(getContext(), mLocalCurrency);
                        Toast.makeText(getContext(), R.string.updating_exchange_rates, Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // item was already addedff
                Toast.makeText(getContext(), R.string.all_items_same_currency, Toast.LENGTH_LONG).show();

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
                    ItemHelper itemHelper = ItemHelper.getInstance(getContext());
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
                        double cartItemAmount = CurrencyUtils.stringAmountToDouble(amount.getText().toString());
                        double cartTotalAmount = CurrencyUtils.stringAmountToDouble(totalAmountTextView.getText().toString());
                        double newCartTotalAmount = cartTotalAmount + cartItemAmount;
                        String newCartTotalAmountStr = CurrencyUtils.doubleAmountToString(newCartTotalAmount,
                                currencyToggle.isChecked() ? CurrencyUtils.CurrencyType.LOCAL : CurrencyUtils.CurrencyType.BTC);

                        // is new amount amount valid?
                        if (!checkAmountAndDisplayIfError(newCartTotalAmountStr)) {
                            // valid amount - no error was displayed
                            int cartItemsCount = Integer.parseInt(totalItemsCountTextView.getText().toString());

                            totalAmountTextView.setText(Double.toString(newCartTotalAmount));
                            totalItemsCountTextView.setText(Integer.toString(cartItemsCount + 1));

                            // create item and add to cart
                            Item item = new Item(null, productNameEditText.getText().toString(), "", cartItemAmount, 0, "", true, new Date());
                            mItemsInCart.add(item);

                            // reset amount and item name
                            amount.setText("0");
                            productNameEditText.setText("");
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
                    exchangeRates.updateExchangeRates(getContext(), mLocalCurrency);

                    if (mBitcoinPaymentAddress.isEmpty() || !BitcoinUtils.validateAddress(mBitcoinPaymentAddress)) {
                        Snackbar mesg = Snackbar.make(coordinatorLayout, R.string.specify_valid_bitcoin_address_message, Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.action_settings), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent goToSettings = new Intent(getContext(), SettingsActivity.class);
                                startActivity(goToSettings);
                            }

                        });
                        mesg.show();
                    } else if(totalAmountTextView.getText().toString().equals("0")) {
                        Toast.makeText(getContext(), R.string.amount_cannot_be_zero, Toast.LENGTH_SHORT).show();
                    } else if(checkIfNetworkConnectionAvailable()) {   // check also displays toast with issue  TODO clean with else clause and simpler check

                        if(exchangeRates.getLastUpdated() != null) {

                            String primaryAmount, secondaryAmount;
                            boolean isPrimaryAmount;
                            if (currencyToggle.isChecked()) {
                                // was local currency - convert to BTC
                                primaryAmount = totalAmountTextView.getText().toString(); //+ " " + mLocalCurrency;
                                secondaryAmount = CurrencyUtils.getBtcFromLocalCurrency(totalAmountTextView.getText().toString()); // "(" + getBtcFromLocalCurrency(amount.getText().toString()) + " BTC)";
                                isPrimaryAmount = false;
                            } else {
                                // was BTC - convert to local currency
                                primaryAmount = totalAmountTextView.getText().toString(); // + " BTC";
                                secondaryAmount = CurrencyUtils.getLocalCurrencyFromBtc(totalAmountTextView.getText().toString()); //"(" + getLocalCurrencyFromBtc(amount.getText().toString()) + " " + mLocalCurrency + ")";
                                isPrimaryAmount = true;
                            }

                            DialogFragment myDialog = PaymentRequestFragment.newInstance(mBitcoinPaymentAddress, mMerchantName, primaryAmount,
                                    secondaryAmount, isPrimaryAmount, mLocalCurrency, String.valueOf(exchangeRates.getBtcToLocalRate()), convertCartItemsToString(mItemsInCart));
                            // for API >= 23 the title is disable by default -- we set a style that enables it
                            myDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.RequestPaymentDialogFragment);
                            myDialog.show(getFragmentManager(), getString(R.string.request_payment_fragment_tag));
                        } else {
                            // exchange rate is not available
                            Snackbar mesg = Snackbar.make(coordinatorLayout, R.string.network_connection_not_available_message, Snackbar.LENGTH_INDEFINITE)
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
        if(!Utilities.isNetworkConnectionAvailable(getContext())) {
            Snackbar.make(coordinatorLayout, R.string.network_connection_not_available_message, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private boolean checkAmountAndDisplayIfError(String newAmount) {
        int error = CurrencyUtils.checkValidAmount(CurrencyUtils.stringAmountToDouble(newAmount),
                currencyToggle.isChecked() ? CurrencyUtils.CurrencyType.LOCAL : CurrencyUtils.CurrencyType.BTC);
        if (error > 0) {
            amount.setText(newAmount);
            return false;
        } else {
            if(error == -1) {
                Toast.makeText(getContext(), R.string.amount_less_than_10000_message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.amount_has_more_decimals_than_allowed, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    @Override
    public void doWhenFragmentBecomesVisible() {
        // nothing to do when it becomes visible for now
        // TODO could update the exchange rates!
    }
}
