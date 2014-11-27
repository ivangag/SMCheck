package org.symptomcheck.capstone.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//TODO#BPR_3 Broadcast receiver called when Patient dismisses Check-In notification. It re-schedules Check-In in the next minutes
public class CheckinDismissingReceiver extends BroadcastReceiver {
    public CheckinDismissingReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        Log.d("CheckinDismissingReceiver","Re-scheduling of dismissed Check-In...");
        SymptomAlarmRequest.get().setAlarm(context, SymptomAlarmRequest.AlarmRequestedType.ALARM_CHECK_IN_REMINDER,true);
    }
}
