package com.cryptocurrencies.bitcoinpos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cryptocurrencies.bitcoinpos.database.TransactionHistoryDb;
import com.cryptocurrencies.bitcoinpos.network.Requests;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;


/**
 * Created by kostas on 14/6/2016.
 */
public class HistoryFragment extends ListFragment implements FragmentIsNowVisible, SwipeRefreshLayout.OnRefreshListener {

    private TransactionHistoryDb mDbHelper;
    private List<HashMap<String,String>> mTransactionHistoryItemList;
    private SwipeRefreshLayout mSwipeLayout;

    public HistoryFragment () {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        updateTransactionHistoryFromCursor(getTransactionHistoryDbCursor());
        updateTransactionHistoryView();
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_history, container, false);
        mSwipeLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.history_swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return fragmentView;
    }


    // implementing SwipeRefreshLayout.OnRefreshListener
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("HISTORY UPDATE", "Updating transaction history items!");
                updateConfirmedStatusTx(getTransactionHistoryDbCursor());
//                updateTransactionHistoryFromCursor(getTransactionHistoryDbCursor());
//                updateTransactionHistoryView();
                mSwipeLayout.setRefreshing(false);
            }
        }, 3000);
    }



    private void updateConfirmedStatusTx(Cursor c) {
        // for each unconfirmed transaction check if confirmed
        if(c.moveToFirst()) {
            do {
                if ("0".equals(c.getString(4))) {
                    String txId = c.getString(0);
                    updateIfTxConfirmed(txId);

                }
            } while (c.moveToNext());

        }
    }


    private Cursor getTransactionHistoryDbCursor() {
        // instantiate DB
        if(mDbHelper == null)
            mDbHelper = new TransactionHistoryDb(getContext());

        // Each row in the list stores amount and date of transaction -- retrieves history from DB
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // make sure transaction entry does not already exist:
        String[] tableColumns = { TransactionHistoryDb.TRANSACTIONS_COLUMN_TX_ID,
                TransactionHistoryDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT,
                TransactionHistoryDb.TRANSACTIONS_COLUMN_LOCAL_CURRENCY,
                TransactionHistoryDb.TRANSACTIONS_COLUMN_CREATED_AT,
                TransactionHistoryDb.TRANSACTIONS_COLUMN_IS_CONFIRMED };

        String sortOrder = TransactionHistoryDb.TRANSACTIONS_COLUMN_CREATED_AT + " DESC";
        Cursor c = db.query(TransactionHistoryDb.TRANSACTIONS_TABLE_NAME, tableColumns, null, null, null, null, sortOrder);

        return c;
    }


    private void updateTransactionHistoryFromCursor(Cursor c) {

        mTransactionHistoryItemList = new ArrayList<HashMap<String,String>>();

        if(c.moveToFirst()) {

            DateFormat dbDf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            dbDf.setTimeZone(TimeZone.getTimeZone("UTC"));
            DateFormat uiDf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            uiDf.setTimeZone(Calendar.getInstance().getTimeZone());
            do {
                HashMap<String, String> item = new HashMap<String, String>();
                //item.put(TransactionHistoryDb.TRANSACTIONS_COLUMN_TX_ID, c.getString(0));
                item.put(TransactionHistoryDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT, c.getString(1) + " " + c.getString(2));

                Date date = null;
                try {
                    date = dbDf.parse(c.getString(3));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                item.put(TransactionHistoryDb.TRANSACTIONS_COLUMN_CREATED_AT, date != null ? uiDf.format(date) : " -- ");

                int isConfirmedImage = "1".equals(c.getString(4)) ? R.drawable.ic_tick_green_24dp : R.drawable.ic_warning_orange_24dp;
                item.put(TransactionHistoryDb.TRANSACTIONS_COLUMN_IS_CONFIRMED, Integer.toString(isConfirmedImage));

                mTransactionHistoryItemList.add(item);
            } while (c.moveToNext());
        }

    }


    // TODO should only check and update UI instead of re-creating the adapter
    private void updateTransactionHistoryView() {
        // define key strings in hashmap
        String[] from = {
                TransactionHistoryDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT,
                TransactionHistoryDb.TRANSACTIONS_COLUMN_CREATED_AT,
                TransactionHistoryDb.TRANSACTIONS_COLUMN_IS_CONFIRMED
        };

        // define ids of view in list view fragment to bind to
        int[] to = { R.id.transaction_history_amount, R.id.transaction_history_date, R.id.transaction_history_is_confirmed };

        // Instantiating an adapter to store each items
        // R.layout.fragment_history defines the layout of each item
        SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), mTransactionHistoryItemList, R.layout.transaction_history_item , from, to);

        setListAdapter(adapter);
    }



    // TODO USED from both here and HistoryFragment ... move to another class that represents Blockr API !!!
    private void updateIfTxConfirmed(final String tx) {
        String url;
        if(BitcoinUtils.isMainNet()) {
            url = "http://btc.blockr.io/api/v1/tx/info/" + tx;
        } else {
            url = "http://tbtc.blockr.io/api/v1/tx/info/" + tx;
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("CONFIRMED TX 2", response.toString());
                        try {
                            if(response.getString("status").equals("success")) {
                                // get the last transaction for this amount on that address (if it exists)
                                int confirmations = response.getJSONObject("data").getInt("confirmations");
                                if(confirmations > 0) {
                                    // transaction was confirmed / update transaction history
                                    String confirmedAt = response.getJSONObject("data").getString("time_utc");
                                    if(updateTransactionToConfirmed(tx, confirmedAt)) {
                                        updateTransactionHistoryFromCursor(getTransactionHistoryDbCursor());
                                        updateTransactionHistoryView();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("CONFIRMED TX ERROR 2", error.toString());
                    }
                });

        Requests.getInstance(getContext()).addToRequestQueue(jsObjRequest);


    }


    // TODO DB method REPEATED AGAIN here and in PaymentRequestFragment !!!
    private boolean updateTransactionToConfirmed(String txId, String confirmedAt) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TransactionHistoryDb.TRANSACTIONS_COLUMN_IS_CONFIRMED, true);
        values.put(TransactionHistoryDb.TRANSACTIONS_COLUMN_CONFIRMED_AT, confirmedAt);

        // row to update
        String selection = TransactionHistoryDb.TRANSACTIONS_COLUMN_TX_ID + " LIKE ?";
        String[] selectioinArgs = {String.valueOf(txId) };


        int count = db.update(TransactionHistoryDb.TRANSACTIONS_TABLE_NAME, values, selection, selectioinArgs);
        if(count > 0)
            return true;
        else
            return false;
    }


    // implementng FragmentIsNotVisible, our interface so that view pager can act when one of its fragments becomes visible
    @Override
    public void doWhenFragmentBecomesVisible() {
        updateTransactionHistoryFromCursor(getTransactionHistoryDbCursor());
        updateTransactionHistoryView();
    }


}
