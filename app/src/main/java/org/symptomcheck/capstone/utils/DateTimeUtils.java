/*
 * ******************************************************************************
 *   Copyright (c) 2014-2015 Ivan Gaglioti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */
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
                    TimeZone.getDefault()).format(format.equals(Constants.TIME.DEFAULT_FORMAT) ? "YYYY-MM-DD hh:mm" : format);
        }
        else{
            humanTime = Constants.STRINGS.EMPTY;
        }
        return humanTime;
    }
}
