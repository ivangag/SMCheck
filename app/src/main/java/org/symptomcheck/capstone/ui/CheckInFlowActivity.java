package org.symptomcheck.capstone.ui;

import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.utils.NotificationHelper;

public class CheckInFlowActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    UserInfo mUser;

    private boolean checkInPermitted = true;

    List<PainMedication> mMedicines = Lists.newArrayList();

    private enum FragmentType{
        FRAGMENT_TYPE_PAIN_LEVEL,
        FRAGMENT_TYPE_FEED_STATUS,
        FRAGMENT_TYPE_MEDICINES,
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_flow);

        mUser = DAOManager.get().getUser();

        if(mUser != null) {

            mMedicines = PainMedication.getAll(mUser.getUserIdentification());

            // Set up the action bar.
            final ActionBar actionBar = getActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setTitle(
                    mUser.getFirstName()
                            + " " + mUser.getLastName()
                            + " " + "Check-In");

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            // When swiping between different sections, select the corresponding
            // tab. We can also use ActionBar.Tab#select() to do this if we have
            // a reference to the Tab.
            mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    actionBar.setSelectedNavigationItem(position);
                }
            });

            // For each of the sections in the app, add a tab to the action bar.
            for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                // Create a tab with text corresponding to the page title defined by
                // the adapter. Also specify this Activity object, which implements
                // the TabListener interface, as the callback (listener) for when
                // this tab is selected.
                actionBar.addTab(
                        actionBar.newTab()
                                .setText(mSectionsPagerAdapter.getPageTitle(i))
                                .setTabListener(this));
            }


        } else{
            checkInPermitted = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds getItemsQuestion to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_check_in_flow, menu);
        return true;
    }

    @Override
    protected void onResume() {
        if(!checkInPermitted) {
            NotificationHelper.showAlertDialog(this, NotificationHelper.AlertType.ALERT_GO_TO_LOGIN, getActionBar().getTitle().toString(), "");
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int totalItem = getCount();
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return PainQuestionFragment.newInstance(position + 1, FragmentType.FRAGMENT_TYPE_PAIN_LEVEL);
            } else if (position == getCount() - 1) {
                return PainQuestionFragment.newInstance(position + 1,FragmentType.FRAGMENT_TYPE_FEED_STATUS);
            } else {
                if (mMedicines.size() > 0) {
                    // for each medicine we have to instantiate a Fragment question
                }
                return PainQuestionFragment.newInstance(position + 1,FragmentType.FRAGMENT_TYPE_MEDICINES);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            final int totalMedicines = mMedicines.size();
            return 1 + totalMedicines + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            String title = null;
            /*
            switch (position) {
                case 0:
                    return getString(R.string.pain_status).toUpperCase(l);
                case 1:
                    return getString(R.string.feed_status).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
            */
            if (position == 0) {
                title = getString(R.string.pain_status).toUpperCase(l);
            } else if (position == getCount() - 1) {
                title = getString(R.string.feed_status).toUpperCase(l);
            } else {
                if (mMedicines.size() > 0) {
                    title = mMedicines.get(position - 1).getMedicationName();
                }
            }
            return title;
        }
    }

    public static void startCheckInFlow(Context context/*, String param1, String param2*/) {
        Intent intent = new Intent(context, CheckInFlowActivity.class);
        //intent.setAction(ACTION_GCM_DEVICE_REGISTRATION);
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PainQuestionFragment extends Fragment {

        View rootView;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static String ARG_FRAGMENT_TYPE = "frg_tpe";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PainQuestionFragment newInstance(int sectionNumber,FragmentType fragmentType) {
            PainQuestionFragment fragment = new PainQuestionFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_FRAGMENT_TYPE,fragmentType.toString());
            fragment.setArguments(args);
            return fragment;
        }

        public PainQuestionFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            final FragmentType fragmentType = FragmentType.valueOf(getArguments().getString(ARG_FRAGMENT_TYPE));
            switch (fragmentType){
                case FRAGMENT_TYPE_PAIN_LEVEL:
                    rootView = inflater.inflate(R.layout.fragment_check_in_question_pain, container, false);
                    break;
                case FRAGMENT_TYPE_FEED_STATUS:
                    rootView = inflater.inflate(R.layout.fragment_check_in_question_feed_status, container, false);
                    break;
                case FRAGMENT_TYPE_MEDICINES:
                    rootView = inflater.inflate(R.layout.fragment_check_in_question_medicine, container, false);
                    break;
            }

            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            TextView title = (TextView) rootView.findViewById(R.id.txt_check_in_header_question);
            final int positionFragment = getArguments().getInt(ARG_SECTION_NUMBER);

            final FragmentType fragmentType = FragmentType.valueOf(getArguments().getString(ARG_FRAGMENT_TYPE));
            switch (fragmentType){
                case FRAGMENT_TYPE_PAIN_LEVEL:
                    title.setText(getString(R.string.pain_title_question));
                    break;
                case FRAGMENT_TYPE_FEED_STATUS:
                    title.setText(getString(R.string.feed_status_title_question));
                    break;
                case FRAGMENT_TYPE_MEDICINES:
                    title.setText(String.format(getString(R.string.medicine_title_question),
                            ((CheckInFlowActivity)getActivity()).mMedicines.get(positionFragment - 2).getMedicationName()));
                    break;
            }


        }
    }

}
