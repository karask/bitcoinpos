package com.cryptocurrencies.bitcoinpos;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by kostas on 14/6/2016.
 */
public class PaymentFragment extends Fragment implements View.OnClickListener {

    Button keypad1, keypad2, keypad3, keypad4, keypad5, keypad6, keypad7, keypad8, keypad9, keypad0, keypadDot, keypadBackspace;
    TextView amount;
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

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_payment, container, false);

        amount = (TextView) fragmentView.findViewById(R.id.amountTextView);

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

        return fragmentView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_backspace:
                break;
            default:
                if(amount.getText().equals("0")) {
                    amount.setText("1");
                } else {
                    amount.append("2");
                }

        }
    }
}
