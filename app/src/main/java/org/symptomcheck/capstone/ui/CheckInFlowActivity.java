package org.symptomcheck.capstone.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.google.common.collect.Lists;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.SyncUtils;
import org.symptomcheck.capstone.adapters.MedicationQuestionAdapter;
import org.symptomcheck.capstone.adapters.MedicationQuestionItem;
import org.symptomcheck.capstone.alarms.SymptomAlarmRequest;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.FeedStatus;
import org.symptomcheck.capstone.model.PainLevel;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.CheckInUtils;
import org.symptomcheck.capstone.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hirondelle.date4j.DateTime;


public class CheckInFlowActivity extends ActionBarActivity {


    @InjectView(R.id.app_bar)
    Toolbar toolbar;
    @InjectView(R.id.txt_toolbar_title)
    TextView toolbarTitle;
    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @InjectView(R.id.pager)
    ViewPager mPager;
    @InjectView(R.id.btn_check_in_confirm_submission)
    ImageButton btnSubmitCheckIn;
    @InjectView(R.id.btn_check_in_goto_next)
    ImageButton btnNext;
    @InjectView(R.id.btn_check_in_goto_previous)
    ImageButton btnPrevious;

    private CheckInPagerAdapter adapter;
    private int currentColor;

    List<PainMedication> mMedicines = Lists.newArrayList();
    private UserInfo mUser;
    private Handler progressBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_flow_material);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        CheckInUtils.getInstance().ResetValuesToDefault();
        mUser = DAOManager.get().getUser();

        progressBarHandler = new Handler();

        toolbarTitle.setText(String.format("%s %s Check-In",mUser.getFirstName(),mUser.getLastName()));
        mMedicines = PainMedication.getAll(mUser.getUserIdentification());
        for(PainMedication medication : mMedicines) {
            CheckInUtils.getInstance().ReportMedicationsResponse.put(medication.getMedicationName(), Constants.STRINGS.NO);
            CheckInUtils.getInstance().ReportMedicationsTakingTime.put(medication.getMedicationName(), Constants.STRINGS.EMPTY);
        }


        adapter = new CheckInPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(adapter);
        tabs.setViewPager(mPager);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        mPager.setPageMargin(pageMargin);

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPrevious.setVisibility(View.GONE);
                btnNext.setVisibility(View.VISIBLE);
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPrevious.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.GONE);
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
            }
        });

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(ViewPager.SCROLL_STATE_SETTLING == state){
                    if(mPager.getCurrentItem() == 1) {
                        mIsSubmitApplyFirstTime = false;
                        btnPrevious.setVisibility(View.VISIBLE);
                        btnNext.setVisibility(View.GONE);
                    }else{
                        btnPrevious.setVisibility(View.GONE);
                        btnNext.setVisibility(View.VISIBLE);
                    }
                    //Toast.makeText(CheckInFlowActivity.this, "Tab Medicine Selected!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        tabs.setOnTabReselectedListener(new PagerSlidingTabStrip.OnTabReselectedListener() {
            @Override
            public void onTabReselected(int position) {
                //Toast.makeText(CheckInFlowActivity.this, "Tab reselected: " + position, Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmitCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCheckInSubmissionRequest();
            }
        });
    }

    public static Intent makeIntentStartActivity(Context context){
        return new Intent(context, CheckInFlowActivity.class);
    }



    volatile boolean mIsSubmitApplyFirstTime = true;
    boolean mIsErrorToShow;
    private void handleCheckInSubmissionRequest() {

        mIsErrorToShow = true;
        String msgError = Constants.STRINGS.EMPTY;
        //verify check-in data consistence
        boolean check = false;
        boolean checkMedicines = true;
        if (CheckInUtils.getInstance().ReportPainLevel.equals(PainLevel.UNKNOWN)) {
            msgError = "Pain Level not reported";
            mPager.setCurrentItem(0);
        } else {
            if(CheckInUtils.getInstance().IsGeneralMedicinesQuestionChecked
                    && !CheckInUtils.getInstance().ReportMedicationsResponse.containsValue(Constants.STRINGS.YES)){
                msgError = "You have to select at least one medicine";
                mPager.setCurrentItem(1);
                checkMedicines = false;
            }else if(!CheckInUtils.getInstance().IsGeneralMedicinesQuestionChecked && mIsSubmitApplyFirstTime){
                mPager.setCurrentItem(1);
                mIsSubmitApplyFirstTime = false;
                checkMedicines = false;
                mIsErrorToShow = false;
            }else {
                for (int idx = 0; idx < mMedicines.size(); idx++) {
                    final String medication = mMedicines.get(idx).getMedicationName();
                    if (CheckInUtils.getInstance().ReportMedicationsResponse.get(medication).equals(Constants.STRINGS.EMPTY)) {
                        msgError = String.format("Pain Medication %s not reported", medication);
                        checkMedicines = false;
                        mPager.setCurrentItem(1);
                    } else if (CheckInUtils.getInstance().ReportMedicationsResponse.get(medication).equals(Constants.STRINGS.YES)
                            && CheckInUtils.getInstance().ReportMedicationsTakingTime.get(medication).equals(Constants.STRINGS.EMPTY)) {
                        msgError = String.format("Pain Medication %s reported without Date & Time", medication);
                        checkMedicines = false;
                        mPager.setCurrentItem(1);
                    }
                    if (!checkMedicines)
                        idx = mMedicines.size();
                }
            }
            check = checkMedicines;
        }
        // in this way we'll check the medicines questions first
        if (check &&  (CheckInUtils.getInstance().ReportFeedStatus.equals(FeedStatus.UNKNOWN))) {
            check = false;
            msgError = "Feed Status not reported";
            mPager.setCurrentItem(0);
        }

        if (check) {
            // Save Check-In and trigger local => cloud sync
            final CheckIn checkInFromUserChoices = buildCheckInFromUserChoices(mMedicines);
            executeCheckInSaving(checkInFromUserChoices);
//            showDialog();
        } else {
            if(mIsErrorToShow)
                Toast.makeText(this, msgError, Toast.LENGTH_LONG).show();
        }

    }


    private CheckIn buildCheckInFromUserChoices(List<PainMedication> Medicines){
        Map<PainMedication, String> meds = new HashMap<PainMedication, String>();
        for(int idx = 0; idx < Medicines.size();idx++) {
            final String medication = Medicines.get(idx).getMedicationName();
            final String time = CheckInUtils.getInstance().ReportMedicationsTakingTime.get(medication);
            meds.put(new PainMedication(medication, time),CheckInUtils.getInstance().ReportMedicationsResponse.get(medication));
        }
        return CheckIn.createCheckIn(CheckInUtils.getInstance().ReportPainLevel, CheckInUtils.getInstance().ReportFeedStatus, meds);
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

    private boolean saveCheckIn(CheckIn checkIn){
        checkIn.setNeedSync(1);
        return DAOManager.get().saveCheckIns(Lists.newArrayList(checkIn),mUser.getUserIdentification());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentColor", currentColor);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentColor = savedInstanceState.getInt("currentColor");
    }

    private enum FragmentType{
        FRAGMENT_TYPE_PAIN_LEVEL,
        FRAGMENT_TYPE_FEED_STATUS,
        FRAGMENT_TYPE_MEDICINES,
    }


    public class CheckInPagerAdapter extends FragmentPagerAdapter {

        public CheckInPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return HealthStatusQuestionFragment.newInstance();
            }else{
                return MedicationQuestionFragment.newInstance(mMedicines);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title;
            if(position == 0){
                title = getString(R.string.health_status);
            }else {
                title = getString(R.string.medicines_header);
            }
            return title;
        }

    }

    public static class HealthStatusQuestionFragment extends Fragment {

        View rootView;
        TextView txtMedicineTakingTime;
        public static HealthStatusQuestionFragment newInstance() {
            return new HealthStatusQuestionFragment();
        }

        public HealthStatusQuestionFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_checkin_feed_pain_questions, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            txtMedicineTakingTime = (TextView)rootView.findViewById(R.id.txt_check_in_medicine_take_time);


            rootView.findViewById(R.id.radioBtnPainModerate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckInUtils.getInstance().ReportPainLevel = PainLevel.MODERATE;
                }
            });
            rootView.findViewById(R.id.radioBtnPainSevere).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckInUtils.getInstance().ReportPainLevel = PainLevel.SEVERE;
                }
            });
            rootView.findViewById(R.id.radioBtnPainWellControlled).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckInUtils.getInstance().ReportPainLevel = PainLevel.WELL_CONTROLLED;
                }
            });

            rootView.findViewById(R.id.radioBtnFeedNo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckInUtils.getInstance().ReportFeedStatus = FeedStatus.NO;
                }
            });
            rootView.findViewById(R.id.radioBtnFeedSome).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckInUtils.getInstance().ReportFeedStatus = FeedStatus.SOME;
                }
            });
            rootView.findViewById(R.id.radioBtnFeedCannotEat).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckInUtils.getInstance().ReportFeedStatus = FeedStatus.CANNOT_EAT;
                }
            });

        }

        @Override
        public void onResume() {
            /*
            if(mFragmentType.equals(FragmentType.FRAGMENT_TYPE_MEDICINES)){
                String timeTaken = CheckInUtils.getInstance().ReportMedicationsTakingTime.get(mMedicineName);
                final boolean YES = ((RadioButton)medicinesQuestionsView.findViewById(R.id.radioBtnMedicineYES)).isChecked();
                txtMedicineTakingTime.setVisibility(YES ? View.VISIBLE : View.GONE);
                if(!timeTaken.equals(Constants.STRINGS.EMPTY)) {
                    timeTaken =  DateTime.forInstant(Long.valueOf(timeTaken), TimeZone.getTimeZone("GMT+00")).format("YYYY-MM-DD hh:mm");
                }
                txtMedicineTakingTime.setText(timeTaken);
            }
            */
            super.onResume();
        }


    }

    public static class MedicationQuestionFragment extends Fragment {

        View rootView;
        List<PainMedication> mPainMedication;
        MedicationQuestionAdapter mMedicationsAdapter;
        ListView mListView;

        public static MedicationQuestionFragment newInstance(List<PainMedication> medications) {
            MedicationQuestionFragment fragment = new MedicationQuestionFragment();
            fragment.setPainMedications(medications);
            return fragment;
        }
        public void setPainMedications(List<PainMedication> painMedications) {
            this.mPainMedication = painMedications;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //return super.onCreateView(inflater, container, savedInstanceState);
            rootView = inflater.inflate(R.layout.fragment_checkin_medications_questions, container, false);
            ((CheckBox)rootView.findViewById(R.id.checkbox_all_medicines_taken)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    CheckInUtils.getInstance().IsGeneralMedicinesQuestionChecked = isChecked;
                    rootView.findViewById(R.id.list_medicines_question).setVisibility(isChecked ? View.VISIBLE:View.GONE);
                }
            });
            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if(null == mMedicationsAdapter){
                List<MedicationQuestionItem> items = Lists.newArrayList();
                for (PainMedication medication : mPainMedication){
                    MedicationQuestionItem item = new MedicationQuestionItem();
                    item.setMedicationName(medication.getMedicationName());
                    items.add(item);
                }
                mMedicationsAdapter = new MedicationQuestionAdapter(getActivity(),items,true);
            }
            mListView = (ListView) rootView.findViewById(R.id.list_medicines_question);

            if (mListView != null) {
                    mListView.setAdapter(mMedicationsAdapter);
                /*
                mListView.setOnTouchListener(new ListView.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle ListView touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });
                */
            }
        }
    }
}
