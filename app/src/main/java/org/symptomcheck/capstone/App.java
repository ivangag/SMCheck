package org.symptomcheck.capstone;

import android.app.Activity;
import android.app.Application;
import android.app.job.JobScheduler;
import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.view.inputmethod.InputMethodManager;

import com.activeandroid.ActiveAndroid;

import org.symptomcheck.capstone.model.ExperienceType;

/**
 * Created by Ivan on 22/10/2014.
 */
public class App extends MultiDexApplication {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //VolleyRequestController.get(this).getRequestQueue().stop();
        //DownloadRDSManager.get().unbindRDSService(this);
        ActiveAndroid.dispose();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //DownloadManager.get().startRDService(this);
        //VolleyRequestController.get(this).getRequestQueue().start();
        //DownloadRDSManager.get().bindRDService(this);
        //CacheDataManager.get().setContext(this);
        ActiveAndroid.initialize(this);
        SyncUtils.CreateSyncAccount(getApplicationContext());
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }


    public static String getPatientExperienceTranslation(ExperienceType experienceType){
        String result = "NA";
        switch (experienceType){
            case SEVERE:
                result = mContext.getResources().getString(R.string.severe);
                break;
            case SEVERE_OR_MODERATE:
                result = mContext.getResources().getString(R.string.severe_or_moderate);
                break;
            case CANNOT_EAT:
                result = mContext.getResources().getString(R.string.cannot_eat);
                break;
            case UNKNOWN:
                break;
        }
        return result;
    }
}
