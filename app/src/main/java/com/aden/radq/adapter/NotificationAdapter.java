package com.aden.radq.adapter;

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

import com.aden.radq.R;
import com.aden.radq.model.Notification;

import java.util.ArrayList;

public class NotificationAdapter extends ArrayAdapter<Notification> {

    private final ArrayList<Notification> notifications;
    private final Context context;

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
            LinearLayout llNotificationCustom = view.findViewById(R.id.llNotificationCustom);
            TextView tvNotification = view.findViewById(R.id.tvNotification);
            TextView tvTimeStamp = view.findViewById(R.id.tvTimeStamp);

            Notification notificationX = notifications.get(position);

            switch (notificationX.getNotification()) {
                case "aW1va2F5YnV0dG9ucHJlc3NlZA":  //okay_button_pressed_code
                    tvNotification.setText(R.string.aW1va2F5YnV0dG9ucHJlc3NlZA);
                    llNotificationCustom.setBackgroundColor(Color.parseColor("#E57373"));
                    break;
                case "aW1ub3Rva2F5YnV0dG9ucHJlc3NlZA":  //not_okay_button_pressed_code
                    tvNotification.setText(R.string.aW1ub3Rva2F5YnV0dG9ucHJlc3NlZA);
                    llNotificationCustom.setBackgroundColor(Color.parseColor("#E57373"));
                    break;
                case "YnV0dG9ubm90cHJlc3NlZHRpbWVzb3Zlcg":  //button_not_pressed_code
                    tvNotification.setText(R.string.YnV0dG9ubm90cHJlc3NlZHRpbWVzb3Zlcg);
                    llNotificationCustom.setBackgroundColor(Color.parseColor("#E57373"));
                    break;
            }
            tvTimeStamp.setText(notificationX.getTimestamp());
        }
        return view;
    }
}
