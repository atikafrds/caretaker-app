package com.atikafrds.caretaker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class CaretakerActivity extends AppCompatActivity {
    private static final String SELECTED_ITEM = "arg_selected_item";
    public static final String TAG = CaretakerActivity.class.getSimpleName();
    public static String partnerId, currentUserId;
    public static UserRole userRole;

    private BottomNavigationView bottomNavigation;
    private int mSelectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        partnerId = getIntent().getStringExtra("partnerId");
        currentUserId = getIntent().getStringExtra("currentUserId");
        userRole = getIntent().getStringExtra("userRole").equals("DEVICE_USER") ? UserRole.DEVICE_USER : UserRole.CARETAKER;

        if (userRole == UserRole.DEVICE_USER) {
            setContentView(R.layout.activity_user);
        } else {
            setContentView(R.layout.activity_caretaker);
        }

        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

        MenuItem selectedItem;
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            selectedItem = bottomNavigation.getMenu().findItem(mSelectedItem);
        } else {
            selectedItem = bottomNavigation.getMenu().getItem(0);
        }
        selectFragment(selectedItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, 0);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        MenuItem homeItem = bottomNavigation.getMenu().getItem(0);
        if (mSelectedItem != homeItem.getItemId()) {
            selectFragment(homeItem);
        } else {
            super.onBackPressed();
        }
    }

    private void selectFragment(MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.menu_home:
                fragment = MapFragment.newInstance();
                break;
            case R.id.menu_partner:
                fragment = PartnerFragment.newInstance();
                break;
            case R.id.menu_notifications:
                fragment = NotificationFragment.newInstance();
                break;
            case R.id.menu_profile:
                fragment = ProfileFragment.newInstance();
                break;
        }

        // uncheck the other items.
        for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
            MenuItem menuItem = bottomNavigation.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == mSelectedItem);
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.commit();
        }
    }
}