package org.symptomcheck.capstone.fragments;

import android.app.Fragment;

/**
 * Created by Ivan on 08/11/2014.
 */
public  abstract class BaseFragment extends Fragment {

    public final static int FRAGMENT_TYPE_PATIENT   = 0;
    public final static int FRAGMENT_TYPE_DOCTORS   = 1;
    public final static int FRAGMENT_TYPE_CHECKINS  = 2;
    public final static int FRAGMENT_TYPE_MEDICATIONS  = 3;


    public abstract int getFragmentType();
}
