package org.symptomcheck.capstone.fragments;

import android.app.Fragment;
import android.widget.Filterable;

import org.symptomcheck.capstone.R;

/**
 * Created by Ivan on 08/11/2014.
 */
public  abstract class BaseFragment extends Fragment {

    public final static int FRAGMENT_TYPE_PATIENT   = 0;
    public final static int FRAGMENT_TYPE_DOCTORS   = 1;
    public final static int FRAGMENT_TYPE_CHECKIN = 2;
    public final static int FRAGMENT_TYPE_MEDICATIONS  = 3;

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
        if(iconId != -1)
            getActivity().getActionBar().setIcon(iconId);
    }

    public abstract int getFragmentType();
}
