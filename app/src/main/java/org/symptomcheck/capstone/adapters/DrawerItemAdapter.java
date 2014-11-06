package org.symptomcheck.capstone.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.symptomcheck.capstone.R;

import java.util.List;

/**
 * Created by igaglioti on 05/11/2014.
 */
public class DrawerItemAdapter extends BaseAdapter {

    private final Context mContext;
    private List<DrawerItem> mItems;

    public DrawerItemAdapter(Context context, List<DrawerItem> items){
        mContext = context;
        mItems = items;
    }
    @Override
    public int getCount() {
        return (mItems != null ? mItems.size() : 0);
    }

    @Override
    public DrawerItem getItem(int i) {
        return (mItems != null ? mItems.get(i) : null);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) viewGroup.getContext()).getLayoutInflater();
            convertView = inflater.inflate(R.layout.drawer_list_adapter_item, viewGroup,false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.txtview_drawer_item);
            holder.imageView = (ImageView) convertView.findViewById(R.id.image_drawer_item);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.position = position;
        DrawerItem item = getItem(position);
        holder.text.setText(item.getTextTitle());
        //holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_doctor));
        holder.imageView.setImageDrawable(mContext.getResources().getDrawable(item.getDrawableResource()));
        return  convertView;
    }

    static class ViewHolder {
        TextView text;
        ImageView imageView;
        int position;
    }
}
