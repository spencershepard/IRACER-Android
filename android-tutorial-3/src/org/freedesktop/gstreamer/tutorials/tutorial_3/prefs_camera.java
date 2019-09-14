package org.freedesktop.gstreamer.tutorials.tutorial_3;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class prefs_camera extends PreferenceFragmentCompat  {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.camera_prefs, rootKey);
    }
}
