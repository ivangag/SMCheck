package org.symptomcheck.capstone.adapters;

import android.app.Activity;
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

    private String[] mItems;

    public DrawerItemAdapter(String[] items){
        mItems = items;
    }
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
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
            convertView = inflater.inflate(R.layout.drawer_list_adapter_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.image_drawer_item);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.position = position;
        holder.text.setText(mItems[position]);
        //holder.imageView.setImageDrawable(R.drawable.ic_doctor);
        return  convertView;
    }

    static class ViewHolder {
        TextView text;
        ImageView imageView;
        int position;
    }
}
