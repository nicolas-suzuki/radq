package com.aden.radqcompanionapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aden.radqcompanionapp.R;
import com.aden.radqcompanionapp.model.Notification;

import java.util.ArrayList;

public class NotificationAdapter extends ArrayAdapter<Notification> {

    private ArrayList<Notification> notifications;
    private Context context;

    public NotificationAdapter(@NonNull Context c, @NonNull ArrayList<Notification> objects) {
        super(c, 0 , objects);
        this.notifications = objects;
        this.context = c;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = null;

        if(notifications != null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.notifications_custom,parent,false);
            TextView notification = (TextView) view.findViewById(R.id.tvNotification);
            TextView timeStamp = (TextView) view.findViewById(R.id.tvTimeStamp);

            Notification notificationX = notifications.get(position);
            notification.setText(notificationX.getNotification());
            timeStamp.setText(notificationX.getTimestamp());
        }
        return view;
    }
}
