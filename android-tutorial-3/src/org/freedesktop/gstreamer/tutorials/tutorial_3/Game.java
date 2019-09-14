package org.freedesktop.gstreamer.tutorials.tutorial_3;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Game {
    static final String TAG = "Game Class";
//    public static final int RACE = 1;
//    public static final int BATTLE_RACE = 2;
//    public static final int SUMO = 3;

    public static Player my_player = null;
    public static ArrayList<Player> players = new ArrayList<>();
    public static Tutorial3 main_activity;
    public static String game_summary = "";
    public static int players_finished = 0;
    public static int players_not_finished = 0;
    public static int number_of_players = 0;
    public static Game mygame;
    //public static int game_type;
    public static String game_type = "";
    public static Player game_host;
    public static boolean multiplayer;
    //Race variables
    public static long startTime;
    public static long elapsedTime;
    public static int total_laps;
    public static boolean gate_trap_set = false;

    public Game (Tutorial3 activity){
        main_activity = activity;
        startTime = 0;
        elapsedTime = 0;
        mygame = this;
      //  total_laps = laps;
    }

    public static JSONObject gameDetailsJSON() {
        JSONObject obj = new JSONObject();
        JSONObject game_details = new JSONObject();
        try {
            game_details.put("type", "GAME_DETAILS_REQUEST_RESPONSE");
            game_details.put("GAME_TYPE", Game.game_type);
            game_details.put("GAME_INFO", total_laps);
            game_details.put("HOST_NAME", my_player.name);
        for ( Player player : players){
            game_details.put("PLAYERS", player.name);
        }
        obj.put("GAME_DATA", game_details);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void Start() {
        startTime = System.currentTimeMillis();
    }

    public static void setMyPlayer(Player player){
        my_player = player;
    }

    public static void endGame(){
        Player.placement_index = 0;
        //nullify players?
    }


    public static boolean allFinished(){
        boolean all_done = true;
        int players_to_finish = 0;
        int players_have_finished = 0;
        for ( Player player : players){
            if(!player.finished){
                all_done = false;
                players_to_finish ++;
            }
            else players_have_finished ++;
        }
        players_not_finished = players_to_finish;
        players_finished = players_have_finished;
        return all_done;
    }

    public static Player getPlayerByIP(String search){  //move to game class
        Player found_player = null;
        for ( Player player : players){
            if(search.equals(player.ip_address)){
                found_player = player;
            }
        }
        return found_player;
    }

    public static Player getWinner(){  //move to game class
        Player winner = null;
        int lowest_place = 100;
        for ( Player player : players){
            if(player.placement <= lowest_place){
                winner = player;
            }
        }
        return winner;
    }

    public static void addToGameSummary(String playerStats){
        game_summary += playerStats;
        Tutorial3.updatePostGameScreen(game_summary);
    }

    public static String getPlayerNames(){
        String playernames = "Players in this game:  ";
        for ( Player player : players){
                playernames += player.name + ", ";
            }
            playernames = playernames.substring(0, playernames.length()-2); //remove the comma and extra space
            return playernames;
        }

    public static void destroyPlayers(){
        Player.placement_index = 0;
        game_summary = "";
        for ( Player player : players){
            player = null;
        }
        players = new ArrayList<>();
        players_finished = 0;
        players_not_finished = 0;
        number_of_players = 0;
        my_player = null;
        game_host = null;
    }

    public static String formatTime(long millis){
        String ms = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        return ms;
    }

    public static void lap(Player player) {
        if(multiplayer) {
            if (Game.my_player == Game.game_host) {  //if I'm the host
                GameServer.broadcastToClients(player.playerActionJsonString("LAP"));
            } else {
                GameClient.sendToServer(player.playerActionJsonString("LAP"));
            }
        }
        if (!player.finished && player.lap_available) {
            player.CountLap(mygame.total_laps, mygame.startTime);
            player.gate_available = true;
            player.lap_available = false;

            if (player == Game.my_player && !player.finished) {
                main_activity.myplayerLapEvent(player);
            }
            if (player == Game.my_player && player.finished) {
                main_activity.myplayerFinishedEvent(player);
            }
            if (Game.allFinished()) {
                main_activity.raceEndEvent(player);
            }

        }
    }

    public static void multiplayerLap(Player player) {
        Log.d(TAG, player.name + " just completed a lap.");
        if (!player.finished && player.lap_available) {
            player.CountLap(mygame.total_laps, mygame.startTime);
            player.gate_available = true;
            player.lap_available = false;
            if (Game.allFinished()) {
                main_activity.raceEndEvent(player);
            }
        }
    }

    public static void gate(Player player){
        if(multiplayer) {
            if (Game.my_player == Game.game_host) {  //if I'm the host
                GameServer.broadcastToClients(player.playerActionJsonString("GATE"));
            } else {
                GameClient.sendToServer(player.playerActionJsonString("GATE"));
            }

            if(gate_trap_set){
                main_activity.hitTrap();
                if (Game.my_player == Game.game_host) {  //if I'm the host
                    GameServer.broadcastToClients(player.playerActionJsonString("TRAP_CANCEL"));
                } else {
                    GameClient.sendToServer(player.playerActionJsonString("TRAP_CANCEL"));
                }
            }
        }
        if (!player.finished && player.gate_available) {
            player.lap_available = true;
            player.gate_available = false;
            main_activity.myplayerGateEvent(player);
        }
    }

    public static void multiplayerGate(Player player){
        if(player != Game.my_player) {  //ignore broadcasts from the server about my own player
            Log.d(TAG, player.name + " just went through a gate.");
            if (!player.finished && player.gate_available) {
                player.lap_available = true;
                player.gate_available = false;
            }
        }
    }

    public static void multiplayerTrap(Player player, boolean local){
        Game.gate_trap_set = true;
        if(player != Game.my_player || local) {  //ignore broadcasts from the server about my own player, send to other players if local
            Log.d(TAG, player.name + " set a trap.");
            if (multiplayer) {
                if (Game.my_player == Game.game_host) {  //if I'm the host
                    GameServer.broadcastToClients(player.playerActionJsonString("TRAP"));
                } else {
                    GameClient.sendToServer(player.playerActionJsonString("TRAP"));
                }
            }
        }
    }

    public static void multiplayerTrapCancel(Player player, boolean local){
        Game.gate_trap_set = false;
        if(player != Game.my_player || local) {  //ignore broadcasts from the server about my own player, send to other players if local
            Log.d(TAG, player.name + " hit a trap.");
            if (multiplayer) {
                if (Game.my_player == Game.game_host) {  //if I'm the host
                    GameServer.broadcastToClients(player.playerActionJsonString("TRAP_CANCEL"));
                } else {
                    GameClient.sendToServer(player.playerActionJsonString("TRAP_CANCEL"));
                }
            }
        }

    }

    public static void multiplayerEMP(Player player, boolean local) {
        if (player != Game.my_player || local) {  //ignore broadcasts from the server about my own player, send to other players if local
            Log.d(TAG, player.name + " fired EMP.");
            if (multiplayer) {
                if (Game.my_player == Game.game_host) {  //if I'm the host
                    GameServer.broadcastToClients(player.playerActionJsonString("EMP"));
                } else {
                    GameClient.sendToServer(player.playerActionJsonString("EMP"));
                }
            }
        }
        if(player != Game.my_player){
            //add shield
            main_activity.hitWithEMP();
        }


    }


}
