package org.freedesktop.gstreamer.tutorials.tutorial_3;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static org.freedesktop.gstreamer.tutorials.tutorial_3.Tutorial3.mainactivity;


public class GameServer {
    static final String TAG = "GameServer";
    Tutorial3 activity;
    public ServerSocket serverSocket;
    String message = "";
    static final int socketServerPORT = 5003;
    static ArrayList<ClientConnection> clientConnections = new ArrayList<>();
    static ArrayList<GameServer> gameServers = new ArrayList<>();

    public GameServer(Tutorial3 activity) {
        this.activity = activity;
        gameServers.add(this);
        new Thread(new ServerConnect()).start();
        setupGame();
    }

    public int getPort() {
        return socketServerPORT;
    }

    public void finalize() throws Throwable{
        super.finalize();
        cleanup();
    }

    public static void cleanup() {
        for (GameServer game_server : gameServers){
            if (game_server.serverSocket != null) {
                try {
                    game_server.serverSocket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            for ( ClientConnection client : clientConnections){
                try {
                    client.hostThreadSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                client = null;
            }
            game_server.serverSocket = null;
            game_server = null;
            clientConnections = new ArrayList<>();
        }
        gameServers = new ArrayList<>();

    }

    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class ServerConnect extends Thread {

        public int clients_count = 0;

        @Override
        public void run() {
            try {
                // create ServerSocket using specified port
                serverSocket = new ServerSocket(socketServerPORT);

                while (true) {
                    Socket socket = serverSocket.accept();
                    final String client_ip = String.valueOf(socket.getInetAddress());
                    clients_count++;
                    Log.d(TAG, client_ip + " connected to our server.");
                    new ClientConnection(socket, clients_count, client_ip).start(); //create a new thread for each connected client
                    Log.d(TAG, "Is this being BLOCKED??");
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class ClientConnection extends Thread {

        private Socket hostThreadSocket;
        private String client_ip;
        public int cnt;

        ClientConnection(Socket socket, int c, String ip) {
            hostThreadSocket = socket;
            client_ip = ip;
            cnt = c;
            clientConnections.add(this);
        }


        //{"GAME_DATA":{"type":"NEW_PLAYER","PLAYER_NAME":"Spencer","PLAYER_IP":"0.0.0.0"}}
//        {"GAME_DATA":{"type":"SETUP_GAME","GAME_TYPE":"RACE","LAPS":"5"}}
//        {"GAME_DATA":{"type":"START"}}
//        {"GAME_DATA":{"type":"LAP","PLAYER_IP":"0.0.0.0"}}
        //        {"GAME_DATA":{"type":"GAME_DETAILS_REQUEST"}}

        @Override
        public void run() {
            Log.d("GAME SERVER", "New client thread for: " + client_ip);
//            OutputStream outputStream;
//            String msgReply = "Hello from Server, you are #" + cnt;
            try {
                BufferedReader in_from_client_reader = new BufferedReader(new InputStreamReader(hostThreadSocket.getInputStream()));
                PrintWriter out_to_client = new PrintWriter(new BufferedWriter(new OutputStreamWriter(hostThreadSocket.getOutputStream())), true);
                while (true) {
                    try {
                        if (in_from_client_reader.ready()) {
                            final String in_from_client = in_from_client_reader.readLine();
                            if (in_from_client != null) {
                                Log.d(TAG, "msg from client:" + in_from_client);
                                final JSONObject game_data = new JSONObject(in_from_client).getJSONObject("GAME_DATA");
                                String type = game_data.get("type").toString();
                                if (type.equals("GAME_DETAILS_REQUEST")) {
                                    Log.d(TAG, "game_details:" + String.valueOf(Game.gameDetailsJSON()));
                                    out_to_client.println(String.valueOf(Game.gameDetailsJSON()));
                                    hostThreadSocket.close(); //disconnect (should remove socket from clients list)
                                    break;
                                }
                                if (type.equals("NEW_PLAYER")) {  //player joins lobby
                                    new Player(game_data.getString("PLAYER_IP"), game_data.getString("PLAYER_NAME"));
                                    Log.d(TAG, "Added a new player:" + game_data.getString("PLAYER_NAME"));
                                    mainactivity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            //out_to_client.println(String.valueOf(Game.gameDetailsJSON()));
                                            game_menu.host_lobby_players.setText(Game.getPlayerNames());
                                        }
                                    });
                                    updateGameDetails();
                                }
                                if (type.equals("LAP")) {
                                    Log.d(TAG, "Counting lap");
                                    mainactivity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                Game.multiplayerLap(Game.getPlayerByIP(game_data.get("PLAYER_IP").toString()));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                                if (type.equals("GATE")) {
                                    Log.d(TAG, "Counting gate");
                                    mainactivity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                Game.multiplayerGate(Game.getPlayerByIP(game_data.get("PLAYER_IP").toString()));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                                if (type.equals("TRAP")) {
                                    Log.d(TAG, "Trap Set");
                                    mainactivity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                Game.multiplayerTrap(Game.getPlayerByIP(game_data.get("PLAYER_IP").toString()), false);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                                if (type.equals("TRAP_CANCEL")) {
                                    Log.d(TAG, "Trap Cancelled");
                                    mainactivity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                Game.multiplayerTrapCancel(Game.getPlayerByIP(game_data.get("PLAYER_IP").toString()), false);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                                if (type.equals("EMP")) {
                                    Log.d(TAG, "EMP fired.");
                                    mainactivity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                Game.multiplayerEMP(Game.getPlayerByIP(game_data.get("PLAYER_IP").toString()), false);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendToClient(String string_to_send) {
            PrintWriter out_to_client = null;
            try {
                out_to_client = new PrintWriter(new BufferedWriter(new OutputStreamWriter(hostThreadSocket.getOutputStream())),
                        true);
                out_to_client.println(string_to_send);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void broadcastToClients(String string_to_send) {
        for (ClientConnection client : clientConnections) {
            client.sendToClient(string_to_send);
            Log.d(TAG, "BroadcastToClients: " + string_to_send + " sent to:" + client.client_ip);
        }
    }

    public static void broadcastToClientsJSON(JSONObject obj) {
        for (ClientConnection client : clientConnections) {
            client.sendToClient(obj.toString());
            Log.d(TAG, "BroadcastToClientsJSON: " + obj.toString() + " sent to:" + client.client_ip);
        }
    }

    public static void updateGameDetails(){
        broadcastToClients(String.valueOf(Game.gameDetailsJSON()));
    }

    public void setupGame(){
        Game.mygame = new Game(mainactivity);
        Game.game_type = "Battle Race";
    }


    public static void startGame() {
        Log.d(TAG, "Starting game.");
        updateGameDetails();
        Tutorial3.mainactivity.game_status_string = "Free Roam";
        Tutorial3.game_status.setText(Tutorial3.mainactivity.game_status_string);
        Game.multiplayer = true;
        GameServer.broadcastToClients("{\"GAME_DATA\":{\"type\":\"START\"}}");
        mainactivity.startGameEvent();
        mainactivity.readyToStartEvent(10);
    }


}
