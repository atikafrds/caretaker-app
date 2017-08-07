package com.atikafrds.caretaker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

        final Notification notification = notifications.get(position);

        TextView notifSender = (TextView) view.findViewById(R.id.notification_sender);
        TextView notifLocation = (TextView) view.findViewById(R.id.notification_location);
        TextView notifTimestamp = (TextView) view.findViewById(R.id.notification_timestamp);
        Button openLocButton = (Button) view.findViewById(R.id.openLocation);
        openLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = notification.getUserName() + " location";
                String uriBegin = "geo:" + Double.toString(notification.getLat()) + "," + Double.toString(notification.getLng());
                String query = Double.toString(notification.getLat()) + "," + Double.toString(notification.getLng()) + "(" + label + ")";
                String encodedQuery = Uri.encode(query);
                String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                Uri uri = Uri.parse(uriString);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                getContext().startActivity(intent);
            }
        });
        notifSender.setText(notification.getUserName());
        notifLocation.setText(notification.getKnownAddress());
        Date date = new Date(notification.getTimestamp().getTime());
        Format format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        notifTimestamp.setText(format.format(date));

        return view;
    }

    public static String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("dd MM yyyy HH:mm");
        return format.format(date);
    }
}