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
package org.symptomcheck.capstone.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.alarms.SymptomAlarmRequest;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.preference.TimePreference;
import org.symptomcheck.capstone.preference.UserPreferencesManager;

/**
 * A {@link PreferenceActivity} that presents a set of application activity_settings. On
 * handset devices, activity_settings are presented as a single list. On tablets,
 * activity_settings are split by category, with category headers shown to the left of
 * the list of activity_settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/activity_settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/activity_settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
//TODO#BPR_3 Settings Activity
public class SettingsActivity extends Activity {


    UserInfo mUser;

    public final static int MODIFY_USER_SETTINGS = 1;
    public static boolean mIsSettingsModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = DAOManager.get().getUser();

        setContentView(R.layout.activity_settings);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        // Display the fragment as the main content.

        // here we would customize Settings Screen according to User type (PATIENT, DOCTOR, ADMIN)
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if(mUser != null) {
            //TODO#BPR_1
            switch (mUser.getUserType()) {
                case PATIENT:
                    fragmentTransaction
                            .replace(R.id.content_settings_checkin_reminder, new CheckInReminderPreferenceFragment()) //TODO#FDAR_2
                            .replace(R.id.content_settings_data_sync, new DataSyncPreferenceFragment())
                            .replace(R.id.content_settings_notification, new NotificationPreferenceFragment());
                    break;
                case DOCTOR:
                    fragmentTransaction
                            .replace(R.id.content_settings_data_sync, new DataSyncPreferenceFragment())
                            .replace(R.id.content_settings_notification, new NotificationPreferenceFragment());
                    break;
                case ADMIN:
                    fragmentTransaction
                            .replace(R.id.content_settings_checkin_reminder, new CheckInReminderPreferenceFragment())
                            .replace(R.id.content_settings_data_sync, new DataSyncPreferenceFragment())
                            .replace(R.id.content_settings_notification, new NotificationPreferenceFragment());
                    break;
                case UNKNOWN:
                    fragmentTransaction
                            .replace(R.id.content_settings_notification, new NotificationPreferenceFragment());
                    break;
            }
        }else{
            fragmentTransaction
                    .replace(R.id.content_settings_notification, new NotificationPreferenceFragment());
        }
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    //@Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified activity_settings UI should be shown. This is
     * true if this is forced via {@link #}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" activity_settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    @Override
    public void onBackPressed() {
        this.setResult(mIsSettingsModified ? RESULT_OK : RESULT_OK);
        super.onBackPressed();
    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                mIsSettingsModified = true;
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else if (preference instanceof TimePreference) {
                mIsSettingsModified = true;

                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            if(mIsSettingsModified){
                SymptomAlarmRequest.get().setAlarm(preference.getContext(), SymptomAlarmRequest.AlarmRequestedType.ALARM_CHECK_IN_REMINDER,false);
            }
            final Preference nextScheduleCheckInPreference = preference.getPreferenceManager().findPreference(UserPreferencesManager.KEY_NEXT_SCHEDULED_CHECKIN);
            if (nextScheduleCheckInPreference != null) {
                nextScheduleCheckInPreference.setSummary(UserPreferencesManager.get().getNextScheduledCheckin(preference.getContext()));
            }
            return true;

        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        Object value;
        if(preference.getKey().equals(UserPreferencesManager.KEY_SYNC_ONLY_WIFI)){
            value = PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getBoolean(preference.getKey(), false);
        }else {
            value = PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), "");
        }
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,value);
    }


    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane activity_settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane activity_settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);


            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(UserPreferencesManager.KEY_SYNC_FREQ));
            bindPreferenceSummaryToValue(findPreference(UserPreferencesManager.KEY_SYNC_ONLY_WIFI));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane activity_settings UI.
     */
    //TODO#FDAR_2 Fragment used to show and set Check-In Alert Reminder
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CheckInReminderPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);


            addPreferencesFromResource(R.xml.pref_check_in);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(UserPreferencesManager.KEY_CHECK_IN_FREQ));
            bindPreferenceSummaryToValue(findPreference(UserPreferencesManager.KEY_CHECK_IN_START));
            bindPreferenceSummaryToValue(findPreference(UserPreferencesManager.KEY_NEXT_SCHEDULED_CHECKIN));
        }
    }
}
