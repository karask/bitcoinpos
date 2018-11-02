package gr.cryptocurrencies.bitcoinpos;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import gr.cryptocurrencies.bitcoinpos.database.PointOfSaleDb;
import gr.cryptocurrencies.bitcoinpos.database.TxStatus;
import gr.cryptocurrencies.bitcoinpos.database.UpdateDbHelper;
import gr.cryptocurrencies.bitcoinpos.network.BlockchainInfoHelper;

import gr.cryptocurrencies.bitcoinpos.network.BlockcypherHelper;
import gr.cryptocurrencies.bitcoinpos.network.RestBitcoinHelper;
import gr.cryptocurrencies.bitcoinpos.utilities.CurrencyUtils;
import gr.cryptocurrencies.bitcoinpos.utilities.DateUtilities;


import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;



public class HistoryFragment extends ListFragment implements FragmentIsNowVisible { //SwipeRefreshLayout.OnRefreshListener

    private PointOfSaleDb mDbHelper;
    private List<HashMap<String,String>> mTransactionHistoryItemList;
    private SwipeRefreshLayout mSwipeLayout;

    private Timer mTimer;
    private SharedPreferences sharedPref;

    private String mLocalCurrency=null;
    private boolean acceptTestnet;

    // datepicker values
    private int mStartYear, mStartMonth, mEndYear, mEndMonth;
    private int secondsAfterTxCreatedToCancel=3600;
    private int millisecondsIntervalToRefreshView=5000;
    // Write external storage permission code
    final private int WRITE_EXTERNAL_PERMISSION_CODE = 123;

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BlockchainInfoHelper.CUSTOM_BROADCAST_ACTION.equals(action)) {

                new RefreshHistoryView().execute("");
                //using this async task to update view, needs to pass an empty string
            }
        }
    };

    public HistoryFragment () {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //getting shared preferences
        //using getActivity instead of getContext, getContext was added in API 23
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mLocalCurrency = sharedPref.getString(getString(R.string.local_currency_key), getString(R.string.default_currency_code));
        acceptTestnet = sharedPref.getBoolean(getString(R.string.accept_testnet_key), false);

    }

    @Override
    public void onResume() {
        super.onResume();
        //refresh view
        updateTransactionHistoryFromCursor(UpdateDbHelper.getTransactionHistoryDbCursor());
        updateTransactionHistoryView();

        //set broadcast receiver to update history view when broadcast is received
        getActivity().registerReceiver(mReceiver,
                new IntentFilter(BlockchainInfoHelper.CUSTOM_BROADCAST_ACTION));

        // set invalid dates for report export
        mStartYear = -1;
        mStartMonth = -1;
        mEndYear = -1;
        mEndMonth = -1;

        // timer runs and updates the database //transferred from onCreate //is cancelled at onStop
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("HISTORY UPDATE", "Updating transaction history items!");
                updateStatusTx(UpdateDbHelper.getTransactionHistoryDbCursor());
                //update database items, if a change has happened to the status of a transaction, the history view is updated also

            }
        }, 1, millisecondsIntervalToRefreshView);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_history, container, false);
        // SwipeRefreshLayout removed
//        mSwipeLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.history_swipe_container);
//        mSwipeLayout.setOnRefreshListener(this);
//        mSwipeLayout.setColorSchemeResources(
//                R.color.colorPrimary,
//                R.color.colorAccent,
//                R.color.colorPrimaryDark);

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
//    @Override
//    public void onRefresh() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("HISTORY UPDATE", "Updating transaction history items!");
//                updateStatusTx(UpdateDbHelper.getTransactionHistoryDbCursor());
//                //update database items, if a change has happened to the status of a transaction, the history view is updated also
//
//                mSwipeLayout.setRefreshing(false);
//            }
//        }, 2000);
//    }


    private class RefreshHistoryView extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return params[0];
        }

        @Override
        public void onPostExecute(String result) {
            //get all database items, sort by date, create listview and update view
            updateTransactionHistoryFromCursor(UpdateDbHelper.getTransactionHistoryDbCursor());
            updateTransactionHistoryView();

        }
    }


    private void updateStatusTx(Cursor c) {

        if(c.moveToFirst()) {

            do {
                if ("0".equals(c.getString(4))) {//ongoing transaction
                    String txId = c.getString(0);
                    String bitcoinAddress = c.getString(6);
                    //double btcAmount = c.getDouble(5);
                    //double amountSatoshi = btcAmount * 100000000;
                    String crypto = c.getString(8);
                    if(crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.BTC))) {
                        boolean mainNet=true;
                        BlockchainInfoHelper.updateOngoingTxToConfirmedByTxId(txId,bitcoinAddress,mainNet);
                    }
                    else if (crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.BTCTEST))) {
                        boolean mainNet=false;
                        BlockchainInfoHelper.updateOngoingTxToConfirmedByTxId(txId,bitcoinAddress,mainNet);
                    }
                    else if (crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.LTC))) {
                        BlockcypherHelper.updateOngoingTxToConfirmedByTxId(txId,bitcoinAddress);
                    }
                    else if (crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.BCH))) {
                        RestBitcoinHelper.updateOngoingTxToConfirmedByTxId(txId,bitcoinAddress);
                    }

                }
                else if ("1".equals(c.getString(4))){//confirmed transaction
                    //tx is confirmed
                }
                else if ("2".equals(c.getString(4))) {//pending transaction
                    int timeUnixEpoch = Integer.parseInt(c.getString(3));//createdAt time
                    int timeDevice = (int) (System.currentTimeMillis()/1000);// get current time to check for cancelled transactions

                    //getting row id for checking pending transactions that dont have txID, when checked the transaction is updated and the txId is set
                    int rowId = c.getInt(7);

                    String bitcoinAddress = c.getString(6);
                    double btcAmount = c.getDouble(5);
                    int amountSatoshi = (int) Math.round(btcAmount * 100000000);//getting INT from DOUBLE value that was saved before, for precise value of satoshis

                    String crypto = c.getString(8);// get saved cryptocurrency from db

                    if(crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.BTC))) {
                        boolean mainNet = true;
                        BlockchainInfoHelper.updatePendingTxToOngoingOrConfirmed( bitcoinAddress, amountSatoshi, rowId, timeUnixEpoch, mainNet );
                    }
                    else if(crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.BTCTEST))) {
                        boolean mainNet = false;
                        BlockchainInfoHelper.updatePendingTxToOngoingOrConfirmed( bitcoinAddress, amountSatoshi, rowId, timeUnixEpoch, mainNet );
                    }
                    else if(crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.LTC))) {
                        BlockcypherHelper.updatePendingTxToOngoingOrConfirmed( bitcoinAddress, amountSatoshi, rowId, timeUnixEpoch );
                    }
                    else if(crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.BCH))) {
                        boolean mainNet = true;
                        RestBitcoinHelper.updatePendingTxToOngoingOrConfirmed( bitcoinAddress, amountSatoshi, rowId, timeUnixEpoch, mainNet );
                    }

                    if(timeDevice-timeUnixEpoch>secondsAfterTxCreatedToCancel){
                        UpdateDbHelper.updateDbTransaction(null, null, rowId, TxStatus.PENDING,TxStatus.CANCELLED);
                    }

                }

            } while (c.moveToNext());

        }

    }


    private void updateTransactionHistoryFromCursor(Cursor c) {

        mTransactionHistoryItemList = new ArrayList<HashMap<String,String>>();

        if (c.moveToFirst()) {

            do {
                HashMap<String, String> item = new HashMap<String, String>();
                // Get BTC double value and convert to string without scientific notation
                DecimalFormat df = new DecimalFormat("#.########");
                String btc8DecimalAmount = df.format(c.getDouble(5));
                String cryptoAcronym = c.getString(8);

                int isConfirmedImage;
                if("1".equals(c.getString(4))) {
                    isConfirmedImage = R.drawable.ic_tick_green_24dp;
                }
                else if("2".equals(c.getString(4))){
                    isConfirmedImage = R.drawable.ic_tx;
                }
                else if("3".equals(c.getString(4))){
                    isConfirmedImage = R.drawable.ic_x;
                }
                else {
                    isConfirmedImage = R.drawable.ic_warning_orange_24dp;
                }


                //item.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID, c.getString(0));
                item.put(PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT, c.getString(1) + " " + c.getString(2));
                item.put(PointOfSaleDb.TRANSACTIONS_COLUMN_CRYPTOCURRENCY_AMOUNT, btc8DecimalAmount + " " + cryptoAcronym);
                item.put(PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT, DateUtilities.getRelativeTimeString(c.getString(3)));
                item.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS, Integer.toString(isConfirmedImage));

                mTransactionHistoryItemList.add(item);


            } while (c.moveToNext());
        }

    }


    // TODO should only check and update UI instead of re-creating the adapter
    private void updateTransactionHistoryView() {
        // define key strings in hashmap
        String[] from = {
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CRYPTOCURRENCY_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS
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
            Toast.makeText(getActivity(), getString(R.string.no_transaction_yet), Toast.LENGTH_SHORT).show();
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
        mDbHelper = PointOfSaleDb.getInstance(getActivity());

        // Each row in the list stores amount and date of transaction -- retrieves history from DB
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // get the following columns:
        String[] tableColumns = { PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CRYPTOCURRENCY_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_CURRENCY,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_MERCHANT_NAME,
                PointOfSaleDb.TRANSACTIONS_COLUMN_PRODUCT_NAME,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CRYPTOCURRENCY_ADDRESS,
                PointOfSaleDb.TRANSACTIONS_COLUMN_EXCHANGE_RATE,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CRYPTOCURRENCY };

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
                PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS + " = 1";
                //PointOfSaleDb.TRANSACTIONS_COLUMN_IS_CONFIRMED + " = 1"; // previously checking if transaction is confirmed (==1)

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                    Toast.makeText(getActivity(), R.string.date_range_not_valid, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), R.string.no_transactions_in_date_range, Toast.LENGTH_SHORT).show();
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
                    "Cryptocurrency", // added Cryptocurrency column in .csv
                    "Cryptocurrency Amount", //"Bitcoin Amount",
                    "Local Amount",
                    "Local Currency",
                    "Exchange Rate (Cryptocurrency->Local)", //"Exchange Rate (BTC->Local)",
                    "Created At (UTC)",
                    "Merchant Name",
                    "Product Name",
                    "Cryptocurrency Address" // bitcoin address
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
                            c.getString(9), // added Cryptocurrency column in .csv
                            btc8DecimalAmount,
                            c.getString(2),
                            c.getString(3),
                            c.getString(8) == null ? "" : c.getString(8), // exchange rate
                            DateUtilities.getRelativeTimeString(c.getString(4)),     // date
                            c.getString(5),                                 // merchant name
                            productName,
                            c.getString(7)                                  //cryptocurrency address // bitcoin address
                    };

                    writer.writeNext(row);

                    // sum all (double) amounts
                    totalBitcoins += amount;
                } while (c.moveToNext());

            }

            // write total amount of bitcoins
            //String[] totalAmountString = { "Total in bitcoins: " + String.format("%.8f", totalBitcoins) };
            //writer.writeNext(totalAmountString);

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

    // implementing FragmentIsNotVisible, our interface so that view pager can act when one of its fragments becomes visible
    @Override
    public void doWhenFragmentBecomesVisible() {

    }



    @Override
    public void onPause(){
        super.onPause();

        if(mTimer != null){
            mTimer.cancel();
            //cancel timer task, assign null and stop checks
        }

        getActivity().unregisterReceiver(mReceiver);
        //unregister the receiver when paused, we dont need to check for status changes
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setting listeners for click to show info about the selected tx and long click to show dialog and ask to delete the tx
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                showDialogToDeleteTx(arg2);
                return true;
            }
        });

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getTransactionsAfterClick(i);
            }
        });
    }

    private void showDialogToDeleteTx(final int selectedItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.delete_transaction_message));
        builder.setCancelable(true);

        builder.setPositiveButton(
                getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //get method to delete transaction
                        UpdateDbHelper.getTransactionsAndDeleteAfterLongClick(selectedItem);
                    }
                });

        builder.setNegativeButton(
                getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder.create();
        alert11.show();
    }

    private void showInfoDialog(String btcAddress, String txId, String status, String crypto, double amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(txId==null) {
            txId = getString(R.string.no_id);
        }

        String title = getString(R.string.bitcoin_tx_title);
        String acronymCrypto = String.valueOf(CurrencyUtils.CurrencyType.BTC);
        String paymentCryptoType = String.valueOf(CurrencyUtils.CurrencyType.BTC);
        if(crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.BTC))) {
            title = getString(R.string.bitcoin_tx_title);
            acronymCrypto = String.valueOf(CurrencyUtils.CurrencyType.BTC);
            paymentCryptoType = acronymCrypto;
        }
        else if(crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.BCH))) {
            title = getString(R.string.bitcoin_cash_tx_title);
            acronymCrypto = String.valueOf(CurrencyUtils.CurrencyType.BCH);
            paymentCryptoType = acronymCrypto;
        }
        else if(crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.LTC))) {
            title = getString(R.string.litecoin_tx_title);
            acronymCrypto = String.valueOf(CurrencyUtils.CurrencyType.LTC);
            paymentCryptoType = acronymCrypto;
        }
        else if(crypto.equals(String.valueOf(CurrencyUtils.CurrencyType.BTCTEST))) {
            title = getString(R.string.bitcoin_testnet_tx_title);
            acronymCrypto = getString(R.string.btctest);
            paymentCryptoType = getString(R.string.bitcoin_testnet_show_in_title);
        }


        DecimalFormat formatter = new DecimalFormat("#.########", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        String amountStr = formatter.format(amount);
        String info = "\n" +getString(R.string.payment_address) + " (" + paymentCryptoType + ")" + ":\n" + btcAddress + "\n\n" + getString(R.string.transaction_id) + ":\n" + txId + "\n\n" +
                getString(R.string.payment_amount) + ":\n" + amountStr + " " + acronymCrypto + "\n\n" + getString(R.string.transaction_status) + ":\n" + status;

        builder.setTitle(title);
        builder.setMessage(info);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getTransactionsAfterClick(id);
                    }
                });


        AlertDialog alert11 = builder.create();
        alert11.show();
    }


    //get transaction info to show in dialog after clicking on an item of the history listview
    private void getTransactionsAfterClick(int selectedItem) {
        // get DB helper
        mDbHelper = PointOfSaleDb.getInstance(getActivity());

        // Each row in the list stores amount and date of transaction -- retrieves history from DB
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // get the following columns:
        String[] tableColumns = { PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CRYPTOCURRENCY_ADDRESS,
                PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS,
                "_ROWID_",// getting also _ROWID_ to delete the selected tx
                PointOfSaleDb.TRANSACTIONS_COLUMN_CRYPTOCURRENCY,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CRYPTOCURRENCY_AMOUNT    };

        String sortOrder = PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT + " DESC";
        Cursor c = db.query(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, tableColumns, null, null, null, null, sortOrder);
        //moving to position of the cursor according to the selected item to delete the transaction
        if(c.moveToPosition(selectedItem)) {
            String crypto = c.getString(5);
            String status;
            if(c.getInt(3)==0){status=getString(R.string.payment_unconfirmed);}
            else if(c.getInt(3)==1){status=getString(R.string.payment_confirmed);}
            else if(c.getInt(3)==2){status=getString(R.string.payment_pending);}
            else {status=getString(R.string.payment_cancelled);}

            double amount = c.getDouble(6);

            //show dialog
            showInfoDialog(c.getString(2),c.getString(1),status, crypto, amount);
        }

    }

}
