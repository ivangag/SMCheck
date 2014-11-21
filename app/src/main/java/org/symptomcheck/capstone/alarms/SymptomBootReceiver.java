package org.symptomcheck.capstone.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This BroadcastReceiver automatically (re)starts the alarm when the device is
 * rebooted. This receiver is set to be disabled (android:enabled="false") in the
 * application's manifest file. When the user sets the alarm, the receiver is enabled.
 * When the user cancels the alarm, the receiver is disabled, so that rebooting the
 * device will not trigger this receiver.
 */
public class SymptomBootReceiver extends BroadcastReceiver {

    final private String  TAG = SymptomBootReceiver.this.getClass().getSimpleName();

    //ReminderReceiver alarm = new ReminderReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
           SymptomAlarmRequest.get().setAlarm(context, SymptomAlarmRequest.AlarmRequestedType.ALARM_CHECK_IN_REMINDER);
        }
        Log.i(TAG,"SymptomBootReceiver=>onReceive");
    }
}
