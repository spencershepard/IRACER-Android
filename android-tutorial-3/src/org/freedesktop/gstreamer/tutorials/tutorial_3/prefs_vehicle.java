package org.freedesktop.gstreamer.tutorials.tutorial_3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

public class prefs_vehicle extends PreferenceFragmentCompat  {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.vehicle_prefs, rootKey);

        Preference shutdown = findPreference("shutdown");
        shutdown.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                shutdown();
                return true;
            }
        });



        Preference reboot = findPreference("reboot");
        reboot.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                reboot();
                return true;
            }
        });
    }

    public void shutdown() {
        if (Tutorial3.mainactivity.vehicle_connected) {
            new AlertDialog.Builder(Tutorial3.mainactivity)
                    .setTitle("Shutdown")
                    .setMessage("Wait for the vehicle's lights to go off, then disconnect the battery.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Tutorial3.mainactivity.socketOutString += "U=S&";
                            Toast.makeText(Tutorial3.mainactivity, "Shutdown command sent.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        } else {
            new AlertDialog.Builder(Tutorial3.mainactivity)
                    .setTitle("Not Connected")
                    .setMessage("Not currently connected.  Would you like to connect now?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Tutorial3.mainactivity.connectToCar();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();

        }

    }

    public void reboot() {
        if (Tutorial3.mainactivity.vehicle_connected) {
            new AlertDialog.Builder(Tutorial3.mainactivity)
                    .setTitle("Reboot")
                    .setMessage("Do you really want to reboot?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Tutorial3.mainactivity.socketOutString += "U=R&";
                            Toast.makeText(Tutorial3.mainactivity, "Reboot command sent.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        } else {
            new AlertDialog.Builder(Tutorial3.mainactivity)
                    .setTitle("Not Connected")
                    .setMessage("Not currently connected.  Would you like to connect now?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Tutorial3.mainactivity.connectToCar();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();

        }

    }
}
