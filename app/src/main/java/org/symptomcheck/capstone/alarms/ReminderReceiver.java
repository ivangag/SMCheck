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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;


//TODO#BPR_3 Broadcast Receiver called when the Check-In reminder alarm is fired, then it starts an Intent Service used for raising Notification
public class ReminderReceiver extends WakefulBroadcastReceiver {

    final private String  TAG = ReminderReceiver.this.getClass().getSimpleName();
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;
  
    @Override
    public void onReceive(Context context, Intent intent) {   
        // BEGIN_INCLUDE(alarm_onreceive)
        Intent service = new Intent(context, ReminderSchedulingService.class);
        service.setAction(ReminderSchedulingService.ACTION_CHECK_IN_SUBMISSION);
        //ReminderSchedulingService.startCheckInReminder(context,"","");
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
        Log.i(TAG,"SymptomAlarmReceiver=>onReceive...");
    }
}
