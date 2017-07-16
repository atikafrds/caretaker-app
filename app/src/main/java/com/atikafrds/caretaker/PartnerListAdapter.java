package com.atikafrds.caretaker;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by t-atika.firdaus on 06/07/17.
 */

public class PartnerListAdapter extends ArrayAdapter<User> {
    private ArrayList<User> userData;
    private Context context;
    private int resource;
    private View view;

    public PartnerListAdapter(Context context, int resource, ArrayList<User> userData) {
        super(context, resource, userData);
        this.context = context;
        this.resource = resource;
        this.userData = userData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(resource, parent, false);

        User user = userData.get(position);

        TextView partnerName = (TextView) view.findViewById(R.id.partnerNameinList);
        TextView partnerPhoneNumber = (TextView) view.findViewById(R.id.partnerPhoneNumberinList);
        partnerName.setText(user.getFullname());
        partnerPhoneNumber.setText(user.getPhoneNumber());

        return view;
    }
}