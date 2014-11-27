/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symptomcheck.capstone;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


// TODO#BPR_3 Service is invoked in response to Intents with action android.content.SyncAdapter, and returns a Binder connection to SyncAdapter.
public class SyncService extends Service {
    private static final String TAG = "SyncService";

    private static final Object sSyncAdapterLock = new Object();
    private static SymptomSyncAdapter sSyncAdapter = null;

    /**
     * Thread-safe constructor, creates static {@link SymptomSyncAdapter} instance.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SymptomSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    /**
     * Logging-only destructor.
     */
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    /**
     * Return Binder handle for IPC communication with {@link SymptomSyncAdapter}.
     *
     * <p>New sync requests will be sent directly to the SyncAdapter using this channel.
     *
     * @param intent Calling intent
     * @return Binder handle for {@link SymptomSyncAdapter}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
