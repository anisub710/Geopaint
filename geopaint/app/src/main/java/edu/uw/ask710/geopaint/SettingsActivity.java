package edu.uw.ask710.geopaint;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Anirudh Subramanyam on 11/11/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    public static class SettingsFragment extends PreferenceFragment {
        public static final String PREF_PEN_KEY = "pref_pen";
        public static final String PREF_COLOR = "pref_color";
        public static final String TAG = "FRAGMENT";
        private boolean defaultVal = false;
        private int color;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.getBoolean(PREF_PEN_KEY, false);
            sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    defaultVal = !defaultVal;
                    sharedPreferences.getBoolean(PREF_PEN_KEY, defaultVal);
                }
            });


        }

    }
}


