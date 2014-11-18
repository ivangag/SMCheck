/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
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
import android.widget.Toast;

import com.activeandroid.content.ContentProvider;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.SyncUtils;
import org.symptomcheck.capstone.accounts.GenericAccountService;
import org.symptomcheck.capstone.cardsui.CustomExpandCard;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.provider.ActiveContract;

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
public class DoctorFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>
,IFragmentListener {

    DoctorCursorCardAdapter mAdapter;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.fragment_card_doctors_list_cursor, container, false);
        //setupListFragment(root);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                SyncUtils.TriggerRefreshPartialLocal(ActiveContract.SYNC_DOCTORS);
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
        super.onActivityCreated(savedInstanceState);
       //hideList(false);
        init();
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
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    static int count = 0;
    private void init() {

        //Vehicle vehicle = new Vehicle();
        //vehicle.setVIN("VIN" + count);
        //long count = vehicle.save();
        mAdapter = new DoctorCursorCardAdapter(getActivity());

        mListView = (CardListView) getActivity().findViewById(R.id.card_doctors_list_cursor);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Loader<Cursor> loader = null;
        loader = new CursorLoader(getActivity(),
                ContentProvider.createUri(Doctor.class, null),
                null, null, null, null
        );
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }
        mAdapter.swapCursor(data);

        //displayList();
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
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public int getFragmentType() {
        return BaseFragment.FRAGMENT_TYPE_DOCTORS;
    }

    @Override
    public void OnFilterData(String textToSearch) {
        mAdapter.getFilter().filter(textToSearch);
    }

    //-------------------------------------------------------------------------------------------------------------
    // Adapter
    //-------------------------------------------------------------------------------------------------------------
    public class DoctorCursorCardAdapter extends CardCursorAdapter {

        private String mDetailedInfo;

        public DoctorCursorCardAdapter(Context context) {
            super(context);
        }

        @Override
        protected Card getCardFromCursor(Cursor cursor) {
            DoctorCursorCard card = new DoctorCursorCard(super.getContext());
            setCardFromCursor(card,cursor);


            //Create a CardHeader
            CardHeader header = new CardHeader(getActivity());

            //Set visible the expand/collapse button
            //header.setButtonExpandVisible(true);

            //Set the header title
            header.setTitle(card.mainHeader);
            header.setPopupMenu(R.menu.popup_patient, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                @Override
                public void onMenuItemClick(BaseCard card, MenuItem item) {
                    Toast.makeText(getContext(), "Click on card="+card.getId()+" item=" +  item.getTitle(), Toast.LENGTH_SHORT).show();
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

            card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
                @Override
                public void onExpandEnd(Card card) {
                    //Toast.makeText(getContext(), "Card Expanded id=" + card.getId(),Toast.LENGTH_SHORT).show();
                }
            });

            /*
            card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
                @Override
                public void onExpandEnd(Card card) {

                }
            });

            card.setOnExpandAnimatorStartListener(new Card.OnExpandAnimatorStartListener() {
                @Override
                public void onExpandStart(Card card) {
                    Toast.makeText(getContext(), "Card Expanded id=" + card.getId(),Toast.LENGTH_SHORT).show();
                }
            })*/

            //This provides a simple (and useless) expand area
            CustomExpandCard expand = new CustomExpandCard(super.getContext(), mDetailedInfo);
            expand.setTitle("Doctor Details");
            //Add Expand Area to Card
            card.addCardExpand(expand);

            return card;
        }

        private void setCardFromCursor(DoctorCursorCard card,Cursor cursor) {

            card.setId(""+cursor.getInt(ID_COLUMN));
            card.mainTitle = cursor.getString(cursor.getColumnIndex(ActiveContract.DOCTORS_COLUMNS.FIRST_NAME))
                    + " " + cursor.getString(cursor.getColumnIndex(ActiveContract.DOCTORS_COLUMNS.LAST_NAME));
            card.secondaryTitle =
                    cursor.getString(cursor.getColumnIndex(ActiveContract.DOCTORS_COLUMNS.DOCTOR_ID))
                            /*+ " " + cursor.getString(cursor.getColumnIndex(ActiveContract.PATIENT_COLUMNS.BIRTH_DATE))*/
            ;
            card.mainHeader = getString(R.string.doctor_header);
            card.resourceIdThumb=R.drawable.ic_doctor;

            //build detailed info to be shown in expand area
             mDetailedInfo = "";
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
    public class DoctorCursorCard extends Card {

        String mainTitle;
        String secondaryTitle;
        String mainHeader;
        int resourceIdThumb;
        private ImageButton mButtonExpandCustom;

        public DoctorCursorCard(Context context) {
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
