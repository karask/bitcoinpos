package gr.cryptocurrencies.bitcoinpos.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by kostas on 2/7/2016. Using Yahoo Finance API
 */
public class ExchangeRates {

    private static ExchangeRates mInstance = null;

    // query to get rates for both BtcToLocal and LocalToBtc pairs (although we only use the BTC to Local rate in our calculations for now)
    private String yahooFinanceUrl1 = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(\"BTC";
    private String yahooFinanceUrl2 = "BTC\")&env=store://datatables.org/alltableswithkeys";

    private double btcToLocalRate = 0.0;
//    private double localToBtcRate = 0.0;
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


    public void updateExchangeRates(Context context, String localCurrencyCode) {
        // query to get rates for both BtcToLocal and LocalToBtc pairs (although we only use the BTC to Local rate in our calculations for now)
        // example http GET: http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22BTCUSD%22,%22USDBTC%22)&env=store://datatables.org/alltableswithkeys
        String url = yahooFinanceUrl1 + localCurrencyCode + "\",\"" + localCurrencyCode + yahooFinanceUrl2;

        StringRequest stringRequest= new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Document doc = null;
                        String result = null;

                        Log.d("YAHOO BABY!!", response.toString());

                        try {
                            doc = DocumentBuilderFactory.newInstance()
                                    .newDocumentBuilder().parse(new InputSource(new StringReader(response.toString())));

                            XPathExpression xpathFromBtc = XPathFactory.newInstance().newXPath().compile("/query/results/rate[1]/Rate");
                            XPathExpression xpathToBtc = XPathFactory.newInstance().newXPath().compile("/query/results/rate[2]/Rate");

                            double exRate = Double.parseDouble((String) xpathFromBtc.evaluate(doc, XPathConstants.STRING));
                            // use 2 decimal precision rounded up!
                            btcToLocalRate = Math.round(exRate * 100.0) / 100.0;

                            // not used since exchange rate difference is large
                            //localToBtcRate = Double.parseDouble((String) xpathToBtc.evaluate(doc, XPathConstants.STRING));

                            lastUpdated = new Date();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (XPathExpressionException e) {
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

//    public double getLocalToBtcRate() {
//        return localToBtcRate;
//    }

    public double getBtcToLocalRate() {
        return btcToLocalRate;
    }

}
