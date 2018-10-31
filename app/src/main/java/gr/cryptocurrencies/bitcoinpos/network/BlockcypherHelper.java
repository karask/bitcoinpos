package gr.cryptocurrencies.bitcoinpos.network;


import android.content.Context;
import android.content.Intent;

import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gr.cryptocurrencies.bitcoinpos.database.PointOfSaleDb;
import gr.cryptocurrencies.bitcoinpos.database.TxStatus;
import gr.cryptocurrencies.bitcoinpos.database.UpdateDbHelper;

import gr.cryptocurrencies.bitcoinpos.utilities.DateUtilities;

public class BlockcypherHelper {
    private static Context context;
    private static PointOfSaleDb mDbHelper;
    public static final String CUSTOM_BROADCAST_ACTION = "gr.cryptocurrencies.bitcoinpos.CUSTOM_BROADCAST";

    //save the context received via constructor in a local variable
    public BlockcypherHelper(Context context){
        this.context=context;
    }

    public static void sendBroadcast(){
        Intent intent = new Intent(CUSTOM_BROADCAST_ACTION);
        context.sendBroadcast(intent);
    }

    public static void updateOngoingTxToConfirmedByTxId(final String txId, final String litecoinAddress) {

        String url;
        //if(BitcoinAddressValidator.isMainNet(litecoinAddress)) {//BitcoinUtils.isMainNet()
            url = "https://api.blockcypher.com/v1/ltc/main/txs/" + txId;
        //} else {
        //    url = "https://api.blockcypher.com/v1/ltc/main/txs/" + txId;  //
        //}

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("ONGOING TO CONF ERR", response.toString());
                        try {
                            if(response.has("block_height") && (response.getInt("block_height")>-1)) {


                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                String confirmedAt = response.getString("confirmed");

                                try {
                                    Date date = df.parse(confirmedAt);
                                    int unixEpoch = (int) (date.getTime()/1000);
                                    confirmedAt = String.valueOf(unixEpoch);

                                    if(UpdateDbHelper.updateDbTransaction(txId, confirmedAt, 0, TxStatus.ONGOING, TxStatus.CONFIRMED)){
                                        sendBroadcast();
                                        //send broadcast to refresh view

                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
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

    public static void updatePendingTxToOngoingOrConfirmed(final String litecoinAddress, final int givenAmount, final int rowId, final int timeCreated) {

        String url;
        //if(AddressValidator.isMainNet(litecoinAddress)) {//BitcoinUtils.isMainNet()
            url = "https://api.blockcypher.com/v1/ltc/main/addrs/" + litecoinAddress;
        //} else {
            //url = "https://api.blockcypher.com/v1/ltc/main/addrs/" + litecoinAddress;
        //}

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("PENDING TO ONG/CONF ERR", response.toString());
                        try {
                            if(response.has("address") && response.getString("address").equals(litecoinAddress)) {
                                if (response.has("unconfirmed_txrefs")) {

                                JSONArray allTxs = (JSONArray) response.getJSONArray("unconfirmed_txrefs");
                                JSONObject ongoingTx;
                                for (int i = 0; i < allTxs.length(); i++) {
                                    JSONObject trx = (JSONObject) allTxs.get(i);

                                    int value = trx.getInt("value");
                                    int confirmations = trx.getInt("confirmations");

                                    String address = trx.getString("address");

                                    DateFormat df = new SimpleDateFormat(DateUtilities.DATABASE_DATE_FORMAT);

                                    //String confirmedAt = trx.getString("confirmed");
                                    //double foundAtDouble = Double.valueOf(confirmedAt);
                                    //int txTimeFromResponse = (int)foundAtDouble;

                                    if (address.equals(litecoinAddress) && value == givenAmount && confirmations == 0) {

                                        //using rowid, update pending to ongoing
                                        UpdateDbHelper.updateDbTransaction(trx.getString("tx_hash"), null, rowId, TxStatus.PENDING, TxStatus.ONGOING);
                                        //send broadcast to update view
                                        sendBroadcast();
                                        break;

                                    }
                                }
                            }

                            else {
                                    JSONArray allTxs = (JSONArray) response.getJSONArray("txrefs");
                                    for (int i = 0; i < allTxs.length(); i++) {
                                        JSONObject trx = (JSONObject) allTxs.get(i);
                                        int value = trx.getInt("value");
                                        String txHash = trx.getString("tx_hash");
                                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                        String confirmedAt = response.getString("confirmed");

                                        try {
                                            Date date = df.parse(confirmedAt);
                                            int txTimeFromResponse = (int) (date.getTime()/1000);
                                            String confirmedTime = String.valueOf(txTimeFromResponse);

                                            if (value == givenAmount && trx.has("block_height") ) {

                                                // check if confirmed tx has happened after the payment request
                                                if (txTimeFromResponse > timeCreated) {
                                                    UpdateDbHelper.updateDbTransaction(txHash, confirmedTime, rowId, TxStatus.PENDING, TxStatus.CONFIRMED);

                                                    //update view
                                                    sendBroadcast();
                                                    break;
                                                }
                                            }

                                        } catch (ParseException e) {
                                            e.printStackTrace();
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

/*
JSON FORMAT
"https://api.blockcypher.com/v1/ltc/main/txs/" + txId;
{
  "block_hash": "1ce31f79b21a09292d07bf2bff2c7ff3b90fd6ff599d9e7e31129029daad5184",
  "block_height": 1516074,
  "block_index": 13,
  "hash": "7d469b3810bc7caf85b7c2b7ac627f71e5e971e1e5cfc3e48f2dd67ca4075c9b",
  "addresses": [
    "LMsU8sigANYcu6doiMshYmbtxYYnRYTJ6e",
    "LNkSCPhpnP6dxkoTyyMecKYvkJHTWpjJnR",
    "LQirzuJkYR5SZKcHm6UQ3Fh7BmkSZK4MTF",
    "LZgWbowdcrhwJXKk5bUZg7dxN9rSz4fkid",
    "LbG8QoTsJbwsoDFehUaSqkPy7fKN8dqoCS"
  ],
  "total": 100888161,
  "fees": 41000,
  "size": 407,
  "preference": "high",
  "relayed_by": "77.37.156.166:9333",
  "confirmed": "2018-10-26T22:09:23Z",
  "received": "2018-10-26T22:09:00.266Z",
  "ver": 1,
  "double_spend": false,
  "vin_sz": 2,
  "vout_sz": 3,
  "confirmations": 2160,
  "confidence": 1,
  "inputs": [
    {
      "prev_hash": "0ebde3f77728a8929661d91c9a6a1714d86dce3d792fd7a300db1ff372f26e73",
      "output_index": 4,
      "script": "47304402204bf6e91b2e14bf08cea3fdae8c682cfa189f30edf8d972c0a51075915098bd77022078ec12474fe9b54e708282398bb3e3c5fb0723e77e3edf90c791bc89d6336a2601210318489a375409d1977e3e2453a1f9112b7bd492552a4652967c803b2280142207",
      "output_value": 100491886,
      "sequence": 4294967295,
      "addresses": [
        "LbG8QoTsJbwsoDFehUaSqkPy7fKN8dqoCS"
      ],
      "script_type": "pay-to-pubkey-hash",
      "age": 1515133
    },
    {
      "prev_hash": "df16599376e4d64136c524bfe42cab6d96a060a55592a6a5d01488c574102397",
      "output_index": 2,
      "script": "483045022100e42eb3b03dcb88f63a6694a754b23b48a7a792bc9500d411ea6bd8cd8521903f02201f3565f95192b6c8374deef702e59c71dbc913a682c5b64dbbd1cf3be95c5fde0121033896fb8db5559c1475557153acb4883fa19635c7d8ce69a7fcda8e10a946a0a1",
      "output_value": 437275,
      "sequence": 4294967295,
      "addresses": [
        "LNkSCPhpnP6dxkoTyyMecKYvkJHTWpjJnR"
      ],
      "script_type": "pay-to-pubkey-hash",
      "age": 1515987
    }
  ],
  "outputs": [
    {
      "value": 391886,
      "script": "76a9143c504c33fe3f8e579ea2d444b860c28506d8c17588ac",
      "addresses": [
        "LQirzuJkYR5SZKcHm6UQ3Fh7BmkSZK4MTF"
      ],
      "script_type": "pay-to-pubkey-hash"
    },
    {
      "value": 100047900,
      "script": "76a9141d08944c0734f7e5d15fac8da1be16fc3a71530688ac",
      "addresses": [
        "LMsU8sigANYcu6doiMshYmbtxYYnRYTJ6e"
      ],
      "script_type": "pay-to-pubkey-hash"
    },
    {
      "value": 448375,
      "script": "76a9149e9791fe1d9d206285189f0aa71359ba89081f1788ac",
      "spent_by": "296b23c8f91b55de590f9d8d6b6c17e9de5f86a6a5de805928cfdc796bf72de2",
      "addresses": [
        "LZgWbowdcrhwJXKk5bUZg7dxN9rSz4fkid"
      ],
      "script_type": "pay-to-pubkey-hash"
    }
  ]
}
 */

/*
JSON FORMAT
"https://api.blockcypher.com/v1/ltc/main/addrs/" + litecoinAddress;
{
  "address": "3M4xfByqCw2ytpcynEcHApPnK2z7ouSvh1",
  "total_received": 207300073,
  "total_sent": 206444640,
  "balance": 855433,
  "unconfirmed_balance": 0,
  "final_balance": 855433,
  "n_tx": 292,
  "unconfirmed_n_tx": 0,
  "final_n_tx": 292,
  "txrefs": [
    {
      "tx_hash": "c0c97e2758f5f47a6d6f74f9b1db3746d2aeda9e39d88ebc49795cae2307a16c",
      "block_height": 1517090,
      "tx_input_n": 1,
      "tx_output_n": -1,
      "value": 305713,
      "ref_balance": 855433,
      "confirmations": 1145,
      "confirmed": "2018-10-28T15:36:08Z",
      "double_spend": false
    },
    {
      "tx_hash": "fe1c92e01cde056411cf874e5170654d734c5c6b34853666e455f03d2d7ea245",
      "block_height": 1517087,
      "tx_input_n": -1,
      "tx_output_n": 22,
      "value": 305713,
      "ref_balance": 1161146,
      "spent": true,
      "spent_by": "c0c97e2758f5f47a6d6f74f9b1db3746d2aeda9e39d88ebc49795cae2307a16c",
      "confirmations": 1148,
      "confirmed": "2018-10-28T15:25:24Z",
      "double_spend": false
    },
    {
      "tx_hash": "177be39c94ebbc450d3b3bf47e2f9bde7d107a56ac4974f31bffc896659e30bf",
      "block_height": 1516055,
      "tx_input_n": 47,
      "tx_output_n": -1,
      "value": 293021,
      "ref_balance": 855433,
      "confirmations": 2180,
      "confirmed": "2018-10-26T21:18:25Z",
      "double_spend": false
    },
    {
      "tx_hash": "c8dd95ffa67b9a902190b2351191b12c3ee3b2a3457726eef7a8972f78133329",
      "block_height": 1516052,
      "tx_input_n": -1,
      "tx_output_n": 26,
      "value": 293021,
      "ref_balance": 1148454,
      "spent": true,
      "spent_by": "177be39c94ebbc450d3b3bf47e2f9bde7d107a56ac4974f31bffc896659e30bf",
      "confirmations": 2183,
      "confirmed": "2018-10-26T21:10:04Z",
      "double_spend": false
    },
    {
      "tx_hash": "05a9606fd69ee0d8ce4c1fcfd6b17c23473693e623a2cd340d8637c4725e9459",
      "block_height": 1514906,
      "tx_input_n": 77,
      "tx_output_n": -1,
      "value": 293612,
      "ref_balance": 855433,
      "confirmations": 3329,
      "confirmed": "2018-10-24T16:56:15Z",
      "double_spend": false
    },
    {
      "tx_hash": "85ac2bdccd31e8fca11ee4bc880f1f58922b5a08dbc6e85110a89719c38d0d19",
      "block_height": 1514904,
      "tx_input_n": -1,
      "tx_output_n": 21,
      "value": 293612,
      "ref_balance": 1149045,
      "spent": true,
      "spent_by": "05a9606fd69ee0d8ce4c1fcfd6b17c23473693e623a2cd340d8637c4725e9459",
      "confirmations": 3331,
      "confirmed": "2018-10-24T16:49:10Z",
      "double_spend": false
    },
    {
      "tx_hash": "0fbc2d35dcefc91700b8da05742ead9ec527b5a4f5b5f1645816302cbe63a10a",
      "block_height": 1513707,
      "tx_input_n": 9,
      "tx_output_n": -1,
      "value": 293612,
      "ref_balance": 855433,
      "confirmations": 4528,
      "confirmed": "2018-10-22T14:43:59Z",
      "double_spend": false
    },
    {
      "tx_hash": "bb3a35abf5e064359d4bf129960cfe0621c65da71c2829e62fe92cb9d41a028b",
      "block_height": 1513697,
      "tx_input_n": -1,
      "tx_output_n": 14,
      "value": 293612,
      "ref_balance": 1149045,
      "spent": true,
      "spent_by": "0fbc2d35dcefc91700b8da05742ead9ec527b5a4f5b5f1645816302cbe63a10a",
      "confirmations": 4538,
      "confirmed": "2018-10-22T14:11:35Z",
      "double_spend": false
    },
    {
      "tx_hash": "db04cb218e2bebb9ae8b2bf339144af5b0e876030e737d37c8d39aaae97b15e2",
      "block_height": 1513164,
      "tx_input_n": 38,
      "tx_output_n": -1,
      "value": 287414,
      "ref_balance": 855433,
      "confirmations": 5071,
      "confirmed": "2018-10-21T15:55:48Z",
      "double_spend": false
    },
    .....MORE TXS
 */
