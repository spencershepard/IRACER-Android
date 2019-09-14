package org.freedesktop.gstreamer.tutorials.tutorial_3;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class prefs_connection extends PreferenceFragmentCompat  {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.connection_prefs, rootKey);

        Preference connect = findPreference("connect");
        connect.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                if(Tutorial3.vehicle_connected){
                    Tutorial3.mainactivity.disconnectFromCar();
                }
                else Tutorial3.mainactivity.connectToCar();
                return true;
            }
        });

    }

}



