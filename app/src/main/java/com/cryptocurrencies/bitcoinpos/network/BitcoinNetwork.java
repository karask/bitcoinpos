package com.cryptocurrencies.bitcoinpos.network;

/**
 * Created by kostas on 2/7/2016.
 */
public interface BitcoinNetwork {
    double getPriceInUSD();
    double usdToLocalCurrency(String currencyCode);
}
