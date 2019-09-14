package org.freedesktop.gstreamer.tutorials.tutorial_3;

import android.app.Activity;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static org.freedesktop.gstreamer.tutorials.tutorial_3.Tutorial3.mainactivity;

public class Player {
    public static int placement_index;

    public String name;
    public String ip_address;
    public int id;
    public int current_lap;
    public long best_lap;
    public long finish_time;
    public boolean finished;
    public long previous_lap_millis;
    public long previous_lap_time;
    public int placement;
    public boolean lap_available;
    public boolean gate_available;
    public boolean has_shield = false;

    public Player(String ip, String playername){
        Game.players.add(this);
        Game.number_of_players += 1;
        ip_address = ip;
        name = playername;
        id = Game.number_of_players; //increment id for each player object added
        finish_time = 0;
        finished = false;
        current_lap = 1;
        best_lap = 0;
        previous_lap_millis = 0;
        previous_lap_time = 0;
        placement = 10000;
        lap_available = false;
        gate_available = true;

        final String toaststring = playername + " (" + ip_address + ") has joined the game.";
        final Activity toastcontext = (Tutorial3) mainactivity;
        toastcontext.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(toastcontext, toaststring, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String playerActionJsonString(String type){
        JSONObject obj = new JSONObject();
        JSONObject gamedata = new JSONObject();
        try {
            obj.put("type", type);
            obj.put("PLAYER_NAME", this.name);
            obj.put("PLAYER_IP", this.ip_address);
            gamedata.put("GAME_DATA", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return gamedata.toString();
    }

    public void finish(Player player){
        Game.allFinished(); //call to update
        finished = true;
        placement_index ++;
        placement = placement_index;
        Game.addToGameSummary(player.placement + "   " + player.name + "    Best lap: " + Game.formatTime(player.best_lap) + "    Final: " + Game.formatTime(player.finish_time) + "\n");
        if(Game.allFinished())Game.endGame();
    }


    public void CountLap(int total_laps, long startTime){
        //Elapsed();
        if(!finished){
            current_lap ++;
            if(previous_lap_millis == 0){
                previous_lap_time = System.currentTimeMillis() - startTime;  //time the first lap
                best_lap = previous_lap_time;  //our only lap is our best
            }
            else{
                previous_lap_time = System.currentTimeMillis() - previous_lap_millis;  //time subsequent laps
                if(previous_lap_time < best_lap)best_lap = previous_lap_time; //save best lap time if shorter than current best
            }
            previous_lap_millis = System.currentTimeMillis();
            if(current_lap > total_laps){
                finish_time = System.currentTimeMillis() - startTime;
                finish(this);
            }
        }
    }



}
