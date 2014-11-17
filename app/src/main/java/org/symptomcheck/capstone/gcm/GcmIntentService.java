/*
 * Copyright (C) 2013 The Android Open Source Project
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

package org.symptomcheck.capstone.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.symptomcheck.capstone.SyncUtils;
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.ui.MainActivity;
import org.symptomcheck.capstone.utils.NotificationHelper;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 2;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public final String TAG = GcmIntentService.this.getClass().getSimpleName();

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                NotificationHelper.sendNotification(getApplicationContext(),NOTIFICATION_ID,
                        "Gcm message","Send error: " + extras.toString(),MainActivity.class,false);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                NotificationHelper.sendNotification(getApplicationContext(),NOTIFICATION_ID,
                        "Gcm message","Deleted messages on server: " + extras.toString(),MainActivity.class,false);
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                final String userType = extras.getString(GcmConstants.GCM_EXTRAS_KEY_USERTYPE);
                final String userName = extras.getString(GcmConstants.GCM_EXTRAS_KEY_USERNAME);
                final String action = extras.getString(GcmConstants.GCM_EXTRAS_KEY_ACTION);
                Log.i(TAG, "GCMMessage Received: " + extras.toString() + "=> " + action + "-" + userName + "-" + userType);

                UserType userOriginMsg = UserType.valueOf(userType);
                handleTriggerSync(action, userOriginMsg);

                // Post notification of received message.
                NotificationHelper.sendNotification(getApplicationContext(),NOTIFICATION_ID,
                        "Gcm message", "Received: " + extras.toString(), MainActivity.class,false);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handleTriggerSync(String action, UserType userTypeSender) {
        switch (userTypeSender){
            case PATIENT:
                if(action.equals(GcmConstants.GCM_ACTION_CHECKIN_RX)) {
                    SyncUtils.TriggerRefreshPartialLocal(ActiveContract.SYNC_CHECK_IN);
                }
                break;
            case DOCTOR:
                if(action.equals(GcmConstants.GCM_ACTION_MEDICATION_RX)){
                    SyncUtils.TriggerRefreshPartialLocal(ActiveContract.SYNC_MEDICINES);
                }
                break;
            case ADMIN:
                break;
            case UNKNOWN:
                break;
        }
    }


}
