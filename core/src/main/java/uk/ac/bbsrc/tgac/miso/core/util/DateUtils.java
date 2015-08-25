package uk.ac.bbsrc.tgac.miso.core.util;

import net.sf.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Created by zakm on 06/08/2015.
 */
public class DateUtils {

    static final int EXPIRY_THRESHOLD_DAYS = 30;

    public enum ExpiryState{
        EXPIRED("expired"),
        SOON_TO_EXPIRE("soon to expire"),
        GOOD_TO_USE("good to use");

        private String name;

        ExpiryState(String name){
            this.name = name;
        }

        public String toString(){
            return name;
        }


    }

    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate asLocalDate(String date){
        return LocalDate.parse(date);

    }

    public static ExpiryState getExpiryState(LocalDate date){
        LocalDate today = LocalDate.now();
        long periodDays = ChronoUnit.DAYS.between(today,date);
        if(periodDays < 0){
            return ExpiryState.EXPIRED;
        }else if(periodDays < EXPIRY_THRESHOLD_DAYS){
            return ExpiryState.SOON_TO_EXPIRE;
        }else{
            return ExpiryState.GOOD_TO_USE;
        }

    }

    public static Timestamp getTimeStampFromJSON(JSONObject timestampJSON){
        long millis = timestampJSON.getLong("time");

        return new Timestamp(millis);

        

    }

    public static String getStringFromTimeStamp(Timestamp timestamp){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String string  = dateFormat.format(timestamp);
        return string;
    }

}
