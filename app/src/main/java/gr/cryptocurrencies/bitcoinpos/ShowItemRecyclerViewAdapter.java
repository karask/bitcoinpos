package gr.cryptocurrencies.bitcoinpos;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import gr.cryptocurrencies.bitcoinpos.ShowItemFragment.OnListFragmentInteractionListener;
import gr.cryptocurrencies.bitcoinpos.database.Item;
import gr.cryptocurrencies.bitcoinpos.utilities.CurrencyUtils;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ShowItemRecyclerViewAdapter extends RecyclerView.Adapter<ShowItemRecyclerViewAdapter.ViewHolder> {

    private final List<Item> mValues;
    private final OnListFragmentInteractionListener mListener;

    public ShowItemRecyclerViewAdapter(List<Item> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_showitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mShowItemNameView.setText(mValues.get(position).getName());
        holder.mShowItemAmountView.setText(CurrencyUtils.doubleAmountToString(mValues.get(position).getAmount(), CurrencyUtils.CurrencyType.BTC)); // BTC, only because we allow 8 decimals

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onShowItemSelectionFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mShowItemNameView;
        public final TextView mShowItemAmountView;
        public Item mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mShowItemNameView = (TextView) view.findViewById(R.id.show_item_name);
            mShowItemAmountView = (TextView) view.findViewById(R.id.show_item_amount);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mShowItemNameView.getText() + "'";
        }
    }
}
