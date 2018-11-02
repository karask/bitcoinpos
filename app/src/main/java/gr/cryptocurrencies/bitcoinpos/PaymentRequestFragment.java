package gr.cryptocurrencies.bitcoinpos;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import gr.cryptocurrencies.bitcoinpos.database.PointOfSaleDb;
import gr.cryptocurrencies.bitcoinpos.database.TxStatus;
import gr.cryptocurrencies.bitcoinpos.database.UpdateDbHelper;

import gr.cryptocurrencies.bitcoinpos.network.BlockchainInfoHelper;
import gr.cryptocurrencies.bitcoinpos.network.BlockcypherHelper;
import gr.cryptocurrencies.bitcoinpos.network.Requests;
import gr.cryptocurrencies.bitcoinpos.network.RestBitcoinHelper;
import gr.cryptocurrencies.bitcoinpos.utilities.BitcoinUtils;
import gr.cryptocurrencies.bitcoinpos.utilities.CurrencyUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PaymentRequestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PaymentRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PaymentRequestFragment extends DialogFragment  {

    private static final String ARG_CRYPTOCURRENCY_ADDRESS = "cryptocurrencyAddress";
    private static final String ARG_MERCHANT_NAME = "merchantName";
    private static final String ARG_PRIMARY_AMOUNT = "primaryAmount";
    private static final String ARG_SECONDARY_AMOUNT = "secondaryAmount";
    private static final String ARG_CRYPTOCURRENCY_IS_PRIMARY = "cryptocurrencyIsPrimary";
    private static final String ARG_LOCAL_CURRENCY = "localCurrency";
    private static final String ARG_EXCHANGE_RATE = "exchangeRate";
    private static final String ARG_ITEMS_NAMES = "itemsNames";
    private static final String ARG_CRYPTOCURRENCY = "cryptocurrency";

    private String mCryptocurrencyAddress;
    private String mMerchantName;
    private String mPrimaryAmount;
    private String mSecondaryAmount;
    private boolean mCryptocurrencyIsPrimary;
    private String mLocalCurrency;
    private String mExchangeRate;
    private String mItemsNames;
    private String mCryptocurrency;

    private ImageView mQrCodeImage;
    private Bitmap mQrCodeBitmap;

    private TextView mMerchantNameTextView;
    private TextView mBitcoinAddressTextView;
    private TextView mPrimaryAmountTextView;
    private TextView mSecondaryAmountTextView;
    private TextView mPaymentTextView;
    private TextView mPaymentStatusTextView;

    private Button mCancelButton;

    private Timer mTimer;
    private String mPaymentTransaction = null;
    private int _rowID;

    SharedPreferences sharedPref;
    RadioGroup radiogroup;

    private PointOfSaleDb mDbHelper;

    private OnFragmentInteractionListener mListener;

    public PaymentRequestFragment() {
        // Required empty public constructor
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BlockchainInfoHelper.CUSTOM_BROADCAST_ACTION.equals(action)) {
                //get row id of the transaction that was just created and update view when there is a change in tx status
                String rowIdToString = String.valueOf(_rowID);
                new RefreshHistoryView().execute(rowIdToString);
            }
        }
    };

    private class RefreshHistoryView extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return params[0];
        }

        @Override
        public void onPostExecute(String rowId) {
            //update view
            int rowID = Integer.parseInt(rowId);

            //check status of transaction that has just been updated and update view
            if(UpdateDbHelper.queryDbTransactionStatusToRefreshView(rowID)==TxStatus.ONGOING){
                mPaymentStatusTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                mPaymentStatusTextView.setText(R.string.payment_unconfirmed);

                mCancelButton.setText(getString(R.string.ok));//set cancel button text to ok
            }
            else if(UpdateDbHelper.queryDbTransactionStatusToRefreshView(rowID)==TxStatus.CONFIRMED){
                mPaymentStatusTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.brightGreen));
                mPaymentStatusTextView.setText(R.string.payment_confirmed);
                //tx is confirmed and timer can be cancelled
                mTimer.cancel();
            }
        }
    }

    private class ShowSelected extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return params[0];
        }

        @Override
        public void onPostExecute(String rowId) {
            //update view
            //String selected = sharedPref.getString("selectedCrypto", String.valueOf(CurrencyUtils.CurrencyType.BTC));

            //Toast.makeText(getActivity(), selected, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bitcoinAddress Parameter 1.
     * @param merchantName Parameter 2.
     * @param primaryAmount
     * @param secondaryAmount
     * @param bitcoinIsPrimary
     * @param localCurrency
     * @param exchangeRate
     * @param itemsNames
     * @return A new instance of fragment PaymentRequestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PaymentRequestFragment newInstance(String bitcoinAddress, String merchantName, String primaryAmount, String secondaryAmount, boolean bitcoinIsPrimary, String localCurrency, String exchangeRate, String itemsNames, String cryptocurrency) {
        PaymentRequestFragment fragment = new PaymentRequestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CRYPTOCURRENCY_ADDRESS, bitcoinAddress);
        args.putString(ARG_MERCHANT_NAME, merchantName);
        args.putString(ARG_PRIMARY_AMOUNT, primaryAmount);
        args.putString(ARG_SECONDARY_AMOUNT, secondaryAmount);
        args.putBoolean(ARG_CRYPTOCURRENCY_IS_PRIMARY, bitcoinIsPrimary);
        args.putString(ARG_LOCAL_CURRENCY, localCurrency);
        args.putString(ARG_EXCHANGE_RATE, exchangeRate);
        args.putString(ARG_ITEMS_NAMES, itemsNames);
        args.putString(ARG_CRYPTOCURRENCY, cryptocurrency);

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mCryptocurrencyAddress = getArguments().getString(ARG_CRYPTOCURRENCY_ADDRESS);
            mMerchantName = getArguments().getString(ARG_MERCHANT_NAME);
            mPrimaryAmount = getArguments().getString(ARG_PRIMARY_AMOUNT);
            mSecondaryAmount = getArguments().getString(ARG_SECONDARY_AMOUNT);
            mCryptocurrencyIsPrimary = getArguments().getBoolean(ARG_CRYPTOCURRENCY_IS_PRIMARY);
            mLocalCurrency = getArguments().getString(ARG_LOCAL_CURRENCY);
            mExchangeRate = getArguments().getString(ARG_EXCHANGE_RATE);
            mItemsNames = getArguments().getString(ARG_ITEMS_NAMES);
            mCryptocurrency = getArguments().getString(ARG_CRYPTOCURRENCY);

            mDbHelper = PointOfSaleDb.getInstance(getActivity());


            //Find screen size
            WindowManager manager = (WindowManager) getActivity().getSystemService(getContext().WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3/4;

            String uriCrypto;
            if(mCryptocurrency.equals(getString(R.string.btc))) { uriCrypto = getString(R.string.bitcoin); }
            else if(mCryptocurrency.equals(getString(R.string.bch))) { uriCrypto = getString(R.string.bitcoin_cash); }
            else if(mCryptocurrency.equals(getString(R.string.ltc))) { uriCrypto = getString(R.string.litecoin); }
            else { uriCrypto = getString(R.string.bitcoin_testnet); }
            // generate QR code to show in image view
            try {
                // generate BIP 21 compatible payment URI

                String bip21Payment = uriCrypto + ":" + mCryptocurrencyAddress +
                        "?amount=" + (mCryptocurrencyIsPrimary ? mPrimaryAmount : mSecondaryAmount) +
                        "&label=" + URLEncoder.encode(mMerchantName, "UTF-8");
                mQrCodeBitmap = encodeAsBitmap(bip21Payment, smallerDimension); //(mBitcoinAddress);
            } catch (WriterException we) {
                we.printStackTrace();
            } catch (UnsupportedEncodingException uee) {
                uee.printStackTrace();
            }

            //set the correct value to btc and local currency for the transaction
            Double setPrimary, setSecondary;
            if(mCryptocurrencyIsPrimary){
                setPrimary = Double.parseDouble(mPrimaryAmount);
                setSecondary = Double.parseDouble(mSecondaryAmount);
            }
            else {
                setPrimary = Double.parseDouble(mSecondaryAmount);
                setSecondary = Double.parseDouble(mPrimaryAmount);
            }

            //get system time and convert to UNIX Epoch to get the created time of the transaction
            int systemTimeNow = (int) (System.currentTimeMillis()/1000);

            radiogroup = (RadioGroup) getActivity().findViewById(R.id.radiogroup);
            View radioButton = radiogroup.findViewById(radiogroup.getCheckedRadioButtonId());
            selected = radiogroup.indexOfChild(radioButton);

            String cryptocurrency="";
            if(selected==0)      { cryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BTC); }
            else if(selected==1) { cryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BCH); }
            else if(selected==2) { cryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.LTC); }
            else if(selected==3) { cryptocurrency = getString(R.string.btctest); }

            //Toast.makeText(getActivity(), String.valueOf(selected), Toast.LENGTH_SHORT).show();

            final int rowID = UpdateDbHelper.addTransaction(mPaymentTransaction, setPrimary, setSecondary, mLocalCurrency,
                    String.valueOf(systemTimeNow), null, mMerchantName, mCryptocurrencyAddress, TxStatus.PENDING, // payment is requested and the transaction is set to pending
                    mItemsNames, mExchangeRate, cryptocurrency);

            _rowID = rowID;

            //send broadcast to update History View
            BlockchainInfoHelper.sendBroadcast();

            
        }
    }
    int selected;
    @Override
    public void onResume() {
        super.onResume();

        //register mReceiver to update view when there is a status change
        getActivity().registerReceiver(mReceiver,
                new IntentFilter(BlockchainInfoHelper.CUSTOM_BROADCAST_ACTION));

        // setup timer to check network for unconfirmed/confirmed transactions
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //new ShowSelected().execute("");
                double amount;
                if (mCryptocurrencyIsPrimary) {
                    amount = Double.parseDouble(mPrimaryAmount);
                } else {
                    amount = Double.parseDouble(mSecondaryAmount);
                }
                //TODO to check if done correctly
                int amountSatoshi = (int) Math.round(amount * 100000000);
                int timeNowEpoch = (int) (System.currentTimeMillis()/1000);//IMPORTANT USE PARENTHESIS!!
                if (mPaymentTransaction == null) {
                    Log.d("CHECK FOR ONG/CONFIRM", amount + "!");
                    boolean mainNet;
                    switch (selected) {
                        case 0: //radiobutton 1 , BTC
                            mainNet = true;
                            BlockchainInfoHelper.updatePendingTxToOngoingOrConfirmed(mCryptocurrencyAddress, amountSatoshi, _rowID, timeNowEpoch, mainNet);
                        break;
                        case 1: //radiobutton 2 , BCH
                            mainNet = true;
                            RestBitcoinHelper.updatePendingTxToOngoingOrConfirmed(mCryptocurrencyAddress, amountSatoshi, _rowID, timeNowEpoch, mainNet);
                            break;
                        case 2: //radiobutton 3 , LTC
                            mainNet=true;
                            BlockcypherHelper.updatePendingTxToOngoingOrConfirmed(mCryptocurrencyAddress, amountSatoshi, _rowID, timeNowEpoch);
                            break;
                        case 3: //radiobutton 4 , BTC TESTNET
                            mainNet = false;
                            BlockchainInfoHelper.updatePendingTxToOngoingOrConfirmed(mCryptocurrencyAddress, amountSatoshi, _rowID, timeNowEpoch, mainNet);
                            break;
                    }
                } else {
                    Log.d("CHECK FOR CONFIRMED", mPaymentTransaction);

                    boolean mainNet;
                    switch (selected) {
                        case 0://radiobutton 1 , BTC
                            mainNet = true;
                            BlockchainInfoHelper.updateOngoingTxToConfirmedByTxId(mPaymentTransaction, mCryptocurrencyAddress, mainNet);
                            break;
                        case 1://radiobutton 1 , BCH
                            mainNet = true;
                            RestBitcoinHelper.updateOngoingTxToConfirmedByTxId(mPaymentTransaction, mCryptocurrencyAddress);
                            break;
                        case 2://radiobutton 1 , LTC
                            mainNet = true;
                            BlockcypherHelper.updateOngoingTxToConfirmedByTxId(mPaymentTransaction, mCryptocurrencyAddress);
                            break;
                        case 3://radiobutton 1 , BTC TEST
                            mainNet = false;
                            BlockchainInfoHelper.updateOngoingTxToConfirmedByTxId(mPaymentTransaction, mCryptocurrencyAddress, mainNet);
                            break;

                    }
                }
            }
        }, 0, 3000);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_payment_request, container, false);

        mMerchantNameTextView = (TextView) fragmentView.findViewById(R.id.merchant_name);
        mMerchantNameTextView.setText(mMerchantName);
        mMerchantNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        mMerchantNameTextView.setTextSize(getResources().getDimension(R.dimen.button_text_size_large));

        mPaymentTextView = (TextView) fragmentView.findViewById(R.id.payment_text);
        mPaymentTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        mPaymentTextView.setTextSize(getResources().getDimension(R.dimen.button_text_size_normal));

        mPaymentStatusTextView = (TextView) fragmentView.findViewById(R.id.payment_status);
        mPaymentStatusTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
        mPaymentStatusTextView.setTextSize(getResources().getDimension(R.dimen.button_text_size_normal));

        mBitcoinAddressTextView = (TextView) fragmentView.findViewById(R.id.bitcoin_address);
        mBitcoinAddressTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        mBitcoinAddressTextView.setTextSize(getResources().getDimension(R.dimen.button_text_size_small));
        mBitcoinAddressTextView.setText(mCryptocurrencyAddress);

        // TODO put "BTC" in strings.xml

        String primaryAmountToDisplay, secondaryAmountToDisplay;
        if (mCryptocurrencyIsPrimary) {
            //primaryAmountToDisplay = mPrimaryAmount + " " + String.valueOf(CurrencyUtils.CurrencyType.BTC);
            primaryAmountToDisplay = mPrimaryAmount + " " + mCryptocurrency;
            secondaryAmountToDisplay = "(" + mSecondaryAmount + " " + mLocalCurrency + ")";
        } else {
            primaryAmountToDisplay = mPrimaryAmount + " " + mLocalCurrency;
            //secondaryAmountToDisplay = "(" + mSecondaryAmount + " " +String.valueOf(CurrencyUtils.CurrencyType.BTC) + ")";
            secondaryAmountToDisplay = "(" + mSecondaryAmount + " " + mCryptocurrency + ")";
        }

        mPrimaryAmountTextView = (TextView) fragmentView.findViewById(R.id.primary_amount);
        mPrimaryAmountTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        mPrimaryAmountTextView.setText(primaryAmountToDisplay);

        mSecondaryAmountTextView = (TextView) fragmentView.findViewById(R.id.secondary_amount);
        mSecondaryAmountTextView.setText(secondaryAmountToDisplay);

        mCancelButton = (Button) fragmentView.findViewById(R.id.cancel_payment_button);
        mCancelButton.setText(getString(R.string.cancel));
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete transaction when cancel button is clicked
                if(mCancelButton.getText().equals(getString(R.string.cancel))) {
                    UpdateDbHelper.getTransactionsAndDeleteAfterCancel();
                }

                // call parents method to close dialog fragment
                mListener.onPaymentRequestFragmentClose(true);
            }
        });

        mQrCodeImage = (ImageView) fragmentView.findViewById(R.id.qr_code_image);
        mQrCodeImage.setImageBitmap(mQrCodeBitmap);


        return fragmentView;
    }

    //not using now onActivityCreated, timer transferred to onResume
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//
//        super.onActivityCreated(savedInstanceState);
//    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @Override
    public void onPause(){
        super.onPause();

        if(mTimer != null){
            mTimer.cancel();
            //cancel timer task, stop checks
        }

        getActivity().unregisterReceiver(mReceiver);
        //unregister receiver on pause
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onPaymentRequestFragmentClose(boolean isCancelled);
    }



    Bitmap encodeAsBitmap(String str, int size) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, size, size);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

//    Bitmap encodeAsBitmap(String str) throws WriterException {
//        String contentsToEncode = str;
//        if (contentsToEncode == null) {
//            return null;
//        }
//        Map<EncodeHintType,Object> hints = null;
//        String encoding = guessAppropriateEncoding(contentsToEncode);
//        if (encoding != null) {
//            hints = new EnumMap<>(EncodeHintType.class);
//            hints.put(EncodeHintType.CHARACTER_SET, encoding);
//        }
//        BitMatrix result;
//        try {
//            result = new MultiFormatWriter().encode(contentsToEncode, BarcodeFormat.QR_CODE, 240, 240, hints);
//        } catch (IllegalArgumentException iae) {
//            // Unsupported format
//            return null;
//        }
//        int width = result.getWidth();
//        int height = result.getHeight();
//        int[] pixels = new int[width * height];
//        for (int y = 0; y < height; y++) {
//            int offset = y * width;
//            for (int x = 0; x < width; x++) {
//                pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
//            }
//        }
//
//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//        return bitmap;
//    }
//
//    private static String guessAppropriateEncoding(CharSequence contents) {
//        // Very crude at the moment
//        for (int i = 0; i < contents.length(); i++) {
//            if (contents.charAt(i) > 0xFF) {
//                return "UTF-8";
//            }
//        }
//        return null;
//    }

}
