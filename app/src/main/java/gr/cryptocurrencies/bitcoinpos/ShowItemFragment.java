package gr.cryptocurrencies.bitcoinpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import gr.cryptocurrencies.bitcoinpos.database.Item;
import gr.cryptocurrencies.bitcoinpos.database.ItemHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ShowItemFragment extends DialogFragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_DIALOG_TYPE = "dialog-type";
    private static final String ARG_ITEM_LIST = "item-list";

    public enum DialogType {
        SELECT_ITEM_LIST,
        SHOW_CART_ITEMS_LIST
    }

    // view objects
    RecyclerView mRecyclerView;

    private int mColumnCount = 1;
    private DialogType mDialogType;
    private List<Item> mItemList;

    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ShowItemFragment() {
    }

    public static ShowItemFragment newInstance(int columnCount, DialogType dialogType, List<Item> itemList) {
        ShowItemFragment fragment = new ShowItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_DIALOG_TYPE, dialogType.name());
        args.putParcelableArrayList(ARG_ITEM_LIST, (ArrayList) itemList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mDialogType = DialogType.valueOf(getArguments().getString(ARG_DIALOG_TYPE));
            if(mDialogType == DialogType.SHOW_CART_ITEMS_LIST) {
                // get from arguments
                mItemList = getArguments().getParcelableArrayList(ARG_ITEM_LIST);
            } else {       // DialogType.SELECT_ITEM_LIST
                // the items db is passed as well now
                mItemList = getArguments().getParcelableArrayList(ARG_ITEM_LIST);
//                // get all items from DB
//                ItemHelper itemHelper = ItemHelper.getInstance(getContext());
//                mItemList = itemHelper.getAll();
            }

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_showitem_list, null);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if(mDialogType == DialogType.SHOW_CART_ITEMS_LIST) {
            builder.setTitle(getString(R.string.cart));

            builder.setNegativeButton(getString(R.string.remove_all),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.onClearCartFragmentInteraction();
                        }
                    });

            builder.setPositiveButton(getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });
        } else {
            builder.setTitle(getString(R.string.choose_item));

            builder.setPositiveButton(getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });
        }

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new ShowItemRecyclerViewAdapter(mItemList, mListener));
        }

        builder.setView(view);
        return builder.create();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
    public interface OnListFragmentInteractionListener {
        void onShowItemSelectionFragmentInteraction(Item item);
        void onClearCartFragmentInteraction();
    }
}
