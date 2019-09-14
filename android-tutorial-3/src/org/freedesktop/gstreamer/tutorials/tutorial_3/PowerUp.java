package org.freedesktop.gstreamer.tutorials.tutorial_3;

import java.util.ArrayList;

public class PowerUp {
    public static ArrayList<PowerUp> powerups = new ArrayList<PowerUp>();
    public static int number_of_items;
    int item_number = 0;
    int image;
    double weight; //odds of item appearing
    int color_filter;
    Runnable method;

    public PowerUp(int _image, double _weight, int _color_filter, Runnable _method){
        number_of_items += 1;
        item_number = number_of_items;
        image = _image;
        weight = _weight;
        color_filter = _color_filter;
        method = _method;
        powerups.add(this);
    }

    public static PowerUp getByItem(int _item_number){
        for ( PowerUp powerup : powerups){
            if(powerup.item_number == _item_number){
                return powerup;
            }
        }
        return null;
    }

    public static PowerUp randomWeightedPowerUp(){
        double totalWeight = 0.0d;
        for ( PowerUp powerup : powerups){
            totalWeight += powerup.weight;
        }
        int randomIndex = -1;
        double random = Math.random() * totalWeight;
        for (int i = 1; i <= number_of_items; ++i)
        {
            random -= getByItem(i).weight;
            if (random <= 0.0d)
            {
                randomIndex = i;
                break;
            }
        }
        return getByItem(randomIndex);
    }

}
