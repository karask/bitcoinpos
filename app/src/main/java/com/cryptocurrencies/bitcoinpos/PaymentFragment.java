package com.cryptocurrencies.bitcoinpos;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by kostas on 14/6/2016.
 */
public class PaymentFragment extends Fragment implements View.OnClickListener {

    private final double maxBTCAmount = 1000;

    Button keypad1, keypad2, keypad3, keypad4, keypad5, keypad6, keypad7, keypad8, keypad9, keypad0, keypadDot, keypadBackspace;
    Button requestPayment;
    ToggleButton currencyToggle;
    TextView amount;
    CoordinatorLayout coordinatorLayout;

    SharedPreferences sharedPreferences;
    String bitcoinPaymentAddress ;

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // get preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String localCurrency = sharedPreferences.getString(getString(R.string.local_currency_key), "NaN");
        bitcoinPaymentAddress = sharedPreferences.getString(getString(R.string.payment_address_key), "");

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_payment, container, false);

        coordinatorLayout = (CoordinatorLayout) fragmentView.findViewById(R.id.coordinatorLayout);

        amount = (TextView) fragmentView.findViewById(R.id.amountTextView);

        currencyToggle = (ToggleButton) fragmentView.findViewById(R.id.currencyToggleButton);
        currencyToggle.setTextOff("BTC");
        currencyToggle.setTextOn(localCurrency);
        currencyToggle.setOnClickListener(this);

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
        keypadBackspace = (Button) fragmentView.findViewById(R.id.btn_backspace);
        keypadBackspace.setOnClickListener(this);

        requestPayment = (Button) fragmentView.findViewById(R.id.request_payment);
        requestPayment.setOnClickListener(this);

        return fragmentView;
    }

    @Override
    public void onClick(View v) {
        if(v instanceof ToggleButton) {
            ToggleButton  currencyToggle = (ToggleButton) v;
            String currentAmount = amount.getText().toString();
            if(currencyToggle.isChecked()) {
                // was local currency - convert to BTC
            } else {
                // was BTC - convert to local currency
            }
        } else {
            // it is a button!
            Button keypad = (Button) v;
            switch (keypad.getId()) {
                case R.id.btn_backspace:
                    String currentAmount = amount.getText().toString();
                    // delete last digit
                    currentAmount = currentAmount.substring(0, currentAmount.length() - 1);
                    if (currentAmount.length() == 0)
                        amount.setText("0");
                    else
                        amount.setText(currentAmount);
                    break;
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
                        if (checkValidAmount(newAmount)) {
                            amount.setText(newAmount);
                        } else {
                            Toast.makeText(getContext(), R.string.amount_less_than_1000_message, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                case R.id.request_payment:
                    if (bitcoinPaymentAddress.isEmpty() || BitcoinUtils.validateAddress(bitcoinPaymentAddress)) {
                        Snackbar mesg = Snackbar.make(coordinatorLayout, R.string.specify_valid_bitcoin_address_message, Snackbar.LENGTH_INDEFINITE)
                                .setAction("Settings", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent goToSettings = new Intent(getContext(), SettingsActivity.class);
                                startActivity(goToSettings);
                            }

                        });
                        mesg.show();
                    }
                    break;
            }
        }


    }

    private boolean checkValidAmount(String amount) {

        double doubleAmount = Double.parseDouble(amount);
        if(currencyToggle.isChecked()) {
            // was local currency
        } else {
            // was BTC
            if(doubleAmount > maxBTCAmount)
                return false;
        }


        return true;
    }
}
