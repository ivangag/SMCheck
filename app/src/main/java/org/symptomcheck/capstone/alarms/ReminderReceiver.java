package org.symptomcheck.capstone.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent 
 * and then starts the IntentService {@code SampleSchedulingService} to do some work.
 */
public class ReminderReceiver extends WakefulBroadcastReceiver {

    final private String  TAG = ReminderReceiver.this.getClass().getSimpleName();
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;
  
    @Override
    public void onReceive(Context context, Intent intent) {   
        // BEGIN_INCLUDE(alarm_onreceive)
        /* 
         * If your receiver intent includes extras that need to be passed along to the
         * service, use setComponent() to indicate that the service should handle the
         * receiver's intent. For example:
         * 
         * ComponentName comp = new ComponentName(context.getPackageName(), 
         *      MyService.class.getName());
         *
         * // This intent passed in this call will include the wake lock extra as well as 
         * // the receiver intent contents.
         * startWakefulService(context, (intent.setComponent(comp)));
         * 
         * In this example, we simply create a new intent to deliver to the service.
         * This intent holds an extra identifying the wake lock.
         */
        Intent service = new Intent(context, ReminderSchedulingService.class);
        service.setAction(ReminderSchedulingService.ACTION_CHECK_IN_SUBMISSION);
        //ReminderSchedulingService.startCheckInReminder(context,"","");
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);

        Log.i(TAG,"SymptomAlarmReceiver=>onReceive...");
        //SyncUtils.ForceRefresh();
        // END_INCLUDE(alarm_onreceive)
    }
}
