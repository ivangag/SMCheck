package org.symptomcheck.capstone.gcm;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.common.collect.Lists;

import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.network.DownloadHelper;
import org.symptomcheck.capstone.utils.BuildInfo;
import org.symptomcheck.capstone.utils.UserPreferencesManager;

import java.io.IOException;
import java.util.List;

import retrofit.RetrofitError;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GcmRegistrationService extends IntentService {

    private static final String TAG = "GcmRegistrationService";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GCM_DEVICE_REGISTRATION = "org.symptomcheck.capstone.gcm.action.REGISTRATION";
    private static final String ACTION_GCM_DEVICE_UNREGISTRATION = "org.symptomcheck.capstone.gcm.action.UNREGISTRATION";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "org.symptomcheck.capstone.gcm.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "org.symptomcheck.capstone.gcm.extra.PARAM2";

    GoogleCloudMessaging gcm;
    private String regid;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     * Project ID: spring-mvc-capstone-test Project Number: 412689184727
     */
    final String SENDER_CAPSTONE_ID = "412689184727";

    /**
     * Starts this service to handle Gcm Device Registration and will perform it if necessary. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startDeviceRegistration(Context context/*, String param1, String param2*/) {
        Intent intent = new Intent(context, GcmRegistrationService.class);
        intent.setAction(ACTION_GCM_DEVICE_REGISTRATION);
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public GcmRegistrationService() {
        super("GcmRegistrationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GCM_DEVICE_REGISTRATION.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleDeviceRegistration(param1, param2);
            } else if (ACTION_GCM_DEVICE_UNREGISTRATION.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleDeviceUnRegistration(param1, param2);
            }
        }
    }

    private void handleDeviceUnRegistration(String param1, String param2) {
        unregistration();
    }

    /**
     * Handle action Gcm Device Registration in the provided background thread with the provided
     * parameters.
     */
    private void handleDeviceRegistration(String param1, String param2) {
        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
        regid = getRegistrationId(getApplicationContext());
        final boolean hasUserGcmId = this.getRemoteGCMIds().contains(regid);
        if (regid.isEmpty()
                || !hasUserGcmId) {
                /*registerInBackground();*/
            registerInBackground();
        }

    }
    private void registerInBackground() {
        String msg;
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            }

            regid = gcm.register(SENDER_CAPSTONE_ID);
            msg = "Device registered, registration ID=" + regid;


            // You should send the registration ID to your server over HTTP, so it
            // can use GCM/HTTP or CCS to send messages to your app.
            try {
                DownloadHelper.get().withRetrofitClient(getApplicationContext()).sendGCMRegistrationId(regid);
                // Persist the regID - no need to register again.
                UserPreferencesManager.get().setGcmRegId(getApplicationContext(), regid);
                UserPreferencesManager.get().setAppVers(getApplicationContext(),
                        BuildInfo.get().getAppVersion(getApplicationContext()));
                Log.i(TAG,msg);
            }catch (Exception exc){
                Log.e(TAG,"registerInBackground error: " + exc.getMessage());
            }

        } catch (IOException ex) {
            msg = "registerInBackground Error :" + ex.getMessage();
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
            /*
            Intent intent = new Intent(getApplicationContext(), GcmRegistrationService.class);
            PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
            try {
                pi.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }*/
            Log.e(TAG,msg);
        }

    }

    private void unregistration(){
        boolean res = true;
        try {
            String regId = UserPreferencesManager.get().getGcmRegId(getApplicationContext());
            DownloadHelper.get().withRetrofitClient(getApplicationContext()).clearGCMRegistration(regId);
        }catch (RetrofitError error){
            Log.e(TAG,"Gcm unregistration:" + error.getMessage());
            res = false;
        }finally {
            if(res){
                UserPreferencesManager.get().setGcmRegId(getApplicationContext(), "");
            }
        }
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        //final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = UserPreferencesManager.get().getGcmRegId(this);
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = UserPreferencesManager.get().getAppVers(this);
        int currentVersion = BuildInfo.get().getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    public List<String> getRemoteGCMIds(){
        //Retrieve gcm reg_ids for the current user
        final UserInfo user = DAOManager.get().getUser();
        List<String> gcmIds = Lists.newArrayList();
        try {
            if (user != null) {
                if (user.getUserType().equals(UserType.PATIENT)) {
                    Patient patient = DownloadHelper.get().withRetrofitClient(getApplicationContext()).findPatientByMedicalRecordNumber(user.getUserIdentification());
                    if (patient != null)
                        gcmIds = patient.getGcmRegistrationIds();
                } else if (user.getUserType().equals(UserType.DOCTOR)) {
                    Doctor doctor = DownloadHelper.get().withRetrofitClient(getApplicationContext()).findDoctorByUniqueDoctorID(user.getUserIdentification());
                    if (doctor != null)
                        gcmIds = doctor.getGcmRegistrationIds();
                }
                for (int i = 0; i < gcmIds.size(); i++) {
                    gcmIds.set(i, gcmIds.get(i).replace("\"", ""));
                }
            }
        }catch (RetrofitError error){
            Log.e(TAG, "RetrofitError=>getRemoteGCMIds: " + error.getMessage());
        }catch (Exception exc){
            Log.e(TAG, "Exception=>getRemoteGCMIds: " + exc.getMessage());
        }
        return gcmIds;
    }

}
