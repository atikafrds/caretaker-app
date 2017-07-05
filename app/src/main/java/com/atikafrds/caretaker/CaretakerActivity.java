package com.atikafrds.caretaker;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class CaretakerActivity extends AppCompatActivity {
    private static final String SELECTED_ITEM = "arg_selected_item";
    public static final String TAG = CaretakerActivity.class.getSimpleName();

    private BottomNavigationView bottomNavigation;
    private int mSelectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caretaker);

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

        // update selected item
        mSelectedItem = item.getItemId();
        switch (mSelectedItem) {
            case 0:
                item.setIcon(R.drawable.map_blue);
                break;
            case 1:
                item.setIcon(R.drawable.partner_blue);
                break;
            case 2:
                item.setIcon(R.drawable.notification_blue);
                break;
            case 3:
                item.setIcon(R.drawable.profile_blue);
                break;
        }

        // uncheck the other items.
        for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
            MenuItem menuItem = bottomNavigation.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == mSelectedItem);
        }

//        updateToolbarText(item.getTitle());

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.commit();
        }
    }

//    private void updateToolbarText(CharSequence text) {
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setTitle(text);
//        }
//    }
}