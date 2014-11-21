package org.symptomcheck.capstone.utils;

import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * Created by Ivan on 21/11/2014.
 */
public class DateTimeUtils {

    public static String convertEpochToHumanTime(String epochTime, String format) {
        final String savedTime = epochTime;
        String humanTime;
        if(savedTime != null &&
                !savedTime.isEmpty()){
            humanTime = DateTime.forInstant(Long.valueOf(savedTime),
                    TimeZone.getDefault()).format(format.equals(Costants.TIME.DEFAULT_FORMAT) ? "YYYY-MM-DD hh:mm" : format);
        }
        else{
            humanTime = Costants.STRINGS.EMPTY;
        }
        return humanTime;
    }
}
