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
package org.symptomcheck.capstone.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.SyncUtils;
import org.symptomcheck.capstone.alarms.SymptomAlarmRequest;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.FeedStatus;
import org.symptomcheck.capstone.model.PainLevel;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.Constants;
import org.symptomcheck.capstone.utils.NotificationHelper;

import hirondelle.date4j.DateTime;
//TODO#BPR_3 Check-In Submission Activity
public class CheckInFlowActivityOld extends Activity implements ActionBar.TabListener {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    UserInfo mUser;
    private boolean checkInPermitted = true;

    public final static String NO = "NO";
    public final static String YES = "YES";
    List<PainMedication> mMedicines = Lists.newArrayList();
    private CheckIn mCheckInFromUserChoices;


    private enum FragmentType{
        FRAGMENT_TYPE_PAIN_LEVEL,
        FRAGMENT_TYPE_FEED_STATUS,
        FRAGMENT_TYPE_MEDICINES,
    }

    private Map<String,String> mReportMedicationsResponse = new HashMap<String, String>(){};
    private Map<String,String> mReportMedicationsTakingTime = new HashMap<String, String>(){};
    private PainLevel mReportPainLevel = PainLevel.UNKNOWN;
    private FeedStatus mReportFeedStatus = FeedStatus.UNKNOWN;

    private Handler progressBarHandler;

    ImageButton mBtnGoToPreviousTab;
    ImageButton mBtnGoToNextTab;
    ImageButton mBtnSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_flow);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        if(getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        mUser = DAOManager.get().getUser();
        // TODO#BPR_2 allow check-in only if user is a Patient and is logged
        if((mUser != null)
            && (mUser.getUserType().equals(UserType.PATIENT))){

            progressBarHandler = new Handler();
            mBtnGoToPreviousTab = (ImageButton) findViewById(R.id.btn_check_in_goto_previous);
            mBtnGoToNextTab = (ImageButton) findViewById(R.id.btn_check_in_goto_next);
            mBtnSubmit = (ImageButton) findViewById(R.id.btn_check_in_confirm_submission);

            mMedicines = PainMedication.getAll(mUser.getUserIdentification());
            for(PainMedication medication : mMedicines){
                mReportMedicationsResponse.put(medication.getMedicationName(), Constants.STRINGS.EMPTY);
                mReportMedicationsTakingTime.put(medication.getMedicationName(), Constants.STRINGS.EMPTY);
            }
            // Set up the action bar.
            final ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                actionBar.setTitle(
                        mUser.getFirstName()
                                + " " + mUser.getLastName()
                                + " " + "Check-In");
            }

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);


            //TODO#BPR_7 here we handle through the swiping the visibility of buttons
            // When swiping between different sections, select the corresponding
            // tab. We can also use ActionBar.Tab#select() to do this if we have
            // a reference to the Tab.
            mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    handleNextPreviousVisibility();
                    if (actionBar != null) {
                       // actionBar.setSelectedNavigationItem(position);
                    }
                }
            });


            // For each of the sections in the app, add a tab to the action bar.
            for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                // Create a tab with text corresponding to the page title defined by
                // the adapter. Also specify this Activity object, which implements
                // the TabListener interface, as the callback (listener) for when
                // this tab is selected.
                if (actionBar != null) {
                    actionBar.addTab(
                            actionBar.newTab()
                                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                                    .setTabListener(this));
                }
            }

            mBtnGoToPreviousTab.setVisibility(View.INVISIBLE);
            mBtnGoToPreviousTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mViewPager.getCurrentItem() > 0)
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                    handleNextPreviousVisibility();
                }
            });

            mBtnGoToNextTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mViewPager.getCurrentItem() < mSectionsPagerAdapter.getCount() - 1)
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                    handleNextPreviousVisibility();
                }
            });

            mBtnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleCheckInSubmissionRequest();
                }
            });


        } else{
            checkInPermitted = false;
        }

    }

    private void handleNextPreviousVisibility(){
        if(mViewPager.getCurrentItem() == 0){
            mBtnGoToPreviousTab.setVisibility(View.INVISIBLE);
            mBtnGoToNextTab.setVisibility(View.VISIBLE);
        }else{
            mBtnGoToPreviousTab.setVisibility(View.VISIBLE);
            if(mViewPager.getCurrentItem() == mSectionsPagerAdapter.getCount() - 1){
                mBtnGoToNextTab.setVisibility(View.INVISIBLE);
            }else{
                mBtnGoToNextTab.setVisibility(View.VISIBLE);
            }
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
            String title = "Check-In";
            if(null != getActionBar()){
                title = getActionBar().getTitle().toString();
            }
            NotificationHelper.showAlertDialog(this, NotificationHelper.AlertType.ALERT_GO_TO_LOGIN,title , "");
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
        // save check-in
        if (id == R.id.action_submit_checkin) {

            handleCheckInSubmissionRequest();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleCheckInSubmissionRequest() {
        String msgError = Constants.STRINGS.EMPTY;
        //verify check-in data consistence
        boolean check = false;
        boolean checkMedicines = true;
        if (mReportPainLevel == PainLevel.UNKNOWN) {
            msgError = "Pain Level not reported";
            mViewPager.setCurrentItem(0);
        } else {
            for (int idx = 0; idx < mMedicines.size(); idx++) {
                final String medication = mMedicines.get(idx).getMedicationName();
                if (mReportMedicationsResponse.get(medication).equals(Constants.STRINGS.EMPTY)) {
                    msgError = String.format("Pain Medication %s not reported", medication);
                    checkMedicines = false;
                    mViewPager.setCurrentItem(1 + idx);
                } else if (mReportMedicationsResponse.get(medication).equals(YES)
                        && mReportMedicationsTakingTime.get(medication).equals(Constants.STRINGS.EMPTY)) {
                    msgError = String.format("Pain Medication %s reported without Date & Time", medication);
                    checkMedicines = false;
                    mViewPager.setCurrentItem(1 + idx);
                }
                if (!checkMedicines)
                    idx = mMedicines.size();
            }
            check = checkMedicines;
        }
        // in this way we'll check the medicines questions first
        if (check &&  (mReportFeedStatus == FeedStatus.UNKNOWN)) {
            check = false;
            msgError = "Feed Status not reported";
            mViewPager.setCurrentItem(mSectionsPagerAdapter.getCount() - 1);
        }

        if (check) {
            // Save Check-In and trigger local => cloud sync
            mCheckInFromUserChoices = makeCheckInFromUserChoices();
            showDialog();
        } else {
            //Toast.makeText(this, msgError, Toast.LENGTH_LONG).show();
        }
    }

    void showDialog() {
        DialogFragment newFragment = AlertCheckSubmissionFragment
                .newInstance(R.string.alert_dialog_title_checkin_submission,CheckIn.getDetailedInfo(mCheckInFromUserChoices,false));
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void executeCheckInSaving(final CheckIn checkIn) {
        final ProgressDialog ringProgressDialog = ProgressDialog.show(this, "Please wait ...",
                "Check-In submission in progress ...", true);
        ringProgressDialog.setCancelable(true);
        new Thread(new Runnable() { //TODO#BPR_8 Check-In saving performed in a background Thread
            @Override
            public void run() {
                try {
                    final boolean checkinRes = saveCheckIn(checkIn);
                    Thread.sleep(3000);
                    progressBarHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (checkinRes) {
                                //re-schedule the next check-in
                                SymptomAlarmRequest.get().setAlarm(getApplicationContext(), SymptomAlarmRequest.AlarmRequestedType.ALARM_CHECK_IN_REMINDER,false);
                                SyncUtils.TriggerRefreshPartialCloud(ActiveContract.SYNC_CHECK_IN);
                                finish();
                                Toast.makeText(getApplicationContext(), "Check-In Submitted Correctly", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Check-In Submission ERROR!!!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (Exception ignored) {
                }
                ringProgressDialog.dismiss();
            }
        }).start();
    }


    private CheckIn makeCheckInFromUserChoices(){
        Map<PainMedication, String> meds = new HashMap<PainMedication, String>();
        for(int idx = 0; idx < mMedicines.size();idx++) {
            final String medication = mMedicines.get(idx).getMedicationName();
            final String time = mReportMedicationsTakingTime.get(medication);
            meds.put(new PainMedication(medication, time), mReportMedicationsResponse.get(medication));
        }
        return CheckIn.createCheckIn(mReportPainLevel,mReportFeedStatus, meds);
    }

    private boolean saveCheckIn(CheckIn checkIn){
        checkIn.setNeedSync(1);
        return DAOManager.get().saveCheckIns(Lists.newArrayList(checkIn),mUser.getUserIdentification());
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
            final int totalItem = getCount();
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return CheckInQuestionFragment.newInstance(position + 1, FragmentType.FRAGMENT_TYPE_PAIN_LEVEL);
            } else if (position == totalItem - 1) {
                return CheckInQuestionFragment.newInstance(position + 1, FragmentType.FRAGMENT_TYPE_FEED_STATUS);
            } else {  //TODO#FDAR_6 Instantiate different Screen showing separate Question for each Medication
                return CheckInQuestionFragment.newInstance(position + 1, FragmentType.FRAGMENT_TYPE_MEDICINES);
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

    //TODO#FDAR_3 Fragment Screen used to show an store Questions Response for Check-In submission
    public static class CheckInQuestionFragment extends Fragment {

        View rootView;
        View painQuestionsView;
        View feedQuestionsView;
        View medicinesQuestionsView;
        TextView txtMedicineTakingTime;
        FragmentType mFragmentType;
        int mPageFragment;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static String ARG_FRAGMENT_TYPE = "frg_tpe";
        String mMedicineName;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static CheckInQuestionFragment newInstance(int sectionNumber,FragmentType fragmentType) {
            CheckInQuestionFragment fragment = new CheckInQuestionFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_FRAGMENT_TYPE,fragmentType.toString());
            fragment.setArguments(args);
            return fragment;
        }

        public CheckInQuestionFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            mFragmentType = FragmentType.valueOf(getArguments().getString(ARG_FRAGMENT_TYPE));
            switch (mFragmentType){
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
            txtMedicineTakingTime = (TextView)rootView.findViewById(R.id.txt_check_in_medicine_take_time);

            final CheckInFlowActivityOld parentActivity = ((CheckInFlowActivityOld)getActivity());
            mPageFragment = getArguments().getInt(ARG_SECTION_NUMBER);

            switch (mFragmentType){
                case FRAGMENT_TYPE_PAIN_LEVEL:
                    //TODO#FDAR_4 CHECK-IN INCLUDES THE QUESTION, “HOW BAD IS YOUR MOUTH PAIN/SORE THROAT?” TO WHICH A PATIENT CAN RESPOND, “WELL-CONTROLLED,” “MODERATE,” OR “SEVERE.
                    title.setText(getString(R.string.pain_title_question));
                    painQuestionsView = rootView.findViewById(R.id.viewRadioBtnPaintQuestions);
                    painQuestionsView.findViewById(R.id.radioBtnPainModerate).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            parentActivity.mReportPainLevel = PainLevel.MODERATE;
                        }
                    });
                    painQuestionsView.findViewById(R.id.radioBtnPainSevere).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            parentActivity.mReportPainLevel = PainLevel.SEVERE;
                        }
                    });
                    painQuestionsView.findViewById(R.id.radioBtnPainWellControlled).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            parentActivity.mReportPainLevel = PainLevel.WELL_CONTROLLED;
                        }
                    });
                    break;
                case FRAGMENT_TYPE_FEED_STATUS:
                    //TODO#FDAR_8 DURING A CHECK-IN, THE PATIENT IS ASKED “DOES YOUR PAIN STOP YOU FROM EATING/DRINKING?” TO THIS, THE PATIENT CAN RESPOND, “NO,” “SOME,” OR “I CAN’T EAT.
                    title.setText(getString(R.string.feed_status_title_question));
                    feedQuestionsView = rootView.findViewById(R.id.viewRadioBtnFeedQuestions);
                    feedQuestionsView.findViewById(R.id.radioBtnFeedNo).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            parentActivity.mReportFeedStatus = FeedStatus.NO;
                        }
                    });
                    feedQuestionsView.findViewById(R.id.radioBtnFeedSome).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            parentActivity.mReportFeedStatus = FeedStatus.SOME;
                        }
                    });
                    feedQuestionsView.findViewById(R.id.radioBtnFeedCannotEat).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            parentActivity.mReportFeedStatus = FeedStatus.CANNOT_EAT;
                        }
                    });
                    break;
                case FRAGMENT_TYPE_MEDICINES:
                    //TODO#FDAR_5 CHECK-IN INCLUDES THE QUESTION, “DID YOU TAKE YOUR PAIN MEDICATION?” TO WHICH A PATIENT CAN RESPOND “YES” OR “NO”.
                    mMedicineName = parentActivity.mMedicines.get(mPageFragment - 2).getMedicationName();
                    title.setText(String.format(getString(R.string.medicine_title_question),mMedicineName)); //TODO#FDAR_6 Separate Question for each Medication
                    medicinesQuestionsView = rootView.findViewById(R.id.viewRadioBtnMedQuestions);
                    medicinesQuestionsView.findViewById(R.id.radioBtnMedicineNO).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            txtMedicineTakingTime.setVisibility(View.GONE);
                            parentActivity.mReportMedicationsResponse.put(mMedicineName,NO);
                        }
                    });
                    medicinesQuestionsView.findViewById(R.id.radioBtnMedicineYES).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            txtMedicineTakingTime.setVisibility(View.VISIBLE);
                            parentActivity.mReportMedicationsResponse.put(mMedicineName, YES);
                            showTimePickerDialog(rootView,mMedicineName); //TODO#FDAR_7 DateTime Dialog is shown when Patient select YES button
                        }
                    });
                    final boolean YES = ((RadioButton)medicinesQuestionsView.findViewById(R.id.radioBtnMedicineYES)).isChecked();
                    txtMedicineTakingTime.setVisibility(YES ? View.VISIBLE : View.GONE);
                    txtMedicineTakingTime.setClickable(true);
                    txtMedicineTakingTime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showTimePickerDialog(rootView,mMedicineName); //TODO#FDAR_7 DateTime Dialog is also shown when Patient select click over the textview allowing to modify the choice
                        }
                    });
                    break;
            }
        }

        @Override
        public void onResume() {
            if(mFragmentType.equals(FragmentType.FRAGMENT_TYPE_MEDICINES)){
                String timeTaken = ((CheckInFlowActivityOld) getActivity()).mReportMedicationsTakingTime.get(mMedicineName);
                final boolean YES = ((RadioButton)medicinesQuestionsView.findViewById(R.id.radioBtnMedicineYES)).isChecked();
                txtMedicineTakingTime.setVisibility(YES ? View.VISIBLE : View.GONE);
                if(!timeTaken.equals(Constants.STRINGS.EMPTY)) {
                    timeTaken =  DateTime.forInstant(Long.valueOf(timeTaken),TimeZone.getTimeZone("GMT+00")).format("YYYY-MM-DD hh:mm");
                }
                txtMedicineTakingTime.setText(timeTaken);
            }
            super.onResume();
        }

        //TODO#FDAR_7 Interactive used by Patient to enter the Date & Time he/shee took the specified medicine
        private void showTimePickerDialog(View v, String mMedicineName) {
            final Dialog dialog = new Dialog(getActivity());

            dialog.setContentView(R.layout.custom_dialog_datetime);

            dialog.setTitle(String.format("%s",mMedicineName));

            dialog.show();

            final DatePicker dp = (DatePicker)dialog.findViewById(R.id.datePicker1);
            final TimePicker tp = (TimePicker)dialog.findViewById(R.id.timePicker1);

            Button btnCancel = (Button)dialog.findViewById(R.id.btnCancelDT);
            Button btnSet = (Button)dialog.findViewById(R.id.btnSetDT);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            btnSet.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    String am_pm = "";
                    int m = dp.getMonth()+1;
                    int d = dp.getDayOfMonth();
                    int y = dp.getYear();

                    int h = tp.getCurrentHour();
                    int min = tp.getCurrentMinute();

                    String strm = String.valueOf(min);

                    if(strm.length()==1){
                        strm = "0"+strm;
                    }
                    int hour24 = h;
                    if(h>12){
                        am_pm = "PM";
                        h = h-12;
                    }else{
                        am_pm = "AM";
                    }

                    String date = m+"/"+d+"/"+y+" "+h+":"+strm+":00 "+am_pm;
                    String time = h+":"+strm+" "+am_pm;

                    //DateTime dateAndTime = new DateTime("2010-01-19 23:59:59");
                    String format = String.format("%d-%02d-%02d %02d:%02d:%02d",y,m,d,hour24,min,0);
                    DateTime dateAndTime = new DateTime(format);

                    long milliFrom1970GMT = dateAndTime.getMilliseconds(TimeZone.getTimeZone("GMT+00"));

                    txtMedicineTakingTime.setText(dateAndTime.toString());

                    ((CheckInFlowActivityOld)getActivity()).mReportMedicationsTakingTime.put(CheckInQuestionFragment.this.mMedicineName, String.valueOf(milliFrom1970GMT));

                    Log.i("CheckInFlow", "milliFrom1970GMT= " + milliFrom1970GMT);
                    //Toast.makeText(getActivity(), "Date: " + date + " Time: " + time, Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                }
            });
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            final FragmentType fragmentType = FragmentType.valueOf(getArguments().getString(ARG_FRAGMENT_TYPE));
            // Make sure that we are currently visible
            if (this.isVisible()) {
                // If we are becoming invisible, then...
                if (!isVisibleToUser) {
                    //Log.d("MyFragment", "Not visible anymore.  Stopping audio.");
                    switch (fragmentType){
                        case FRAGMENT_TYPE_PAIN_LEVEL:
                            if(painQuestionsView != null){
                                    PainLevel painLevel = PainLevel.UNKNOWN;
                                    RadioButton radioButtonModerate = ((RadioButton)painQuestionsView.findViewById(R.id.radioBtnPainModerate));
                                    RadioButton radioButtonSever = ((RadioButton)painQuestionsView.findViewById(R.id.radioBtnPainSevere));
                                    RadioButton radioButtonWell = ((RadioButton)painQuestionsView.findViewById(R.id.radioBtnPainWellControlled));
                                    if(radioButtonModerate.isChecked()){
                                        painLevel = PainLevel.MODERATE;
                                    }else if(radioButtonSever.isChecked()){
                                        painLevel = PainLevel.SEVERE;
                                    }else if(radioButtonWell.isChecked()){
                                        painLevel = PainLevel.WELL_CONTROLLED;
                                    }
                            }
                            break;
                        case FRAGMENT_TYPE_FEED_STATUS:
                            break;
                        case FRAGMENT_TYPE_MEDICINES:
                            break;
                    }
                }
            }
        }
    }
    public static class AlertCheckSubmissionFragment extends DialogFragment {

        public static AlertCheckSubmissionFragment newInstance(int title,String message) {
            AlertCheckSubmissionFragment frag = new AlertCheckSubmissionFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            args.putString("message", message);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final int title = getArguments().getInt("title");
            final String message = getArguments().getString("message");

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_check_in)
                    .setMessage(message)
                    .setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    ((CheckInFlowActivityOld) getActivity())
                                            .confirmCheckInSubmission();
                                }
                            })
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    ((CheckInFlowActivityOld) getActivity())
                                            .cancelCheckInSubmission();
                                }
                            }).create();
        }
    }

    private void cancelCheckInSubmission() {

    }

    private void confirmCheckInSubmission() {
        executeCheckInSaving(mCheckInFromUserChoices);
    }

}
