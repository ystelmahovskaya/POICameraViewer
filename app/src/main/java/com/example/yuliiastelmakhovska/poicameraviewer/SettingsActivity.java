package com.example.yuliiastelmakhovska.poicameraviewer;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import java.text.ParseException;
import java.util.List;

import static com.example.yuliiastelmakhovska.poicameraviewer.FullscreenMainActivity.controller;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            preference.setSummary("");

            return true;
        }
    };


    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }


    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }


    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FullscreenMainActivity.lastLocation = null;
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || DBPreferenceFragment.class.getName().equals(fragmentName)
                || PlacesPreferenceFragment.class.getName().equals(fragmentName);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PlacesPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_places);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("listPlaces"));
            bindPreferenceSummaryToValue(findPreference("listPlaces"));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                FullscreenMainActivity.lastLocation = null;
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FavoritesPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_places);
            setHasOptionsMenu(true);
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                FullscreenMainActivity.lastLocation = null;
                return true;
            }
            return super.onOptionsItemSelected(item);
        }


    }


    public static class DBPreferenceFragment extends PreferenceFragment {
        SharedPreferences prefs;
        SharedPreferences.OnSharedPreferenceChangeListener listener;
        SwitchPreference showFavourites;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_favorites);
            showFavourites= (SwitchPreference) findPreference("show_favourites");
            setHasOptionsMenu(true);
            prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                    if (key.equals("checkboxPref")) {
                        boolean deleteAll = prefs.getBoolean("checkboxPref", false);
                        if (deleteAll) {
                            controller.repo.deleteAll();
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                            editor.putBoolean("checkboxPref", false);
                            Log.i("deleteAll", "in");
                            editor.commit();

                        }
                    }
                    if (key.equals("show_favourites")) {
                        boolean test = prefs.getBoolean("show_favourites", false);
                       Log.i("show_favourites",""+test);
                        if (test) {
                            showFavourites.setSummary("Enabled");
                            try {
                                controller.setFavorites();
                                FullscreenMainActivity.lastLocation = null;
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            showFavourites.setSummary("Disabled");
                            controller.disableFavorites();
                            FullscreenMainActivity.lastLocation = null;
                        }
                    }
                }
            };
            prefs.registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                FullscreenMainActivity.lastLocation = null;
                return true;
            }
            return super.onOptionsItemSelected(item);


        }
    }
}