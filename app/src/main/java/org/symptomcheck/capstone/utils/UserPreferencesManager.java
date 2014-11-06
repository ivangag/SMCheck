package org.symptomcheck.capstone.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import org.symptomcheck.capstone.ui.LoginActivity;

/**
 * Created by igaglioti on 06/11/2014.
 */
public class UserPreferencesManager {

    private static final String REMEMBER_LOGIN = "remember_login";
    private static final String USERNAME_LOGIN = "user_login";
    private static final String PASSWORD_LOGIN = "pw_login";
    private static final String PROPERTY_REG_ID = "gcm_reg_id";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private static UserPreferencesManager ourInstance = new UserPreferencesManager();


    public static UserPreferencesManager get() {
        return ourInstance;
    }

    private UserPreferencesManager() {
    }

    public boolean getLoginRememberMe(Context context){
       return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(REMEMBER_LOGIN, false);
    }

    public String getLoginUsername(Context context){
       return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USERNAME_LOGIN, "");
    }

    public String getLoginPassword(Context context){
       return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PASSWORD_LOGIN, "");
    }

    public void setLoginRememberMe(Context context, boolean rememberMe){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(REMEMBER_LOGIN, rememberMe).commit();
    }

    public void setLoginUsername(Context context, String username) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(USERNAME_LOGIN, username).commit();
    }
    public void setLoginPassword(Context context, String password) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PASSWORD_LOGIN, password).commit();
    }


    public void setGcmRegId(Context context, String gcmRegId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PROPERTY_REG_ID, gcmRegId).commit();
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
}
