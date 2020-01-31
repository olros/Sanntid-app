package com.olros.bussapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.Map;

public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottomsheet, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        NavigationView navigation_view = (NavigationView) getView().findViewById(R.id.navigation_view);

        Menu menu = navigation_view.getMenu();

        SharedPreferences pref = getContext().getSharedPreferences("Favourites", 0);
        Map<String, ?> allEntries = pref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            int id = Integer.parseInt(entry.getKey());
            String navn = entry.getValue().toString();
            menu.add(
                    0,
                    id,
                    0,
                    navn
            ).setIcon(R.drawable.ic_star_border_black);
        }

        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_fav_title:
                        //Toast.makeText(BusActivity.this, "Toggle click", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        String navn = item.getTitle().toString();
                        String id = String.valueOf(item.getItemId());
                        Intent intent = new Intent(getContext(), BusActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("navn", navn);
                        intent.putExtra("holdeplass", id);
                        getActivity().finish();
                        startActivity(intent);
                        return true;
                }
            }
        });
    }
}
