package org.symptomcheck.capstone.preference;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.symptomcheck.capstone.SyncUtils;
import org.symptomcheck.capstone.utils.Constants;

/**
 * Created by igaglioti on 06/11/2014.
 */
public class UserPreferencesManager {

    private static final String TAG = "UserPreferencesManager";

    private static final String REMEMBER_LOGIN = "remember_login";
    private static final String USERNAME_LOGIN = "user_login";
    private static final String PASSWORD_LOGIN = "pw_login";
    private static final String PROPERTY_REG_ID = "gcm_reg_id";
    private static final String PROPERTY_BEARER_TOKEN = "bearer_token";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private static final String PROPERTY_IS_LOGGED = "is_logged" ;

    public static final String KEY_CHECK_IN_FREQ = "checkin_frequency";
    public static final String KEY_CHECK_IN_START = "checkin_start_time";
    public static final String KEY_SYNC_FREQ = "sync_frequency";
    public static final String KEY_SYNC_ONLY_WIFI = "sync_only_wifi";
    public static final String KEY_NEW_NOTIFICATIONS_ALERT = "notifications_new_message";
    public static final String KEY_NEXT_SCHEDULED_CHECKIN = "next_schedule_checkin";
    public static final String KEY_LAST_SUBMITTED_CHECKIN_TIME = "latest_checkin_time";
    private static UserPreferencesManager ourInstance = new UserPreferencesManager();
    private static Context mContext;

    public static final int DEFAULT_CHECK_IN_TIMES = 4;



    public static UserPreferencesManager get() {
        return ourInstance;
    }

    private UserPreferencesManager() {

    }

    public String getBearerToken(Context context){
       return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PROPERTY_BEARER_TOKEN, "");
    }

    public boolean isLogged(Context context){
       return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PROPERTY_IS_LOGGED, false);
    }

    public boolean getLoginRememberMe(Context context){
       return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(REMEMBER_LOGIN, false);
    }

    public boolean isSyncActiveOnlyViaWifi(Context context){
       return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SYNC_ONLY_WIFI, false);
    }

    public String getLatestCheckinSubmission(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LAST_SUBMITTED_CHECKIN_TIME, Constants.STRINGS.EMPTY);
    }
    private String getStartHourMinutes(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_CHECK_IN_START, Constants.STRINGS.EMPTY);
    }

    public int getStartCheckInHour(Context context){
       int hour = 8;
       String hourStr = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_CHECK_IN_START, Constants.STRINGS.EMPTY);
        if(!hourStr.isEmpty()
            && hourStr.contains(":")){
            try {
                hour = Integer.valueOf(hourStr.substring(0, hourStr.indexOf(':')));
            }catch (Exception ignored){
            }
        }
        return hour;
    }
    public int getStartCheckInMinute(Context context){
        int minute = 0;
        String minuteStr = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_CHECK_IN_START, Constants.STRINGS.EMPTY);
        if(!minuteStr.isEmpty()
                && minuteStr.contains(":")){
            try {
                minute = Integer.valueOf(minuteStr.substring(minuteStr.indexOf(':') + 1));
            }catch (Exception ignored){
            }
        }
        return minute;
    }

    /**
     * Get minutes (in seconds) of Syncing frequency chosen by User
     * @param context Context
     * @return Minutes in seconds (e.g. for 3 minutes it returns 180 seconds)
     */
    public int getCheckInTimes(Context context){
        int times = DEFAULT_CHECK_IN_TIMES;
        String str = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_CHECK_IN_FREQ, Constants.STRINGS.EMPTY);
        if(!str.isEmpty()){
            times = Integer.valueOf(str);
        }
        return times;
    }

    /**
     * Get minutes (in seconds) of Syncing frequency chosen by User
     * @param context Context
     * @return Minutes in seconds (e.g. for 3 minutes it returns 180 seconds)
     */
    public long getSyncFrequency(Context context){
        long minutes = SyncUtils.SYNC_FREQUENCY;
        String minuteStr = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_SYNC_FREQ, Constants.STRINGS.EMPTY);
        if(!minuteStr.isEmpty()){
            try {
                minutes = Integer.valueOf(minuteStr) * 60;
            }catch (Exception ignored){
            }
        }
        return minutes;
    }

    /*
    public String getLoginUsername(Context context){
       return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USERNAME_LOGIN, "");
    }

    public String getLoginPassword(Context context){
       return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PASSWORD_LOGIN, "");
    }


    public void setLoginUsername(Context context, String username) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(USERNAME_LOGIN, username).commit();
    }
    public void setLoginPassword(Context context, String password) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PASSWORD_LOGIN, password).commit();
    }
    */

    public void setLatestCheckinSubmission(Context context, String time){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_LAST_SUBMITTED_CHECKIN_TIME,time);
    }

    public void setActiveSyncOnlyViaWifi(Context context, boolean active){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(KEY_SYNC_ONLY_WIFI, active).commit();
    }


    public void setLogged(Context context, boolean logged){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PROPERTY_IS_LOGGED, logged).commit();
    }
    public void setLoginRememberMe(Context context, boolean rememberMe){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(REMEMBER_LOGIN, rememberMe).commit();
    }

    public void setBearerToken(Context context, String token) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PROPERTY_BEARER_TOKEN, token).commit();
    }


    public void setGcmRegId(Context context, String gcmRegId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PROPERTY_REG_ID, gcmRegId).commit();
    }
    public void setNextScheduledCheckin(Context context, String nextTime) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_NEXT_SCHEDULED_CHECKIN, nextTime).commit();
    }
    public void setAppVers(Context context, int appVers) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(PROPERTY_APP_VERSION, appVers).commit();
    }


    public String getGcmRegId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PROPERTY_REG_ID, "");
    }

    public int getAppVers(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
    }

    public String getNextScheduledCheckin(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_NEXT_SCHEDULED_CHECKIN, Constants.STRINGS.EMPTY);
    }

    public void printAll(Context context){
        StringBuilder sb = new StringBuilder();
        sb
                .append(REMEMBER_LOGIN).append(":").append(this.getLoginRememberMe(context)).append(" - ")
                .append(PROPERTY_REG_ID).append(":").append(this.getGcmRegId(context)).append(" - ")
                .append(PROPERTY_BEARER_TOKEN).append(":").append(this.getBearerToken(context)).append(" - ")
                .append(PROPERTY_IS_LOGGED).append(":").append(this.isLogged(context)).append(" - ")
                .append(KEY_CHECK_IN_FREQ).append(":").append(this.getSyncFrequency(context)).append(" - ")
                .append(KEY_SYNC_ONLY_WIFI).append(":").append(this.isSyncActiveOnlyViaWifi(context)).append(" - ")
                .append(KEY_CHECK_IN_START).append(":").append(this.getStartHourMinutes(context)).append(" - ")
                .append(KEY_NEXT_SCHEDULED_CHECKIN).append(":").append(this.getNextScheduledCheckin(context));
        Log.i(TAG,sb.toString());
    }
}
