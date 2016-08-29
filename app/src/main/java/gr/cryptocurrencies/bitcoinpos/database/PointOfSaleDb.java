package gr.cryptocurrencies.bitcoinpos.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kostas on 12/7/2016.
 */
public class PointOfSaleDb extends SQLiteOpenHelper {
    private static PointOfSaleDb sInstance;

    private static final int DATABASE_VERSION = 4;
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
    public static final String TRANSACTIONS_COLUMN_PRODUCT_NAME = "product_name";
    public static final String TRANSACTIONS_COLUMN_EXCHANGE_RATE = "exchange_rate";

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
                    TRANSACTIONS_COLUMN_PRODUCT_NAME + " TEXT , " +
                    TRANSACTIONS_COLUMN_EXCHANGE_RATE + " TEXT , " +
                    TRANSACTIONS_COLUMN_IS_CONFIRMED + " NUMERIC NOT NULL )";

    private static final String TRANSACTIONS_TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TRANSACTIONS_TABLE_NAME;




    public static final String ITEMS_TABLE_NAME = "items";

    public static final String ITEMS_COLUMN_ΙΤΕΜ_ID = "item_id";
    public static final String ITEMS_COLUMN_NAME = "name";
    public static final String ITEMS_COLUMN_DESCRIPTION = "description";
    public static final String ITEMS_COLUMN_COLOR = "color";
    public static final String ITEMS_COLUMN_CREATED_AT = "created_at";
    public static final String ITEMS_COLUMN_DISPLAY_ORDER = "display_order";
    public static final String ITEMS_COLUMN_AMOUNT = "amount";
    public static final String ITEMS_COLUMN_IS_AVAILABLE = "is_available";

    private static final String ITEMS_TABLE_CREATE =
            "CREATE TABLE " + ITEMS_TABLE_NAME + " (" +
                    ITEMS_COLUMN_ΙΤΕΜ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                    ITEMS_COLUMN_NAME + " TEXT NOT NULL ,  " +
                    ITEMS_COLUMN_DESCRIPTION + " TEXT , " +
                    ITEMS_COLUMN_COLOR + " TEXT , " +
                    ITEMS_COLUMN_CREATED_AT + " TEXT NOT NULL , " +
                    ITEMS_COLUMN_DISPLAY_ORDER + " INTEGER NOT NULL , " +
                    ITEMS_COLUMN_AMOUNT + " REAL NOT NULL , " +
                    ITEMS_COLUMN_IS_AVAILABLE + " NUMERIC NOT NULL )";





    // implement singleton pattern
    public static synchronized PointOfSaleDb getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new PointOfSaleDb(context.getApplicationContext());
        }
        return sInstance;
    }

    // constructor is private for singleton pattern
    private PointOfSaleDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TRANSACTIONS_TABLE_CREATE);
        db.execSQL(ITEMS_TABLE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now update is used for development. So we delete and recreate the table
        // losing all the data.
        //db.execSQL(TRANSACTIONS_TABLE_DELETE);
        //onCreate(db);

        // otherwise migration code would be required
        // add product_name column if old version was 1
        if (newVersion > oldVersion) {

            if (oldVersion == 1) {
                db.execSQL("ALTER TABLE " + TRANSACTIONS_TABLE_NAME + " ADD COLUMN " + TRANSACTIONS_COLUMN_PRODUCT_NAME + " TEXT");
                db.execSQL("ALTER TABLE " + TRANSACTIONS_TABLE_NAME + " ADD COLUMN " + TRANSACTIONS_COLUMN_EXCHANGE_RATE + " TEXT");
                db.execSQL(ITEMS_TABLE_CREATE);
            }

            if (oldVersion == 2) {
                db.execSQL("ALTER TABLE " + TRANSACTIONS_TABLE_NAME + " ADD COLUMN " + TRANSACTIONS_COLUMN_EXCHANGE_RATE + " TEXT");
                db.execSQL(ITEMS_TABLE_CREATE);
            }

            if (oldVersion == 3) {
                db.execSQL(ITEMS_TABLE_CREATE);
            }

        }
    }

}
