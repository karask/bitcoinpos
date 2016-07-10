package com.cryptocurrencies.bitcoinpos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.vision.text.Text;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PaymentRequestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PaymentRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PaymentRequestFragment extends DialogFragment  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_BITCOIN_ADDRESS = "bitcoinAddress";
    private static final String ARG_MERCHANT_NAME = "merchantName";
    private static final String ARG_PRIMARY_AMOUNT = "primaryAmount";
    private static final String ARG_SECONDARY_AMOUNT = "secondaryAmount";

    // TODO: Rename and change types of parameters
    private String mBitcoinAddress;
    private String mMerchantName;
    private String mPrimaryAmount;
    private String mSecondaryAmount;

    private ImageView mQrCodeImage;
    private Bitmap mQrCodeBitmap;

    private TextView mMerchantNameTextView;
    private TextView mBitcoinAddressTextView;
    private TextView mPrimaryAmountTextView;
    private TextView mSecondaryAmountTextView;
    private TextView mPaymentTextView;
    private TextView mPaymentStatusTextView;

    private Button mCancelButton;

    private OnFragmentInteractionListener mListener;

    public PaymentRequestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bitcoinAddress Parameter 1.
     * @param merchantName Parameter 2.
     * @param primaryAmount
     * @param secondaryAmount
     * @return A new instance of fragment PaymentRequestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PaymentRequestFragment newInstance(String bitcoinAddress, String merchantName, String primaryAmount, String secondaryAmount) {
        PaymentRequestFragment fragment = new PaymentRequestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BITCOIN_ADDRESS, bitcoinAddress);
        args.putString(ARG_MERCHANT_NAME, merchantName);
        args.putString(ARG_PRIMARY_AMOUNT, primaryAmount);
        args.putString(ARG_SECONDARY_AMOUNT, secondaryAmount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mBitcoinAddress = getArguments().getString(ARG_BITCOIN_ADDRESS);
            mMerchantName = getArguments().getString(ARG_MERCHANT_NAME);
            mPrimaryAmount = getArguments().getString(ARG_PRIMARY_AMOUNT);
            mSecondaryAmount = getArguments().getString(ARG_SECONDARY_AMOUNT);

            //Find screen size
            WindowManager manager = (WindowManager) getActivity().getSystemService(getContext().WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3/4;

            // generate QR code to show in image view
            try {
                mQrCodeBitmap = encodeAsBitmap(mBitcoinAddress, smallerDimension); //(mBitcoinAddress);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
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
        mBitcoinAddressTextView.setText(mBitcoinAddress);

        mPrimaryAmountTextView = (TextView) fragmentView.findViewById(R.id.primary_amount);
        mPrimaryAmountTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        mPrimaryAmountTextView.setText(mPrimaryAmount);

        mSecondaryAmountTextView = (TextView) fragmentView.findViewById(R.id.secondary_amount);
        mSecondaryAmountTextView.setText(mSecondaryAmount);

        mCancelButton = (Button) fragmentView.findViewById(R.id.cancel_payment_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPaymentCancellation();
            }
        });

        mQrCodeImage = (ImageView) fragmentView.findViewById(R.id.qr_code_image);
        mQrCodeImage.setImageBitmap(mQrCodeBitmap);

        return fragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onPaymentCancellation();
        }
    }

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
        void onPaymentCancellation();
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
