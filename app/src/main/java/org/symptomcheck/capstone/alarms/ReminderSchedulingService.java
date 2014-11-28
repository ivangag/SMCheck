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

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.ui.CheckInFlowActivity;
import org.symptomcheck.capstone.ui.LoginActivity;

//TODO#BPR_3 Intent Service used for raising Check-In Alert Notification
public class ReminderSchedulingService extends IntentService {

    private final String TAG = ReminderSchedulingService.this.getClass().getSimpleName();
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION_CHECK_IN_SUBMISSION = "org.symptomcheck.capstone.alarms.action.CHECK_IN_SUBMISSION";

    public ReminderSchedulingService() {
        super("ReminderSchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_IN_SUBMISSION.equals(action)) {
                handleActionCheckInSubmission();
            }
        }
    }

    /**
     * Send notification
     *
     */
    private void handleActionCheckInSubmission() {
        sendNotification(getString(R.string.checkin_reminder_text));
    }

    /**
     * Post a notification
     * @param msg message to send with the notification
     */
    private void sendNotification(String msg) { //TODO#FDAR_2 create notification to alert the Patient it's time to do the Check-In
        Log.i(TAG,"sendNotification");
        final NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, CheckInFlowActivity.class),0);

        PendingIntent deleteIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(this, CheckinDismissingReceiver.class),0);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_doctor)
                        .setContentTitle(getString(R.string.checkin_reminder_title))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setTicker(getString(R.string.txt_checkin_reminder_ticker))
                        .setSound(alarmSound)
                        .setDeleteIntent(deleteIntent)
                        .setContentText(msg);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
