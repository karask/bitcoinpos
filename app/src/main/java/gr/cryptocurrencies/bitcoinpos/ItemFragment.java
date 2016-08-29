package gr.cryptocurrencies.bitcoinpos;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import gr.cryptocurrencies.bitcoinpos.database.Item;
import gr.cryptocurrencies.bitcoinpos.database.ItemHelper;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ItemFragment extends Fragment implements FragmentIsNowVisible {

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    private OnFragmentInteractionListener mListener;

    private List<Item> mAllItems;

    // views parameters
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;

    private RecyclerView.Adapter mRecyclerViewAdapter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    // used from MainActivity to get the recyclerview and adapter to update UI
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    // used from MainActivity to get the recyclerview and adapter to update UI
    public TextView getEmptyView() {
        return mEmptyView;
    }


    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemFragment newInstance(int columnCount) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        // get all items from DB
        ItemHelper itemHelper = ItemHelper.getInstance(getContext());
        mAllItems = itemHelper.getAll();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_recyclerView);
        mEmptyView = (TextView) view.findViewById(R.id.empty_textView);

        mRecyclerViewAdapter = new ItemRecyclerViewAdapter(mAllItems, mListener);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        // if no items make appropriate object visible
        if(mAllItems.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            // Set the adapter
            if (mRecyclerView instanceof RecyclerView) {
                Context context = view.getContext();
                //RecyclerView recyclerView = (RecyclerView) view;
                if (mColumnCount <= 1) {
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
            }
        }

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.add(Menu.NONE, R.id.add_item, 500, R.string.add_item);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setIcon(R.drawable.ic_plus_white_24dp);
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



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_item) {
            getNewItemDetails();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void getNewItemDetails() {
        DialogFragment myDialog = AddItemDialogFragment.newInstance(-1, "", 0);
        // for API >= 23 the title is disable by default -- we set a style that enables it
        myDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AddItemDialogFragment);
        myDialog.show(getFragmentManager(), getString(R.string.add_item_dialog_fragment_tag));
    }



    @Override
    public void doWhenFragmentBecomesVisible() {
        // nothing to do when it becomes visible for now
        // TODO could update the exchange rates!
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
        void onListItemClickFragmentInteraction(Item item);
    }
}
