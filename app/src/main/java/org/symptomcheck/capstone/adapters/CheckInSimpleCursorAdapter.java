package org.symptomcheck.capstone.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.FeedStatus;
import org.symptomcheck.capstone.model.PainLevel;
import org.symptomcheck.capstone.provider.ActiveContract;

/**
 * Created by igaglioti on 25/02/2015.
 */
public class CheckInSimpleCursorAdapter extends CursorAdapter {

    private Context mContext;
    private int mItemLayout;

    static class ViewHolder {
        // each data item is just a string in this case
        protected TextView vCheckInStatus;
        protected TextView vCheckInTime;
        protected View viewCheckInDetails;
        protected View viewHeaderCheckInDetails;
        protected ListView mListView;
        protected ImageButton mBtnCheckInDetailsInfo;
        protected boolean expand = true;
        protected int originalLayoutBottomHeight;
        protected int originalItemViewHeight;
        protected boolean isFirstToggle = true;
        protected boolean isExpandStatusForced = false;

        public ViewHolder(View v) {
            vCheckInStatus =  (TextView) v.findViewById(R.id.txtViewCheckInPainLevel);
            vCheckInTime = (TextView)  v.findViewById(R.id.txtViewCheckInTime);
            viewCheckInDetails =  v.findViewById(R.id.viewCheckInDetails);
            viewHeaderCheckInDetails =  v.findViewById(R.id.viewHeaderCheckInDetails);
            mListView = (ListView)  v.findViewById(R.id.list_medicines_question);
            mBtnCheckInDetailsInfo = (ImageButton)  v.findViewById(R.id.btnCheckInDetailsInfo);
            //v.setOnClickListener(this);
            mBtnCheckInDetailsInfo.setTag(this);
        }
        
    }
    public CheckInSimpleCursorAdapter(Context context, Cursor c, int layout) {
        super(context, c, false);
        mContext = context;
        mItemLayout = layout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(this.mItemLayout, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final ViewHolder holder  =   (ViewHolder)    view.getTag();
        final CheckIn checkIn = CheckIn.getByUnitId(cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.UNIT_ID)));

        final PainLevel painLevel = Enum.valueOf(PainLevel.class,cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.PAIN_LEVEL)));
        final FeedStatus feedStatus =  Enum.valueOf(FeedStatus.class, cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.FEED_STATUS)));
        //final TextView vCheckInStatus =  (TextView) view.findViewById(R.id.txtViewCheckInPainLevel);
        holder.vCheckInStatus.setText(painLevel + " - " + feedStatus);
        
        
    }
    
    
        /* Example code shows further optimization using ViewHolder Pattern with CursoAdapter
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder  =   (ViewHolder)    view.getTag();
        holder.name.setText(cursor.getString(holder.nameIndex));
        holder.time.setText(cursor.getString(holder.timeIndex));
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup
            p parent) {
        View   view    =   LayoutInflater.from(context).inflate
        p (R.layout.time_row,  null);
        ViewHolder holder  =   new ViewHolder();
        holder.name    =   (TextView)  view.findViewById(R.id.task_name);
        holder.time    =   (TextView)  view.findViewById(R.id.task_time);
        holder.nameIndex   =   cursor.getColumnIndexOrThrow
        p (TaskProvider.Task.NAME);
        holder.timeIndex   =   cursor.getColumnIndexOrThrow
        p (TaskProvider.Task.DATE);
        view.setTag(holder);
        return view;
    }*/
}
