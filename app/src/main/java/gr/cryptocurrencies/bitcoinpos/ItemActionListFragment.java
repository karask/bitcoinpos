package gr.cryptocurrencies.bitcoinpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import gr.cryptocurrencies.bitcoinpos.database.Item;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ItemActionListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ItemActionListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemActionListFragment extends DialogFragment {
    private static final String ARG_ITEM_ID = "item_id";

    private int mItemId;

    private OnFragmentInteractionListener mListener;

    public ItemActionListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param itemId Parameter 1.
     * @return A new instance of fragment ItemActionListFragment.
     */
    public static ItemActionListFragment newInstance(String itemId) {
        ItemActionListFragment fragment = new ItemActionListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID, itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItemId = Integer.valueOf(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.select))
                .setItems(R.array.item_list_actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0) {
                            // edit action
                            mListener.onEditItemAction(mItemId);
                        } else {
                            // delete action
                            mListener.onDeleteItemAction(mItemId);
                        }
                    }
                });

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
        void onEditItemAction(int itemId);
        void onDeleteItemAction(int itemId);
    }
}
