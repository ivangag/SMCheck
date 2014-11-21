package org.symptomcheck.capstone.utils;

import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * Created by Ivan on 21/11/2014.
 */
public class DateTimeUtils {

    public static String convertEpochToHumanTime(String epochTime) {
        final String savedTime = epochTime;
        String humanTime;
        if(savedTime != null &&
                !savedTime.isEmpty()){
            humanTime = DateTime.forInstant(Long.valueOf(savedTime),
                    TimeZone.getDefault()).format("YYYY-MM-DD hh:mm");
        }
        else{
            humanTime = Costants.STRINGS.EMPTY;
        }
        return humanTime;
    }
}
