package org.freedesktop.gstreamer.tutorials.tutorial_3;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.freedesktop.gstreamer.tutorials.tutorial_3.Tutorial3.mainactivity;


public class GameClient_bkup extends AsyncTask<String, String, String> {
    static final String TAG = "GameClient";

    public static int GAME_SERVER_PORT = 5003;
    public String game_host_ip;
    public static GameClient_bkup my_game_client = null;
    public static Socket game_server_socket = null;
    public static PrintWriter out_to_server;
    public static BufferedReader in_from_server;

    GameClient_bkup(String _host_ip, String _host_name) {
        game_host_ip = _host_ip;
        my_game_client = this;
        Player hostplayer = new Player(game_host_ip, _host_name);
        Log.d(TAG, "Starting GameClient");
    }

    public void finalize() throws Throwable{
        super.finalize();
        cleanup();
    }
    
    public static void cleanup(){
        if (game_server_socket != null) {
            try {
                game_server_socket.close();
                Log.d("GAME_CLIENT", "Closing socket.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(my_game_client != null && !my_game_client.isCancelled())my_game_client.cancel(true);
        game_server_socket = null;
        my_game_client = null;
    }

    @Override
    protected String doInBackground(String... params) {
        Log.d(TAG, "Starting background thread");
        try {
            Log.d(TAG, "Trying to open socket with " + game_host_ip);
            InetAddress serverAddr = InetAddress.getByName(game_host_ip);
            game_server_socket = new Socket(serverAddr, GAME_SERVER_PORT);
            Log.d(TAG, "Socket opened.");
            out_to_server = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(game_server_socket.getOutputStream())),
                    true);
            in_from_server = new BufferedReader(new InputStreamReader(game_server_socket.getInputStream()));

            JSONObject obj = new JSONObject();
            obj.put("type", "NEW_PLAYER");
            obj.put("PLAYER_NAME", Game.my_player.name);
            obj.put("PLAYER_IP", Game.my_player.ip_address);
            JSONObject game_data = new JSONObject().put("GAME_DATA", obj);
            out_to_server.println(game_data);  //send my player info to the server
            while (true) {
                if (in_from_server.ready()) {
                    String incomingMessage = in_from_server.readLine();
                    final String incoming_string = incomingMessage;
                    if (incomingMessage != null) {
                        Log.d(TAG, "Received from server: " + incomingMessage);
                        mainactivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {  //send lap events to server, recieve other player events from server
                                if (incoming_string.contains("GAME_DATA")) {
                                    JSONObject obj = null;
                                    try {
                                        obj = new JSONObject(incoming_string);
                                        final JSONObject gamedata = obj.getJSONObject("GAME_DATA");
                                        String type = null;
                                        type = gamedata.get("type").toString();
                                        if (type.equals("NEW_PLAYER")) {
                                            Log.d(TAG, "Adding Player");
                                            new Player(gamedata.get("PLAYER_NAME").toString(), gamedata.get("PLAYER_IP").toString());
                                        } else if (type.equals("SETUP_GAME")) {  //can be called multiple times from game lobby as players/game details updated
                                            Game.destroyPlayers();
                                            // Game.setMyPlayer(Game.getPlayerByIP(value2));
                                            Game.mygame = null;
                                            //  if(gamedata.get("GAME_TYPE").toString().equals("RACE"))Game.mygame = new Game(Integer.valueOf(gamedata.get("LAPS").toString()), Tutorial3.mainactivity);
                                        } else if (type.equals("START")) {
                                            Log.d(TAG, "Starting game.");
                                            mainactivity.startGameEvent();
                                            mainactivity.readyToStartEvent(10);
                                        } else if (type.equals("LAP")) {
                                            Log.d(TAG, "Counting lap");
                                            Game.multiplayerLap(Game.getPlayerByIP(gamedata.get("PLAYER_IP").toString()));
                                        } else if (type.equals("GATE")) {
                                            Log.d(TAG, "Counting gate");
                                            Game.multiplayerGate(Game.getPlayerByIP(gamedata.get("PLAYER_IP").toString()));
                                        } else if (type.equals("TRAP")) {
                                            Log.d(TAG, "Trap set");
                                            Game.multiplayerTrap(Game.getPlayerByIP(gamedata.get("PLAYER_IP").toString()),false);
                                        } else if (type.equals("TRAP_CANCEL")) {
                                            Log.d(TAG, "Trap cancelled");
                                            Game.multiplayerTrapCancel(Game.getPlayerByIP(gamedata.get("PLAYER_IP").toString()),false);
                                        } else if (type.equals("EMP")) {
                                            Log.d(TAG, "EMP fired.");
                                            Game.multiplayerEMP(Game.getPlayerByIP(gamedata.get("PLAYER_IP").toString()), false);
                                        } else if (type.equals("GAME_DETAILS_REQUEST_RESPONSE")) {
                                            //populate host search screen
                                            game_menu.lobby_game_details.setText(gamedata.get("GAME_INFO").toString());

                                            //populate multiplayer lobby screen
                                            game_menu.lobby_game_type.setText(gamedata.get("GAME_TYPE").toString());
                                            game_menu.lobby_game_details.setText(gamedata.get("GAME_INFO").toString());
                                            game_menu.lobby_host.setText(gamedata.get("HOST_NAME").toString());
                                            game_menu.lobby_players.setText(gamedata.get("PLAYERS").toString());

                                            if(Game.mygame == null){  //setup game
                                                Game.mygame = new Game(mainactivity);
                                            }
                                                   //modify game details
                                            Game.total_laps = Integer.valueOf(gamedata.get("GAME_INFO").toString());
                                            Game.game_type = gamedata.get("GAME_TYPE").toString();
                                            Game.multiplayer = true;
                                            Tutorial3.mainactivity.game_status_string = Game.game_type;
                                            Tutorial3.mainactivity.game_status.setText(Tutorial3.mainactivity.game_status_string);

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        });
                    }
                }
            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (game_server_socket != null) {
                try {
                    game_server_socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        // return response;
        String result = "response string";
        return result;

    }


    @Override
    protected void onPostExecute(String result) {
        try {
            game_server_socket.close();
            Log.d(TAG, "Closing game server socket.");
            cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onPostExecute(result);
    }


    public static JSONObject request(String request_string){  //use te existing socket if we have the game client running
        return request(request_string, game_server_socket);
    }


    public static JSONObject request(String request_string, Socket socket) {  //specify a socket if we haven't created a gameclient object

        JSONObject game_data = null;
        Log.d(TAG, "Sending request:" + request_string);
        try {
            BufferedReader response_from_server = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter request_to_server = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            game_data = new JSONObject().put("GAME_DATA", new JSONObject().put("type", request_string));
            request_to_server.println(game_data);
            long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) < 5000) {  //wait for the server
                Log.d("GAME_CLIENT request", "waiting for response");
                if (response_from_server.ready()) {  //make sure there is data to read before calling readLine() which is BLOCKING
                    Log.d("GAME_CLIENT request", "in_from_server.ready()");
                    String incomingMessage = "";
                    incomingMessage = response_from_server.readLine();
                    Log.d("GAME_CLIENT REQUEST", "Incoming message: " + incomingMessage);
                    game_data = new JSONObject(incomingMessage).getJSONObject("GAME_DATA");
                    if (game_data.getString("type").equals(request_string + "_RESPONSE")) {
                        JSONObject responseObj = game_data;
                        Log.d("GAME_CLIENT", "JSON response obj: " + String.valueOf(responseObj));
                        return responseObj;
                    }
                    else return null;  //might want to delete this line to wait for expected response
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void sendToServer(String string_to_send) {

        try {
            PrintWriter out_to_server = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(game_server_socket.getOutputStream())),
                    true);
            out_to_server.println(string_to_send);
            Log.d(TAG, "Sending to server: " + string_to_send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

//Sample JSON:
//{"GAME_DETAILS":{"GAME_TYPE":"Sumo","GAME_DETAILS":"Pads: 3, Gates: 1, Laps: 5","PLAYERS":"MaliciousFister, QuickSilver, KidKollide", "HOST": "Spencer"}}
//
//        {"GAME_DATA":{"type":"NEW_PLAYER","PLAYER_NAME":"Spencer","PLAYER_IP":"0.0.0.0"}}
//        {"GAME_DATA":{"type":"SETUP_GAME","GAME_TYPE":"RACE","LAPS":"5"}}
//        {"GAME_DATA":{"type":"START"}}
//        {"GAME_DATA":{"type":"LAP","PLAYER_IP":"0.0.0.0"}}