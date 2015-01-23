package org.symptomcheck.capstone.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.common.collect.Lists;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.FeedStatus;
import org.symptomcheck.capstone.model.PainLevel;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hirondelle.date4j.DateTime;
import it.gmariotti.cardslib.library.view.CardListView;


public class SamplePagerSlidingActivity extends ActionBarActivity {


    @InjectView(R.id.app_bar)
    Toolbar toolbar;
    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @InjectView(R.id.pager)
    ViewPager pager;

    private MyPagerAdapter adapter;
    private Drawable oldBackground = null;
    private int currentColor;

    List<PainMedication> mMedicines = Lists.newArrayList();
    private UserInfo mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_flow_material);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mUser = DAOManager.get().getUser();

        mMedicines = PainMedication.getAll(mUser.getUserIdentification());
        for(PainMedication medication : mMedicines){
            mReportMedicationsResponse.put(medication.getMedicationName(), Constants.STRINGS.EMPTY);
            mReportMedicationsTakingTime.put(medication.getMedicationName(), Constants.STRINGS.EMPTY);
        }


        adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        tabs.setViewPager(pager);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);


        tabs.setOnTabReselectedListener(new PagerSlidingTabStrip.OnTabReselectedListener() {
            @Override
            public void onTabReselected(int position) {
                Toast.makeText(SamplePagerSlidingActivity.this, "Tab reselected: " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()) {
            case R.id.action_contact:
                QuickContactFragment.newInstance().show(getSupportFragmentManager(), "QuickContactFragment");
                return true;
        }*/
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
    public final static String NO = "NO";
    public final static String YES = "YES";

    private Map<String,String> mReportMedicationsResponse = new HashMap<String, String>(){};
    private Map<String,String> mReportMedicationsTakingTime = new HashMap<String, String>(){};
    private PainLevel mReportPainLevel = PainLevel.UNKNOWN;
    private FeedStatus mReportFeedStatus = FeedStatus.UNKNOWN;


    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            /*
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
            */
            if (position == 0) {
                return CheckInQuestionFragment.newInstance(position + 1, FragmentType.FRAGMENT_TYPE_PAIN_LEVEL);
            }else{
                return MedicationQuestionFragment.newInstance(mMedicines);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            final int totalMedicines = mMedicines.size();
            //return 1 + totalMedicines + 1;
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            String title = null;
            /*
            if (position == 0) {
                title = getString(R.string.pain_status).toUpperCase(l);
            } else if (position == getCount() - 1) {
                title = getString(R.string.feed_status).toUpperCase(l);
            } else {
                if (mMedicines.size() > 0) {
                    title = mMedicines.get(position - 1).getMedicationName();
                }
            }
            */
            if(position == 0){
                title = getString(R.string.pain_status);
            }else {
                title = getString(R.string.medicines_header);
            }
            return title;
        }

    }

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

            final SamplePagerSlidingActivity parentActivity = ((SamplePagerSlidingActivity)getActivity());
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
                String timeTaken = ((SamplePagerSlidingActivity) getActivity()).mReportMedicationsTakingTime.get(mMedicineName);
                final boolean YES = ((RadioButton)medicinesQuestionsView.findViewById(R.id.radioBtnMedicineYES)).isChecked();
                txtMedicineTakingTime.setVisibility(YES ? View.VISIBLE : View.GONE);
                if(!timeTaken.equals(Constants.STRINGS.EMPTY)) {
                    timeTaken =  DateTime.forInstant(Long.valueOf(timeTaken), TimeZone.getTimeZone("GMT+00")).format("YYYY-MM-DD hh:mm");
                }
                txtMedicineTakingTime.setText(timeTaken);
            }
            super.onResume();
        }

        //TODO#FDAR_7 Interactive used by Patient to enter the Date & Time he/she took the specified medicine
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

                    ((SamplePagerSlidingActivity)getActivity()).mReportMedicationsTakingTime.put(CheckInQuestionFragment.this.mMedicineName, String.valueOf(milliFrom1970GMT));

                    Log.i("CheckInFlow", "milliFrom1970GMT= " + milliFrom1970GMT);
                    //Toast.makeText(getActivity(), "Date: " + date + " Time: " + time, Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                }
            });
        }
    }

    public static class SuperAwesomeCardFragment extends  Fragment{

        public static Fragment newInstance(int position) {
            return new SuperAwesomeCardFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_check_in_flow,container,false);
            ButterKnife.inject(this, rootView);
            ViewCompat.setElevation(rootView, 50);
            //textView.setText("CARD "+position);
            return rootView;
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
            rootView = inflater.inflate(R.layout.fragment_checkin_questions, container, false);
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
                mMedicationsAdapter = new MedicationQuestionAdapter(getActivity(),items);
            }
            mListView = (ListView) rootView.findViewById(R.id.list_questions);

            if (mListView != null) {
                mListView.setAdapter(mMedicationsAdapter);
            }
        }
    }

    static class MedicationQuestionItem {
        private String medicationName;
        private String medicationTakingTime;
        private String medicationTaken;

        public String getMedicationName() {
            return medicationName;
        }

        public void setMedicationName(String medicationName) {
            this.medicationName = medicationName;
        }

        public String getMedicationTakingTime() {
            return medicationTakingTime;
        }

        public void setMedicationTakingTime(String medicationTakingTime) {
            this.medicationTakingTime = medicationTakingTime;
        }

        public String getMedicationTaken() {
            return medicationTaken;
        }

        public void setMedicationTaken(String medicationTaken) {
            this.medicationTaken = medicationTaken;
        }

    }

    static class MedicationQuestionAdapter extends BaseAdapter{
        private final Context mContext;
        private List<MedicationQuestionItem> mMedications;

        MedicationQuestionAdapter(Context mContext, List<MedicationQuestionItem> medications) {
            this.mContext = mContext;
            this.mMedications = medications;
        }

        @Override
        public int getCount() {
            return mMedications.size();
        }

        @Override
        public MedicationQuestionItem getItem(int position) {
            return mMedications.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mMedications.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = ((Activity) parent.getContext()).getLayoutInflater();
                convertView = inflater.inflate(R.layout.drawer_list_adapter_item, parent,false);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.txtview_drawer_item);
                holder.question_switch = (Switch) convertView.findViewById(R.id.switch_question);
                holder.imageView = (ImageView) convertView.findViewById(R.id.image_drawer_item);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.position = position;
            MedicationQuestionItem item = getItem(position);
            //holder.question_switch.setText(item.getMedicationName());
            holder.text.setText("Did you take " + item.getMedicationName() + "?");
            holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_medicine));
            //holder.imageView.setImageDrawable(mContext.getResources().getDrawable(item.getDrawableResource()));
            return  convertView;
        }


        static class ViewHolder {
            Switch question_switch;
            TextView text;
            ImageView imageView;
            int position;
        }
    }
}
