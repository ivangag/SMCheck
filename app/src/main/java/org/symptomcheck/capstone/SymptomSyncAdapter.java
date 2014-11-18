/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symptomcheck.capstone;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


import com.activeandroid.query.Update;
import com.google.common.collect.Lists;

import org.symptomcheck.capstone.bus.DownloadEvent;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.Question;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.network.DownloadHelper;
import org.symptomcheck.capstone.network.SymptomManagerSvcApi;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.NetworkHelper;
import org.symptomcheck.capstone.preference.UserPreferencesManager;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
class SymptomSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String TAG = SymptomSyncAdapter.this.getClass().getSimpleName();

    private SymptomManagerSvcApi mSymptomClient;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SymptomSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        //mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SymptomSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        //mContentResolver = context.getContentResolver();
    }


    static Integer count = 0;
    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link android.content.AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to perform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        final String accessToken = UserPreferencesManager.get().getBearerToken(getContext());
        final boolean isOnline =  NetworkHelper.isOnline(getContext());
        final boolean isOnlineOverWifi =  NetworkHelper.isOnlineOverWifi(getContext());

        Log.i(TAG, String.format("Beginning network synchronization:%s; isOnline:%b; isOnlineOverWifi:%b",
                extras.toString(),isOnline,isOnlineOverWifi));

        if(!accessToken.isEmpty()) {
            mSymptomClient = DownloadHelper.get().setAccessToken(accessToken).withRetrofitClient(getContext());

            String active_repo_local_to_sync;
            String active_repo_cloud_to_sync;

            boolean forceSync = ((extras.containsKey(ContentResolver.SYNC_EXTRAS_MANUAL)
                                    && extras.getBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED)));

            if (forceSync) {
                active_repo_local_to_sync = ActiveContract.SYNC_ALL;
                active_repo_cloud_to_sync = ActiveContract.SYNC_ALL;
            } else if (extras.isEmpty()){
                // Periodic sync raised by system
                // normally he we would check only if remote data are changed,
                // for instance through a If-Modified-Since head request or similar pattern
                active_repo_local_to_sync = ActiveContract.SYNC_ALL;
                active_repo_cloud_to_sync = ActiveContract.SYNC_ALL;
            }else {
                active_repo_local_to_sync = extras.getString(SyncUtils.SYNC_LOCAL_ACTION_PARTIAL, ActiveContract.SYNC_NONE);
                active_repo_cloud_to_sync = extras.getString(SyncUtils.SYNC_CLOUD_ACTION_PARTIAL, ActiveContract.SYNC_NONE);
            }

            final UserInfo user = DAOManager.get().getUser();
            if (user != null) {
                Log.i(TAG, "sync data with: " + user.toString());
                if (user.getLogged()) {
                    updateCloudData(active_repo_cloud_to_sync, user);
                    updateLocalData(active_repo_local_to_sync, user);
                }
            }

        }
        EventBus.getDefault().post(new DownloadEvent.Builder().setStatus(true).setValueEvnt(count).Build());

        Log.i(TAG, "Network synchronization completed");
    }

    /**
     * Upload local data to remote repository
     * Basically we have to upload new Check-In submitted from the Patient
     * and Patient Medications list updated from the Doctor
     * @param sync sync type to be performed
     */
    private void updateCloudData(String sync, UserInfo user) {
        String method = new Object(){}.getClass().getEnclosingMethod().getName();
        Log.i(TAG, method);
        //if(sync.equals(ActiveContract.SYNC_NONE)) {
        switch (user.getUserType()) {
               // if PATIENT
            // 1) look for Check-In to upload N.B Check-in are "marked" with a needSync field set to true
            case PATIENT:
                // get CheckIn to sync
                List<CheckIn> checkIns = CheckIn.getAllToSync();
                if(checkIns.size() > 0) {
                    Log.i(TAG, method + "::CheckIns to sync: " + checkIns.size());
                    for (CheckIn checkIn : checkIns) {
                        checkIn.setQuestions(Question.getAll(checkIn));
                        try {
                            mSymptomClient.addCheckIn(user.getUserIdentification(), checkIn);
                            new Update(CheckIn.class)
                                    .set("needSync = 0")
                                    .where("_id = ?", checkIn.getId())
                                    .execute();
                            Log.i(TAG, method + "::addCheckIn: " + checkIn.getId());
                        }catch (RetrofitError error){
                            DownloadHelper.get().handleRetrofitError(getContext(),error);
                            Log.e(TAG, method + "::addCheckIn error: " +  error.getMessage() +
                                    "; Status: " + error.getResponse().getStatus());
                        }catch (Exception e){
                            if(e.getCause().getClass().equals(RetrofitError.class)){
                                DownloadHelper.get().handleRetrofitError(getContext(), (RetrofitError) e.getCause());
                            }
                            Log.e(TAG,"Error " + method + e.getMessage());
                        }
                    }
                }
                break;
            // if DOCTOR
            // 2) look for Medication to upload N.B Medication are "marked" with a needSync field set to true
            case DOCTOR:
                break;

        }
        //}
    }

    /**
     * Download data from the cloud in order to sync local repo with the remote one
     * @param sync sync type to be performed
     */
    void updateLocalData(String sync, UserInfo user){
       //final UserInfo user = DAOManager.get().getUser();
       //if(user != null) {
           //Log.i(TAG, "updateLocalData with: " + user.toString());
           //if (user.getLogged()) {
               switch (user.getUserType()) {
                   case DOCTOR:
                       if(sync.equals(ActiveContract.SYNC_ALL)) {
                           // get and save Doctor detail
                           syncDoctorBaseInfo(user);
                           //get and save Doctor' Patients
                           syncDoctorPatients(user);
                           //sync All Patients' information
                           //get and save Patients' Check-Ins
                           syncPatientsCheckIns(user);
                           //get and save Patients' Medicines
                           syncPatientsMedicines(user);
                       }else if(sync.equals(ActiveContract.SYNC_PATIENTS)){
                           syncDoctorPatients(user);
                       }else if(sync.equals(ActiveContract.SYNC_CHECK_IN)){
                           syncDoctorBaseInfo(user);
                           syncPatientsCheckIns(user);
                       }else if(sync.equals(ActiveContract.SYNC_DOCTORS)){
                           syncDoctorBaseInfo(user);
                       }
                       break;
                   case PATIENT:
                       if(sync.equals(ActiveContract.SYNC_ALL)) {
                           // get and save Patient detail
                           syncPatientBaseInfo(user);
                           //sync All Patient' information
                           //get and save Patient' Doctors
                           syncPatientDoctors(user);
                           //get and save Patients' Check-Ins
                           syncPatientsCheckIns(user);
                           //get and save Patients' Medicines
                           syncPatientsMedicines(user);
                       }else if(sync.equals(ActiveContract.SYNC_MEDICINES)) {
                           syncPatientsMedicines(user);
                       }else if(sync.equals(ActiveContract.SYNC_CHECK_IN)) {
                           syncPatientsCheckIns(user);
                       }else if(sync.equals(ActiveContract.SYNC_PATIENTS)) {
                           syncPatientBaseInfo(user);
                       }else if(sync.equals(ActiveContract.SYNC_DOCTORS)) {
                           syncPatientDoctors(user);
                       }
                       break;
                   case ADMIN:
                       break;
                   default:
                       break;
               }
         //  }
       //}
    }

    private List<Doctor> syncPatientDoctors(UserInfo user) {
        String method = new Object(){}.getClass().getEnclosingMethod().getName();
        Log.i(TAG, method);
        List<Doctor> doctors = null;
        try {
            doctors = (List<Doctor>) mSymptomClient.findDoctorsByPatient(user.getUserIdentification());
            DAOManager.get().rebuildDoctors(doctors, user.getUserIdentification());
        }catch (RetrofitError e){
            DownloadHelper.get().handleRetrofitError(getContext(),e);
            Log.e(TAG, "Retrofit:" + e.getMessage() + "; Status: " + e.getResponse().getStatus());
        }catch (Exception e){
            if(e.getCause().getClass().equals(RetrofitError.class)){
                DownloadHelper.get().handleRetrofitError(getContext(), (RetrofitError) e.getCause());
            }
            Log.e(TAG,"Error rebuildDoctors:" + e.getMessage());
        }
        return doctors;
    }


    private void syncPatientBaseInfo(UserInfo user) {
        String method = new Object(){}.getClass().getEnclosingMethod().getName();
        Log.i(TAG, method);
        try {
            Patient patient = mSymptomClient.findPatientByMedicalRecordNumber(user.getUserIdentification());;
            DAOManager.get().rebuildPatients(Lists.newArrayList(patient), user.getUserIdentification());
        }catch (RetrofitError e){
            DownloadHelper.get().handleRetrofitError(getContext(),e);
            Log.e(TAG, "Retrofit:" + e.getMessage() + "; Status: " + e.getResponse().getStatus());
        }catch (Exception e){
            if(e.getCause().getClass().equals(RetrofitError.class)){
                DownloadHelper.get().handleRetrofitError(getContext(), (RetrofitError) e.getCause());
            }
            Log.e(TAG,"Error syncPatientBaseInfo:" + e.getMessage());
        }
    }


    private void syncDoctorBaseInfo(UserInfo user){
        String method = new Object(){}.getClass().getEnclosingMethod().getName();
        Log.i(TAG, method);
        try {
            Doctor doctor = mSymptomClient.findDoctorByUniqueDoctorID(user.getUserIdentification());
            DAOManager.get().rebuildDoctors(Lists.newArrayList(doctor), user.getUserIdentification());
        }catch (RetrofitError e){
            DownloadHelper.get().handleRetrofitError(getContext(),e);
            Log.e(TAG, "Retrofit:" + e.getMessage() + "; Status: " + e.getResponse().getStatus());
        }catch (Exception e){
            if(e.getCause().getClass().equals(RetrofitError.class)){
                DownloadHelper.get().handleRetrofitError(getContext(), (RetrofitError) e.getCause());
            }
            Log.e(TAG,"Error syncDoctorBaseInfo:" + e.getMessage());
        }
    }

    private List<Patient> syncDoctorPatients(UserInfo user) {
        String method = new Object(){}.getClass().getEnclosingMethod().getName();
        Log.i(TAG, method);

        List<Patient> patients = null;

        try {
             patients = (List<Patient>) mSymptomClient.findPatientsByDoctor(user.getUserIdentification());
             DAOManager.get().rebuildPatients(patients, user.getUserIdentification());
        }catch (RetrofitError e){
            DownloadHelper.get().handleRetrofitError(getContext(),e);
            Log.e(TAG, "Retrofit:" + e.getMessage() + "; Status: " + e.getResponse().getStatus());
        }catch (Exception e){
            Log.e(TAG,"Error rebuildPatients:" + e.getMessage());
            if(e.getCause().getClass().equals(RetrofitError.class)){
                DownloadHelper.get().handleRetrofitError(getContext(), (RetrofitError) e.getCause());
            }
        }
        return patients;
    }

    private boolean syncPatientsCheckIns(UserInfo user) {
        String method = new Object(){}.getClass().getEnclosingMethod().getName();
        Log.i(TAG, method);
        boolean sync = true;
        List<Patient> patients = Patient.getAll();
        try {
            if(patients != null){
                DAOManager.get().deleteCheckIns();
                for(Patient patient : patients){
                    List<CheckIn> checkIns = (List<CheckIn>) mSymptomClient.findCheckInsByPatient(patient.getMedicalRecordNumber());
                    if((checkIns != null)&& (checkIns.size() > 0))
                        DAOManager.get().saveCheckIns(checkIns,
                                patient.getMedicalRecordNumber(),
                                user.getUserIdentification(), false);
                }
            }else {
                Log.e(TAG, "syncPatientsCheckIns=> sync not possible: patients = null!");
            }
        }catch (RetrofitError e){
            DownloadHelper.get().handleRetrofitError(getContext(),e);
            Log.e(TAG, "syncPatientsCheckIns=>Retrofit:" + e.getMessage() + "; Status: " + e.getResponse().getStatus());
            sync = false;
        }catch (Exception e){
            Log.e(TAG,"syncPatientsCheckIns=>Error saveCheckIns:" + e.getMessage());
            if(e.getCause().getClass().equals(RetrofitError.class)){
                DownloadHelper.get().handleRetrofitError(getContext(), (RetrofitError) e.getCause());
            }
            sync = false;
        }
        return sync;
    }

    private boolean syncPatientsMedicines(UserInfo user) {
        String method = new Object(){}.getClass().getEnclosingMethod().getName();
        Log.i(TAG, method);

        boolean sync = true;
        List<Patient> patients = Patient.getAll();
        try {
            if(patients != null){
                DAOManager.get().deleteMedicines();
                for(Patient patient : patients){
                    List<PainMedication> medications = (List<PainMedication>) mSymptomClient.findPainMedicationsByPatient(patient.getMedicalRecordNumber());
                    if((medications != null)&& (medications.size() > 0))
                        DAOManager.get().savePainMedications(medications, patient.getMedicalRecordNumber());
                }
            }else {
                Log.e(TAG, "syncPatientsMedicines=> sync not possible: patients = null!");
            }
        }catch (RetrofitError e){
            DownloadHelper.get().handleRetrofitError(getContext(),e);
            Log.e(TAG, "syncPatientsMedicines=>Retrofit:" + e.getMessage() + "; Status: " + e.getResponse().getStatus());
            sync = false;
        }catch (Exception e){
            if(e.getCause().getClass().equals(RetrofitError.class)){
                DownloadHelper.get().handleRetrofitError(getContext(), (RetrofitError) e.getCause());
            }
            Log.e(TAG,"syncPatientsMedicines=>Error saveCheckIns:" + e.getMessage());
            sync = false;
        }
        return sync;
    }

}
