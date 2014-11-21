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
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.preference.UserPreferencesManager;

import java.util.Calendar;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * Created by igaglioti on 05/11/2014.
 */
public class SymptomAlarmRequest {

    public static int ALARM_REMINDER;
    public static int ALARM_CHECK_ALERTS;

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

    // BEGIN_INCLUDE(set_alarm)
    /**
     * Sets a repeating alarm that runs once a day at approximately 8:30 a.m. When the
     * alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     * @param context
     */
    private void setReminderAlarm(Context context) {

        alarmReminderMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        alarmReminderIntent = PendingIntent.getBroadcast(context, 0, intent, 0);


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // Set the alarm's trigger time to 8:30 a.m.
        //calendar.set(Calendar.HOUR_OF_DAY, 8);
        //calendar.set(Calendar.MINUTE, 30);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long millisecondsTo24 = calendar.getTimeInMillis();

        final int checkInPeriodicity =  UserPreferencesManager.get().getCheckInTimes(context);
        final int hour =  UserPreferencesManager.get().getStartCheckInHour(context);
        final int minutes =  UserPreferencesManager.get().getStartCheckInMinute(context);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        long millisecondsStartTime = calendar.getTimeInMillis();
        boolean setNextDay =  DateTime.now(TimeZone.getDefault()).getMilliseconds(TimeZone.getDefault())
                              >  millisecondsStartTime;

        long diffTo24 = ((millisecondsTo24 - millisecondsStartTime));
        long intervalRepeatFrequency =  diffTo24 / checkInPeriodicity;

        if(CheckIn.getCountFromMidNight() >= UserPreferencesManager.get().getCheckInTimes(context)){
            setNextDay = true;
        }

        millisecondsStartTime = calendar.getTimeInMillis() + (setNextDay ? AlarmManager.INTERVAL_DAY : 0);

        Log.d(TAG,
                "checkInPeriodicity: " + checkInPeriodicity + " - " +
                "time: " + hour + ":" + minutes +
                " - intervalRepeatFrequency: " + intervalRepeatFrequency +
                " - millisecondsStartTime: " + millisecondsStartTime +
                " - setNextDay? " + (setNextDay ? "YES"  :"NO")

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
                millisecondsStartTime,
                //AlarmManager.INTERVAL_DAY,
                intervalRepeatFrequency,
                alarmReminderIntent);
        /*
        // Wake up the device to fire a one-time alarm in one minute.
        alarmReminderMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        15 * 1000, alarmReminderIntent);*/

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, SymptomBootReceiver.class);
        PackageManager pm = context.getPackageManager();
        //AlarmManager.INTERVAL_DAY / N
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);


        Log.i(TAG, "setReminderAlarm...");
    }
    // END_INCLUDE(set_alarm)

    /**
     * Cancels the alarm.
     * @param context
     */
    // BEGIN_INCLUDE(cancel_alarm)
    private void cancelReminderAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmReminderMgr != null) {
            alarmReminderMgr.cancel(alarmReminderIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, SymptomBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Log.i(TAG,"cancelReminderAlarm");
    }
    // END_INCLUDE(cancel_alarm)
}
