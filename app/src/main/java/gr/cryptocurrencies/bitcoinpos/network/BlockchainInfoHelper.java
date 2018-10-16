package gr.cryptocurrencies.bitcoinpos.network;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.SimpleAdapter;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import gr.cryptocurrencies.bitcoinpos.R;
import gr.cryptocurrencies.bitcoinpos.database.PointOfSaleDb;
import gr.cryptocurrencies.bitcoinpos.database.TxStatus;
import gr.cryptocurrencies.bitcoinpos.database.UpdateDbHelper;
import gr.cryptocurrencies.bitcoinpos.utilities.BitcoinAddressValidator;
import gr.cryptocurrencies.bitcoinpos.utilities.BitcoinUtils;
import gr.cryptocurrencies.bitcoinpos.utilities.DateUtilities;


public class BlockchainInfoHelper {

    private static Context context;
    private static PointOfSaleDb mDbHelper;
    public static final String CUSTOM_BROADCAST_ACTION = "gr.cryptocurrencies.bitcoinpos.CUSTOM_BROADCAST";

    //save the context received via constructor in a local variable
    public BlockchainInfoHelper(Context context){
        this.context=context;
    }

    public static void sendBroadcast(){
        Intent intent = new Intent(CUSTOM_BROADCAST_ACTION);
        context.sendBroadcast(intent);
    }

    public static void updateOngoingTxToConfirmedByTxId(final String txId, final String bitcoinAddress) {

        String url;
        if(BitcoinAddressValidator.isMainNet(bitcoinAddress)) {//BitcoinUtils.isMainNet()
            url = "https://blockchain.info/rawtx/" + txId;
        } else {
            url = "https://testnet.blockchain.info/rawtx/" + txId;
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("ONGOING TO CONF ERR", response.toString());
                        try {
                            if(response.has("block_height")) {

                                String confirmedAt = response.getString("time");
                                if(UpdateDbHelper.updateDbTransaction(txId, confirmedAt, 0, TxStatus.ONGOING, TxStatus.CONFIRMED)){
                                    sendBroadcast();
                                    //send broadcast to refresh view

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ONGOING TO CONF ERR 2", error.toString());
                    }
                });

        Requests.getInstance(context).addToRequestQueue(jsObjRequest);


    }

    public static void updatePendingTxToOngoingOrConfirmed(final String bitcoinAddress, final int givenAmount, final int rowId, final int timeCreated) {

        String url;
        if(BitcoinAddressValidator.isMainNet(bitcoinAddress)) {//BitcoinUtils.isMainNet()
            url = "https://blockchain.info/rawaddr/" + bitcoinAddress;
        } else {
            url = "https://testnet.blockchain.info/rawaddr/" + bitcoinAddress;
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("PENDING TO ONG/CONF ERR", response.toString());
                        try {
                            if(response.has("address") && response.getString("address").equals(bitcoinAddress)) {
                                JSONArray allTxs = (JSONArray) response.getJSONArray("txs");
                                JSONObject ongoingTx;
                                for(int i=0; i<allTxs.length(); i++) {
                                    JSONObject trx = (JSONObject) allTxs.get(i);
                                    JSONArray outArray = (JSONArray) trx.getJSONArray("out");
                                    String confirmedAt = trx.getString("time");
                                    double foundAtDouble = Double.valueOf(confirmedAt);
                                    int txTimeFromResponse = (int)foundAtDouble;

                                    for (int outIndex = 0; outIndex < outArray.length(); outIndex++) {
                                        JSONObject output = (JSONObject) outArray.get(outIndex);

                                        String addr = output.getString("addr");
                                        int amountInSatoshis = (int) output.getInt("value");
                                        //double amount = amountInSatoshis / 100000000; // amount in BTC

                                        //check transactions according to payment address
                                        if (addr.equals(bitcoinAddress)) {
                                            if (amountInSatoshis==givenAmount && !trx.has("block_height")) {

                                                //using rowid, update pending to ongoing
                                                UpdateDbHelper.updateDbTransaction(trx.getString("hash"), null, rowId, TxStatus.PENDING, TxStatus.ONGOING);
                                                //send broadcast to update view
                                                sendBroadcast();
                                                break;

                                            }
                                            else if (amountInSatoshis==givenAmount && trx.has("block_height")) {

                                                // check if confirmed tx has happened after the payment request
                                                if(txTimeFromResponse > timeCreated) {
                                                    UpdateDbHelper.updateDbTransaction(trx.getString("hash"), confirmedAt, rowId, TxStatus.PENDING, TxStatus.CONFIRMED);

                                                    //update view
                                                    sendBroadcast();
                                                    break;
                                                }
                                            }
                                        }
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
                        Log.e("PEND TO ONG/CONF ERR 2", error.toString());
                    }
                });

        Requests.getInstance(context).addToRequestQueue(jsObjRequest);


    }

}
