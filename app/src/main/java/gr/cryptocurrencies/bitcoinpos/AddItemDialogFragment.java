package gr.cryptocurrencies.bitcoinpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link DialogFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddItemDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddItemDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddItemDialogFragment extends DialogFragment {
    private static final String ARG_ITEM_ID = "itemId";
    private static final String ARG_ITEM_NAME = "itemName";
    private static final String ARG_ITEM_PRICE = "itemPrice";

    private int mItemId;
    private String mItemName;
    private double mItemPrice;

    private OnFragmentInteractionListener mListener;

    private EditText mItemNameEditText, mItemPriceEditText;

    // use fragment to edit item (instead of adding one)
    private boolean mIsEditMode;

    public AddItemDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param itemId Item's id
     * @param itemName Name of the item
     * @param itemPrice Price of the item
     * @return A new instance of fragment AddItemDialogFragment.
     */
    public static AddItemDialogFragment newInstance(int itemId, String itemName, double itemPrice) {
        AddItemDialogFragment fragment = new AddItemDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_ID, itemId);
        args.putString(ARG_ITEM_NAME, itemName);
        args.putDouble(ARG_ITEM_PRICE, itemPrice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItemId = getArguments().getInt(ARG_ITEM_ID);
            mItemName = getArguments().getString(ARG_ITEM_NAME);
            mItemPrice = getArguments().getDouble(ARG_ITEM_PRICE);

            // edit requires db item_id; otherwise -1 is used to add
            mIsEditMode = mItemId >= 0;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.add_item))
                .setPositiveButton(getString(mIsEditMode ? R.string.update : R.string.add),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = mItemNameEditText.getText().toString();
                                String price = mItemPriceEditText.getText().toString();
                                if(name.isEmpty() || price.isEmpty()) {
                                    Toast.makeText(getContext(), R.string.name_price_cannot_be_empty, Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    mItemName = name;
                                    mItemPrice = Double.parseDouble(price);
                                }

                                mListener.onAddOrUpdateItemFragmentInteraction(mItemId, mItemName, mItemPrice);

                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onCancelItemFragmentInteraction();
                            }
                        });

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.fragment_add_item_dialog, null);

        mItemNameEditText = (EditText) view.findViewById(R.id.item_name_edittext);
        mItemPriceEditText = (EditText) view.findViewById(R.id.item_price_edittext);

        // if item name or amount is not empty then populate (Edit)
        if(mIsEditMode) {
            mItemNameEditText.setText(mItemName);
            mItemPriceEditText.setText(String.valueOf(mItemPrice));
        }
        builder.setView(view);

        return builder.create();
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
        void onAddOrUpdateItemFragmentInteraction(int itemId, String itemName, double itemPrice);
        void onCancelItemFragmentInteraction();
    }
}
