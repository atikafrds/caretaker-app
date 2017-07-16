package com.atikafrds.caretaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by t-atika.firdaus on 15/07/17.
 */

public class NotificationAdapter extends ArrayAdapter<Notification> {
    private ArrayList<Notification> notifications;
    private Context context;
    private int resource;
    private View view;

    public NotificationAdapter(Context context, int resource, ArrayList<Notification> notifications) {
        super(context, resource, notifications);
        this.context = context;
        this.resource = resource;
        this.notifications = notifications;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(resource, parent, false);

        Notification notification = notifications.get(position);

        TextView notifSender = (TextView) view.findViewById(R.id.notification_sender);
        TextView notifLocation = (TextView) view.findViewById(R.id.notification_location);
        TextView notifTimestamp = (TextView) view.findViewById(R.id.notification_timestamp);
        notifSender.setText(notification.getUserName());
        notifLocation.setText(notification.getKnownAddress());
        notifTimestamp.setText(notification.getDate().toString());

        return view;
    }
}