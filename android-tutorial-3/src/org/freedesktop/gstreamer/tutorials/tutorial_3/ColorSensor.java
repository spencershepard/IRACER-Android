package org.freedesktop.gstreamer.tutorials.tutorial_3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.ArrayList;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;
import static org.freedesktop.gstreamer.tutorials.tutorial_3.Tutorial3.mainactivity;

public class ColorSensor {
    public static double tolerance = 0.01;
    public static double hue_tolerance = 2;
    public static double saturation_tolerance = 2;

    public static ArrayList<ColorSensor> sensors = new ArrayList<>();
    public int red;
    public int blue;
    public int green;

    public float hue;
    public float saturation;
    public float luminance;


    public ColorSensor(){
        sensors.add(this);
    }

    static boolean match_color_ratio = true;

    public Boolean matchWithRatio(int _red, int _green, int _blue){  //function to compare actual color sensor data with our instances
        if(match_color_ratio) {
            int sum = red + green + blue;
            float red_ratio = ((float) red / (float) sum);
            float green_ratio = ((float) green / (float) sum);
            float blue_ratio = ((float) blue / (float) sum);
            int _sum = _red + _green + _blue;
            float _red_ratio = ((float) _red / (float) _sum);
            float _green_ratio = ((float) _green / (float) _sum);
            float _blue_ratio = ((float) _blue / (float) _sum);

            //if(red_ratio == _red_ratio && green_ratio == _green_ratio && blue_ratio == _blue_ratio)return true;
            if (Math.abs(red_ratio - _red_ratio) < tolerance && Math.abs(green_ratio - _green_ratio) < tolerance && Math.abs(blue_ratio - _blue_ratio) < tolerance)
                return true;
            else return false;
        }
        else {
            if(Math.abs(red -_red) < tolerance && Math.abs(green -_green) < tolerance && Math.abs(blue - _blue) < tolerance)return true;
            else return false;
        }
    }

    public boolean matchHSV(float hsv[]){
        if (Math.abs(hsv[0] - hue) < hue_tolerance && Math.abs(hsv[1] - saturation) < saturation_tolerance){
            return true;
        }
        else return false;
    }

    public void calibrate(int [] colors){
        red = colors[0];
        green = colors[1];
        blue = colors[2];

        final String toaststring = "Calibrated with " + red + " " + green + " " + blue;
        final Activity toastcontext = (Tutorial3) mainactivity;
        toastcontext.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(toastcontext, toaststring, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void calibrateHSV(float [] hsv){
        hue = hsv[0];
        saturation = hsv[1];
        luminance = hsv[2];

        final String toaststring = "Calibrated with " + hue + " " + saturation;
        final Activity toastcontext = (Tutorial3) mainactivity;
        toastcontext.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(toastcontext, toaststring, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void save(String name){
        SharedPreferences sharedPref = getDefaultSharedPreferences(Tutorial3.mainactivity);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(name + "_calibration_r", Integer.toString(red));
        editor.putString(name + "_calibration_g", Integer.toString(green));
        editor.putString(name + "_calibration_b", Integer.toString(blue));
        editor.commit();
    }

    public static int [] load(String name){
        int [] colors = new int[3];
        String r = name + "_calibration_r";
        String g = name + "_calibration_g";
        String b = name + "_calibration_b";
        SharedPreferences sharedPref = getDefaultSharedPreferences(Tutorial3.mainactivity);
        colors[0] = Integer.parseInt(sharedPref.getString(r, "0"));
        colors[1] = Integer.parseInt(sharedPref.getString(g, "0"));
        colors[2] = Integer.parseInt(sharedPref.getString(b, "0"));
        return colors;
    }

    public void saveHSV(String name){
        SharedPreferences sharedPref = getDefaultSharedPreferences(Tutorial3.mainactivity);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(name + "_calibration_hue", Float.toString(hue));
        editor.putString(name + "_calibration_saturation", Float.toString(saturation));
        editor.putString(name + "_calibration_luminance", Float.toString(luminance));
        editor.commit();
    }

    public static float [] loadHSV(String name){
        float [] cali = new float[3];
        String _hue = name + "_calibration_hue";
        String _sat = name + "_calibration_saturation";
        String _lum = name + "_calibration_luminance";
        SharedPreferences sharedPref = getDefaultSharedPreferences(Tutorial3.mainactivity);
        cali[0] = Float.parseFloat(sharedPref.getString(_hue, "0"));
        cali[1] = Float.parseFloat(sharedPref.getString(_sat, "0"));
        cali[2] = Float.parseFloat(sharedPref.getString(_lum, "0"));
        return cali;
    }

    public static float [] convertToHSV(int _red, int _green, int _blue){
        double red_d = (double)_red / 255.0;
        double green_d = (double)_green / 255.0;
        double blue_d = (double)_blue / 255.0;

        //FIND MIN AND MAX
        double min = 1.0; //possible that color values would be greater than 255, which would mean min not set below
        if(red_d < min)min = red_d;
        if(green_d < min)min = green_d;
        if(blue_d < min)min = blue_d;
        double max = 0.0;
        if(red_d > max)max = red_d;
        if(green_d > max)max = green_d;
        if(blue_d > max)max = blue_d;

        //CALCULATE LUMINANCE
        double luminance = (max + min)/2.f;

        //CALCULATE SATURATION
        double saturation = 0.0;
        if(max > 0.0){ //prevent division by zero
            saturation = (max-min)/(max);
        }
        else saturation = 0.0;

        //CALCULATE HUE
        double _hue = 0.0;
        if(red_d == max)_hue = (green_d - blue_d)/(max-min);
        else if(green_d == max)_hue = 2.0 + (blue_d - red_d)/(max-min);
        else if(blue_d == max)_hue = 4.0 + (red_d - green_d)/(max-min);
        _hue = _hue * 60;
        if(_hue < 0)_hue = _hue + 360.0;

        //RETURN VALUES
        float HSV[] = new float[3];
        HSV[0]= (float)_hue; //hue
        HSV[1]= (float)saturation;  //saturation
        HSV[2]= (float)luminance; //luminance value
        return HSV;
    }

}
