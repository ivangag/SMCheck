package org.symptomcheck.capstone.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.Constants;
import org.symptomcheck.capstone.utils.DateTimeUtils;

/**
 * Created by igaglioti on 09/02/2015.
 */
public class CheckInRecyclerCursorAdapter extends CursorRecyclerAdapter<CheckInRecyclerCursorAdapter.ViewHolder> {

    public CheckInRecyclerCursorAdapter(Cursor cursor) {
        super(cursor);
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        protected TextView vName;
        protected TextView vSurname;
        public ViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.txtName);
            vSurname = (TextView)  v.findViewById(R.id.txtSurname);
        }
    }

    @Override
    public void onBindViewHolderCursor(ViewHolder holder, Cursor cursor) {

        final CheckIn checkIn = CheckIn.getByUnitId(cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.UNIT_ID)));
        //final int checkInId = cursor.getInt(ID_COLUMN);
        //card.setId(""+ checkInId);
        holder.vName.setText(cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.PAIN_LEVEL))
                + " - " + cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.FEED_STATUS)))
        ;
        if(checkIn != null) {
            holder.vSurname.setText("Submitted on " + DateTimeUtils.convertEpochToHumanTime(checkIn.getIssueDateTime(), Constants.TIME.DEFAULT_FORMAT));

            /*
            final Patient patient = Patient.getByMedicalNumber(mPatientOwner.getMedicalRecordNumber());
            card.mainHeader = patient.getFirstName() + " " + patient.getLastName() + " " + getString(R.string.checkin_header);


            switch (checkIn.getIssuePainLevel()){
                case UNKNOWN:
                case WELL_CONTROLLED:
                    card.resourceIdAlertIcon = R.drawable.ic_alert_green;
                    card.resourceIdMainTextColor = getResources().getColor(R.color.card_background_green);
                    break;
                case MODERATE:
                    card.resourceIdAlertIcon = R.drawable.ic_alert_orange;
                    card.resourceIdMainTextColor = getResources().getColor(R.color.card_background_orange);
                    break;
                case SEVERE:
                    card.resourceIdAlertIcon = R.drawable.ic_alert_red;
                    card.resourceIdMainTextColor = getResources().getColor(R.color.card_background_red);
                    break;
            }
            */
        }else{
            //card.resourceIdAlertIcon = R.drawable.ic_alert_green;
        }
        //card.resourceIdThumb=R.drawable.ic_check_in;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public CheckInRecyclerCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_google_cardview_checkins, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }
}
