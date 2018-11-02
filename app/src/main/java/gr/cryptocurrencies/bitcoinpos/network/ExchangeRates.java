package gr.cryptocurrencies.bitcoinpos.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Date;

import org.json.JSONObject;
import org.json.JSONException;

import gr.cryptocurrencies.bitcoinpos.R;
import gr.cryptocurrencies.bitcoinpos.utilities.CurrencyUtils;


public class ExchangeRates {

    private static ExchangeRates mInstance = null;

    // query to get rates for both BtcToLocal and LocalToBtc pairs (although we only use the BTC to Local rate in our calculations for now)
    private String blockchainInfoTickerUrl = "https://blockchain.info/ticker";
    //BTC to BCH ticker from Bittrex
    private String bittrexInfoTickerUrl = "https://bittrex.com/api/v1.1/public/getticker?market=BTC-";

    private String cryptocompareInfoTickerUrl = "https://min-api.cryptocompare.com/data/price?fsym="; //+  BCH or LTC  +"&tsyms=BTC";
    private String cryptocompareSuffix = "&tsyms=BTC";
    // 1 BTC price in local currency
    private double btcToLocalRate = 0.0;
    private Date lastUpdated;
    private Date lastUpdatedBittrex;
    private Date lastUpdatedCryptocompare;

    //convert BTC to other cryptos
    private double btcToOtherCryptos=0.0;

    private double btcToLtc=0.0;
    private double btcToBch=0.0;

    private double btcToBchPercentage = 0.0 ;
    private double btcToLtcPercentage = 0.0 ;

    private ExchangeRates(){
    }

    public static ExchangeRates getInstance(){
        if(mInstance == null)
        {
            mInstance = new ExchangeRates();
        }
        return mInstance;
    }


    public void updateExchangeRates(Context context, final String localCurrencyCode) {
        // query to get rates for BtcToLocal pairs
        StringRequest stringRequest= new StringRequest(Request.Method.GET, blockchainInfoTickerUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        String result = null;

                        Log.d("Blockchain.Info BABY!!", response.toString());

                        try {
                            JSONObject obj = new JSONObject(response.toString());

                            double exRate = obj.getJSONObject(localCurrencyCode).getDouble("last" );

                            // use 2 decimal precision rounded up!
                            btcToLocalRate = Math.round(exRate * 100.0) / 100.0;

                            lastUpdated = new Date();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("EXCHANGE ERROR", error.toString());
            }
        });

        Requests.getInstance(context).addToRequestQueue(stringRequest);

    }

    // echange rates from Bittrex, not used now
//    public void updateExchangeRatesBittrex(final Context context, final String cryptoCurrencyCode) {
//        // query to get rates for BtcToLocal pairs
//        final String suffix = cryptoCurrencyCode.toString();
//        String cryptoTickerUrl = bittrexInfoTickerUrl + suffix;
//
//        StringRequest stringRequest= new StringRequest(Request.Method.GET, cryptoTickerUrl,
//                new Response.Listener<String>() {
//
//                    @Override
//                    public void onResponse(String response) {
//                        String result = null;
//
//                        Log.d("Bittrex Info", response.toString());
//
//                        try {
//                            JSONObject obj = new JSONObject(response.toString());
//
//                            double exRate = obj.getJSONObject("result").getDouble("Last");
//                            //Toast.makeText(context, String.valueOf(exRate),Toast.LENGTH_SHORT).show();
//                            btcToOtherCryptos = exRate;
//
//                            lastUpdatedBittrex = new Date();
//                            // use 2 decimal precision rounded up!
//                            //btcToLocalRate = Math.round(exRate * 100.0) / 100.0;
//
//                            //lastUpdated = new Date();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("EXCHANGE ERROR", error.toString());
//            }
//        });
//
//        if(suffix.equals("BTCTEST")){
//            btcToOtherCryptos=1.0d;
//        }
//        else {
//            Requests.getInstance(context).addToRequestQueue(stringRequest);
//        }
//    }

    public void updateExchangeRatesCryptocompare(Context context, final String cryptoCurrencyCode) {



        String cryptoCode = cryptoCurrencyCode;
        if(cryptoCurrencyCode.equals(String.valueOf(CurrencyUtils.CurrencyType.BTCTEST))){
            cryptoCode = String.valueOf(R.string.btctest);
        }

        final String code = cryptoCode.toString();
        String cryptoTickerUrl = cryptocompareInfoTickerUrl + code + cryptocompareSuffix;

        StringRequest stringRequest= new StringRequest(Request.Method.GET, cryptoTickerUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        String result = null;

                        Log.d("Cryptocompare Info", response.toString());

                        try {
                            JSONObject obj = new JSONObject(response.toString());

                            double exRate = (double) obj.getDouble("BTC");

                            btcToOtherCryptos = exRate;

                            //btcToBchPercentage = exRateBch;
                            //btcToLtcPercentage = exRateLtc;

                            lastUpdatedCryptocompare = new Date();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("EXCHANGE ERROR", error.toString());
            }
        });

            Requests.getInstance(context).addToRequestQueue(stringRequest);

    }



    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Date getLastUpdatedBittrex() {
        return lastUpdatedBittrex;
    }

    public Date getLastUpdatedCryptocompare() {
        return lastUpdatedCryptocompare;
    }

    public double getBtcToLocalRate() {
        return btcToLocalRate;
    }

    public double getOtherCryptosToLocalRate(){
        return btcToLocalRate * btcToOtherCryptos;
    }


}

/*
JSON FORMAT            // exchange rate of BCH with reference to BTC
https://min-api.cryptocompare.com/data/price?fsym=BCH&tsyms=BTC

{"BTC":0.06604}
 */
