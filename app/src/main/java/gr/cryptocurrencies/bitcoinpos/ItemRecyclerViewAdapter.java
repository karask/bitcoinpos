package gr.cryptocurrencies.bitcoinpos;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import gr.cryptocurrencies.bitcoinpos.ItemFragment.OnFragmentInteractionListener;
import gr.cryptocurrencies.bitcoinpos.database.Item;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder> {

    private final List<Item> mValues;
    private final OnFragmentInteractionListener mListener;

    public ItemRecyclerViewAdapter(List<Item> items, OnFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mItemNameView.setText(mValues.get(position).getName());
        holder.mItemAmountView.setText(Double.toString(mValues.get(position).getAmount()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListItemClickFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void addItem(Item item) {
        mValues.add(item);
        this.notifyItemInserted(mValues.size() - 1);
    }

    public void removeItem(int id) {
        int indexOfId = getIndexInView(id);
        if(indexOfId >= 0) {
            mValues.remove(indexOfId);
            this.notifyItemRemoved(indexOfId);
        }
    }

    public void updateItem(Item item) {
        int indexOfId = getIndexInView(item.getItemId());
        mValues.set(indexOfId, item);
        this.notifyItemChanged(indexOfId);
    }

    public void updateItemsList(List<Item> newlist) {
        mValues.clear();
        mValues.addAll(newlist);
        this.notifyDataSetChanged();
    }

    private int getIndexInView(int itemId) {
        // find indexOfId
        int indexOfId = -1;
        for (int i = 0; i < mValues.size(); i++) {
            if (mValues.get(i).getItemId() == itemId) {
                indexOfId = i;
                break;
            }
        }
        return indexOfId;
    }






    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mItemNameView;
        public final TextView mItemAmountView;
        public Item mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mItemNameView = (TextView) view.findViewById(R.id.item_name);
            mItemAmountView = (TextView) view.findViewById(R.id.item_amount);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItemNameView.getText() + "'";
        }
    }


}
