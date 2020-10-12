package com.aden.radqcompanionapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aden.radqcompanionapp.R;
import com.aden.radqcompanionapp.model.Notification;

import java.util.ArrayList;

public class NotificationAdapter extends ArrayAdapter<Notification> {

    private ArrayList<Notification> notifications;
    private Context context;

    private TextView tvNotification;
    private TextView tvTimeStamp;

    private LinearLayout llNotificationCustom;

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
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.notifications_custom,parent,false);
            llNotificationCustom = view.findViewById(R.id.llNotificationCustom);
            tvNotification = view.findViewById(R.id.tvNotification);
            tvTimeStamp = view.findViewById(R.id.tvTimeStamp);

            Notification notificationX = notifications.get(position);
            tvNotification.setText(notificationX.getNotification());
            //TODO change this
            if(notificationX.getNotification().toLowerCase().equals("i'm not okay button pressed")){
                llNotificationCustom.setBackgroundColor(Color.parseColor("#E57373"));
            }
            tvTimeStamp.setText(notificationX.getTimestamp());
        }
        return view;
    }
}

