package org.symptomcheck.capstone.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by igaglioti on 06/11/2014.
 */
public class BuildInfo {
    private static BuildInfo ourInstance = new BuildInfo();

    public static BuildInfo get() {
        return ourInstance;
    }

    private BuildInfo() {
    }

    public boolean IsDebug(Context ctx){
        boolean debug = false;
        try {
            if ((ctx.getPackageManager().getPackageInfo(
                    ctx.getPackageName(), 0).applicationInfo.flags &
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                debug = true;
            } else {
                debug = false;
                //Release mode
                //BASE_SERVICE_URL = "http://www.example.com";
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return debug;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
