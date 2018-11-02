package gr.cryptocurrencies.bitcoinpos.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class PointOfSaleDb extends SQLiteOpenHelper {
    private static PointOfSaleDb sInstance;

    private static final int DATABASE_VERSION = 6;  // v4: database was version 4 before
                                                    // -> v5 : changed TRANSACTIONS_COLUMN_TX_ID to TEXT
                                                    // -> v6 : renamed TRANSACTIONS_COLUMN_BITCOIN_AMOUNT and TRANSACTIONS_COLUMN_BITCOIN_ADDRESS to ->
                                                    // -> v6 : -> TRANSACTIONS_COLUMN_CRYPTOCURRENCY_AMOUNT and TRANSACTIONS_COLUMN_CRYPTOCURRENCY_ADDRESS

    public static final String DATABASE_NAME = "History.db";//private

    public static final String TRANSACTIONS_TABLE_NAME = "transactions";

    // previous TRANSACTIONS TABLE COLYMNS
    /*
    public static final String TRANSACTIONS_COLUMN_TX_ID = "transaction_id";
    public static final String TRANSACTIONS_COLUMN_BITCOIN_AMOUNT = "bitcoin_amount";
    public static final String TRANSACTIONS_COLUMN_LOCAL_AMOUNT = "local_amount";
    public static final String TRANSACTIONS_COLUMN_LOCAL_CURRENCY = "local_currency";
    public static final String TRANSACTIONS_COLUMN_CREATED_AT = "created_at";
    public static final String TRANSACTIONS_COLUMN_CONFIRMED_AT = "confirmed_at";
    public static final String TRANSACTIONS_COLUMN_MERCHANT_NAME = "merchant_name";
    public static final String TRANSACTIONS_COLUMN_BITCOIN_ADDRESS = "bitcoin_address";
    //public static final String TRANSACTIONS_COLUMN_IS_CONFIRMED = "is_confirmed";  //previous column name for Tx Status, set to true or false only
    //public static final String TRANSACTIONS_COLUMN_TX_STATUS = "is_confirmed";//keeping previous name
    public static final String TRANSACTIONS_COLUMN_TX_STATUS = "transaction_status";
    public static final String TRANSACTIONS_COLUMN_PRODUCT_NAME = "product_name";
    public static final String TRANSACTIONS_COLUMN_EXCHANGE_RATE = "exchange_rate";
    //public static final String TRANSACTIONS_COLUMN_PRIMARY_KEY = "primary_key";
    //could also use this new column TRANSACTIONS_COLUMN_PRIMARY_KEY as primary key
    //_ROWID_ is currently used for checking pending transaction
    public static final String TRANSACTIONS_COLUMN_CRYPTOCURRENCY = "cryptocurrency";
    */


    // COLUMN NAMES USED IN PREVIOUS VERSIONS, NOW USING THEM TO MIGRATE TO NEW TABLE
    public static final String TRANSACTIONS_COLUMN_BITCOIN_AMOUNT = "bitcoin_amount";//  previous name
    public static final String TRANSACTIONS_COLUMN_BITCOIN_ADDRESS = "bitcoin_address";//  previous name
    public static final String TRANSACTIONS_COLUMN_TX_STATUS_PREVIOUS_NAME = "is_confirmed";//  previous name


    // new columns for table TRANSACTIONS
    public static final String TRANSACTIONS_COLUMN_TX_ID = "transaction_id";
    public static final String TRANSACTIONS_COLUMN_CRYPTOCURRENCY_AMOUNT = "cryptocurrency_amount";
    public static final String TRANSACTIONS_COLUMN_LOCAL_AMOUNT = "local_amount";
    public static final String TRANSACTIONS_COLUMN_LOCAL_CURRENCY = "local_currency";
    public static final String TRANSACTIONS_COLUMN_CREATED_AT = "created_at";
    public static final String TRANSACTIONS_COLUMN_CONFIRMED_AT = "confirmed_at";
    public static final String TRANSACTIONS_COLUMN_MERCHANT_NAME = "merchant_name";
    public static final String TRANSACTIONS_COLUMN_CRYPTOCURRENCY_ADDRESS = "cryptocurrency_address";
    public static final String TRANSACTIONS_COLUMN_TX_STATUS = "transaction_status";
    public static final String TRANSACTIONS_COLUMN_PRODUCT_NAME = "product_name";
    public static final String TRANSACTIONS_COLUMN_EXCHANGE_RATE = "exchange_rate";
    public static final String TRANSACTIONS_COLUMN_CRYPTOCURRENCY = "cryptocurrency";


    //public static final String TRANSACTIONS_COLUMN_IS_CONFIRMED = "is_confirmed";  //previous column name for Tx Status, set to true or false only
    //public static final String TRANSACTIONS_COLUMN_TX_STATUS = "is_confirmed";// kept previous name in version v5

    //_ROWID_ is currently used for checking pending transaction
    //public static final String TRANSACTIONS_COLUMN_PRIMARY_KEY = "primary_key";
    //could also use this new column TRANSACTIONS_COLUMN_PRIMARY_KEY as primary key






    /*
    //NOT USING NOW, PREVIOUS COMMAND TO CREATE transactions TABLE
    // change TRANSACTIONS_COLUMN_TX_ID to just TEXT so to accept also NULL values when transaction is pending
    private static final String TRANSACTIONS_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TRANSACTIONS_TABLE_NAME + " (" +
                    TRANSACTIONS_COLUMN_TX_ID + " TEXT , " +
                    TRANSACTIONS_COLUMN_BITCOIN_AMOUNT + " REAL NOT NULL ,  " +
                    TRANSACTIONS_COLUMN_LOCAL_AMOUNT + " REAL NOT NULL , " +
                    TRANSACTIONS_COLUMN_LOCAL_CURRENCY + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_CREATED_AT + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_CONFIRMED_AT + " TEXT , " +
                    TRANSACTIONS_COLUMN_MERCHANT_NAME + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_BITCOIN_ADDRESS + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_PRODUCT_NAME + " TEXT , " +
                    TRANSACTIONS_COLUMN_EXCHANGE_RATE + " TEXT , " +
                    TRANSACTIONS_COLUMN_TX_STATUS + " NUMERIC NOT NULL )";

    // change TRANSACTIONS_COLUMN_TX_ID to just TEXT so to accept also NULL values when transaction is pending
    */

    private static final String TRANSACTIONS_TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TRANSACTIONS_TABLE_NAME;


    // NOT USED
    //MIGRATE TO NEW TABLE ON UPGRADE
    /*
    private static final String TRANSACTIONS_TABLE_CREATE_NEW =
            "CREATE TABLE IF NOT EXISTS transactionsNew (" +
                    TRANSACTIONS_COLUMN_TX_ID + " TEXT , " +
                    TRANSACTIONS_COLUMN_BITCOIN_AMOUNT + " REAL NOT NULL ,  " +
                    TRANSACTIONS_COLUMN_LOCAL_AMOUNT + " REAL NOT NULL , " +
                    TRANSACTIONS_COLUMN_LOCAL_CURRENCY + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_CREATED_AT + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_CONFIRMED_AT + " TEXT , " +
                    TRANSACTIONS_COLUMN_MERCHANT_NAME + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_BITCOIN_ADDRESS + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_PRODUCT_NAME + " TEXT , " +
                    TRANSACTIONS_COLUMN_EXCHANGE_RATE + " TEXT , " +
                    TRANSACTIONS_COLUMN_TX_STATUS + " NUMERIC NOT NULL );";
    */


    private static final String TRANSACTIONS_CREATE_NEW_TABLE_V6 =
            "CREATE TABLE transactions (" +
                    TRANSACTIONS_COLUMN_TX_ID + " TEXT , " +
                    TRANSACTIONS_COLUMN_CRYPTOCURRENCY_AMOUNT + " REAL NOT NULL,  " +
                    TRANSACTIONS_COLUMN_LOCAL_AMOUNT + " REAL NOT NULL, " +
                    TRANSACTIONS_COLUMN_LOCAL_CURRENCY + " TEXT NOT NULL, " +
                    TRANSACTIONS_COLUMN_CREATED_AT + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_CONFIRMED_AT + " TEXT , " +
                    TRANSACTIONS_COLUMN_MERCHANT_NAME + " TEXT NOT NULL, " +
                    TRANSACTIONS_COLUMN_CRYPTOCURRENCY_ADDRESS + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_PRODUCT_NAME + " TEXT , " +
                    TRANSACTIONS_COLUMN_EXCHANGE_RATE + " TEXT , " +
                    TRANSACTIONS_COLUMN_TX_STATUS + " NUMERIC NOT NULL , "+
                    TRANSACTIONS_COLUMN_CRYPTOCURRENCY + " TEXT DEFAULT 'BTC' )  ";

    private static final String TRANSACTIONS_CREATE_NEW_TEMP_TABLE_V6 =
            "CREATE TABLE transactions_temp (" +
                    TRANSACTIONS_COLUMN_TX_ID + " TEXT , " +
                    TRANSACTIONS_COLUMN_CRYPTOCURRENCY_AMOUNT + " REAL NOT NULL,  " +
                    TRANSACTIONS_COLUMN_LOCAL_AMOUNT + " REAL NOT NULL, " +
                    TRANSACTIONS_COLUMN_LOCAL_CURRENCY + " TEXT NOT NULL, " +
                    TRANSACTIONS_COLUMN_CREATED_AT + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_CONFIRMED_AT + " TEXT , " +
                    TRANSACTIONS_COLUMN_MERCHANT_NAME + " TEXT NOT NULL, " +
                    TRANSACTIONS_COLUMN_CRYPTOCURRENCY_ADDRESS + " TEXT NOT NULL , " +
                    TRANSACTIONS_COLUMN_PRODUCT_NAME + " TEXT , " +
                    TRANSACTIONS_COLUMN_EXCHANGE_RATE + " TEXT , " +
                    TRANSACTIONS_COLUMN_TX_STATUS + " NUMERIC NOT NULL , "+
                    TRANSACTIONS_COLUMN_CRYPTOCURRENCY + " TEXT DEFAULT 'BTC' )  ";

    //NOT USED
    //private static final String INSERT_OLD_INTO_NEW_TABLE = " INSERT INTO transactionsNew SELECT * FROM transactions ; ";


    private static final String DROP_OLD_TABLE = " DROP TABLE transactions ";
    private static final String DROP_TEMPORARY_TABLE = " DROP TABLE IF EXISTS transactionsNew  ";
    private static final String RENAME_TABLE_TO_TRANSACTIONS = " ALTER TABLE transactions_temp RENAME TO transactions  ";


    //NOT USED
    //private static final String TRANSACTIONS_TABLE_MIGRATE = TRANSACTIONS_TABLE_CREATE_NEW + INSERT_OLD_INTO_NEW_TABLE + DROP_OLD_TABLE + RENAME_TABLE_TO_TRANSACTIONS;


   // MIGRATE DATA FROM PREVIOUS TO NEW TEMPORARY TABLE
    private static final String INSERT_OLD_INTO_NEW_TEMPORARY_TABLE_V6 = " INSERT INTO transactions_temp ( " + TRANSACTIONS_COLUMN_TX_ID + " , " +  TRANSACTIONS_COLUMN_CRYPTOCURRENCY_AMOUNT + " , " + TRANSACTIONS_COLUMN_LOCAL_AMOUNT + " , " +
            TRANSACTIONS_COLUMN_LOCAL_CURRENCY + " , " + TRANSACTIONS_COLUMN_CREATED_AT + " , " + TRANSACTIONS_COLUMN_CONFIRMED_AT + " , " + TRANSACTIONS_COLUMN_MERCHANT_NAME + " , " + TRANSACTIONS_COLUMN_CRYPTOCURRENCY_ADDRESS + " , " + TRANSACTIONS_COLUMN_PRODUCT_NAME + " , " +
            TRANSACTIONS_COLUMN_EXCHANGE_RATE + " , " + TRANSACTIONS_COLUMN_TX_STATUS  + " ) SELECT " + // SELECTING COLUMNS TO TRANSFER DUE TO BITCOIN AMOUNT, BITCOIN ADDRESS and TX STATUS COLUMN NAME CHANGE

            TRANSACTIONS_COLUMN_TX_ID + " , " +  TRANSACTIONS_COLUMN_BITCOIN_AMOUNT + " , " + TRANSACTIONS_COLUMN_LOCAL_AMOUNT + " , " +
            TRANSACTIONS_COLUMN_LOCAL_CURRENCY + " , " + TRANSACTIONS_COLUMN_CREATED_AT + " , " + TRANSACTIONS_COLUMN_CONFIRMED_AT + " , " + TRANSACTIONS_COLUMN_MERCHANT_NAME + " , " + TRANSACTIONS_COLUMN_BITCOIN_ADDRESS + " , " + TRANSACTIONS_COLUMN_PRODUCT_NAME + " , " +
            TRANSACTIONS_COLUMN_EXCHANGE_RATE + " , " + TRANSACTIONS_COLUMN_TX_STATUS_PREVIOUS_NAME + " FROM transactions  ";



    private static final String ADD_COLUMN_CRYPTOCURRENCY = " ALTER TABLE " + TRANSACTIONS_TABLE_NAME + " ADD COLUMN " + TRANSACTIONS_COLUMN_CRYPTOCURRENCY + " TEXT DEFAULT 'BTC' ;";
    private static final String ADD_COLUMN_TX_STATUS = " ALTER TABLE " + TRANSACTIONS_TABLE_NAME + " ADD COLUMN " + TRANSACTIONS_COLUMN_TX_STATUS + " NUMERIC ;";
    private static final String ADD_COLUMN_CRYPRO_AMOUNT = " ALTER TABLE " + TRANSACTIONS_TABLE_NAME + " ADD COLUMN " + TRANSACTIONS_COLUMN_CRYPTOCURRENCY_AMOUNT + " REAL ;";
    private static final String ADD_COLUMN_CRYPRO_ADDRESS = " ALTER TABLE " + TRANSACTIONS_TABLE_NAME + " ADD COLUMN " + TRANSACTIONS_COLUMN_CRYPTOCURRENCY_ADDRESS + " TEXT ;";

    //private static final String TRANSACTIONS_TABLE_MIGRATE_V6 = TRANSACTIONS_CREATE_NEW_TEMP_TABLE_V6 + INSERT_OLD_INTO_NEW_TEMPORARY_TABLE_V6 + RENAME_TABLE_TO_TRANSACTIONS ;


    public static final String ITEMS_TABLE_NAME = "items";

    private static final String ITEMS_TABLE_DELETE =
            "DROP TABLE IF EXISTS " + ITEMS_TABLE_NAME;

    public static final String ITEMS_COLUMN_ΙΤΕΜ_ID = "item_id";
    public static final String ITEMS_COLUMN_NAME = "name";
    public static final String ITEMS_COLUMN_DESCRIPTION = "description";
    public static final String ITEMS_COLUMN_COLOR = "color";
    public static final String ITEMS_COLUMN_CREATED_AT = "created_at";
    public static final String ITEMS_COLUMN_DISPLAY_ORDER = "display_order";
    public static final String ITEMS_COLUMN_AMOUNT = "amount";
    public static final String ITEMS_COLUMN_IS_AVAILABLE = "is_available";

    private static final String ITEMS_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + ITEMS_TABLE_NAME + " (" +
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
        //db.execSQL(TRANSACTIONS_TABLE_DELETE);
        //db.execSQL(TRANSACTIONS_TABLE_CREATE);

        db.execSQL(TRANSACTIONS_CREATE_NEW_TABLE_V6); // includes cryptocurrency column
        db.execSQL(ITEMS_TABLE_CREATE);

    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now update is used for development. So we delete and recreate the table
        // losing all the data.
        //db.execSQL(TRANSACTIONS_TABLE_DELETE);
        //db.execSQL(ITEMS_TABLE_DELETE);
        //onCreate(db);


        // otherwise migration code would be required
        // add product_name column if old version was 1
        if (newVersion > oldVersion) {
            //onCreate(db);
            if (oldVersion == 1) {
                db.execSQL("ALTER TABLE " + TRANSACTIONS_TABLE_NAME + " ADD COLUMN " + TRANSACTIONS_COLUMN_PRODUCT_NAME + " TEXT");
                db.execSQL("ALTER TABLE " + TRANSACTIONS_TABLE_NAME + " ADD COLUMN " + TRANSACTIONS_COLUMN_EXCHANGE_RATE + " TEXT");
                db.execSQL(ITEMS_TABLE_CREATE);

                db.execSQL(TRANSACTIONS_CREATE_NEW_TEMP_TABLE_V6);  //create transactions_temp
                db.execSQL(INSERT_OLD_INTO_NEW_TEMPORARY_TABLE_V6); // insert transactions to transactions_temp
                db.execSQL(DROP_OLD_TABLE);  // drop transactions
                db.execSQL(RENAME_TABLE_TO_TRANSACTIONS);  // rename transactions_temp to transactions
            }

            if (oldVersion == 2) {
                db.execSQL("ALTER TABLE " + TRANSACTIONS_TABLE_NAME + " ADD COLUMN " + TRANSACTIONS_COLUMN_EXCHANGE_RATE + " TEXT");
                db.execSQL(ITEMS_TABLE_CREATE);

                db.execSQL(TRANSACTIONS_CREATE_NEW_TEMP_TABLE_V6);  //create transactions_temp
                db.execSQL(INSERT_OLD_INTO_NEW_TEMPORARY_TABLE_V6); // insert transactions to transactions_temp
                db.execSQL(DROP_OLD_TABLE);  // drop transactions
                db.execSQL(RENAME_TABLE_TO_TRANSACTIONS);  // rename transactions_temp to transactions
            }

            if (oldVersion == 3) {
                db.execSQL(ITEMS_TABLE_CREATE);

                db.execSQL(TRANSACTIONS_CREATE_NEW_TEMP_TABLE_V6);  //create transactions_temp
                db.execSQL(INSERT_OLD_INTO_NEW_TEMPORARY_TABLE_V6); // insert transactions to transactions_temp
                db.execSQL(DROP_OLD_TABLE);  // drop transactions
                db.execSQL(RENAME_TABLE_TO_TRANSACTIONS);  // rename transactions_temp to transactions
            }

            if (oldVersion == 4) {
                db.execSQL(TRANSACTIONS_CREATE_NEW_TEMP_TABLE_V6);  //create transactions_temp
                db.execSQL(INSERT_OLD_INTO_NEW_TEMPORARY_TABLE_V6); // insert transactions to transactions_temp
                db.execSQL(DROP_OLD_TABLE);  // drop transactions
                db.execSQL(RENAME_TABLE_TO_TRANSACTIONS);  // rename transactions_temp to transactions
            }

            if (oldVersion == 5) {
                db.execSQL(TRANSACTIONS_CREATE_NEW_TEMP_TABLE_V6);  //create transactions_temp
                db.execSQL(INSERT_OLD_INTO_NEW_TEMPORARY_TABLE_V6); // insert transactions to transactions_temp
                db.execSQL(DROP_OLD_TABLE);  // drop transactions
                db.execSQL(RENAME_TABLE_TO_TRANSACTIONS);  // rename transactions_temp to transactions
            }

        }

    }


    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TRANSACTIONS_TABLE_DELETE);
        db.execSQL(ITEMS_TABLE_DELETE);

        onCreate(db);

    }

}
