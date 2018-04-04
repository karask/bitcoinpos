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

/**
 * Created by kostas on 2/7/2016. Using Blockchain.info exchange rate (15m delay)
 */
public class ExchangeRates {

    private static ExchangeRates mInstance = null;

    // query to get rates for both BtcToLocal and LocalToBtc pairs (although we only use the BTC to Local rate in our calculations for now)
    private String blockchainInfoTickerUrl = "https://blockchain.info/ticker";

    // 1 BTC price in local currency
    private double btcToLocalRate = 0.0;
    private Date lastUpdated;

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



    public Date getLastUpdated() {
        return lastUpdated;
    }

    public double getBtcToLocalRate() {
        return btcToLocalRate;
    }

}
