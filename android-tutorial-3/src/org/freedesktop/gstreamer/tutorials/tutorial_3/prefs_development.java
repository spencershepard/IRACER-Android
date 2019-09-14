package org.freedesktop.gstreamer.tutorials.tutorial_3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

public class prefs_development extends PreferenceFragmentCompat  {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.development_prefs, rootKey);

        Preference calibrate_finish = findPreference("cal-finish");
        calibrate_finish.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(Tutorial3.mainactivity)
                        .setTitle("Calibrate Finish Line")
                        .setMessage("Position the vehicle over the pad and press okay to calibrate.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Tutorial3.mainactivity.socketOutString += "U=CAL_0&";
                                Toast.makeText(Tutorial3.mainactivity, "Calibration command sent.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });

        Preference calibrate_gate = findPreference("cal-gate");
        calibrate_gate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(Tutorial3.mainactivity)
                        .setTitle("Calibrate Gate")
                        .setMessage("Position the vehicle over the pad and press okay to calibrate.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Tutorial3.mainactivity.socketOutString += "U=CAL_1&";
                                Toast.makeText(Tutorial3.mainactivity, "Calibration command sent.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });

        Preference calibrate_boost = findPreference("cal-boost");
        calibrate_boost.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(Tutorial3.mainactivity)
                        .setTitle("Calibrate Boost Pad")
                        .setMessage("Position the vehicle over the pad and press okay to calibrate.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Tutorial3.mainactivity.socketOutString += "U=CAL_2&";
                                Toast.makeText(Tutorial3.mainactivity, "Calibration command sent.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });

        Preference calibrate_powerup = findPreference("cal-powerup");
        calibrate_powerup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(Tutorial3.mainactivity)
                        .setTitle("Calibrate Powerup Pad")
                        .setMessage("Position the vehicle over the pad and press okay to calibrate.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Tutorial3.mainactivity.socketOutString += "U=CAL_3&";
                                Toast.makeText(Tutorial3.mainactivity, "Calibration command sent.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });
//        Preference calibrate_default = findPreference("cal-default");
//        calibrate_default.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                new AlertDialog.Builder(Tutorial3.mainactivity)
//                        .setTitle("Revert to default")
//                        .setMessage("Are you sure you want to revert to defaults?  Your calibration data will be lost.")
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                Tutorial3.mainactivity.socketOutString += "U=CAL_DEFAULT&";
//                                Toast.makeText(Tutorial3.mainactivity, "Calibration command sent.", Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .setNegativeButton(android.R.string.no, null).show();
//                return true;
//            }
//        });

    }


}
