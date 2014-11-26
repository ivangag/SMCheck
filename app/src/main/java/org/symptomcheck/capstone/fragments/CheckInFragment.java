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
import android.content.Loader;
import android.content.SyncStatusObserver;
import android.database.Cursor;
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

import com.activeandroid.content.ContentProvider;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.SyncUtils;
import org.symptomcheck.capstone.accounts.GenericAccountService;
import org.symptomcheck.capstone.cardsui.CustomExpandCard;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.Constants;
import org.symptomcheck.capstone.utils.DateTimeUtils;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardCursorAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * List with Cursor Example
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class CheckInFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, IFragmentListener {

    CheckinCursorCardAdapter mAdapter;
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

    private static final String ARG_PATIENT_ID = "patient_id";
    String mMedicineName;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     * @param patientId
     */
    public static CheckInFragment newInstance(String patientId) {
        CheckInFragment fragment = new CheckInFragment();
        Bundle args = new Bundle();
        //if (patientId != -1) {
            //args.putLong(ARG_PATIENT_ID, patientId);
            args.putString(ARG_PATIENT_ID, patientId);
        //}
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.fragment_card_checkins_list_cursor, container, false);
        setupListFragment(root);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                SyncUtils.TriggerRefreshPartialLocal(ActiveContract.SYNC_CHECK_IN);
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
        setIconActionBar();
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
        if(mPatientOwner != null){
            title = //mPatientOwner.getFirstName() + " " +
                    mPatientOwner.getLastName() + "'s " + getString(R.string.checkins_header);
        }
        return title;
    }

    @Override
    public String getIdentityOwnerId() {
        return getArguments().getString(ARG_PATIENT_ID, Constants.STRINGS.EMPTY);
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

    String mSelectionQuery = null;
    Patient mPatientOwner = null;
    private void init() {

        final String patientMedicalNumber = getArguments().getString(ARG_PATIENT_ID, Constants.STRINGS.EMPTY);
        if(!patientMedicalNumber.isEmpty()) {
            mPatientOwner = Patient.getByMedicalNumber(patientMedicalNumber);
        }
        if (mPatientOwner != null) {
            mSelectionQuery = ActiveContract.CHECKIN_COLUMNS.PATIENT + " = " + mPatientOwner.getId();
        }

        mAdapter = new CheckinCursorCardAdapter(getActivity());
        mListView = (CardListView) getActivity().findViewById(R.id.card_checkins_list_cursor);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }

        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return queryAllField(charSequence.toString(), mSelectionQuery);
            }
        });
        // Force start background query to load sessions
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(),
                ContentProvider.createUri(CheckIn.class, null),
                ActiveContract.CHECK_IN_TABLE_PROJECTION, mSelectionQuery, null,
                ActiveContract.CHECKIN_COLUMNS.ISSUE_TIME + " asc"
        );
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
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
    /**
     * Create a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
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
                public void run() {
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

    public static final int ID_COLUMN = 0;


    @Override
    public int getFragmentType() {
        return BaseFragment.FRAGMENT_TYPE_CHECKIN;
    }

    @Override
    public void OnFilterData(String textToSearch) {
        if(mAdapter != null)
            mAdapter.getFilter().filter(textToSearch);
    }

    @Override
    public void OnSearchOnLine(String textToSearch) {

    }

    //-------------------------------------------------------------------------------------------------------------
    // Adapter
    //-------------------------------------------------------------------------------------------------------------
    public class CheckinCursorCardAdapter extends CardCursorAdapter {



        public CheckinCursorCardAdapter(Context context) {
            super(context);
        }

        @Override
        protected Card getCardFromCursor(Cursor cursor) {
            final CheckIn checkIn = CheckIn.getByUnitId(cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.UNIT_ID)));

            CheckinCursorCard card = new CheckinCursorCard(super.getContext());
            setCardFromCursor(card,cursor,checkIn);

            //Create a CardHeader
            CardHeader header = new CardHeader(getActivity());

            //Set visible the expand/collapse button
            //header.setButtonExpandVisible(true);

            //Set the header title
            header.setTitle(card.mainHeader);
            /*header.setPopupMenu(R.menu.popup_checkin, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                @Override
                public void onMenuItemClick(BaseCard card, MenuItem item) {
                }
            });*/
            //Add Header to card
            card.addCardHeader(header);


            //Add the thumbnail
            CardThumbnail thumb = new CardThumbnail(getActivity());
            thumb.setDrawableResource(card.resourceIdThumb);
            card.addCardThumbnail(thumb);

            String mDetailedCheckInInfo = "";
            if(checkIn != null) {
                mDetailedCheckInInfo = CheckIn.getDetailedInfo(checkIn,true);
            }
            // Add expand card
            CustomExpandCard expand = new CustomExpandCard(super.getContext(),mDetailedCheckInInfo);
            card.addCardExpand(expand);

            return card;
        }

        private void setCardFromCursor(CheckinCursorCard card, Cursor cursor, CheckIn checkIn) {
            final int checkInId = cursor.getInt(ID_COLUMN);
            card.setId(""+ checkInId);
            card.mainTitle = cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.PAIN_LEVEL))
                        + " - " + cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.FEED_STATUS))
                            ;
            if(checkIn != null) {
                card.secondaryTitle = "Submitted on " + DateTimeUtils.convertEpochToHumanTime(checkIn.getIssueDateTime(), Constants.TIME.DEFAULT_FORMAT);
                final Patient patient = Patient.getByMedicalNumber(checkIn.getPatientMedicalNumber());
                card.mainHeader = patient.getFirstName() + " " + patient.getLastName() + " " + getString(R.string.checkin_header);
            }
            card.resourceIdThumb=R.drawable.ic_check_in;

            //retrieve image
            //byte[] byteArray = Base64.decode("",Base64.DEFAULT);
            //Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            //ImageView image = (ImageView) getActivity().findViewById(R.id.imageChartApi);
            //image.setImageBitmap(bmp);
            //Picasso.with(getActivity()).load(R.id.imageChartApi).resize(150,150).get();
            //build detailed info to be shown in expand area
            // retrieve questions from checkin

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

        //mAdapter.notifyDataSetChanged();*/

    }

    //-------------------------------------------------------------------------------------------------------------
    // Cards
    //-------------------------------------------------------------------------------------------------------------
    public class CheckinCursorCard extends Card {

        String mainTitle;
        String secondaryTitle;
        String mainHeader;
        int resourceIdThumb;
        private ImageButton mButtonExpandCustom;

        public CheckinCursorCard(Context context) {
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
                        ViewToClickToExpand.builder()
                                .highlightView(false)
                                .setupView(mButtonExpandCustom);

                setViewToClickToExpand(extraCustomButtonExpand);
            }
        }
    }
}
