/*
 * Copyright 2013 Google Inc.
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
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.common.collect.Lists;

import org.symptomcheck.capstone.accounts.GenericAccountService;
import org.symptomcheck.capstone.provider.ActiveContract;

import java.util.List;


/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {
    public static final long SYNC_FREQUENCY = 60 * 15;  // 1 hour (in seconds)
    private static final String CONTENT_AUTHORITY = ActiveContract.CONTENT_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    public static final String SYNC_LOCAL_ACTION_PARTIAL = "sync_local_partial";
    public static final String SYNC_CLOUD_ACTION_PARTIAL = "sync_cloud_partial";
    public static final String SYNC_ONLINE_SEARCH_ACTION = "sync_online_search";

    public static final String ONLINE_QUERY_TEXT = "online_query_text";
    public static final String SYNC_ENTITY_ID = "sync_entity_id";
    public static final String SYNC_OWNER_ENTITY_ID = "sync_owner_entity_id";

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void CreateSyncAccount(Context context) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = GenericAccountService.GetAccount();
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(
                    account, CONTENT_AUTHORITY, new Bundle(),SYNC_FREQUENCY);
            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            //ForceRefresh();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void ForceRefresh() {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                GenericAccountService.GetAccount(),      // Sync account
                ActiveContract.CONTENT_AUTHORITY, // Content authority
                b);                                      // Extras
    }

    /**
     * Trigger partial sync by downloading fresh data from the Server and saving them locally
     * @param repoToUpdate constant defines the table / db source to sync
     * @see org.symptomcheck.capstone.provider.ActiveContract
     */
    public static void TriggerRefreshPartialLocal(String repoToUpdate){
        Bundle b = new Bundle();
        b.putString(SYNC_LOCAL_ACTION_PARTIAL,repoToUpdate);
        ContentResolver.requestSync(
                GenericAccountService.GetAccount(),      // Sync account
                ActiveContract.CONTENT_AUTHORITY, // Content authority
                b);
    }

    /**
     * Trigger partial sync in order to upload local pending data to remote Server
     * @param repoToUpdate constant defines the table / db source to sync
     * @see org.symptomcheck.capstone.provider.ActiveContract
     */
    public static void TriggerRefreshPartialCloud(String repoToUpdate){
        Bundle b = new Bundle();
        b.putString(SYNC_CLOUD_ACTION_PARTIAL,repoToUpdate);
        ContentResolver.requestSync(
                GenericAccountService.GetAccount(),      // Sync account
                ActiveContract.CONTENT_AUTHORITY, // Content authority
                b);
    }

    /**
     * Trigger partial sync in order to upload local pending data to remote Server
     * @param repoToUpdate constant defines the table / db source to sync
     * @param patientId
     * @see org.symptomcheck.capstone.provider.ActiveContract
     */
    public static void TriggerRefreshPartialCloud(String repoToUpdate, String entityId, String owner_entity_id){
        Bundle b = new Bundle();
        b.putString(SYNC_OWNER_ENTITY_ID,owner_entity_id);
        b.putString(SYNC_ENTITY_ID,entityId);
        b.putString(SYNC_CLOUD_ACTION_PARTIAL,repoToUpdate);
        ContentResolver.requestSync(
                GenericAccountService.GetAccount(),      // Sync account
                ActiveContract.CONTENT_AUTHORITY, // Content authority
                b);
    }

    /**
     * Trigger online search by populating corresponding local repository used by UI to fetch result
     * @param repoWhereSearch constant defines the table / db source to sync
     * @see org.symptomcheck.capstone.provider.ActiveContract
     */
    public static void TriggerOnlineSearch(String repoWhereSearch, String... querySearch){
        Bundle b = new Bundle();
        b.putString(SYNC_ONLINE_SEARCH_ACTION,repoWhereSearch);
        List<String> queryParam = Lists.newArrayList();
        for (String query: querySearch){

        }
        //b.putStringArray(ONLINE_QUERY_TEXT,data);
        ContentResolver.requestSync(
                GenericAccountService.GetAccount(),      // Sync account
                ActiveContract.CONTENT_AUTHORITY, // Content authority
                b);
    }

}
