package org.symptomcheck.capstone.fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;

import com.activeandroid.content.ContentProvider;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.provider.ActiveContract;

/**
 * Created by Ivan on 08/11/2014.
 */
public  abstract class BaseFragment extends Fragment {

    public final static int FRAGMENT_TYPE_PATIENT   = 0;
    public final static int FRAGMENT_TYPE_DOCTORS   = 1;
    public final static int FRAGMENT_TYPE_CHECKIN = 2;
    public final static int FRAGMENT_TYPE_MEDICINES = 3;

    public abstract int getFragmentType();

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
            default:
                break;

        }
        if(iconId != -1) {
            if(getActivity().getActionBar() != null) {
                getActivity().getActionBar().setIcon(iconId);
            }
        }
    }

    protected Cursor queryAllField(String filterPattern){

        Cursor cursor = null;
        Uri uriContentProvider = getDefaultUriProvider();
        if (filterPattern.isEmpty()){
           cursor = getDefaultCursorProvider(uriContentProvider);
        }else {
            switch (getFragmentType()) {

                case FRAGMENT_TYPE_PATIENT:
                    uriContentProvider = ContentProvider.createUri(Patient.class, null);
                    //final String filterPattern = charSequence.toString();
                        cursor = getActivity().getContentResolver()
                                .query(uriContentProvider,
                                        ActiveContract.PATIENT_TABLE_PROJECTION,
                                        ActiveContract.PATIENT_COLUMNS.LAST_NAME + " LIKE ? OR " +
                                                ActiveContract.PATIENT_COLUMNS.PATIENT_ID + " LIKE ? OR " +
                                                ActiveContract.PATIENT_COLUMNS.FIRST_NAME + " LIKE ?",
                                        new String[]{"%" + filterPattern + "%", "%" + filterPattern + "%"}
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
                                    ActiveContract.CHECKIN_COLUMNS.FEED_STATUS + " LIKE ? OR " +
                                            ActiveContract.CHECKIN_COLUMNS.PAIN_LEVEL + " LIKE ?",
                                    new String[]{"%" + filterPattern + "%", "%" + filterPattern + "%"}
                                    , ActiveContract.CHECKIN_COLUMNS.ISSUE_TIME + " asc");
                    break;
                case FRAGMENT_TYPE_MEDICINES:
                    cursor = getActivity().getContentResolver()
                            .query(uriContentProvider,
                                    ActiveContract.MEDICINES_TABLE_PROJECTION,
                                    ActiveContract.MEDICINES_COLUMNS.NAME + " LIKE ? OR " +
                                            ActiveContract.MEDICINES_COLUMNS.PATIENT_ID + " LIKE ?",
                                    new String[]{"%" + filterPattern + "%", "%" + filterPattern + "%"}
                                    , ActiveContract.MEDICINES_COLUMNS.NAME + " asc");
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
            default:
                break;
        }
        return uriContentProvider;
    }

    protected Cursor getDefaultCursorProvider(Uri uriContentProvider) {
        Cursor cursor = null;
        switch (getFragmentType()){
            case FRAGMENT_TYPE_PATIENT:
                cursor = getActivity().getContentResolver()
                                .query(uriContentProvider,
                                ActiveContract.PATIENT_TABLE_PROJECTION, null, null, null);
                break;
            case FRAGMENT_TYPE_DOCTORS:
                cursor = getActivity().getContentResolver()
                        .query(uriContentProvider,
                                ActiveContract.DOCTOR_TABLE_PROJECTION, null, null, null);
                break;
            case FRAGMENT_TYPE_CHECKIN:
                cursor = getActivity().getContentResolver()
                        .query(uriContentProvider,
                                ActiveContract.CHECK_IN_TABLE_PROJECTION, null, null, null);
                break;
            case FRAGMENT_TYPE_MEDICINES:
                cursor = getActivity().getContentResolver()
                        .query(uriContentProvider,
                                ActiveContract.MEDICINES_TABLE_PROJECTION, null, null, null);
                break;
            default:
                break;
        }
        return cursor;
    }

}
