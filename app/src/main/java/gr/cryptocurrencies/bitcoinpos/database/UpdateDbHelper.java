package gr.cryptocurrencies.bitcoinpos.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static gr.cryptocurrencies.bitcoinpos.network.BlockchainInfoHelper.CUSTOM_BROADCAST_ACTION;


public class UpdateDbHelper {

    private static PointOfSaleDb mDbHelper;
    private static Context context;

    public UpdateDbHelper(Context context){
        this.context=context;
    }

    //send broadcast to refresh view in HistoryFragment and in PaymentRequestFragment
    public static void sendBroadcast(){
        Intent intent = new Intent(CUSTOM_BROADCAST_ACTION);
        context.sendBroadcast(intent);
    }

    public static boolean updateDbTransaction(String txId, String confirmedAt, int rowid, int previousStatus, int finalStatus){
        mDbHelper = PointOfSaleDb.getInstance(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String selection=null;
        String[] selectionArgs=new String[1];
        switch (previousStatus) {
            case TxStatus.PENDING:
                if(finalStatus==TxStatus.ONGOING) {
                    values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS, TxStatus.ONGOING);
                    values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID, txId);

                    selection = "_ROWID_" + " = ? ";
                    selectionArgs[0] = String.valueOf(rowid);

                }
                else if (finalStatus==TxStatus.CONFIRMED)
                {
                    values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS, TxStatus.CONFIRMED);
                    values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_CONFIRMED_AT, confirmedAt);
                    values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID, txId);

                    // row to update
                    selection = "_ROWID_" + " = ? ";
                    selectionArgs[0] = String.valueOf(rowid);

                }
                else if(finalStatus==TxStatus.CANCELLED){
                    values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS, TxStatus.CANCELLED);

                    selection = "_ROWID_" + " = ? ";
                    selectionArgs[0] =  String.valueOf(rowid);

                }
                break;
            case TxStatus.ONGOING:
                values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS, TxStatus.CONFIRMED);
                values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_CONFIRMED_AT, confirmedAt);

                // row to update
                selection = PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID + " LIKE ?";
                selectionArgs[0] = String.valueOf(txId);

                break;
        }

        int count = db.update(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, values, selection, selectionArgs);
        if(count > 0)
            return true;
        else
            return false;

    }


    //add Transaction method
    public static int addTransaction(String txId, double btcAmount,
                               double localAmount, String localCurrency,
                               String createdAt, String confirmedAt,
                               String merchantName, String bitcoinAddress,
                               int isConfirmed, String itemsNames,
                               String exchangeRate) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // make sure transaction entry does not already exist:  folder_name is null or
        String[] tableColumns = { PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID };
        String whereClause =  PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID + " IS NULL OR "+ PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID + " = ?";
        String[] whereArgs = { txId };
        if(txId==null){
            whereArgs =  new String[]{""};
        }

        Cursor c = db.query(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, tableColumns, whereClause, whereArgs, null, null, null);

        // if no row found with that txId add the transaction
        // check if count=0
        int rowId=0;
        if ( (c != null && c.getCount() == 0) || txId==null ) {
            ContentValues values = new ContentValues();
            if(txId==null) {
                values.putNull(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID);
            }
            else {
                values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID, txId);
            }
            values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_AMOUNT, btcAmount);
            values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT, localAmount);
            values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_CURRENCY, localCurrency);
            values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT, createdAt);
            values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_CONFIRMED_AT, confirmedAt);
            values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_MERCHANT_NAME, merchantName);
            values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_ADDRESS, bitcoinAddress);
            values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS, isConfirmed);
            values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_PRODUCT_NAME, itemsNames);
            values.put(PointOfSaleDb.TRANSACTIONS_COLUMN_EXCHANGE_RATE, exchangeRate);
            db.insert(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, null, values);

            rowId = getRowIdFromDb();
            //get row id to return
        }

        return rowId;
    }

    //used in addTransaction to get the rowId of just created Tx
    public static int getRowIdFromDb() {
        // get DB helper
        mDbHelper = PointOfSaleDb.getInstance(context);

        // Each row in the list stores amount and date of transaction -- retrieves history from DB
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // get the following columns:
        String[] tableColumns = { PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT,
                "_ROWID_"};// getting also _ROWID_ to save the txID after getting the response

        String sortOrder = PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT + " DESC";
        Cursor c = db.query(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, tableColumns, null, null, null, null, sortOrder);
        int rowId=0;
        if(c.moveToFirst()) {
            rowId = Integer.parseInt(c.getString(1));
        }
        return rowId;
    }

    //query the database and delete the selected row
    public static void getTransactionsAndDeleteAfterLongClick(int selectedItem) {
        // get DB helper
        mDbHelper = PointOfSaleDb.getInstance(context);

        // Each row in the list stores amount and date of transaction -- retrieves history from DB
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // get the following columns:
        String[] tableColumns = { PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT,
                "_ROWID_"};// getting also _ROWID_ to delete the selected tx

        String sortOrder = PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT + " DESC";
        Cursor c = db.query(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, tableColumns, null, null, null, null, sortOrder);
        //moving to position of the cursor according to the selected item to delete the transaction
        if(c.moveToPosition(selectedItem)) {
            int rowId = Integer.parseInt(c.getString(1));

            String selection = "_ROWID_" + " = ? ";
            String[] selectionArgs = {String.valueOf(rowId)};
            int count = db.delete(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, selection, selectionArgs);

            //send broadcast to update view
            sendBroadcast();
        }

    }

    //query the database and delete the pending transaction that was just created when cancel is clicked
    public static void getTransactionsAndDeleteAfterCancel() {
        // get DB helper
        mDbHelper = PointOfSaleDb.getInstance(context);

        // Each row in the list stores amount and date of transaction -- retrieves history from DB
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // get the following columns:
        String[] tableColumns = { PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT,
                "_ROWID_"};// getting also _ROWID_ to delete the selected tx

        String sortOrder = PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT + " DESC";
        Cursor c = db.query(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, tableColumns, null, null, null, null, sortOrder);
        //moving to first position to get last created transaction
        if(c.moveToFirst()) {
            int rowId = Integer.parseInt(c.getString(1));

            String selection = "_ROWID_" + " = ? ";
            String[] selectionArgs = {String.valueOf(rowId)};
            int count = db.delete(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, selection, selectionArgs);

            //send broadcast to update view
            sendBroadcast();
        }

    }

    //get all items in database and return Cursor
    public static Cursor getTransactionHistoryDbCursor() {
        // get DB helper
        mDbHelper = PointOfSaleDb.getInstance(context);

        // Each row in the list stores amount and date of transaction -- retrieves history from DB
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // get the following columns:
        String[] tableColumns = { PointOfSaleDb.TRANSACTIONS_COLUMN_TX_ID,
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_CURRENCY,
                PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS,
                PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_ADDRESS,
                "_ROWID_"};// getting also _ROWID_ to save the txID after getting the response

        String sortOrder = PointOfSaleDb.TRANSACTIONS_COLUMN_CREATED_AT + " DESC";
        Cursor c = db.query(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, tableColumns, null, null, null, null, sortOrder);

        return c;
    }

    //get status of transaction in Payment Request Fragment
    public static int queryDbTransactionStatusToRefreshView(int rowid) {
        //
        mDbHelper = PointOfSaleDb.getInstance(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = "_ROWID_" + " = ? ";
        String[] selectioinArgs = { String.valueOf(rowid) };

        // get the following columns:
        String[] tableColumns = { PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS};


        Cursor c = db.query(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, tableColumns, selection, selectioinArgs, null, null, null);

        int status=TxStatus.PENDING;//default to pending
        if(c.moveToFirst()) {
            status = Integer.parseInt(c.getString(0));
        }

        return status;
    }

    public static boolean checkIfAlreadyCreatedTx(double amount, String bitcoinAddress) {
        // get DB helper
        mDbHelper = PointOfSaleDb.getInstance(context);
        boolean[] result={false,false};//return if there is another tx already saved and tx status (pending or ongoing)

        // Each row in the list stores amount and date of transaction -- retrieves history from DB
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // get the following columns:
        String[] tableColumns = {
                PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_ADDRESS,
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_AMOUNT,
                PointOfSaleDb.TRANSACTIONS_COLUMN_LOCAL_CURRENCY,
                PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS};//

        String selection = PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_AMOUNT + " = ? AND " + PointOfSaleDb.TRANSACTIONS_COLUMN_BITCOIN_ADDRESS + " = ? " + " AND (" + PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS + " = ? OR " + PointOfSaleDb.TRANSACTIONS_COLUMN_TX_STATUS + " = ?)";
        String[] whereArgs = { String.valueOf(amount), bitcoinAddress, String.valueOf(TxStatus.PENDING), String.valueOf(TxStatus.ONGOING) };

        Cursor c = db.query(PointOfSaleDb.TRANSACTIONS_TABLE_NAME, tableColumns, selection, whereArgs, null, null,null);

        boolean queryStatus = false;
        if(c.moveToFirst()){
            //found at least one transaction with the same amount, return true
            queryStatus = true;
            return queryStatus;
        }
        else{
            //no transaction found with the same amount, return false
            queryStatus = false;
            return queryStatus;
        }

    }


}
