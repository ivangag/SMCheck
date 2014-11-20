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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.provider.ActiveContract;

import java.util.List;

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
public class MedicinesFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, IFragmentListener {

    MedicinesCursorCardAdapter mAdapter;
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
    private Patient mPatientOwner;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MedicinesFragment newInstance(long patientId) {
        MedicinesFragment fragment = new MedicinesFragment();
        Bundle args = new Bundle();
        if (patientId > 0) {
            args.putLong(ARG_PATIENT_ID, patientId);
        }
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
        View root= inflater.inflate(R.layout.fragment_card_medicines_list_cursor, container, false);
        setupListFragment(root);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                SyncUtils.TriggerRefreshPartialLocal(ActiveContract.SYNC_MEDICINES);
                return true;
            case R.id.action_add_medicines:
                showInsertNewMedicationDialog(mPatientOwner.getMedicalRecordNumber());
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        inflater.inflate(R.menu.menu_medicines, menu);
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
            title = mPatientOwner.getFirstName() + " " + mPatientOwner.getLastName() + " " + getString(R.string.medicine_header);
        }
        return title;
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
                //displayList();
                refreshItem.setActionView(null);
            }
        }
    }

    static int count = 0;
    String mSelection = null;
    private void init() {

        final Long patientId = getArguments().getLong(ARG_PATIENT_ID);

        if((patientId > 0)){
            mPatientOwner = Patient.getById(patientId);
            if(mPatientOwner != null) {
                mSelection = ActiveContract.MEDICINES_COLUMNS.PATIENT_ID + " = '" + mPatientOwner.getMedicalRecordNumber() + "'";
            }
        }else if (DAOManager.get().getUser().getUserType().equals(UserType.PATIENT)){
            mPatientOwner = Patient.getByMedicalNumber(DAOManager.get().getUser().getUserIdentification());
        }
        //Vehicle vehicle = new Vehicle();
        //vehicle.setVIN("VIN" + count);
        //long count = vehicle.save();
        mAdapter = new MedicinesCursorCardAdapter(getActivity());

        mListView = (CardListView) getActivity().findViewById(R.id.card_medicines_list_cursor);
        //mListView.setEmptyView(getActivity().findViewById(android.R.id.empty));

        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }


        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return queryAllField(charSequence.toString(),mSelection);
            }
        });
        // Force start background query to load sessions

        getLoaderManager().restartLoader(0, null, this);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Loader<Cursor> loader = null;
        loader = new CursorLoader(getActivity(),
                ContentProvider.createUri(PainMedication.class, null),
                ActiveContract.MEDICINES_TABLE_PROJECTION, mSelection, null,
                ActiveContract.MEDICINES_COLUMNS.NAME + " asc"
        );
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }
        mAdapter.swapCursor(data);

        //displayEmptyListMessage(data.getCount() > 0);
        displayList(data.getCount() <= 0);
        //displayList();
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
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
        return BaseFragment.FRAGMENT_TYPE_MEDICINES;
    }

    @Override
    public void OnFilterData(String textToSearch) {
        mAdapter.getFilter().filter(textToSearch);
    }



    private void showInsertNewMedicationDialog(final String patientMedicalNumber) {
        final Dialog dialog = new Dialog(getActivity());

        dialog.setContentView(R.layout.custom_dialog_add_medication);

        dialog.setTitle("New Pain Medication");

        dialog.show();

        final EditText entry_medication_name = (EditText) dialog.findViewById(R.id.txt_new_medication);
        final View view_error_message = dialog.findViewById(R.id.layout_medication_error);
        final TextView textView_error_message = (TextView) dialog.findViewById(R.id.txt_medication_error);

        entry_medication_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                view_error_message.setVisibility(View.GONE);
            }
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        Button btnSet = (Button) dialog.findViewById(R.id.btnSet);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                final String medicationName = entry_medication_name.getText().toString();

                boolean existMedication = false;
                //check if medication exists for the Patient
                List<PainMedication> medications = PainMedication.getAll(patientMedicalNumber);

                for (PainMedication medication : medications) {
                    existMedication = medication.getMedicationName().toUpperCase()
                            .equals(medicationName.toUpperCase());
                    if (existMedication) {
                        break;
                    }
                }
                if (existMedication) {
                    view_error_message.setVisibility(View.VISIBLE);
                    textView_error_message.setText(getString(R.string.txt_error_medication_exists));

                }else if(medicationName.isEmpty()){
                    view_error_message.setVisibility(View.VISIBLE);
                    textView_error_message.setText(getString(R.string.txt_error_medication_empty));
                } else {
                    Toast.makeText(getActivity(), "New Medication inserted successfully: " + medicationName.toUpperCase(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                //Log.i("Medication", "New Medication inserted: " + entry_medication_name.getText().toString());

            }
        });
    }

    public static class AlertMedicationDeleteFragment extends DialogFragment {

        public static AlertMedicationDeleteFragment newInstance(int title,String message, String medicationName) {
            AlertMedicationDeleteFragment frag = new AlertMedicationDeleteFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            args.putString("message", message);
            args.putString("medication", medicationName);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final int title = getArguments().getInt("title");
            final String message = getArguments().getString("message");
            final String medicationName = getArguments().getString("medication");

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_medicine)
                    .setMessage(message)
                    .setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    Toast.makeText(getActivity(),"Medication " + medicationName + " removed successfully",Toast.LENGTH_SHORT).show();
                                }
                            })
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    //Toast.makeText(getActivity(),"Medication removed successfully",Toast.LENGTH_SHORT);
                                }
                            }).create();
        }
    }
    //-------------------------------------------------------------------------------------------------------------
    // Adapter
    //-------------------------------------------------------------------------------------------------------------
    public class MedicinesCursorCardAdapter extends CardCursorAdapter {

        private String mDetailedMedicineInfo;


        public MedicinesCursorCardAdapter(Context context) {
            super(context);
        }

        @Override
        protected Card getCardFromCursor(Cursor cursor) {
            MedicineCursorCard card = new MedicineCursorCard(super.getContext());
            setCardFromCursor(card,cursor);


            //Create a CardHeader
            CardHeader header = new CardHeader(getActivity());

            //Set visible the expand/collapse button
            //header.setButtonExpandVisible(true);

            //Set the header title
            header.setTitle(card.mainHeader);
            header.setPopupMenu(R.menu.popup_medicines, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                @Override
                public void onMenuItemClick(BaseCard card, MenuItem item) {
                    final int id = item.getItemId();
                    final String mMedicineName = ((MedicineCursorCard)card).mainTitle;
                    if(id == R.id.menu_pop_delete_medicine){
                        AlertMedicationDeleteFragment.newInstance(R.id.txt_medication_delete,
                                "Are you sure to delete " + mMedicineName + " ?", mMedicineName)
                        .show(getFragmentManager(),"Alert_Medication_Delete");
                    }
                    //Toast.makeText(getContext(), "Click on card="+card.getId()+" item=" +  item.getTitle(), Toast.LENGTH_SHORT).show();
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

            //This provides a simple (and useless) expand area
            CustomExpandCard expand = new CustomExpandCard(super.getContext(), mDetailedMedicineInfo);
            //expand.setTitle("Check-In Details");
            //Add Expand Area to Card
            card.addCardExpand(expand);

            return card;
        }

        private void setCardFromCursor(MedicineCursorCard card,Cursor cursor) {
            final int medicineId = cursor.getInt(ID_COLUMN);
            card.setId(""+ medicineId);
            card.mainTitle = cursor.getString(cursor.getColumnIndex(ActiveContract.MEDICINES_COLUMNS.NAME));

            card.mainHeader = getString(R.string.medicine_header);
            card.resourceIdThumb=R.drawable.ic_medicine;

            final PainMedication painMedication = PainMedication.getById(medicineId);
            if(painMedication != null) {
                //card.secondaryTitle = painMedication.getIssueDateTimeClear(); // fromMilliseconds.format("YYYY-MM-DD hh:ss");
                mDetailedMedicineInfo = PainMedication.getDetailedInfo(painMedication);
            }
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
    public class MedicineCursorCard extends Card {

        String mainTitle;
        String secondaryTitle;
        String mainHeader;
        int resourceIdThumb;
        private ImageButton mButtonExpandCustom;

        public MedicineCursorCard(Context context) {
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
