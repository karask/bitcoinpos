package gr.cryptocurrencies.bitcoinpos.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gr.cryptocurrencies.bitcoinpos.R;
import gr.cryptocurrencies.bitcoinpos.database.TxStatus;
import gr.cryptocurrencies.bitcoinpos.database.UpdateDbHelper;


public class RestBitcoinHelper {

    private static Context context;

    public RestBitcoinHelper(Context context){
        this.context=context;
    }

    public static final String CUSTOM_BROADCAST_ACTION = "gr.cryptocurrencies.bitcoinpos.CUSTOM_BROADCAST";


    public static void sendBroadcast(){
        Intent intent = new Intent(CUSTOM_BROADCAST_ACTION);
        context.sendBroadcast(intent);
    }


    public static void updateOngoingTxToConfirmedByTxId(final String tx, final String bchAddress) {

        String url;
        //if(BitcoinUtils.isNotTestnetAddress(bchAddress)) {//BitcoinUtils.isMainNet()
        //url = "https://rest.bitbox.earth/v1/transaction/details/" + tx;   ////bitbox earth
        url = "https://rest.bitcoin.com/v1/transaction/details/" + tx;
        //} else {
        //    url = "https://trest.bitbox.earth/v1/transaction/details/" + tx;  ////bitbox earth testnet
        // }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {


                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("CONFIRMED TX", response.toString());
                        try {
                            if(response.has("blockheight") && (response.getInt("blockheight")>-1)) {

                                String confirmedAt = response.getString("blocktime");
                                if (UpdateDbHelper.updateDbTransaction(tx, confirmedAt, 0,  TxStatus.ONGOING, TxStatus.CONFIRMED)) {
                                    sendBroadcast();
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

        Requests.getInstance(context).addToRequestQueue(jsObjRequest);


    }

    public static void updatePendingTxToOngoingOrConfirmed(final String bitcoinAddress, final int givenAmount, final int rowId, final int timeCreated, final boolean mainNet) {

        String url;

        //if(mainNet){
        //url = "https://rest.bitbox.earth/v1/address/unconfirmed/bitcoincash:" + bitcoinAddress;   bitbox earth, same url syntax with rest.bitcoin.com
        url = "https://rest.bitcoin.com/v1/address/unconfirmed/bitcoincash:" + bitcoinAddress;
        //} else {
        //    url = "https://testnet.blockchain.info/rawaddr/" + bitcoinAddress;
        //}

        final JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        Log.d("pending to ongoing tx", response.toString());
                        try {

                            for(int i=0;i<response.length();i++){
                                JSONObject jsonObject = response.getJSONObject(i);

                                int satoshis = jsonObject.getInt("satoshis");
                                if(givenAmount==satoshis){
                                    String txid = jsonObject.getString("txid");
                                    UpdateDbHelper.updateDbTransaction(txid, null, rowId, TxStatus.PENDING, TxStatus.ONGOING);
                                    sendBroadcast();
                                    break;
                                }

                            }

//                            if(response.has("unconfirmedTxApperances")) {
//                                int txApperances = response.getInt("unconfirmedtxApperances");
//                                int balanceSatoshi = response.getInt("balanceSat");
//
//                                JSONArray transactions = (JSONArray) response.getJSONArray("transactions");
//
//                                for(int i=0;i<transactions.length();i++ ){
//                                    String hash = transactions.get(i).toString();
//                                    if(txApperances>0 && givenAmount==balanceSatoshi){
//                                        updatePendingTxToOngoing(hash, rowId);
//                                        sendBroadcast();
//                                        break;
//                                    }
//                                }
//
//
//                            }
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

        Requests.getInstance(context).addToRequestQueue(jsObjRequest);


    }


    public static void isAddressValidViaRest(final String address, final SharedPreferences.Editor editor, final String editorString) {

        String url;

        url = "https://rest.bitcoin.com/v1/util/validateAddress/bitcoincash:" + address;


        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("validation error", response.toString());
                        try {
                            if(response.has("isvalid")) {

                                boolean isValid = response.getBoolean("isvalid");
                                if(isValid) {

                                    editor.putString(editorString, address);
                                    editor.apply();

                                }
                                else {
                                    Toast.makeText(context, R.string.invalid_address_message, Toast.LENGTH_SHORT).show();
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("validation error 2", error.toString());
                    }
                });

        Requests.getInstance(context).addToRequestQueue(jsObjRequest);


    }

}

/*
JSON FORMAT
"https://rest.bitcoin.com/v1/transaction/details/" + tx;
{"txid":"0e3e2357e806b6cdb1f70b54c3a3a17b6714ee1f0e68bebb44a74b1efd512098","version":1,"locktime":0,"vin":
[{"coinbase":"04ffff001d0104","sequence":4294967295,"n":0}],"vout":
[{"value":"50.00000000","n":0,"scriptPubKey":{"hex":"410496b538e853519c726a2c91e61ec11600ae1390813a627c66fb8be7947be63c52da7589379515d4e0a604f8141781e62294721166bf621e73a82cbf2342c858eeac","asm":"0496b538e853519c726a2c91e61ec11600ae1390813a627c66fb8be7947be63c52da7589379515d4e0a604f8141781e62294721166bf621e73a82cbf2342c858ee OP_CHECKSIG","addresses":
["12c6DSiU4Rq3P4ZxziKxzrL5LmMBrzjrJX"],"type":"pubkeyhash"},"spentTxId":null,"spentIndex":null,"spentHeight":null}],"blockhash":"00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048","blockheight":1,"confirmations":554447,"time":1231469665,"blocktime":1231469665,"isCoinBase":true,"valueOut":50,"size":134}
 */

/*
JSON FORMAT
"https://rest.bitcoin.com/v1/address/unconfirmed/bitcoincash:" + bitcoinAddress;
[
  {
    "txid": "string",
    "vout": 0,
    "scriptPubKey": "string",
    "amount": 0,
    "satoshis": 0,
    "height": 0,
    "confirmations": 0,
    "legacyAddress": "string",
    "cashAddress": "string"
  }
]
 */

/*
JSON FORMAT address validation
 "https://rest.bitcoin.com/v1/util/validateAddress/bitcoincash:" + address;
{
  "isvalid": true,
  "address": "string",
  "scriptPubKey": "string",
  "ismine": true,
  "iswatchonly": true,
  "isscript": true
}
 */
