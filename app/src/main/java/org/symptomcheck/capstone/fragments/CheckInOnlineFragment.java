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
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;

import org.symptomcheck.capstone.App;
import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.accounts.GenericAccountService;
import org.symptomcheck.capstone.cardsui.CustomExpandCard;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.CheckInOnlineWrapper;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.network.DownloadHelper;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.Constants;
import org.symptomcheck.capstone.utils.DateTimeUtils;

import java.util.Collection;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardCursorAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardListView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * List with Cursor Example
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class CheckInOnlineFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, IFragmentListener {

    private static final String TAG = "CheckInOnlineFragment";
    private Handler progressBarHandler = new Handler();
    CheckinCursorCardAdapter mAdapter;
    CardListView mListView;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CheckInOnlineFragment newInstance() {
        return new CheckInOnlineFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.fragment_card_checkins_online_list_cursor, container, false);
        setupListFragment(root);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                //SyncUtils.TriggerRefreshPartialLocal(ActiveContract.SYNC_CHECK_IN);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setIconActionBar();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        Log.d(TAG,"onActivityCreated");
        hideList(false);
        displayList(CheckInOnlineWrapper.getAll().isEmpty());

    }

    @Override
    public String getTitleText() {
        return getString(R.string.checkins_online_header);
    }

    @Override
    public String getIdentityOwnerId() {
        return Constants.STRINGS.EMPTY;
    }

    String mSelectionQuery = Constants.STRINGS.EMPTY;
    private void init() {

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
                ContentProvider.createUri(CheckInOnlineWrapper.class, null),
                ActiveContract.CHECK_ONLINE_IN_TABLE_PROJECTION, mSelectionQuery, null,
                ActiveContract.CHECKIN_COLUMNS.ISSUE_TIME + " desc"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }
        Log.d(TAG,"onLoadFinished. Count: " + data.getCount());
        mAdapter.swapCursor(data);
        displayList(data.getCount() <= 0);
        OnFilterData(Constants.STRINGS.EMPTY);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        /*
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }*/
    }

    public static final int ID_COLUMN = 0;


    @Override
    public int getFragmentType() {
        return BaseFragment.FRAGMENT_TYPE_ONLINE_CHECKIN;
    }

    @Override
    public void OnFilterData(String textToSearch) {
        if(mAdapter != null) {
            mAdapter.getFilter().filter(textToSearch);
        }
    }

    @Override
    public void OnSearchOnLine(String textToSearch) {
        // here we have to trigger background sync service by stimulating a Server Hosted search
        if(mAdapter != null) {
            if(!textToSearch.isEmpty()){
                final String[] names = textToSearch.split(" ");
                final StringBuilder lastName = new StringBuilder();
                if(names.length > 1) {
                    final String firstName = names[0];
                    //e.g. La Rosa => length = 3
                    for (int i = 1; i < names.length; i++) {
                        lastName.append(names[i]);
                        if (i < names.length - 1) {
                            lastName.append(" ");
                        }
                    }
                    Log.d(TAG,"StartSearching");
                    //hideList(true);
                    //performOnlineSearch(firstName, lastName.toString());
                    performOnlineSearchWithProgress(firstName,lastName.toString());
                }
            }else {
                mAdapter.getFilter().filter(textToSearch);
            }
        }
    }

    private void performOnlineSearch(String FirstName, String LastName){
        DownloadHelper.get()
                .withRetrofitClient(getActivity())
                .findCheckInsByPatientName(DAOManager.get().getUser().getUserIdentification(),FirstName,LastName, new Callback<Collection<CheckIn>>() {
                    @Override
                    public void success(Collection<CheckIn> checkIns, Response response) {
                        Log.d(TAG, "success. CheckinsFound:" + checkIns.size());
                        DAOManager.get().saveCheckInsOnline((List<CheckIn>) checkIns, Constants.STRINGS.EMPTY, Constants.STRINGS.EMPTY);
                        Log.d(TAG, "CheckinsOnlineSaved:" + CheckInOnlineWrapper.getAll().size());
                        Log.d(TAG,"EndSearching");
                        displayList(checkIns.isEmpty());
                        OnFilterData(Constants.STRINGS.EMPTY);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d(TAG, error.getCause().getMessage());
                    }
                });
    }

    private void performOnlineSearchWithProgress(final String FirstName, final String LastName) {
        final ProgressDialog ringProgressDialog = ProgressDialog.show(getActivity(), FirstName + " " + LastName + " Checkin-Data",
                getActivity().getResources().getString(R.string.txt_search_online_running), true);
        ringProgressDialog.setCancelable(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<CheckIn> checkIns = (List<CheckIn>) DownloadHelper.get().withRetrofitClient(getActivity())
                            .findCheckInsByPatientName(DAOManager.get().getUser().getUserIdentification(), FirstName, LastName);
                    final boolean checkinRes = checkIns.size() > 0;
                    Log.d(TAG, "CheckinsFound:" + checkIns.size());
                    Thread.sleep(1000);
                    progressBarHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (checkinRes) {
                                hideList(true);
                                DAOManager.get().saveCheckInsOnline(checkIns, Constants.STRINGS.EMPTY, Constants.STRINGS.EMPTY);
                                displayList(checkIns.isEmpty());
                                OnFilterData(Constants.STRINGS.EMPTY);
                                Log.d(TAG,"EndSearching");
                            } else {
                                Log.d(TAG, "ErrorOnFindCheckInsByPatientName");
                            }
                        }
                    });
                } catch (Exception error) {
                    Log.d(TAG, "FindCheckInsByPatientName Error: " + error.getCause().getMessage());
                }
                ringProgressDialog.dismiss();
            }
        }).start();
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
            final CheckInOnlineWrapper checkIn = CheckInOnlineWrapper.getByUnitId(
                    cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.UNIT_ID)));

            CheckinCursorCard card = new CheckinCursorCard(super.getContext());
            setCardFromCursor(card,cursor,checkIn);

            //Create a CardHeader
            CardHeader header = new CardHeader(getActivity());
            //Set the header title
            header.setTitle(card.mainHeader);
            //Add Header to card
            card.addCardHeader(header);


            //Add the thumbnail
            CardThumbnail thumb = new CardThumbnail(getActivity());
            thumb.setDrawableResource(card.resourceIdThumb);
            card.addCardThumbnail(thumb);

            String detailedCheckInInfo = Constants.STRINGS.EMPTY;
            if(checkIn != null) {
                card.secondaryTitle = "Submitted on " + DateTimeUtils.convertEpochToHumanTime(checkIn.getIssueDateTime(), Constants.TIME.DEFAULT_FORMAT);
                detailedCheckInInfo = CheckInOnlineWrapper.getDetailedInfo(checkIn);
            }
            // Add expand card
            CustomExpandCard expand = new CustomExpandCard(super.getContext(),detailedCheckInInfo);
            card.addCardExpand(expand);

            return card;
        }

        private void setCardFromCursor(CheckinCursorCard card, Cursor cursor, CheckInOnlineWrapper checkIn) {
            final int checkInId = cursor.getInt(ID_COLUMN);
            card.setId(""+ checkInId);
            card.mainTitle = cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.PAIN_LEVEL))
                        + " - " + cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.FEED_STATUS))
                            ;
            final Patient patient = Patient.getByMedicalNumber(checkIn.getPatientMedicalNumber());
            card.mainHeader = patient.getFirstName() + " "  + patient.getLastName() + " " +  getString(R.string.checkin_header);
            card.resourceIdThumb=R.drawable.ic_check_in;

        }
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
