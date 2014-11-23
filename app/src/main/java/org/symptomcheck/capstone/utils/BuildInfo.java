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
