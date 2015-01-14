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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.alarms.CheckinDismissingReceiver;
import org.symptomcheck.capstone.ui.CheckInFlowActivity;
import org.symptomcheck.capstone.ui.LoginActivity;

/**
 * Created by igaglioti on 12/11/2014.
 */
public class NotificationHelper {

    public final static String NEXT_ACTIVITY_TO_LAUNCH = "next_activity";
    public final static int GO_TO_MAIN = 0;
    public final static int GO_TO_CHECK_IN = 1;

    public enum AlertType{
        ALERT_GO_TO_LOGIN,
        ALERT_CHECK_IN_CONFIRMATION,
    }
    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    public static void sendNotification(Context context,
                                        int NOTIFICATION_ID,
                                        String titleMsg,
                                        String msg,
                                        Class<?> pendingIntClass,
                                        boolean cancelAllPresent,
                                        String action,
                                        Bundle data) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, pendingIntClass);
        if(!action.isEmpty()){
            notificationIntent.setAction(action);
        }
        if(data != null){
            notificationIntent.putExtras(data);
        }

        //
        if(cancelAllPresent)
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent ,  0);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_doctor)
                        .setContentTitle(titleMsg)
                        .setTicker(msg)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setSound(alarmSound)
                        .setContentText(msg);


        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    /**
     * Convenient method to build and show an Alert dialog
     * @param context Context used to build alert dialog
     * @param alertType type of alert
     * @param Title Title of alert
     * @param Message Message to be shown in the body alert
     */
    public static void showAlertDialog(final Activity context, AlertType alertType,
                                       String Title, String Message){


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title
        alertDialogBuilder.setTitle(Title);
        switch (alertType){

            case ALERT_GO_TO_LOGIN:
                // set dialog message
                alertDialogBuilder
                        .setMessage("You aren't logged. Do you want re-enter credential?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close
                                // current activity
                                context.finish();
                                Intent intent = new Intent(context,LoginActivity.class);
                                intent.putExtra(NEXT_ACTIVITY_TO_LAUNCH,GO_TO_CHECK_IN);
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                                context.finish();
                            }
                        });
                break;
            case ALERT_CHECK_IN_CONFIRMATION:
                // set dialog message
                alertDialogBuilder
                        .setMessage(Message)
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close
                                // current activity
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });
                break;
        }
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    /**
     * Post a notification
     * @param msg message to send with the notification
     */
    public static void raiseCheckinReminderNotification(Context context, int NOTIFICATION_ID, String msg) { //TODO#FDAR_2 create notification to alert the Patient it's time to do the Check-In
        final NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, CheckInFlowActivity.class),0);

        PendingIntent deleteIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(context, CheckinDismissingReceiver.class),0);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_doctor)
                        .setContentTitle(context.getString(R.string.checkin_reminder_title))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setTicker(context.getString(R.string.txt_checkin_reminder_ticker))
                        .setSound(alarmSound)
                        .setDeleteIntent(deleteIntent)
                        .setContentText(msg);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
