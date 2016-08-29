package gr.cryptocurrencies.bitcoinpos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import gr.cryptocurrencies.bitcoinpos.utilities.DateUtilities;

/**
 * Created by kostas on 23/8/2016.
 */
public class ItemHelper {
    private static ItemHelper ourInstance = new ItemHelper();

    private static PointOfSaleDb mDbHelper;
    private static SQLiteDatabase mDb;

    public static ItemHelper getInstance(Context context) {
        if(mDbHelper == null) {
            mDbHelper = PointOfSaleDb.getInstance(context);
        }
        if(mDb == null) {
            mDb = mDbHelper.getWritableDatabase();
        }
        return ourInstance;
    }

    private ItemHelper() {
    }


    public int insert(Item item) {

        ContentValues values = new ContentValues();
        values.put(PointOfSaleDb.ITEMS_COLUMN_NAME, item.getName());
        values.put(PointOfSaleDb.ITEMS_COLUMN_AMOUNT, item.getAmount());
        values.put(PointOfSaleDb.ITEMS_COLUMN_IS_AVAILABLE, item.isAvailable());
        values.put(PointOfSaleDb.ITEMS_COLUMN_COLOR, item.getColor());
        values.put(PointOfSaleDb.ITEMS_COLUMN_DESCRIPTION, item.getDescription());
        values.put(PointOfSaleDb.ITEMS_COLUMN_DISPLAY_ORDER, item.getDisplayOrder());
        values.put(PointOfSaleDb.ITEMS_COLUMN_CREATED_AT, DateUtilities.getDateAsString(item.getCreatedAt()));
        long itemId = mDb.insert(PointOfSaleDb.ITEMS_TABLE_NAME, null, values);

        return (int)itemId;
    }


    public Item get(int id) {
        Item item = null;

        String[] tableColumns = { PointOfSaleDb.ITEMS_COLUMN_ΙΤΕΜ_ID, PointOfSaleDb.ITEMS_COLUMN_NAME,
                                  PointOfSaleDb.ITEMS_COLUMN_DESCRIPTION, PointOfSaleDb.ITEMS_COLUMN_AMOUNT,
                                  PointOfSaleDb.ITEMS_COLUMN_DISPLAY_ORDER, PointOfSaleDb.ITEMS_COLUMN_COLOR,
                                  PointOfSaleDb.ITEMS_COLUMN_IS_AVAILABLE, PointOfSaleDb.ITEMS_COLUMN_CREATED_AT };
        String whereClause = PointOfSaleDb.ITEMS_COLUMN_ΙΤΕΜ_ID + " = ?";
        String[] whereArgs = { Integer.toString(id) };
        Cursor c = mDb.query(PointOfSaleDb.ITEMS_TABLE_NAME, tableColumns, whereClause, whereArgs, null, null, null);

        if(c.moveToFirst()) {
            item = new Item(new Integer(c.getInt(0)), c.getString(1),
                                        c.getString(2), c.getDouble(3),
                                        c.getInt(4), c.getString(5),
                                        c.getInt(6) > 0, DateUtilities.getStringAsDate(c.getString(7)) );
        }
        c.close();

        return item;
    }


    public int update(Item item) {

        Item oldItem = get(item.getItemId());

        if(oldItem != null) {

            // note that item_id and created_at cannot be updated
            ContentValues values = new ContentValues();
            values.put(PointOfSaleDb.ITEMS_COLUMN_NAME, item.getName());
            values.put(PointOfSaleDb.ITEMS_COLUMN_DESCRIPTION, item.getDescription());
            values.put(PointOfSaleDb.ITEMS_COLUMN_AMOUNT, item.getAmount());
            values.put(PointOfSaleDb.ITEMS_COLUMN_DISPLAY_ORDER, item.getDisplayOrder());
            values.put(PointOfSaleDb.ITEMS_COLUMN_COLOR, item.getColor());
            values.put(PointOfSaleDb.ITEMS_COLUMN_IS_AVAILABLE, item.isAvailable());

            // row to update
            String selection = PointOfSaleDb.ITEMS_COLUMN_ΙΤΕΜ_ID + " = ?";
            String[] selectioinArgs = { String.valueOf(oldItem.getItemId()) };

            int count = mDb.update(PointOfSaleDb.ITEMS_TABLE_NAME, values, selection, selectioinArgs);

            return count;
        }

        return 0;
    }


    public boolean delete(int id) {
        Item toDeleteItem = get(id);

        if(toDeleteItem != null) {
            String whereClause = PointOfSaleDb.ITEMS_COLUMN_ΙΤΕΜ_ID + "=?";
            String[] whereArgs = new String[] { String.valueOf(id) };
            return mDb.delete(PointOfSaleDb.ITEMS_TABLE_NAME, whereClause, whereArgs) > 0;
        }

        return false;
    }


    public List<Item> getAll() {
        // get the following columns:
        String[] tableColumns = { PointOfSaleDb.ITEMS_COLUMN_ΙΤΕΜ_ID, PointOfSaleDb.ITEMS_COLUMN_NAME,
                                  PointOfSaleDb.ITEMS_COLUMN_DESCRIPTION, PointOfSaleDb.ITEMS_COLUMN_AMOUNT,
                                  PointOfSaleDb.ITEMS_COLUMN_DISPLAY_ORDER, PointOfSaleDb.ITEMS_COLUMN_COLOR,
                                  PointOfSaleDb.ITEMS_COLUMN_IS_AVAILABLE, PointOfSaleDb.ITEMS_COLUMN_CREATED_AT };

        String sortOrder = PointOfSaleDb.ITEMS_COLUMN_DISPLAY_ORDER + " ASC";
        Cursor c = mDb.query(PointOfSaleDb.ITEMS_TABLE_NAME, tableColumns, null, null, null, null, sortOrder);

        ArrayList<Item> list = new ArrayList<>();
        if (c.moveToFirst()) {
            do{
                Item item = new Item(new Integer(c.getInt(0)), c.getString(1),
                                                 c.getString(2), c.getDouble(3),
                                                 c.getInt(4), c.getString(5),
                                                 c.getInt(6) > 0, DateUtilities.getStringAsDate(c.getString(7)) );

                list.add(item);
            } while(c.moveToNext());
        }

        return list;
    }

}
