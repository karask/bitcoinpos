package com.cryptocurrencies.bitcoinpos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kostas on 12/7/2016.
 */
public class TransactionHistoryDb extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "History.db";

    public static final String TRANSACTIONS_TABLE_NAME = "transactions";

    public static final String TRANSACTIONS_COLUMN_TX_ID = "transaction_id";
    public static final String TRANSACTIONS_COLUMN_BITCOIN_AMOUNT = "bitcoin_amount";
    public static final String TRANSACTIONS_COLUMN_LOCAL_AMOUNT = "local_amount";
    public static final String TRANSACTIONS_COLUMN_LOCAL_CURRENCY = "local_currency";
    public static final String TRANSACTIONS_COLUMN_CREATED_AT = "created_at";
    public static final String TRANSACTIONS_COLUMN_CONFIRMED_AT = "confirmed_at";
    public static final String TRANSACTIONS_COLUMN_MERCHANT_NAME = "merchant_name";
    public static final String TRANSACTIONS_COLUMN_BITCOIN_ADDRESS = "bitcoin_address";
    public static final String TRANSACTIONS_COLUMN_IS_CONFIRMED = "is_confirmed";

    private static final String TRANSACTIONS_TABLE_CREATE =
            "CREATE TABLE " + TRANSACTIONS_TABLE_NAME + " (" +
                    TRANSACTIONS_COLUMN_TX_ID + " TEXT PRIMARY KEY , " +
                    TRANSACTIONS_COLUMN_BITCOIN_AMOUNT + " REAL NOT NULL ,  " +
                    TRANSACTIONS_COLUMN_LOCAL_AMOUNT + " REAL NOT NULL , " +
                    TRANSACTIONS_COLUMN_LOCAL_CURRENCY + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_CREATED_AT + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_CONFIRMED_AT + " TEXT , " +
                    TRANSACTIONS_COLUMN_MERCHANT_NAME + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_BITCOIN_ADDRESS + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_IS_CONFIRMED + " NUMERIC NOT NULL )";

    private static final String TRANSACTIONS_TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TRANSACTIONS_TABLE_NAME;

    TransactionHistoryDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TRANSACTIONS_TABLE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now update is used for development. So we delete and recreate the table
        // losing all the data.
        db.execSQL(TRANSACTIONS_TABLE_DELETE);
        onCreate(db);

        // otherwise migration code would be required
    }

}
