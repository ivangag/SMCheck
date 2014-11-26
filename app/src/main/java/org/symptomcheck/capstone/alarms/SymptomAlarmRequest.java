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
package org.symptomcheck.capstone.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.preference.UserPreferencesManager;
import org.symptomcheck.capstone.utils.Constants;

import java.util.Calendar;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * Created by igaglioti on 05/11/2014.
 */
public class SymptomAlarmRequest {

    public enum AlarmRequestedType{
        ALARM_CHECK_IN_REMINDER,
        ALARM_CHECK_ALERTS
    }

    private static SymptomAlarmRequest ourInstance = new SymptomAlarmRequest();
    private AlarmManager alarmReminderMgr;
    private PendingIntent alarmReminderIntent;
    private String TAG = SymptomAlarmRequest.this.getClass().getSimpleName();

    public static SymptomAlarmRequest get() {
        return ourInstance;
    }

    private SymptomAlarmRequest() {
    }



    public void setAlarm(Context ctx,AlarmRequestedType alarmRequestedType){
        switch (alarmRequestedType){
            case ALARM_CHECK_IN_REMINDER:
                if(DAOManager.get().getUser().getUserType() == UserType.PATIENT)
                    setReminderAlarm(ctx);
                break;
            case ALARM_CHECK_ALERTS:
                break;
            default:
                break;
        }
    }

    public void cancelAlarm(Context ctx,AlarmRequestedType alarmRequestedType){
        switch (alarmRequestedType){
            case ALARM_CHECK_IN_REMINDER:
                cancelReminderAlarm(ctx);
                break;
            case ALARM_CHECK_ALERTS:
                break;
            default:
                break;
        }
    }

    /**
     * Sets a repeating alarm that runs N times a day at time chosen by Patient (e.g. 8:30 a.m.) When the
     * alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     * @param context Context
     */
    private void setReminderAlarm(Context context) {

        alarmReminderMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        alarmReminderIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendarStartOfToday = Calendar.getInstance();
        calendarStartOfToday.setTimeInMillis(System.currentTimeMillis());

        calendarStartOfToday.set(Calendar.HOUR_OF_DAY, 0);
        calendarStartOfToday.set(Calendar.MINUTE, 0);
        calendarStartOfToday.set(Calendar.SECOND, 0);
        long timeToday_00 = calendarStartOfToday.getTimeInMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long timeToday_24 = calendar.getTimeInMillis();

        //final String latestCheckin = UserPreferencesManager.get().getLatestCheckinSubmission(context);
        final UserInfo user = DAOManager.get().getUser();
        if(user != null) {
            final CheckIn latestCheckInSubmitted = CheckIn.getLatestOne();
            String latestCheckin = Constants.STRINGS.EMPTY;
            if(latestCheckInSubmitted != null){
                if(latestCheckInSubmitted.getIssueDateTime() != null){
                    latestCheckin = latestCheckInSubmitted.getIssueDateTime();
                }
            }


            long timeLatestCheckin = (latestCheckin.equals(Constants.STRINGS.EMPTY)) ? 0 : Long.valueOf(latestCheckin);

            final int checkInUserPeriodicity = UserPreferencesManager.get().getCheckInTimes(context);
            final int hour = UserPreferencesManager.get().getStartCheckInHour(context);
            final int minutes = UserPreferencesManager.get().getStartCheckInMinute(context);
            final int userCheckinTimePref = UserPreferencesManager.get().getCheckInTimes(context);

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, 0);
            long timeDailyStartAsPreference = calendar.getTimeInMillis();

            final long timeNow = DateTime.now(TimeZone.getDefault()).getMilliseconds(TimeZone.getDefault());

            final int checkInThisDay = CheckIn.getCountInThisDay();
            // we have to check if the latest check-in was done yesterday

            long intervalRepeatFrequency;
            long timeOfNextStartAsDue;

            if (timeLatestCheckin < timeToday_00) {
                Log.d(TAG,"timeLatestCheckin IS < timeToday_00");
                if (timeNow >= timeDailyStartAsPreference) {
                    Log.d(TAG,"timeNow IS >= timeDailyStartAsPreference");
                    timeOfNextStartAsDue = timeNow + AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15;
                } else {
                    Log.d(TAG,"timeNow IS < timeDailyStartAsPreference");
                    timeOfNextStartAsDue = timeDailyStartAsPreference;
                }
                intervalRepeatFrequency = (timeToday_24 - timeOfNextStartAsDue) / userCheckinTimePref;

            } else {
                Log.d(TAG,"timeLatestCheckin IS >= timeToday_00");
                intervalRepeatFrequency = (timeToday_24 - timeDailyStartAsPreference) / userCheckinTimePref;
                if (checkInThisDay >= userCheckinTimePref) {
                    Log.d(TAG,"checkInThisDay IS <= userCheckinTimePref");
                    timeDailyStartAsPreference += AlarmManager.INTERVAL_DAY;
                    timeOfNextStartAsDue = timeDailyStartAsPreference;
                }else {
                    if(timeNow > timeDailyStartAsPreference) {
                        Log.d(TAG,"timeNow IS > timeDailyStartAsPreference");
                        timeOfNextStartAsDue = timeLatestCheckin + intervalRepeatFrequency;
                    }
                    else {
                        Log.d(TAG,"timeNow IS <= timeDailyStartAsPreference");
                        intervalRepeatFrequency = (timeToday_24 - timeNow) / (userCheckinTimePref - checkInThisDay);
                        timeOfNextStartAsDue = timeNow + intervalRepeatFrequency;
                    }
                }

            }

            boolean isNextDay = (timeOfNextStartAsDue > timeToday_24);

            String nextTime = DateTime.forInstant(timeDailyStartAsPreference, TimeZone.getDefault()).format("DD-MM hh:mm");
            String nextTimeStartAsDue = DateTime.forInstant(timeOfNextStartAsDue, TimeZone.getDefault()).format("DD-MM hh:mm");
            String latestCheckinTime = DateTime.forInstant(timeLatestCheckin, TimeZone.getDefault()).format("DD-MM hh:mm");
            UserPreferencesManager.get().setNextScheduledCheckin(context, nextTimeStartAsDue);

            Log.d(TAG,
                    "checkInUserPeriodicity:" + checkInUserPeriodicity + " - " +
                            "checkInThisDay:" + checkInThisDay + " - " +
                            "time:" + hour + ":" + minutes +
                            " - latestCheckinTime:" + latestCheckinTime +
                            " - intervalRepeatFrequency:" + intervalRepeatFrequency + "(" + ((double) (intervalRepeatFrequency / 3600)) + ")" +
                            " - timeOfNextStart:" + timeDailyStartAsPreference + "(" + nextTime + ")" +
                            " - nextTimeStartAsDue:" + "(" + nextTimeStartAsDue + ")" +
                            " - isNextDay?" + (isNextDay ? "YES" : "NO")
            );

        /*
         * If you don't have precise time requirements, use an inexact repeating alarm
         * the minimize the drain on the device battery.
         *
         * The call below specifies the alarm type, the trigger time, the interval at
         * which the alarm is fired, and the alarm's associated PendingIntent.
         * It uses the alarm type RTC_WAKEUP ("Real Time Clock" wake up), which wakes up
         * the device and triggers the alarm according to the time of the device's clock.
         *
         * Alternatively, you can use the alarm type ELAPSED_REALTIME_WAKEUP to trigger
         * an alarm based on how much time has elapsed since the device was booted. This
         * is the preferred choice if your alarm is based on elapsed time--for example, if
         * you simply want your alarm to fire every 60 minutes. You only need to use
         * RTC_WAKEUP if you want your alarm to fire at a particular date/time. Remember
         * that clock-based time may not translate well to other locales, and that your
         * app's behavior could be affected by the user changing the device's time setting.
         *
         * Here are some examples of ELAPSED_REALTIME_WAKEUP:
         *
         * // Wake up the device to fire a one-time alarm in one minute.
         * alarmReminderMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
         *         SystemClock.elapsedRealtime() +
         *         60*1000, alarmReminderIntent);
         *
         * // Wake up the device to fire the alarm in 30 minutes, and every 30 minutes
         * // after that.
         * alarmReminderMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
         *         AlarmManager.INTERVAL_HALF_HOUR,
         *         AlarmManager.INTERVAL_HALF_HOUR, alarmReminderIntent);
         */

            // Set the alarm to fire at approximately 8:30 a.m., according to the device's
            // clock, and to repeat once a day.
        /*alarmReminderMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmReminderIntent);*/

            // Set the alarm to fire immediately, according to the device's
            // clock, and to repeat once a minute.....
        /*alarmReminderMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * 30 * 1, alarmReminderIntent);*/

            // Set the alarm to fire at approximately Start Check-In Time chosen by Patient, according to the device's
            // clock, and to repeat once a day.
            alarmReminderMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    timeOfNextStartAsDue,
                    intervalRepeatFrequency,
                    alarmReminderIntent);
        /*
        // Wake up the device to fire a one-time alarm in one minute.
        alarmReminderMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        15 * 1000, alarmReminderIntent);*/

            // Enable {@code SymptomBootReceiver} to automatically restart the alarm when the
            // device is rebooted.
            ComponentName receiver = new ComponentName(context, SymptomBootReceiver.class);
            PackageManager pm = context.getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);


            Log.i(TAG, "setReminderAlarm");
        }
    }

   /* private void setReminderAlarm(Context context) {

        alarmReminderMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        alarmReminderIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long timeMissingToEndOfDay = calendar.getTimeInMillis();

        final int checkInPeriodicity =  UserPreferencesManager.get().getCheckInTimes(context);
        final int hour =  UserPreferencesManager.get().getStartCheckInHour(context);
        final int minutes =  UserPreferencesManager.get().getStartCheckInMinute(context);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);

        long timeOfNextStartAsPreferences = calendar.getTimeInMillis();
        final long timeNow = DateTime.now(TimeZone.getDefault()).getMilliseconds(TimeZone.getDefault());
        final int checkInThisDay = CheckIn.getCountInThisDay();
        boolean setNextDay = (timeNow >  timeOfNextStartAsPreferences)
                                    || (checkInThisDay >= UserPreferencesManager.get().getCheckInTimes(context));

        long timeOfNextStartAsDue = timeOfNextStartAsPreferences;

        timeOfNextStartAsPreferences = timeOfNextStartAsPreferences + (setNextDay ? AlarmManager.INTERVAL_DAY : 0);
        long timeRemainingToEndOfDay;
        if (timeMissingToEndOfDay < timeOfNextStartAsPreferences){
            timeRemainingToEndOfDay = timeMissingToEndOfDay - timeNow;
        }else {
            timeRemainingToEndOfDay = timeMissingToEndOfDay - timeOfNextStartAsPreferences;
        }
        final long intervalRepeatFrequency =  timeRemainingToEndOfDay / (checkInPeriodicity - checkInThisDay);
        if(setNextDay) { //check if it would be done next day
            if (checkInThisDay >= 4) { // if checkin required is reached minimum required then schedule for the next day
                timeOfNextStartAsDue += (AlarmManager.INTERVAL_DAY);
            } else { // otherwise schedule it for the next half hour
                timeOfNextStartAsDue = timeNow + AlarmManager.INTERVAL_FIFTEEN_MINUTES;
            }
        }
        String nextTime = DateTime.forInstant(timeOfNextStartAsPreferences,TimeZone.getDefault()).format("DD-MM hh:mm");
        String nextTimeStartAsDue = DateTime.forInstant(timeOfNextStartAsDue,TimeZone.getDefault()).format("DD-MM hh:mm");
        UserPreferencesManager.get().setNextScheduledCheckin(context,nextTimeStartAsDue);

        Log.d(TAG,
                "checkInPeriodicity:" + checkInPeriodicity + " - " +
                "checkInThisDay:" + checkInThisDay + " - " +
                "time:" + hour + ":" + minutes +
                " - intervalRepeatFrequency:" + intervalRepeatFrequency + "(" + ((double)(intervalRepeatFrequency/3600)) + ")" +
                " - timeOfNextStart:" + timeOfNextStartAsPreferences + "(" + nextTime + ")" +
                " - nextTimeStartAsDue:" + "(" + nextTimeStartAsDue + ")" +
                " - setNextDay?" + (setNextDay ? "YES"  :"NO")
        );

        *//*
         * If you don't have precise time requirements, use an inexact repeating alarm
         * the minimize the drain on the device battery.
         *
         * The call below specifies the alarm type, the trigger time, the interval at
         * which the alarm is fired, and the alarm's associated PendingIntent.
         * It uses the alarm type RTC_WAKEUP ("Real Time Clock" wake up), which wakes up
         * the device and triggers the alarm according to the time of the device's clock.
         *
         * Alternatively, you can use the alarm type ELAPSED_REALTIME_WAKEUP to trigger
         * an alarm based on how much time has elapsed since the device was booted. This
         * is the preferred choice if your alarm is based on elapsed time--for example, if
         * you simply want your alarm to fire every 60 minutes. You only need to use
         * RTC_WAKEUP if you want your alarm to fire at a particular date/time. Remember
         * that clock-based time may not translate well to other locales, and that your
         * app's behavior could be affected by the user changing the device's time setting.
         *
         * Here are some examples of ELAPSED_REALTIME_WAKEUP:
         *
         * // Wake up the device to fire a one-time alarm in one minute.
         * alarmReminderMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
         *         SystemClock.elapsedRealtime() +
         *         60*1000, alarmReminderIntent);
         *
         * // Wake up the device to fire the alarm in 30 minutes, and every 30 minutes
         * // after that.
         * alarmReminderMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
         *         AlarmManager.INTERVAL_HALF_HOUR,
         *         AlarmManager.INTERVAL_HALF_HOUR, alarmReminderIntent);
         *//*

        // Set the alarm to fire at approximately 8:30 a.m., according to the device's
        // clock, and to repeat once a day.
        *//*alarmReminderMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmReminderIntent);*//*

        // Set the alarm to fire immediately, according to the device's
        // clock, and to repeat once a minute.....
        *//*alarmReminderMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * 30 * 1, alarmReminderIntent);*//*

        // Set the alarm to fire at approximately Start Check-In Time chosen by Patient, according to the device's
        // clock, and to repeat once a day.
        alarmReminderMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                timeOfNextStartAsDue,
                intervalRepeatFrequency,
                alarmReminderIntent);
        *//*
        // Wake up the device to fire a one-time alarm in one minute.
        alarmReminderMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        15 * 1000, alarmReminderIntent);*//*

        // Enable {@code SymptomBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, SymptomBootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);


        Log.i(TAG, "setReminderAlarm");
    }
*/
    /**
     * Cancels the alarm.
     * @param context Context
     */
    private void cancelReminderAlarm(Context context) {
        alarmReminderMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        // If the alarm has been set, cancel it.
        if (alarmReminderMgr != null) {
            alarmReminderMgr.cancel(alarmReminderIntent);
        }

        // Disable {@code SymptomBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, SymptomBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Log.i(TAG,"cancelReminderAlarm");
    }

}
