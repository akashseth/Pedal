package com.pedal.app.pedal;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Akash seth on 1/17/2016.
 */
public class CustomAdapterForHistory extends ArrayAdapter<ObjectHistoryItem> {

    Context mContext;
    int layoutResourceId;
    ObjectHistoryItem data[] = null;

    public CustomAdapterForHistory(Context mContext, int layoutResourceId, ObjectHistoryItem data[]) {
        super(mContext, layoutResourceId, data);
        this.mContext = mContext;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = ((Activity) mContext).getLayoutInflater();
        convertView = layoutInflater.inflate(layoutResourceId, parent, false);

        ImageView activityModeIcon = (ImageView) convertView.findViewById(R.id.activityModeImage);
        ImageView expandIcon = (ImageView) convertView.findViewById(R.id.expand);

        TextView activityModeText = (TextView) convertView.findViewById(R.id.activityModeText);
        TextView date = (TextView) convertView.findViewById(R.id.date);
        TextView time = (TextView) convertView.findViewById(R.id.time);
        TextView totalDistance = (TextView) convertView.findViewById(R.id.totalDistance);
        TextView totalTime = (TextView) convertView.findViewById(R.id.totalTime);
        TextView totalFriends = (TextView) convertView.findViewById(R.id.totalFiends);

        ObjectHistoryItem items = data[position];

        activityModeIcon.setImageResource(items.activityMoodIcon);

        activityModeText.setText(items.activityModeText);
        date.setText(items.date);
        time.setText(items.time);
        totalDistance.setText(items.totalDistance);
        totalTime.setText(items.totalTime);
        totalFriends.setText(items.totalFriends);

        return convertView;

    }
}
