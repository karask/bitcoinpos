package gr.cryptocurrencies.bitcoinpos.utilities;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by kostas on 27/7/2016.
 */
public class DateUtilities {

    public final static String DATABASE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public final static String UTC = "UTC";

    public final static String USER_INTERFACE_DATE_FORMAT = "yyyy-MM-dd  HH:mm:ss";

    public static String getRelativeTimeString(String date) {

        DateFormat dbDf = new SimpleDateFormat(DateUtilities.DATABASE_DATE_FORMAT);
        dbDf.setTimeZone(TimeZone.getTimeZone(DateUtilities.UTC));
        DateFormat uiDf = new SimpleDateFormat(DateUtilities.USER_INTERFACE_DATE_FORMAT);
        uiDf.setTimeZone(Calendar.getInstance().getTimeZone());

        Date dbDate = null;
        try {
            dbDate = dbDf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long dbTimeInMillis = dbDate.getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        // convert to friendly relative time string onl if difference less than 23 hours
        String relativeTimeString;
        if(currentTimeInMillis - dbTimeInMillis < 23*60*60 * 1000) {
            // only get friendly relative string if we have a translation for that language
            // Currently only Locale.ENGLISH
            if(Locale.getDefault().getLanguage().equals( Locale.ENGLISH.toString() ) ||
                    Locale.getDefault().getLanguage().equals( "el" )) {
                relativeTimeString = DateUtils.getRelativeTimeSpanString(dbTimeInMillis, currentTimeInMillis, DateUtils.MINUTE_IN_MILLIS).toString();
            } else {
                relativeTimeString = dbDate != null ? uiDf.format(dbDate) : " -- ";
            }
        } else {
            relativeTimeString = dbDate != null ? uiDf.format(dbDate) : " -- ";
        }

        return relativeTimeString;
    }

    public static String getDateAsString(Date date) {
        DateFormat dbDf = new SimpleDateFormat(DateUtilities.DATABASE_DATE_FORMAT);
        dbDf.setTimeZone(TimeZone.getTimeZone(DateUtilities.UTC));

        return dbDf.format(date);
    }

    public static Date getStringAsDate(String date) {
        DateFormat dbDf = new SimpleDateFormat(DateUtilities.DATABASE_DATE_FORMAT);
        dbDf.setTimeZone(TimeZone.getTimeZone(DateUtilities.UTC));

        Date dbDate = null;
        try {
            dbDate = dbDf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dbDate;
    }


}
