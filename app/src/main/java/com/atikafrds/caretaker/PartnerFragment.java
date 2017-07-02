package com.atikafrds.caretaker;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by t-atika.firdaus on 22/06/17.
 */

public class PartnerFragment extends Fragment {
    public static PartnerFragment newInstance() {
        PartnerFragment fragment = new PartnerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.partner_fragment, container, false);
    }
}
