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

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ReminderSchedulingService extends IntentService {

    private final String TAG = ReminderSchedulingService.this.getClass().getSimpleName();
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_CHECK_IN_SUBMISSION = "org.symptomcheck.capstone.alarms.action.CHECK_IN_SUBMISSION";
    private static final String ACTION_BAZ = "org.symptomcheck.capstone.alarms.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "org.symptomcheck.capstone.alarms.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "org.symptomcheck.capstone.alarms.extra.PARAM2";
    private NotificationManager mNotificationManager;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startCheckInReminder(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ReminderSchedulingService.class);
        intent.setAction(ACTION_CHECK_IN_SUBMISSION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ReminderSchedulingService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public ReminderSchedulingService() {
        super("ReminderSchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_IN_SUBMISSION.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionCheckInSubmission(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheckInSubmission(String param1, String param2) {
        // TODO: Handle action
        sendNotification(getString(R.string.checkin_reminder_text));
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // Post a notification indicating whether a doodle was found.

    private void sendNotification(String msg) {
        Log.i(TAG,"sendNotification");
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, CheckInFlowActivity.class),0);
        PendingIntent deleteIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, LoginActivity.class),0);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_doctor)
                        .setContentTitle(getString(R.string.checkin_reminder_title))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setSound(alarmSound)
                        .setDeleteIntent(deleteIntent)
                        .setContentText(msg);



        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
