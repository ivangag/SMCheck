package org.symptomcheck.capstone.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
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
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.FeedStatus;
import org.symptomcheck.capstone.model.PainLevel;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.utils.CheckInUtils;
import org.symptomcheck.capstone.utils.Constants;

import java.util.List;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hirondelle.date4j.DateTime;


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
        for(PainMedication medication : mMedicines) {
            CheckInUtils.getInstance().ReportMedicationsResponse.put(medication.getMedicationName(), Constants.STRINGS.EMPTY);
            CheckInUtils.getInstance().ReportMedicationsTakingTime.put(medication.getMedicationName(), Constants.STRINGS.EMPTY);
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
        //getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
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


    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return CheckInQuestionFragment.newInstance();
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

    public static class CheckInQuestionFragment extends Fragment {

        View rootView;
        View medicinesQuestionsView;
        TextView txtMedicineTakingTime;
        FragmentType mFragmentType;
        String mMedicineName;
        public static CheckInQuestionFragment newInstance() {
            CheckInQuestionFragment fragment = new CheckInQuestionFragment();
            return fragment;
        }

        public CheckInQuestionFragment() {

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

            TextView title = (TextView) rootView.findViewById(R.id.txt_check_in_header_question);
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

            /*
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
                            showDateTimePickerDialog(rootView,mMedicineName); //TODO#FDAR_7 DateTime Dialog is shown when Patient select YES button
                        }
                    });
                    final boolean YES = ((RadioButton)medicinesQuestionsView.findViewById(R.id.radioBtnMedicineYES)).isChecked();
                    txtMedicineTakingTime.setVisibility(YES ? View.VISIBLE : View.GONE);
                    txtMedicineTakingTime.setClickable(true);
                    txtMedicineTakingTime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDateTimePickerDialog(rootView,mMedicineName); //TODO#FDAR_7 DateTime Dialog is also shown when Patient select click over the textview allowing to modify the choice
                        }
                    });
                    break;
            }
            */
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
                mMedicationsAdapter = new MedicationQuestionAdapter(getActivity(),items);
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
            final ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = ((Activity) parent.getContext()).getLayoutInflater();
                convertView = inflater.inflate(R.layout.medicines_checkin_list_adapter_item, parent,false);
                holder = new ViewHolder();
                holder.txtMedicineName = (TextView) convertView.findViewById(R.id.txtview_medicine_item);
                holder.txtMedicineTime = (TextView) convertView.findViewById(R.id.txtview_medicine_time);
                holder.checkBox_question = (CheckBox) convertView.findViewById(R.id.checkbox_medicine_taken);
                holder.switch_question = (Switch) convertView.findViewById(R.id.switch_question);
                holder.imageView = (ImageView) convertView.findViewById(R.id.image_drawer_item);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final MedicationQuestionItem item = getItem(position);
            holder.position = position;
            //holder.switch_question.setText(item.getMedicationName());
            holder.txtMedicineName.setText(item.getMedicationName());

            String timeTaken = CheckInUtils.getInstance().ReportMedicationsTakingTime.get(item.getMedicationName());

            if(!timeTaken.equals(Constants.STRINGS.EMPTY)) {
                timeTaken =  DateTime.forInstant(Long.valueOf(timeTaken), TimeZone.getTimeZone("GMT+00")).format("YYYY-MM-DD hh:mm");
            }
            //holder.txtMedicineTime.setText("23/01/2015 12:00");
            holder.txtMedicineTime.setText(timeTaken);

            holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_medicine));
            //holder.imageView.setImageDrawable(mContext.getResources().getDrawable(item.getDrawableResource()));
            holder.checkBox_question.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final boolean YES = holder.checkBox_question.isChecked();
                    if(isChecked){

                        Toast.makeText(buttonView.getContext(),"Medicine " + item.getMedicationName() + " taken",Toast.LENGTH_SHORT).show();
                        //showDateTimePickerDialog(buttonView.getContext(), holder, item.getMedicationName());
                        DatePickerDialogFragment.show(buttonView.getContext(),holder,item.getMedicationName());
                    }
                    holder.txtMedicineTime.setVisibility(YES ? View.VISIBLE : View.GONE);
                }
            });
            return  convertView;
        }

        //TODO#FDAR_7 Interactive used by Patient to enter the Date & Time he/she took the specified medicine
        private void showDateTimePickerDialog(final Context context, final ViewHolder holder, final String medicineName) {
            final Dialog dialog = new Dialog(context);

            dialog.setContentView(R.layout.custom_dialog_datetime);

            dialog.setTitle(String.format("%s",medicineName));

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

                    holder.txtMedicineTime.setText(dateAndTime.toString());

                    CheckInUtils.getInstance().ReportMedicationsTakingTime.put(medicineName, String.valueOf(milliFrom1970GMT));

                    Log.i("CheckInFlow", "milliFrom1970GMT= " + milliFrom1970GMT);
                    //Toast.makeText(getActivity(), "Date: " + date + " Time: " + time, Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                }
            });
        }

        static class ViewHolder {
            CheckBox checkBox_question;
            Switch switch_question;
            TextView txtMedicineName;
            TextView txtMedicineTime;
            ImageView imageView;
            int position;
        }

        static class TimePickerDialogFragment {

            public static void show(final Context context, final ViewHolder holder, final String medicineName
                                    ,final int day, final int month, final int year) {
                new MaterialDialog.Builder(context)
                        .title(String.format("%s",medicineName))
                        //.content(R.string.exit_question)
                        .customView(R.layout.dialog_timepicker,false)
                        .positiveText(R.string.alert_dialog_ok)
                        //.negativeText(R.string.alert_dialog_no)
                        .icon(context.getResources().getDrawable(R.drawable.ic_medicine))
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                String am_pm = "";
                                final TimePicker tp = (TimePicker) dialog.getCustomView().findViewById(R.id.timePickerCheckIn);
                                int h = tp.getCurrentHour();
                                int min = tp.getCurrentMinute();
                                int hour24 = h;
                                if(h>12){
                                    am_pm = "PM";
                                    h = h-12;
                                }else{
                                    am_pm = "AM";
                                }
                                String format = String.format("%d-%02d-%02d %02d:%02d:%02d",year,month,day,hour24,min,0);
                                DateTime dateAndTime = new DateTime(format);

                                long milliFrom1970GMT = dateAndTime.getMilliseconds(TimeZone.getTimeZone("GMT+00"));

                                holder.txtMedicineTime.setText(dateAndTime.toString());

                                CheckInUtils.getInstance().ReportMedicationsTakingTime.put(medicineName, String.valueOf(milliFrom1970GMT));
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }

        static class DatePickerDialogFragment {

            public static void show(final Context context, final ViewHolder holder, final String medicineName) {
                new MaterialDialog.Builder(context)
                        .title(String.format("%s",medicineName))
                                //.content(R.string.exit_question)
                        .customView(R.layout.dialog_datepicker,false)
                        .positiveText(R.string.alert_dialog_ok)
                                //.negativeText(R.string.alert_dialog_no)
                        .icon(context.getResources().getDrawable(R.drawable.ic_medicine))
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                dialog.dismiss();
                                final DatePicker dp = (DatePicker) dialog.getCustomView().findViewById(R.id.datePickerCheckIn);
                                int month = dp.getMonth()+1;
                                int day = dp.getDayOfMonth();
                                int year = dp.getYear();
                                TimePickerDialogFragment.show(context,holder,medicineName,day,month,year);
                            }
                        })
                        .show();
            }
        }
    }
}
