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
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


import org.symptomcheck.capstone.bus.DownloadEvent;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.network.DownloadHelper;
import org.symptomcheck.capstone.utils.BuildInfo;

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
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public final String TAG = SyncAdapter.this.getClass().getSimpleName();

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        //mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
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

        /*
        if(BuildInfo.get().IsDebug(getContext())) {
            android.os.Debug.waitForDebugger();  // this line is key
        }*/
        Log.i(TAG, "Beginning network synchronization");
        updateLocalData();
        EventBus.getDefault().post(new DownloadEvent.Builder().setStatus(true).setValueEvnt(count).Build());

        Log.i(TAG, "Network synchronization complete");
    }

    void updateLocalData(){
       //final UserInfo user = DownloadHelper.get().getUser();
        final UserInfo user = DAOManager.get().getUser();
        Log.i(TAG, "updateLocalData with: " + user.toString());
       if(user.isLogged()){
           switch (user.getUserType()){
               case DOCTOR:
                   //get and save Patients
                   List<Patient> patients = syncPatients(user);
                   //get and save Patients' Check-Ins
                   syncPatientsCheckIns(user, patients);
                   break;
               case PATIENT:
                   // get and save Check-Ins Data

                   // get and save Medication
                   break;
               case ADMIN:
                   break;
               default:
                   break;
           }
       }
    }

    private List<Patient> syncPatients(UserInfo user) {
        List<Patient> patients = null;

        try {
             patients = (List<Patient>) DownloadHelper.get().withRetrofitClient().findPatientsByDoctor(user.getUserIdentification());
             DAOManager.get().savePatients(patients,user.getUserIdentification());
        }catch (RetrofitError e){
            Log.e(TAG, "Retrofit:" + e.getMessage());
        }catch (Exception e){
            Log.e(TAG,"Error savePatients:" + e.getMessage());
        }
        return patients;
    }

    private boolean syncPatientsCheckIns(UserInfo user, List<Patient> patients) {
        boolean sync = true;

        try {
            if(patients != null){
                for(Patient patient : patients){
                    List<CheckIn> checkIns = (List<CheckIn>) DownloadHelper.get().withRetrofitClient().findCheckInsByPatient(patient.getMedicalRecordNumber());
                    if((checkIns != null)&& (checkIns.size() > 0))
                        DAOManager.get().saveCheckIns(checkIns, patient.getMedicalRecordNumber(),user.getUserIdentification());
                }
            }else {
                Log.e(TAG, "syncPatientsCheckIns=> sync not possible: patients = null!");
            }
        }catch (RetrofitError e){
            Log.e(TAG, "syncPatientsCheckIns=>Retrofit:" + e.getMessage());
            sync = false;
        }catch (Exception e){
            Log.e(TAG,"syncPatientsCheckIns=>Error saveCheckIns:" + e.getMessage());
            sync = false;
        }
        return sync;
    }
}
