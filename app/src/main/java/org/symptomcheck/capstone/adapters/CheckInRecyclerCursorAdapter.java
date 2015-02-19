package org.symptomcheck.capstone.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.FeedStatus;
import org.symptomcheck.capstone.model.PainLevel;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.CheckInUtils;
import org.symptomcheck.capstone.utils.Constants;
import org.symptomcheck.capstone.utils.DateTimeUtils;

/**
 * Created by igaglioti on 09/02/2015.
 */
public class CheckInRecyclerCursorAdapter extends CursorRecyclerAdapter<CheckInRecyclerCursorAdapter.ViewHolder> {

    IRecyclerItemToggleListener mListener;

    public static interface IRecyclerItemToggleListener{
        void onItemToggled(int position);
    }
    
    public void addEventListener(IRecyclerItemToggleListener listener){
        mListener = listener;
    }
    
    private final Context mContext;

    public CheckInRecyclerCursorAdapter(Cursor cursor, Context context) {
        super(cursor);
        this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                lastPosition = -1;
            }
        });
        mContext = context;
    }

    

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        protected TextView vCheckInStatus;
        protected TextView vCheckInTime;
        protected View viewCheckInDetails;
        protected View viewHeaderCheckInDetails;
        protected ListView mListView;
        protected ImageButton mBtnCheckInDetailsInfo;
        protected boolean isShown = true;
        public ViewHolder(View v) {
            super(v);
            vCheckInStatus =  (TextView) v.findViewById(R.id.txtViewCheckInPainLevel);
            vCheckInTime = (TextView)  v.findViewById(R.id.txtViewCheckInTime);
            //viewCheckInDetails = (View)  v.findViewById(R.id.viewCheckInDetails);
            viewHeaderCheckInDetails = (View)  v.findViewById(R.id.viewHeaderCheckInDetails);
            mListView = (ListView)  v.findViewById(R.id.list_medicines_question);
            mBtnCheckInDetailsInfo = (ImageButton)  v.findViewById(R.id.btnCheckInDetailsInfo);
        }
        
    }


//    @Override
//    public void onContentChanged(){
//        super.onContentChanged();
//        lastPosition = -1;
//    }
    
    private int lastPosition = -1;
    @Override
    public void onBindViewHolderCursor(final ViewHolder holder, Cursor cursor) {

        Animation slide = AnimationUtils.loadAnimation(mContext, (cursor.getPosition() > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        //Animation animation = AnimationUtils.loadAnimation(getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);        
        //holder.itemView.startAnimation(slide);

        lastPosition = cursor.getPosition();
        final CheckIn checkIn = CheckIn.getByUnitId(cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.UNIT_ID)));
        //final int checkInId = cursor.getInt(ID_COLUMN);
        //card.setId(""+ checkInId);
        final PainLevel painLevel = Enum.valueOf(PainLevel.class,cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.PAIN_LEVEL)));
        final FeedStatus feedStatus =  Enum.valueOf(FeedStatus.class, cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.FEED_STATUS)));
        holder.vCheckInStatus.setText(painLevel + " - " + feedStatus);
        if(checkIn != null) {
            //if(holder.mListView != null) {
                holder.mListView.setAdapter(new MedicationQuestionAdapter(mContext, MedicationQuestionItem.makeItemByCheckinQuestions(checkIn.getItemsQuestion()), false));
            //}
            holder.vCheckInTime.setText("Submitted on " + DateTimeUtils.convertEpochToHumanTime(checkIn.getIssueDateTime(), Constants.TIME.DEFAULT_FORMAT));

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
        switch(painLevel)
        {
            case MODERATE:
                holder.vCheckInStatus.setTextColor(CheckInUtils.SM_CHECKIN_COLORS[1]);
                break;
            case SEVERE:
                holder.vCheckInStatus.setTextColor(CheckInUtils.SM_CHECKIN_COLORS[2]);
                break;
            case WELL_CONTROLLED:
                holder.vCheckInStatus.setTextColor(CheckInUtils.SM_CHECKIN_COLORS[0]);
            case UNKNOWN:
                break;
        }

        holder.mBtnCheckInDetailsInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ViewGroup.LayoutParams p = holder.itemView.getLayoutParams();
//                if(holder.isShown){
//                    p.height -= 260;
//
//                }else{
//                    p.height += 260;
//                }
//                holder.itemView.requestLayout();
//                holder.isShown = !holder.isShown;
                //holder.viewCheckInDetails.setVisibility(holder.viewCheckInDetails.getVisibility()
                holder.mListView.setVisibility(holder.mListView.getVisibility()
                        == View.GONE ? View.VISIBLE :View.GONE );

//                if(mListener != null
                        //&& holder.viewCheckInDetails.getVisibility() == View.VISIBLE){
                    //mListener.onItemToggled(holder.getPosition());
//                }
            }
        });
        //card.resourceIdThumb=R.drawable.ic_check_in;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public CheckInRecyclerCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_google_cardview_checkins, parent, false);
        // set the view's size, margins, paddings and layout parameters        
        v.setClickable(true);
        return new ViewHolder(v);
    }
}
