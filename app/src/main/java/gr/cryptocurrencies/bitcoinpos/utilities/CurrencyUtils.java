package gr.cryptocurrencies.bitcoinpos.utilities;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import gr.cryptocurrencies.bitcoinpos.network.ExchangeRates;


public class CurrencyUtils {

    private static final double maxBTCAmount = 10000;

    public enum CurrencyType {
        BTC,
        BTCTEST,
        BCH,
        LTC,
        ETH,
        LOCAL
    }
    public static double stringAmountToDouble(String amountStr) {
        return Double.parseDouble(amountStr);
    }

    public static String doubleAmountToString(double amount, CurrencyType currencyType) {

        DecimalFormat formatter;
        if(currencyType == CurrencyType.BTC) {
            formatter = new DecimalFormat("#.########", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        }
        else if(currencyType == CurrencyType.BCH) {
            formatter = new DecimalFormat("#.########", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        }
        else if(currencyType == CurrencyType.LTC) {
            formatter = new DecimalFormat("#.########", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        }
        else if(currencyType == CurrencyType.ETH) {
            formatter = new DecimalFormat("#.########", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        }
        else if(currencyType == CurrencyType.BTCTEST) {
            formatter = new DecimalFormat("#.########", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        }
        else {
            formatter = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        }
        formatter.setRoundingMode( RoundingMode.HALF_UP );
        return formatter.format(amount);
    }

    // TODO: use doubleAmountToString for cleaner code
    public static String getLocalCurrencyFromBtc(String amount) {

        ExchangeRates exchangeRates = ExchangeRates.getInstance();

        double newAmount = Double.parseDouble(amount) * exchangeRates.getBtcToLocalRate();
        DecimalFormat formatter = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        formatter.setRoundingMode( RoundingMode.HALF_UP );
        return formatter.format(newAmount);
    }

    public static String getLocalCurrencyFromOtherCryptos(String amount) {

        ExchangeRates exchangeRates = ExchangeRates.getInstance();

        double newAmount = Double.parseDouble(amount) * exchangeRates.getOtherCryptosToLocalRate();
        DecimalFormat formatter = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        formatter.setRoundingMode( RoundingMode.HALF_UP );
        return formatter.format(newAmount);
    }

    // TODO: use doubleAmountToString for cleaner code
    public static String getBtcFromLocalCurrency(String amount) {

        ExchangeRates exchangeRates = ExchangeRates.getInstance();

        // the following was losing precision at every toggling!!
        //double newAmount = Double.parseDouble(currentAmount) * exchangeRates.getLocalToBtcRate();

        double newAmount = Double.parseDouble(amount) / exchangeRates.getBtcToLocalRate();
        DecimalFormat formatter = new DecimalFormat("#.########", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        formatter.setRoundingMode( RoundingMode.HALF_UP );
        return formatter.format(newAmount);
    }

    public static String getOtherCryptosFromLocalCurrency(String amount){
        ExchangeRates exchangeRates = ExchangeRates.getInstance();

        // the following was losing precision at every toggling!!
        //double newAmount = Double.parseDouble(currentAmount) * exchangeRates.getLocalToBtcRate();

        double newAmount = Double.parseDouble(amount) / exchangeRates.getOtherCryptosToLocalRate();
        DecimalFormat formatter = new DecimalFormat("#.########", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        formatter.setRoundingMode( RoundingMode.HALF_UP );
        return formatter.format(newAmount);
    }


    // temporarily returns int to specify the error -- this should be substituted with a proper enum
    // that ALSO contains the actual strings (from R.string)
    // Values: -1: GreaterThanAllowedValue, -2: NoMoreDecimalsAllowed
    public static int checkValidAmount(double doubleAmount, CurrencyType currencyType) {

        ExchangeRates exchangeRates = ExchangeRates.getInstance();

        String amount = CurrencyUtils.doubleAmountToString(doubleAmount, currencyType);
        if(currencyType == CurrencyType.LOCAL) {
            // was local currency

            // allow only 2 decimal digits
            int dotIndex = amount.indexOf(".");
            if(dotIndex != -1 && amount.substring(dotIndex).length() > 3)
                return -2;

            if(exchangeRates.getLastUpdated() != null) {
                // if equiv amount in BTC should not be more than maxBTCAmount
                if(doubleAmount / exchangeRates.getBtcToLocalRate() > maxBTCAmount)
                    return -1;
            }
        } else {
            // was BTC

            // allow only 8 decimal digits
            int dotIndex = amount.indexOf(".");
            if(dotIndex != -1 && amount.substring(dotIndex).length() > 9)
                return -2;

            if(doubleAmount > maxBTCAmount)
                return -1;
        }

        return 1;
    }


}
