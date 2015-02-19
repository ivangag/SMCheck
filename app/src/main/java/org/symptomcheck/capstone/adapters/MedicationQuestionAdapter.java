package org.symptomcheck.capstone.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.model.Question;
import org.symptomcheck.capstone.utils.CheckInUtils;
import org.symptomcheck.capstone.utils.Constants;

import java.util.List;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * Created by igaglioti on 16/02/2015.
 */
public class MedicationQuestionAdapter extends BaseAdapter {
    private final Context mContext;
    private final boolean mHandleCheckInFlow;
    private List<MedicationQuestionItem> mMedications;

    
    public MedicationQuestionAdapter(Context mContext, List<MedicationQuestionItem> medications, boolean handleCheckInFlow) {
        this.mContext = mContext;
        this.mMedications = medications;
        this.mHandleCheckInFlow = handleCheckInFlow;
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
            holder.txtMedicineName = (TextView) convertView.findViewById(R.id.txtView_medicine_item);
            holder.txtMedicineTime = (TextView) convertView.findViewById(R.id.txtView_medicine_time);
            holder.checkBox_question = (CheckBox) convertView.findViewById(R.id.checkbox_medicine_taken);
            holder.switch_question = (Switch) convertView.findViewById(R.id.switch_question);
            holder.imageView = (ImageView) convertView.findViewById(R.id.image_drawer_item);

            convertView.setTag(holder);
            
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final MedicationQuestionItem item = getItem(position);
        holder.position = position;
        holder.txtMedicineName.setText(item.getMedicationName());
        String timeTaken = Constants.STRINGS.EMPTY;
        if(mHandleCheckInFlow) {
            timeTaken = CheckInUtils.getInstance().ReportMedicationsTakingTime.get(item.getMedicationName());

            if (!timeTaken.equals(Constants.STRINGS.EMPTY)) {
                timeTaken = DateTime.forInstant(Long.valueOf(timeTaken), TimeZone.getTimeZone("GMT+00")).format("YYYY-MM-DD hh:mm");
            }

            holder.checkBox_question.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final boolean YES = holder.checkBox_question.isChecked();
                    if (isChecked) {
                        CheckInUtils.getInstance().ReportMedicationsResponse.put(item.getMedicationName(), Constants.STRINGS.YES);
                        Toast.makeText(buttonView.getContext(), "Medicine " + item.getMedicationName() + " taken", Toast.LENGTH_SHORT).show();
                        DatePickerDialogFragment.show(buttonView.getContext(), holder, item.getMedicationName());
                    }
                    holder.txtMedicineTime.setVisibility(YES ? View.VISIBLE : View.GONE);
                }
            });
        }else{
            final boolean isTaken = item.IsTaken();
            holder.checkBox_question.setClickable(false);
            holder.checkBox_question.setChecked(isTaken);
            holder.txtMedicineTime.setVisibility(isTaken ? View.VISIBLE : View.GONE);
            if(isTaken){
                //timeTaken = DateTime.forInstant(Long.valueOf(item.getMedicationTakingTime()), TimeZone.getTimeZone("GMT+00")).format("YYYY-MM-DD hh:mm");
                timeTaken = item.getMedicationTakingTime();
            }
        }
        holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_medicine));
        holder.txtMedicineTime.setText(timeTaken);
        return  convertView;
    }


    static class ViewHolder {
        CheckBox checkBox_question;
        Switch switch_question;
        TextView txtMedicineName;
        TextView txtMedicineTime;
        ImageView imageView;
        int position;
    }
    //TODO#FDAR_7 Interactive used by Patient to enter the Date & Time he/she took the specified medicine
    static class TimePickerDialogFragment {

        public static void show(final Context context, final ViewHolder holder, final String medicineName
                ,final int day, final int month, final int year) {
            new MaterialDialog.Builder(context)
                    .title(String.format("%s - Set Time",medicineName))
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

                            final long milliFrom1970GMT = dateAndTime.getMilliseconds(TimeZone.getTimeZone("GMT+00"));

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
                    .title(String.format("%s - Set Date",medicineName))
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
