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

import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.activeandroid.content.ContentProvider;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.CheckInOnlineWrapper;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.PatientExperience;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.Constants;

/**
 * Created by Ivan on 08/11/2014.
 */
public  abstract class BaseFragment extends Fragment {

    public final static int FRAGMENT_TYPE_PATIENT   = 0;
    public final static int FRAGMENT_TYPE_DOCTORS   =  1;
    public final static int FRAGMENT_TYPE_CHECKIN = 2;
    public final static int FRAGMENT_TYPE_MEDICINES = 3;
    public final static int FRAGMENT_TYPE_EXPERIENCES = 4;
    public final static int FRAGMENT_TYPE_ONLINE_CHECKIN = 5;

    public static final String TITLE_NONE = "";

    protected boolean mListShown;
    protected View mProgressContainer;
    protected View mListContainer;
    protected View mEmptyListContainer;


    public abstract int getFragmentType();
    public abstract String getTitleText();
    public abstract String getIdentityOwnerId(); // unique if of reference entity (for instance CheckIn=>PatientUniqueId)

    /**
     * Setup the list fragment
     *
     * @param root
     */
    protected void setupListFragment(View root) {

        switch (getFragmentType()){
            case FRAGMENT_TYPE_PATIENT:
                mListContainer = root.findViewById(R.id.card_patients_listContainer);
                break;
            case FRAGMENT_TYPE_DOCTORS:
                mListContainer = root.findViewById(R.id.card_doctors_listContainer);
                break;
            case FRAGMENT_TYPE_CHECKIN:
                mListContainer = root.findViewById(R.id.card_checkins_listContainer);
                break;
            case FRAGMENT_TYPE_MEDICINES:
                mListContainer = root.findViewById(R.id.card_medicines_listContainer);
                break;
            case FRAGMENT_TYPE_EXPERIENCES:
                mListContainer = root.findViewById(R.id.card_experiences_listContainer);
                break;
            case FRAGMENT_TYPE_ONLINE_CHECKIN:
                mListContainer = root.findViewById(R.id.card_checkins_online_listContainer);
                break;

            default:
                break;
        }

        mProgressContainer = root.findViewById(R.id.cards_progressContainer);
        mEmptyListContainer = root.findViewById(R.id.cards_empty_list);
        mListShown = true;
    }

    protected void displayList(boolean isEmpty){
        if (isResumed()) {
            setListVisibility(true,isEmpty);
        } else {
            setListVisibilityNoAnimation(true,isEmpty);
        }
    }

    /**
     * @param shown
     * @param animate
     * @param isEmpty
     */
    protected void setListShown(boolean shown, boolean animate, boolean isEmpty) {
        //Log.d("BaseFragment",String.format("FragmentType:%d. setListShown:%b. animate:%b. isEmpty:%b.",getFragmentType(),shown,animate,isEmpty));
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                if(!isEmpty) {
                    mListContainer.startAnimation(AnimationUtils.loadAnimation(
                            getActivity(), android.R.anim.fade_in));
                }else {
                    mEmptyListContainer.startAnimation(AnimationUtils.loadAnimation(
                            getActivity(), android.R.anim.fade_in));
                }
            }
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            mEmptyListContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
            mEmptyListContainer.setVisibility(View.GONE);
        }
    }
    protected void hideList(boolean animate){
        setListShown(false, animate, false);
    }

    public void setListVisibility(boolean shown, boolean isEmpty) {
        setListShown(shown, true,isEmpty);
    }

    public void setListVisibilityNoAnimation(boolean shown, boolean isEmpty) {
        setListShown(shown, false,isEmpty);
    }


    protected void setIconActionBar(){
        int iconId = -1;
        switch (getFragmentType()) {
            case FRAGMENT_TYPE_PATIENT:
                iconId = R.drawable.ic_patient_small;
                break;
            case FRAGMENT_TYPE_DOCTORS:
                iconId = R.drawable.ic_doctor;
                break;
            case FRAGMENT_TYPE_CHECKIN:
                iconId = R.drawable.ic_check_in;
                break;
            case FRAGMENT_TYPE_EXPERIENCES:
                iconId = R.drawable.ic_experience_2;
                break;
            case FRAGMENT_TYPE_MEDICINES:
                iconId = R.drawable.ic_medicine;
                break;
            case FRAGMENT_TYPE_ONLINE_CHECKIN:
                iconId = R.drawable.ic_action_web_site;
                break;

            default:
                break;

        }
        if(iconId != -1) {
            if(getActivity().getActionBar() != null) {
                getActivity().getActionBar().setIcon(iconId);
            }
        }
    }


    private String buildQuerySelection(){
        final String uniqueId = getIdentityOwnerId();
        String selection = Constants.STRINGS.EMPTY;
        switch (getFragmentType()){
            case FRAGMENT_TYPE_PATIENT:
                break;
            case FRAGMENT_TYPE_DOCTORS:
                break;
            case FRAGMENT_TYPE_ONLINE_CHECKIN:
                break;
            case FRAGMENT_TYPE_CHECKIN:
                if(!uniqueId.isEmpty())
                    selection = ActiveContract.CHECKIN_COLUMNS.PATIENT + " = " + Patient.getByMedicalNumber(uniqueId).getId();
                break;
            case FRAGMENT_TYPE_MEDICINES:
                if(!uniqueId.isEmpty())
                    selection =  ActiveContract.MEDICINES_COLUMNS.PATIENT + " = '" + uniqueId + "'";
                break;
            case FRAGMENT_TYPE_EXPERIENCES:
                if(!uniqueId.isEmpty())
                    selection =  ActiveContract.EXPERIENCES_COLUMNS.PATIENT + " = '" + uniqueId + "'";
                break;
            default:
                break;
        }
        //Log.d("buildQuerySelection", selection);
        return selection;
    }

    protected Cursor queryAllField(String filterPattern,String selection){

        Cursor cursor = null;
        selection = buildQuerySelection();
        Uri uriContentProvider = getDefaultUriProvider();
        if (filterPattern.isEmpty()){
           cursor = getDefaultCursorProvider(uriContentProvider,selection);
        }else {
            if((selection != null)
                  && !selection.isEmpty() ) {
                selection = selection + " AND ";
            }
            filterPattern = filterPattern.trim();
            switch (getFragmentType()) {

                case FRAGMENT_TYPE_PATIENT:
                    uriContentProvider = ContentProvider.createUri(Patient.class, null);
                        cursor = getActivity().getContentResolver()
                                .query(uriContentProvider,
                                        ActiveContract.PATIENT_TABLE_PROJECTION,
                                        ActiveContract.PATIENT_COLUMNS.LAST_NAME + " LIKE ? OR " +
                                                ActiveContract.PATIENT_COLUMNS.PATIENT_ID + " LIKE ? OR " +
                                                ActiveContract.PATIENT_COLUMNS.FIRST_NAME + " LIKE ?",
                                        new String[]{"%" + filterPattern + "%", "%" + filterPattern + "%", "%" + filterPattern + "%"}
                                        , ActiveContract.PATIENT_COLUMNS.FIRST_NAME + " asc");

                    break;
                case FRAGMENT_TYPE_DOCTORS:
                        cursor = getActivity().getContentResolver()
                                .query(uriContentProvider,
                                        ActiveContract.DOCTOR_TABLE_PROJECTION,
                                        ActiveContract.DOCTORS_COLUMNS.LAST_NAME + " LIKE ? OR " +
                                                ActiveContract.DOCTORS_COLUMNS.DOCTOR_ID + " LIKE ? OR " +
                                                ActiveContract.DOCTORS_COLUMNS.FIRST_NAME + " LIKE ?",
                                        new String[]{"%" + filterPattern + "%",
                                                "%" + filterPattern + "%",
                                                "%" + filterPattern + "%"
                                        }
                                        , ActiveContract.DOCTORS_COLUMNS.FIRST_NAME + " asc");
                    break;
                case FRAGMENT_TYPE_CHECKIN:
                    cursor = getActivity().getContentResolver()
                            .query(uriContentProvider,
                                    ActiveContract.CHECK_IN_TABLE_PROJECTION,
                                   selection + "( " + ActiveContract.CHECKIN_COLUMNS.FEED_STATUS + " LIKE ? OR " +
                                            ActiveContract.CHECKIN_COLUMNS.PAIN_LEVEL + " LIKE ? )",
                                    new String[]{"%" + filterPattern + "%", "%" + filterPattern + "%"}
                                    , ActiveContract.CHECKIN_COLUMNS.ISSUE_TIME + " desc");
                    break;
                case FRAGMENT_TYPE_MEDICINES:
                    cursor = getActivity().getContentResolver()
                            .query(uriContentProvider,
                                    ActiveContract.MEDICINES_TABLE_PROJECTION,
                                    selection + "( " + ActiveContract.MEDICINES_COLUMNS.NAME + " LIKE ? OR " +
                                    ActiveContract.MEDICINES_COLUMNS.PRODUCT_ID + " LIKE ? OR " +
                                            ActiveContract.MEDICINES_COLUMNS.PATIENT + " LIKE ? )",
                                    new String[]{"%" + filterPattern + "%", "%" + filterPattern + "%","%" + filterPattern + "%"}
                                    , ActiveContract.MEDICINES_COLUMNS.NAME + " asc");
                    break;
                case FRAGMENT_TYPE_EXPERIENCES:
                    cursor = getActivity().getContentResolver()
                            .query(uriContentProvider,
                                    ActiveContract.EXPERIENCES_TABLE_PROJECTION,
                                    selection + "( " + ActiveContract.EXPERIENCES_COLUMNS.EXPERIENCE_TYPE + " LIKE ? OR " +
                                    ActiveContract.EXPERIENCES_COLUMNS.START_EXPERIENCE_TIME + " LIKE ? OR " +
                                            ActiveContract.EXPERIENCES_COLUMNS.END_EXPERIENCE_TIME + " LIKE ? )",
                                    new String[]{"%" + filterPattern + "%", "%" + filterPattern + "%","%" + filterPattern + "%"}
                                    , ActiveContract.EXPERIENCES_COLUMNS.EXPERIENCE_DURATION + " desc");
                    break;
               case FRAGMENT_TYPE_ONLINE_CHECKIN:
                   cursor = getActivity().getContentResolver()
                           .query(uriContentProvider,
                                   ActiveContract.CHECK_ONLINE_IN_TABLE_PROJECTION,
                                   selection + "( " + ActiveContract.CHECKIN_COLUMNS.FEED_STATUS + " LIKE ? OR " +
                                           ActiveContract.CHECKIN_COLUMNS.PAIN_LEVEL + " LIKE ? )",
                                   new String[]{"%" + filterPattern + "%", "%" + filterPattern + "%"}
                                   , ActiveContract.CHECKIN_COLUMNS.ISSUE_TIME + " desc");
                    break;

                default:
                    break;
            }
        }
        return cursor;
    }

    private Uri getDefaultUriProvider(){

        Uri uriContentProvider = Uri.EMPTY;// = ContentProvider.createUri(Patient.class, null);
        switch (getFragmentType()){
            case FRAGMENT_TYPE_PATIENT:
                uriContentProvider = ContentProvider.createUri(Patient.class, null);
            break;
            case FRAGMENT_TYPE_DOCTORS:
                uriContentProvider = ContentProvider.createUri(Doctor.class, null);
                break;
            case FRAGMENT_TYPE_CHECKIN:
                uriContentProvider = ContentProvider.createUri(CheckIn.class, null);
                break;
            case FRAGMENT_TYPE_MEDICINES:
                uriContentProvider = ContentProvider.createUri(PainMedication.class, null);
                break;
            case FRAGMENT_TYPE_EXPERIENCES:
                uriContentProvider = ContentProvider.createUri(PatientExperience.class, null);
                break;
            case FRAGMENT_TYPE_ONLINE_CHECKIN:
                uriContentProvider = ContentProvider.createUri(CheckInOnlineWrapper.class, null);
                break;

            default:
                break;
        }
        return uriContentProvider;
    }

    protected Cursor getDefaultCursorProvider(Uri uriContentProvider, String selection) {
        Cursor cursor = null;
        if(getActivity() != null) {
            switch (getFragmentType()) {
                case FRAGMENT_TYPE_PATIENT:
                    cursor = getActivity().getContentResolver()
                            .query(uriContentProvider,
                                    ActiveContract.PATIENT_TABLE_PROJECTION, selection, null, ActiveContract.PATIENT_COLUMNS.FIRST_NAME + " asc");
                    break;
                case FRAGMENT_TYPE_DOCTORS:
                    cursor = getActivity().getContentResolver()
                            .query(uriContentProvider,
                                    ActiveContract.DOCTOR_TABLE_PROJECTION, selection, null, ActiveContract.DOCTORS_COLUMNS.FIRST_NAME + " asc");
                    break;
                case FRAGMENT_TYPE_CHECKIN:
                    cursor = getActivity().getContentResolver()
                            .query(uriContentProvider,
                                    ActiveContract.CHECK_IN_TABLE_PROJECTION, selection, null,
                                    ActiveContract.CHECKIN_COLUMNS.ISSUE_TIME + " desc");
                    break;
                case FRAGMENT_TYPE_MEDICINES:
                    cursor = getActivity().getContentResolver()
                            .query(uriContentProvider,
                                    ActiveContract.MEDICINES_TABLE_PROJECTION, selection, null,
                                    ActiveContract.MEDICINES_COLUMNS.NAME + " asc");
                    break;
             case FRAGMENT_TYPE_EXPERIENCES:
                    cursor = getActivity().getContentResolver()
                            .query(uriContentProvider,
                                    ActiveContract.EXPERIENCES_TABLE_PROJECTION, selection, null,
                                    ActiveContract.EXPERIENCES_COLUMNS.EXPERIENCE_DURATION + " desc");
                    break;
                case FRAGMENT_TYPE_ONLINE_CHECKIN:
                    cursor = getActivity().getContentResolver()
                            .query(uriContentProvider,
                                    ActiveContract.CHECK_ONLINE_IN_TABLE_PROJECTION, selection, null,
                                    ActiveContract.CHECKIN_COLUMNS.ISSUE_TIME + " desc");
                    break;
                default:
                    break;
            }
        }
        return cursor;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle();
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
    }

    protected void setTitle(){
        final String titleResId = getTitleText();
        if (!titleResId.equals(TITLE_NONE)) {
            if(getActivity() != null)
                getActivity().setTitle(titleResId);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                if(getActivity() != null)
                    NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
