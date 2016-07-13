package com.cryptocurrencies.bitcoinpos;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by kostas on 14/6/2016.
 */
public class HistoryFragment extends ListFragment {

    private TransactionHistoryDb mDbHelper;
    private List<HashMap<String,String>> mTransactionHistoryItemList;

    public HistoryFragment () {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateTransactionHistoryFromDb();
    }

    @Override
    public void onResume() {
//        updateTransactionHistoryFromDb();
//        updateTransactionHistoryView();
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        updateTransactionHistoryView();
        return super.onCreateView(inflater, container, savedInstanceState);
    }



    private void updateTransactionHistoryFromDb() {
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
        SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), mTransactionHistoryItemList, R.layout.fragment_history, from, to);

        setListAdapter(adapter);
    }

}
