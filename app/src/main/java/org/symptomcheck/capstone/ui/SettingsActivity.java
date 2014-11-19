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
import android.text.TextUtils;
import android.view.View;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.preference.TimePreference;

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
public class SettingsActivity extends Activity {

    public static final String KEY_CHECK_IN_FREQ = "checkin_frequency";
    public static final String KEY_CHECK_IN_START = "checkin_start_time";
    public static final String KEY_SYNC_FREQ = "sync_frequency";
    public static final String KEY_SYNC_ONLY_WIFI = "sync_only_wifi";
    public static final String KEY_NEW_NOTIFICATIONS_ALERT = "notifications_new_message";


    UserInfo mUser;

    /*
    public static void startSettingActivity(Context context){
        Intent intent = new Intent(context,SettingsActivity.class);
        context.startActivity(intent);
    }
    */


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //setupSimplePreferencesScreen();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = DAOManager.get().getUser();

        setContentView(R.layout.activity_settings);

        // Display the fragment as the main content.

        // here we would customize Settings Screen according to User type (PATIENT, DOCTOR, ADMIN)
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if(mUser != null) {
            switch (mUser.getUserType()) {
                case PATIENT:
                    fragmentTransaction
                            .replace(R.id.content_settings_checkin_reminder, new CheckInReminderPreferenceFragment())
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


    /**
     * Shows the simplified activity_settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }




        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        //addPreferencesFromResource(R.xml.pref_general);

        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_notifications);
        //getPreferenceScreen().addPreference(fakeHeader);
        //addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        //getPreferenceScreen().addPreference(fakeHeader);
        //addPreferencesFromResource(R.xml.pref_data_sync);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        //bindPreferenceSummaryToValue(findPreference("example_text"));
        //bindPreferenceSummaryToValue(findPreference("example_list"));
        //bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
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

/*    /*//**
     /*//* {@inheritDoc}
    /*//*
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        //if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        //}
    }*/

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
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

            } else if (preference instanceof TimePreference){
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
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
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
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
            bindPreferenceSummaryToValue(findPreference(KEY_SYNC_FREQ));
            //bindPreferenceSummaryToValue(findPreference(KEY_SYNC_ONLY_WIFI));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane activity_settings UI.
     */
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
            bindPreferenceSummaryToValue(findPreference(KEY_CHECK_IN_FREQ));
            bindPreferenceSummaryToValue(findPreference(KEY_CHECK_IN_START));
        }
    }
}