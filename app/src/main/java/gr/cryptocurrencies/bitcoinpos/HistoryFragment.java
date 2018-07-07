package gr.cryptocurrencies.bitcoinpos;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import gr.cryptocurrencies.bitcoinpos.database.PointOfSaleDb;
import gr.cryptocurrencies.bitcoinpos.network.Requests;
import gr.cryptocurrencies.bitcoinpos.utilities.BitcoinUtils;
import gr.cryptocurrencies.bitcoinpos.utilities.DateUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by kostas on 14/6/2016.
 */
public class HistoryFragment extends ListFragment implements FragmentIsNowVisible, SwipeRefreshLayout.OnRefreshListener {

    private PointOfSaleDb mDbHelper;
    private List<HashMap<String,String>> mTransactionHistoryItemList;
    private SwipeRefreshLayout mSwipeLayout;

    // datepicker values
    private int mStartYear, mStartMonth, mEndYear, mEndMonth;

    // Write external storage permission code
    final private int WRITE_EXTERNAL_PERMISSION_CODE = 123;

    public HistoryFragment () {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTransactionHistoryFromCursor(getTransactionHistoryDbCursor());
        updateTransactionHistoryView();
        // set invalid dates for report export
        mStartYear = -1;
        mStartMonth = -1;
        mEndYear = -1;
        mEndMonth = -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_history, container, false);
        mSwipeLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.history_swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark);

        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.add(Menu.NONE, R.id.transactions_report, 500, R.string.transactions_report);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setIcon(R.drawable.ic_file_chart_white_24dp);
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
        }, 2000);
    }



    private void updateConfirmedStatusTx(Cursor c) {
        // for each unconfirmed transaction check if confirmed
        if(c.moveToFirst()) {
            do {
                if ("0".equals(c.getString(4))) {
                    String txId = c.getString(0);
                    String bitcoinAddress = c.getString(6);
                    double btcAmount = c.getDouble(5);
                    updateIfTxConfirmed(txId, bitcoinAddress, btcAmount);

                }
            } while (c.moveToNext());

        }
    }


    private Cursor getTransactionHistoryDbCursor() {
        // get DB helper
        mDbHelper = PointOfSaleDb.getInstance(getContext());

        // Each row in the list stores amount and date of transaction -- retrieves history from DB
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // get the following columns:
        String[] tableColumns = { PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID,
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_CURRENCY,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_IS_CONFIRMED,
                PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_ADDRESS };

        String sortOrder = PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT + " DESC";
        Cursor c = db.query(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, tableColumns, null, null, null, null, sortOrder);

        return c;
    }


    private void updateTransactionHistoryFromCursor(Cursor c) {

        mTransactionHistoryItemList = new ArrayList<HashMap<String,String>>();

        if (c.moveToFirst()) {

            do {
                HashMap<String, String> item = new HashMap<String, String>();
                // Get BTC double value and convert to string without scientific notation
                DecimalFormat df = new DecimalFormat("#.########");
                String btc8DecimalAmount = df.format(c.getDouble(5));

                //item.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID, c.getString(0));
                item.put(PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT, c.getString(1) + " " + c.getString(2));
                item.put(PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_AMOUNT, btc8DecimalAmount + " BTC");
                item.put(PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT, DateUtilities.getRelativeTimeString(c.getString(3)));

                int isConfirmedImage = "1".equals(c.getString(4)) ? R.drawable.ic_tick_green_24dp : R.drawable.ic_warning_orange_24dp;
                item.put(PointOfSaleDb.TRANSACTIONS_COLUMN_IS_CONFIRMED, Integer.toString(isConfirmedImage));

                mTransactionHistoryItemList.add(item);
            } while (c.moveToNext());
        }

    }


    // TODO should only check and update UI instead of re-creating the adapter
    private void updateTransactionHistoryView() {
        // define key strings in hashmap
        String[] from = {
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_IS_CONFIRMED
        };

        // define ids of view in list view fragment to bind to
        int[] to = { R.id.transaction_history_amount, R.id.transaction_history_btc_amount, R.id.transaction_history_date, R.id.transaction_history_is_confirmed };

        // checking that activity is not null before proceeding since sometime through the lifecycle of the fragment
        // the getActivity() returns null!
        if(getActivity() != null) {
            // Instantiating an adapter to store each items -- R.layout.fragment_history defines the layout of each item
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), mTransactionHistoryItemList, R.layout.transaction_history_item, from, to);
            setListAdapter(adapter);
        }

    }


    // TODO USED from both here and PaymentRequestFragment ... move to another class that represents Blockchain.Info API !!!
    private void updateIfTxConfirmed(final String tx, final String bitcoinAddress, final double amount) {
        String url;
        if(BitcoinUtils.isMainNet()) {
            url = "https://blockchain.info/rawaddr/" + bitcoinAddress;
        } else {
            url = "https://testnet.blockchain.info/rawaddr/" + bitcoinAddress;
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("CONFIRMED TX 2", response.toString());
                        try {
                            if(response.has("address") && response.getString("address").equals(bitcoinAddress)) {
                                JSONArray allTxs = (JSONArray) response.getJSONArray("txs");
                                JSONObject ongoingTx;
                                for(int i=0; i<allTxs.length(); i++) {
                                    JSONObject trx = (JSONObject) allTxs.get(i);
                                    if (trx.getString("hash").equals(tx) && trx.has("block_height")) {
                                        // transaction was confirmed / update transaction history
                                        String confirmedAt = trx.getString("time");
                                        if (updateTransactionToConfirmed(tx, confirmedAt)) {
                                            updateTransactionHistoryFromCursor(getTransactionHistoryDbCursor());
                                            updateTransactionHistoryView();
                                        }
                                    }
                                    break;
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
        values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_IS_CONFIRMED, true);
        values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_CONFIRMED_AT, confirmedAt);

        // row to update
        String selection = PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID + " LIKE ?";
        String[] selectioinArgs = {String.valueOf(txId) };


        int count = db.update(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, values, selection, selectioinArgs);
        if(count > 0)
            return true;
        else
            return false;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.transactions_report) {
            getTransactionsReport();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getTransactionsReport() {
        if(mTransactionHistoryItemList.size() == 0) {
            Toast.makeText(getContext(), getString(R.string.no_transaction_yet), Toast.LENGTH_SHORT).show();
        } else {
            showDatePicker();
        }
    }


    // display dialog with explanation of why the permission is needed
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), okListener)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();
    }


    private Cursor getTransactionHistoryInRangeCursor(int startYear, int startMonth, int endYear, int endMonth) {
        // get DB helper
        mDbHelper = PointOfSaleDb.getInstance(getContext());

        // Each row in the list stores amount and date of transaction -- retrieves history from DB
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // get the following columns:
        String[] tableColumns = { PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID,
                PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_CURRENCY,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_MERCHANT_NAME,
                PointOfSaleDb.TRANSACTIONS_COLUMN_PRODUCT_NAME,
                PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_ADDRESS,
                PointOfSaleDb.TRANSACTIONS_COLUMN_EXCHANGE_RATE };

        Calendar start = Calendar.getInstance();
        start.set(startYear, startMonth, 1, 0,0);
        long startInUnixTime = start.getTimeInMillis() / 1000L;
        Calendar end = Calendar.getInstance();
        end.set(endYear, endMonth, 31, 0,0);
        long endInUnixTime = end.getTimeInMillis() / 1000L;

        // date in db used to be yyyy-mm-dd ...
        //String paddedStartMonth = String.format("%02d", mStartMonth +1); // +1 since index starts from 0
        //String paddedEndMonth = String.format("%02d", mEndMonth +1 +1);  // 2nd +1 for sql <= text comparison
        String whereClause = PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT + " >= " + startInUnixTime + " and " +
                PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT + " <= " + endInUnixTime + " and " +
                PointOfSaleDb.TRANSACTIONS_COLUMN_IS_CONFIRMED + " = 1";

        String sortOrder = PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT + " DESC";
        Cursor c = db.query(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, tableColumns, whereClause, null, null, null, sortOrder);

        return c;
    }

    /**
     * Displays the start and end date picker dialog
     */
    private void showDatePicker() {
        // Inflate your custom layout containing 2 DatePickers
        LayoutInflater inflater = (LayoutInflater) getLayoutInflater(null);
        View customView = inflater.inflate(R.layout.custom_date_picker, null);

        // Define your date pickers
        final DatePicker dpStartDate = (DatePicker) customView.findViewById(R.id.dpStartDate);
        // remove day selection
        ((ViewGroup) dpStartDate).findViewById(Resources.getSystem().getIdentifier("day", "id", "android")).setVisibility(View.GONE);

        final DatePicker dpEndDate = (DatePicker) customView.findViewById(R.id.dpEndDate);
        // remove day selection
        ((ViewGroup) dpEndDate).findViewById(Resources.getSystem().getIdentifier("day", "id", "android")).setVisibility(View.GONE);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(customView); // Set the view of the dialog to your custom layout
        builder.setTitle(getString(R.string.select_time_period));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean areDatesValid = checkValidDates(dpStartDate.getYear(), dpStartDate.getMonth(), dpEndDate.getYear(), dpEndDate.getMonth());
                if(areDatesValid) {
                    mStartYear = dpStartDate.getYear();
                    mStartMonth = dpStartDate.getMonth();
                    mEndYear = dpEndDate.getYear();
                    mEndMonth = dpEndDate.getMonth();
                    getStoragePermission();
                } else {
                    Toast.makeText(getContext(), R.string.date_range_not_valid, Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // set invalid dates for report export
                mStartYear = -1;
                mStartMonth = -1;
                mEndYear = -1;
                mEndMonth = -1;
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        builder.create().show();
    }

    private boolean checkValidDates(int startYear, int startMonth, int endYear, int endMonth) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
            String startDateStr = String.format("%d-%02d", startYear, (startMonth +1)); // +1 since first index is 0
            Date startDate = dateFormat.parse(startDateStr);
            String endDateStr = String.format("%d-%02d", endYear, (endMonth +1));
            Date endDate = dateFormat.parse(endDateStr);
            if(startDate.compareTo(endDate) > 0)
                return false;
            else
                return true;
        } catch (ParseException pe) {
            pe.printStackTrace();
        }

        return false;
    }


    private void getStoragePermission() {
        if(mStartYear != -1) {
            Cursor transactionsInRange = getTransactionHistoryInRangeCursor(mStartYear, mStartMonth, mEndYear, mEndMonth);

            if(transactionsInRange.getCount() == 0) {
                Toast.makeText(getContext(), R.string.no_transactions_in_date_range, Toast.LENGTH_SHORT).show();
            } else {
                if(Build.VERSION.SDK_INT < 23) {
                    exportReportInRange(mStartYear, mStartMonth, mEndYear, mEndMonth, transactionsInRange);
                } else {
                    // check for write external storage permission
                    int hasWriteExternalStorage = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (hasWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showMessageOKCancel(getString(R.string.access_external_storage_required_message),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    WRITE_EXTERNAL_PERMISSION_CODE);
                                        }
                                    });
                            return;
                        }
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_EXTERNAL_PERMISSION_CODE);
                        return;
                    } else {
                        exportReportInRange(mStartYear, mStartMonth, mEndYear, mEndMonth, transactionsInRange);
                    }
                }
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    exportReportInRange(mStartYear, mStartMonth, mEndYear, mEndMonth, null);
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), R.string.no_access_to_download_folder, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void exportReportInRange(int startYear, int startMonth, int endYear, int endMonth, Cursor confirmedTxsInRange) {
        Cursor c;
        StringBuilder csvStr = new StringBuilder();
        double totalBitcoins = 0;

        // get Downloads folders path and construct csv filename
        File downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        int counter = 1;
        File csvFile;
        csvFile = new File(downloadsPath.toString() + "/BitcoinPoS-" + counter + ".csv");
        while(csvFile.exists()) {
            counter++;
            csvFile = new File(downloadsPath.toString() + "/BitcoinPoS-" + counter + ".csv");
        }

        try {
            FileOutputStream fos = new FileOutputStream(csvFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            CSVWriter writer = new CSVWriter(osw);

            // add headers
            String[] headers = {
                    "Transaction Id",
                    "Bitcoin Amount",
                    "Local Amount",
                    "Local Currency",
                    "Exchange Rate (BTC->Local)",
                    "Created At (UTC)",
                    "Merchant Name",
                    "Product Name",
                    "Bitcoin Address"
            };
            writer.writeNext(headers);

            if (confirmedTxsInRange == null)
                c = getTransactionHistoryInRangeCursor(startYear, startMonth, endYear, endMonth);
            else
                c = confirmedTxsInRange;

            // for each tx enter a row in csv file
            // for each unconfirmed transaction check if confirmed
            if (c.moveToFirst()) {
                do {
                    // Get BTC double value and convert to string without scientific notation
                    DecimalFormat df = new DecimalFormat("#.########");
                    double amount = c.getDouble(1);
                    String btc8DecimalAmount = df.format(amount);

                    // Get only product name from "name[-|-]0.5" strings that DB contains
                    String dbProduct = c.getString(6) == null ? "" : c.getString(6);
                    String productName = dbProduct.substring(0, dbProduct.lastIndexOf("[-|-]"));

                    String[] row = {
                            c.getString(0),
                            btc8DecimalAmount,
                            c.getString(2),
                            c.getString(3),
                            c.getString(8) == null ? "" : c.getString(8), // exchange rate
                            DateUtilities.getRelativeTimeString(c.getString(4)),     // date
                            c.getString(5),                                 // merchant name
                            productName,
                            c.getString(7)                                  // bitcoin address
                    };

                    writer.writeNext(row);

                    // sum all (double) amounts
                    totalBitcoins += amount;
                } while (c.moveToNext());

            }

            // write total amount of bitcoins
            String[] totalAmountString = { "Total in bitcoins: " + String.format("%.8f", totalBitcoins) };
            writer.writeNext(totalAmountString);

            writer.close();
            osw.close();
            fos.close();

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        Toast.makeText(getActivity(), getString(R.string.csv_file_saved, csvFile.getName()), Toast.LENGTH_LONG).show();
    }


    // implementng FragmentIsNotVisible, our interface so that view pager can act when one of its fragments becomes visible
    @Override
    public void doWhenFragmentBecomesVisible() {
        Cursor c = getTransactionHistoryDbCursor();
        updateConfirmedStatusTx(c);
        updateTransactionHistoryFromCursor(c);
        updateTransactionHistoryView();
    }


}
