package org.symptomcheck.capstone;

import android.app.Activity;
import android.app.Application;
import android.app.job.JobScheduler;
import android.view.inputmethod.InputMethodManager;

import com.activeandroid.ActiveAndroid;

/**
 * Created by Ivan on 22/10/2014.
 */
public class App extends Application {


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
}
