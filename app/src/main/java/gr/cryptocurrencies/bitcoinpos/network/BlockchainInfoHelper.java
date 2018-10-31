package gr.cryptocurrencies.bitcoinpos.network;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.util.Log;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import gr.cryptocurrencies.bitcoinpos.database.PointOfSaleDb;
import gr.cryptocurrencies.bitcoinpos.database.TxStatus;
import gr.cryptocurrencies.bitcoinpos.database.UpdateDbHelper;



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

    public static void updateOngoingTxToConfirmedByTxId(final String txId, final String bitcoinAddress, final boolean mainNet) {

        String url;
        //if(AddressValidator.isMainNet(bitcoinAddress)) {//BitcoinUtils.isMainNet()
        if(mainNet){
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

    public static void updatePendingTxToOngoingOrConfirmed(final String bitcoinAddress, final int givenAmount, final int rowId, final int timeCreated, final boolean mainNet) {

        String url;
        //if(AddressValidator.isMainNet(bitcoinAddress)) {//BitcoinUtils.isMainNet()
        //according to boolean mainNet go select the correct url
        if(mainNet){
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




/*
JSON SAMPLE
"https://blockchain.info/rawtx/" + txId;

{
   "ver":1,
   "inputs":[
      {
         "sequence":4294967295,
         "witness":"",
         "prev_out":{
            "spent":true,
            "tx_index":379094749,
            "type":0,
            "addr":"16tgTQAWfuVwxYrTF2iNLJEBnhVGPdDDJ4",
            "value":296997,
            "n":2,
            "script":"76a914409ec74a7fc3714372fbd3e6f6cd5190696d95af88ac"
         },
         "script":"483045022100d26812e724e88805af9142b9ed5cc199f8743a00f2c95b8d80a4d09cb46780fa02206e054505fe29bf4194743adbb62bf0b595e6db17f84981c61accce645bff0fb801210330ddf06bac929da180a6bc2d10a3a4b8b8fd584efc102be91ba0eff37dde025c"
      },
      {
         "sequence":4294967295,
         "witness":"",
         "prev_out":{
            "spent":true,
            "tx_index":379087764,
            "type":0,
            "addr":"1L3n82Eei6rZYYwSdTdJLvTScjT19ypQn2",
            "value":74672,
            "n":0,
            "script":"76a914d0f0e310e9eb2983c2a7d3778a2445bd3599eb9788ac"
         },
         "script":"483045022100822a57a4ffb60a2054dadfc723eb2c533861993c8804da40b5a5f97d78f0182d02204ab0df07a14be102443ffa8120bf00a95a9883b88b9e49b605b3c1c5707177b7012102e445b511e0babc4973d83d2ac185b1c3363ac152711b70396c47211694655413"
      }
   ],
   "weight":1488,
   "block_height":544560,
   "relayed_by":"127.0.0.1",
   "out":[
      {
         "spent":true,
         "tx_index":379095028,
         "type":0,
         "addr":"1B73DKqLZqra8NUsdE8s5gr6maeQh9k9ui",
         "value":2873,
         "n":0,
         "script":"76a9146ed599bac94ff2459d03064f9fd56b83d444aeda88ac"
      },
      {
         "spent":true,
         "tx_index":379095028,
         "type":0,
         "addr":"33417MNNsitxEwfShMbuLom9VHmvCe8y32",
         "value":366926,
         "n":1,
         "script":"a9140ef02e891e14214fcd03501299d5f680bf5781d187"
      }
   ],
   "lock_time":0,
   "size":372,
   "double_spend":false,
   "time":1538786564,
   "tx_index":379095028,
   "vin_sz":2,
   "hash":"52804f70000918b724a1226d11978788863c85b17d09def912ae7f021c7cc796",
   "vout_sz":2
}


 */

/*
JSON FORMAT
"https://blockchain.info/rawaddr/" + bitcoinAddress;
{
    "hash160":"7a97cd93944f97ae43294b53a6dd21de0fc8ff8d",
    "address":"1CBDHs41Mmia2s2SME9xGFsD5WXtsr9Z4S",
    "n_tx":2,
    "total_received":15478000,
    "total_sent":15478000,
    "final_balance":0,
    "txs":[

{
   "ver":1,
   "inputs":[
      {
         "sequence":4294967295,
         "witness":"",
         "prev_out":{
            "spent":true,
            "tx_index":378561070,
            "type":0,
            "addr":"1CBDHs41Mmia2s2SME9xGFsD5WXtsr9Z4S",
            "value":15478000,
            "n":1,
            "script":"76a9147a97cd93944f97ae43294b53a6dd21de0fc8ff8d88ac"
         },
         "script":"483045022100b8e1d4bd2d306f485996c5f96ca0f359faaedf6f8db65e893f0e6fc95afdbba3022011d7f8a7331c8287ce64f80dbd3650bf3211627770451426e1c3ef03fd2baaa001210243f84c6c636ec45a0f5359e3b03f4902db14a46dabdac1e6bf727a3eda437b99"
      }
   ],
   "weight":904,
   "block_height":544364,
   "relayed_by":"127.0.0.1",
   "out":[
      {
         "spent":false,
         "tx_index":378730497,
         "type":0,
         "addr":"17kZjj3RR1UGVMFy8XNNUmRC4sictnY2a4",
         "value":237422,
         "n":0,
         "script":"76a9144a0df7ddb4b30ae644df0bde67daa072a01a7f3988ac"
      },
      {
         "spent":true,
         "tx_index":378730497,
         "type":0,
         "addr":"168moD6BTcoXVcCQJdZQUSchrLaTwfmfMG",
         "value":15239674,
         "n":1,
         "script":"76a9143850ef5f8172d54b7696af507e13aba5923cd75488ac"
      }
   ],
   "lock_time":0,
   "result":0,
   "size":226,
   "time":1538662911,
   "tx_index":378730497,
   "vin_sz":1,
   "hash":"a1ff40cfc1d339819c05bea352c337600677936cd5396694458839a1ffb927bd",
   "vout_sz":2
},

{
   "ver":1,
   "inputs":[
      {
         "sequence":4294967295,
         "witness":"",
         "prev_out":{
            "spent":true,
            "tx_index":378511721,
            "type":0,
            "addr":"1GZVD1zhZAdYMBrs47T9R3c9x1KKXketM4",
            "value":4762676,
            "n":0,
            "script":"76a914aaaebe63ed25435aaafd9f136f5dcf212c6acd4f88ac"
         },
         "script":"48304502210092cc01363c713248e8209bbd1815c4393cd8ea26a9d888af938048b2093cde5e02202e2644a52cad7b595cdf3666754c6cca8155a625f43daba61488979a8273248a01210370508b1f79ebf9bc22a00de0b4a8d7064488c086b577ad7f1cc9aeab2d7ebad3"
      },
      {
         "sequence":4294967295,
         "witness":"",
         "prev_out":{
            "spent":true,
            "tx_index":378431186,
            "type":0,
            "addr":"1AXjMuLKeunocBtxyg2VLWVbQ2XCkCLbHx",
            "value":7763000,
            "n":1,
            "script":"76a9146888f8b9317bbed371f60f307e8366cb8d7a814a88ac"
         },
         "script":"47304402200e6f4ea33a9b38d12242c4c405ab698b881cfd931161ae921551915a46a5db4d02206681d552fd8cb07cafd8e590e60b6bd611c1f688ddbeaceab3fe7738458c60940121021943ff2aed408231cd8540c1d8f2925e435ccf48658f8e68b58cdbda78c79781"
      },
      {
         "sequence":4294967295,
         "witness":"",
         "prev_out":{
            "spent":true,
            "tx_index":377807742,
            "type":0,
            "addr":"1GZVD1zhZAdYMBrs47T9R3c9x1KKXketM4",
            "value":5331420,
            "n":0,
            "script":"76a914aaaebe63ed25435aaafd9f136f5dcf212c6acd4f88ac"
         },
         "script":"483045022100f10a9e2a091b3fcadaa66bd824ff2df6aae630c78e90ca40fad70ec6bd057e6502206091535130528ee10051e650558f9daa7adae87e49a25b63b4fdc60d7146f06f01210370508b1f79ebf9bc22a00de0b4a8d7064488c086b577ad7f1cc9aeab2d7ebad3"
      }
   ],
   "weight":2084,
   "block_height":544264,
   "relayed_by":"127.0.0.1",
   "out":[
      {
         "spent":true,
         "tx_index":378561070,
         "type":0,
         "addr":"1NZG2SsNQXPQm97hQZJFtUsa54wSbxpaMA",
         "value":2377008,
         "n":0,
         "script":"76a914ec747e9225381236ee28b8a3316dcde77d1eee0088ac"
      },
      {
         "spent":true,
         "tx_index":378561070,
         "type":0,
         "addr":"1CBDHs41Mmia2s2SME9xGFsD5WXtsr9Z4S",
         "value":15478000,
         "n":1,
         "script":"76a9147a97cd93944f97ae43294b53a6dd21de0fc8ff8d88ac"
      }
   ],
   "lock_time":0,
   "result":-15478000,
   "size":521,
   "time":1538599091,
   "tx_index":378561070,
   "vin_sz":3,
   "hash":"29010542fa0b1e063883e2d7152d77d54dc086051c50077fd0459a4ad26f358c",
   "vout_sz":2
}]
}

 */

