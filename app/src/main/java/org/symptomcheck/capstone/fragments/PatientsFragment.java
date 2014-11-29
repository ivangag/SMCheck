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

package org.symptomcheck.capstone.fragments;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.content.ContentProvider;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.SyncUtils;
import org.symptomcheck.capstone.accounts.GenericAccountService;
import org.symptomcheck.capstone.cardsui.CustomExpandCard;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.Constants;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardCursorAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;

//TODO#BPR_6 Patients Fragment Interface Screen
public class PatientsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>
    ,IFragmentListener {


    private Doctor mDoctorOwner = null;
    private Uri mUriContentProvider;
    private static String ARG_DOCTOR_ID = "doctor_id";
    PatientCursorCardAdapter mAdapter;
    CardListView mListView;
    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     *
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object mSyncObserverHandle;
    private Menu mOptionsMenu;


    public static PatientsFragment newInstance(long doctorId) {
        PatientsFragment fragment = new PatientsFragment();
        Bundle args = new Bundle();
        if (doctorId != -1) {
            args.putLong(ARG_DOCTOR_ID, doctorId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.fragment_card_patients_list_cursor, container, false);
        setupListFragment(root);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                SyncUtils.TriggerRefreshPartialLocal(ActiveContract.SYNC_PATIENTS);
                //SyncUtils.ForceRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        //inflater.inflate(R.menu.cards, menu);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //SyncUtils.CreateSyncAccount(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        init();
        super.onActivityCreated(savedInstanceState);
        hideList(false);
    }

    @Override
    public String getTitleText() {
        String title = TITLE_NONE;
        if(mDoctorOwner != null){
            title = //mDoctorOwner.getFirstName() + " " +
                    mDoctorOwner.getLastName() + "'s " + getString(R.string.patients_header);
        }
        return title;
    }

    @Override
    public String getIdentityOwnerId() {
        return Constants.STRINGS.EMPTY;
    }


    /**
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occuring, false otherwise
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setRefreshActionButtonState(boolean refreshing) {
        if (mOptionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                hideList(true);
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                displayList(false);
                OnFilterData(Constants.STRINGS.EMPTY);
                refreshItem.setActionView(null);
            }
        }
    }


    private void init() {
        mUriContentProvider=  ContentProvider.createUri(Patient.class, null);
        final  UserInfo user = DAOManager.get().getUser();

        //TODO#BPR_1
        if(user.getUserType().equals(UserType.DOCTOR)){
            mDoctorOwner = Doctor.getByDoctorNumber(user.getUserIdentification());
        }

        mAdapter = new PatientCursorCardAdapter(getActivity());
        mListView = (CardListView) getActivity().findViewById(R.id.card_patients_list_cursor);
        //mListView.setEmptyView(getActivity().findViewById(android.R.id.empty));
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }

        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return queryAllField(charSequence.toString(),null);
            }
        });
        // Force start background query to load sessions
        getLoaderManager().restartLoader(0, null, this);
    }

    //TODO#BPR_3 Create Cursor over ContentProvider
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Loader<Cursor> loader = null;
        loader = new CursorLoader(getActivity(),
                mUriContentProvider,
                null, null, null, ActiveContract.PATIENT_COLUMNS.FIRST_NAME + " asc"
        );
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }
        mAdapter.swapCursor(data);

        displayList(data.getCount() <= 0);
        OnFilterData(Constants.STRINGS.EMPTY);
    }
    /**
     * Crfate a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() { //TODO#BPR_8
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = GenericAccountService.GetAccount();
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setRefreshActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, ActiveContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, ActiveContract.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    public void onResume() {
        super.onResume();

        mSyncStatusObserver.onStatusChanged(0);
        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public int getFragmentType() {
        return BaseFragment.FRAGMENT_TYPE_PATIENT;
    }

    @Override
    public void OnFilterData(String textToSearch) {
        if(mAdapter != null)
            mAdapter.getFilter().filter(textToSearch);
    }

    @Override
    public void OnSearchOnLine(String textToSearch) {

    }

    public static final int ID_COLUMN = 0;
    //-------------------------------------------------------------------------------------------------------------
    // Adapter
    //-------------------------------------------------------------------------------------------------------------
    public class PatientCursorCardAdapter extends CardCursorAdapter {




        public PatientCursorCardAdapter(Context context) {
            super(context);
        }

        @Override
        protected Card getCardFromCursor(Cursor cursor) {

            final Patient patient = Patient.getByMedicalNumber(cursor.getString(cursor.getColumnIndex(ActiveContract.PATIENT_COLUMNS.PATIENT_ID)));
            PatientCursorCard card = new PatientCursorCard(super.getContext());
            setCardFromCursor(card,cursor);

            //Create a CardHeader
            CardHeader header = new CardHeader(getActivity());

            //Set the header title
            header.setTitle(card.mainHeader);
            header.setPopupMenu(R.menu.popup_patient, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                @Override
                public void onMenuItemClick(BaseCard card, MenuItem item) {
                    int id = item.getItemId();
                    final String ownerId = patient.getMedicalRecordNumber();
                    final String phoneNumber = patient.getPhoneNumber();
                    final String email = patient.getEmail();
                    Activity activity = getActivity();
                    //Long cardId = Long.valueOf(card.getId());
                    if(id == R.id.menu_pop_open_check_ins){ //TODO#FDAR_10 Doctor clicked the menu voice for monitoring Check-Ins data
                        if((activity != null)
                            && (activity instanceof ICardEventListener)){
                            ((ICardEventListener)(activity)).OnCheckInOpenRequired(ownerId);
                        }
                    }else if(id == R.id.menu_pop_open_medicines){
                        if((activity != null)
                                && (activity instanceof ICardEventListener)){
                            ((ICardEventListener)(activity)).OnMedicinesOpenRequired(ownerId);
                        }
                    }else if(id == R.id.menu_pop_call_patient){
                        if(phoneNumber != null) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            //callIntent.setData(Uri.parse("tel:123456789"));
                            callIntent.setData(Uri.parse("tel:" + phoneNumber));
                            startActivity(callIntent);
                        }
                    } else if(id == R.id.menu_pop_send_email_patient){
                        if(email != null) {
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("message/rfc822");
                            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"email"});
                            i.putExtra(Intent.EXTRA_SUBJECT, "Your Doctor wishes to hear more from you");
                            i.putExtra(Intent.EXTRA_TEXT   , "Here write your body message");
                            try {
                                startActivity(Intent.createChooser(i, "Send mail..."));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                }
            });

            //Add Header to card
            card.addCardHeader(header);


            //Add the thumbnail
            CardThumbnail thumb = new CardThumbnail(getActivity());
            thumb.setDrawableResource(card.resourceIdThumb);
            card.addCardThumbnail(thumb);


            //Simple clickListener
            card.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    //Toast.makeText(getContext(), "Card id=" + card.getId() + " Title=" + card.getCardHeader().getTitle(), Toast.LENGTH_SHORT).show();
                }
            });


            //This provides a simple (and useless) expand area
            String detailedInfo = "";
            if(patient != null) {
                detailedInfo = Patient.getDetailedInfo(patient);
            }
            CustomExpandCard expand = new CustomExpandCard(super.getContext(), detailedInfo);
            //expand.setTitle("Patient Details");
            //Add Expand Area to Card
            card.addCardExpand(expand);

            return card;
        }

        private void setCardFromCursor(PatientCursorCard card,Cursor cursor) {

            card.setId(""+cursor.getInt(ID_COLUMN));
            card.mainTitle = cursor.getString(cursor.getColumnIndex(ActiveContract.PATIENT_COLUMNS.FIRST_NAME))
                    + " " + cursor.getString(cursor.getColumnIndex(ActiveContract.PATIENT_COLUMNS.LAST_NAME));
            card.secondaryTitle = getString(R.string.medical_number) + ": "
                     + cursor.getString(cursor.getColumnIndex(ActiveContract.PATIENT_COLUMNS.PATIENT_ID))
            //                + " " + cursor.getString(cursor.getColumnIndex(ActiveContract.PATIENT_COLUMNS.BIRTH_DATE))
            ;
            card.mainHeader = getString(R.string.patient_header);
            card.resourceIdThumb=R.drawable.ic_patient_small;
        }
    }

    private void removeCard(Card card) {

        //Use this code to delete getItemsQuestion on DB
        /*
        ContentResolver resolver = getActivity().getContentResolver();
        long noDeleted = resolver.delete
                (CardCursorContract.CardCursor.CONTENT_URI,
                        CardCursorContract.CardCursor.KeyColumns.KEY_ID + " = ? ",
        new String[]{card.getId()});

        mAdapter.notifyDataSetChanged();
*/
    }

    //-------------------------------------------------------------------------------------------------------------
    // Cards
    //-------------------------------------------------------------------------------------------------------------
    public class PatientCursorCard extends Card {

        String mainTitle;
        String secondaryTitle;
        String mainHeader;
        int resourceIdThumb;
        private ImageButton mButtonExpandCustom;

        public PatientCursorCard(Context context) {
            super(context, R.layout.carddemo_cursor_inner_content);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            //Retrieve elements
            TextView mTitleTextView = (TextView) parent.findViewById(R.id.carddemo_cursor_main_inner_title);
            TextView mSecondaryTitleTextView = (TextView) parent.findViewById(R.id.carddemo_cursor_main_inner_subtitle);
            mButtonExpandCustom = (ImageButton)parent.findViewById(R.id.card_rds_expand_button_info);

            if (mTitleTextView != null)
                mTitleTextView.setText(mainTitle);

            if (mSecondaryTitleTextView != null)
                mSecondaryTitleTextView.setText(secondaryTitle);


            if(mButtonExpandCustom != null) {
                mButtonExpandCustom.setBackgroundResource(R.drawable.card_menu_button_expand);

                mButtonExpandCustom.setClickable(true);

                ViewToClickToExpand extraCustomButtonExpand =
                        ViewToClickToExpand.builder().highlightView(false)
                                .setupView(mButtonExpandCustom);

                setViewToClickToExpand(extraCustomButtonExpand);
            }
        }
    }
}
