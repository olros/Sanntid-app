package com.olros.bussapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

public class BottomNavigationDrawerFragmentMain extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottomsheet_main, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        NavigationView navigation_view = (NavigationView) getView().findViewById(R.id.navigation_view_main);
        navigation_view.setItemIconTintList(null);

        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_menu_arcore:
                        Intent intent1 = new Intent(getContext(), LocationActivity.class);
                        startActivity(intent1);
                        return true;
                    case R.id.nav_menu_kart:
                        Intent intent2 = new Intent(getContext(), MapsActivity.class);
                        startActivity(intent2);
                        return true;
                    default:
                        return true;
                }
            }
        });
    }
}
