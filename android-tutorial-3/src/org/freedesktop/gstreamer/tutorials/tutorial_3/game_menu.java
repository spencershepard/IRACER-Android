package org.freedesktop.gstreamer.tutorials.tutorial_3;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;
import static java.lang.Integer.valueOf;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link game_menu.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link game_menu#newInstance} factory method to
 * create an instance of this fragment.
 */
public class game_menu extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private MyStringListener listener;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View myview;
    private View game_menu_screen;
    private View solo_game_screen;
    private View join_multiplayer_screen;
    private View host_multiplayer_screen;
    private View game_in_progress_screen;
    private View multiplayer_lobby_screen;
    private Button refresh_hosts_btn;
    public EditText player_name_edit;
    public static TextView host_lobby_players;
    public static TextView lobby_game_details;
    public static TextView lobby_players;
    public static TextView lobby_game_type;
    public static TextView lobby_host;
    public Spinner laps_spinner;

    private OnFragmentInteractionListener mListener;

    AsyncTask search;

    public game_menu() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment game_menu.
     */
    // TODO: Rename and change types and number of parameters
    public static game_menu newInstance(String param1, String param2) {
        game_menu fragment = new game_menu();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.game_menu, container, false);

        Spinner gametypespinner = v.findViewById(R.id.gametype_spinner);
        gametypespinner.setOnItemSelectedListener(this);
        laps_spinner = v.findViewById(R.id.laps_spinner);
        laps_spinner.setOnItemSelectedListener(this);

        game_menu_screen = v.findViewById(R.id.game_menu_layout);
        solo_game_screen = v.findViewById(R.id.solo_race_layout);
        join_multiplayer_screen = v.findViewById(R.id.mp_client_layout);
        host_multiplayer_screen = v.findViewById(R.id.mp_host_lobby);
        game_in_progress_screen = v.findViewById(R.id.game_inprogress_layout);
        multiplayer_lobby_screen = v.findViewById(R.id.mp_client_lobby);
        lobby_game_details = v.findViewById(R.id.game_details_tv);
        lobby_players = v.findViewById(R.id.players_tv);
        lobby_game_type = v.findViewById(R.id.game_type_tv);
        lobby_host = v.findViewById(R.id.lobby_host);
        host_lobby_players = v.findViewById(R.id.host_lobby_players);

        Button solo_button = (Button) v.findViewById(R.id.solo_button);
        solo_button.setOnClickListener(this);
        Button solo_quit_button = (Button) v.findViewById(R.id.solo_cancel_btn);
        solo_quit_button.setOnClickListener(this);
        Button join_button = (Button) v.findViewById(R.id.join_multiplayer_btn);
        join_button.setOnClickListener(this);
        Button host_button = (Button) v.findViewById(R.id.host_multiplayer_btn);
        host_button.setOnClickListener(this);
        Button solo_start = (Button) v.findViewById(R.id.solo_start_btn);
        solo_start.setOnClickListener(this);
        Button quit_game_button = (Button) v.findViewById(R.id.quit_game_btn);
        quit_game_button.setOnClickListener(this);
        refresh_hosts_btn = v.findViewById(R.id.refresh_btn);
        refresh_hosts_btn.setOnClickListener(this);
        Button quit_game_lobby_button = (Button) v.findViewById(R.id.lobby_quit_btn);
        quit_game_lobby_button.setOnClickListener(this);
        Button host_start_btn = v.findViewById(R.id.host_lobby_ready_btn);
        host_start_btn.setOnClickListener(this);
        Button host_quit_btn = (Button) v.findViewById(R.id.host_quit_btn);
        host_quit_btn.setOnClickListener(this);

        SharedPreferences sharedPref = getDefaultSharedPreferences(getActivity());
        String player_name = sharedPref.getString("player_name", "Player Name");
        player_name_edit = v.findViewById(R.id.player_name_edit);
        player_name_edit.setText(player_name);

        setAllMyViewsInvisible();
        if(Game.mygame != null){  //Check if game is in progress
            game_in_progress_screen.setVisibility(View.VISIBLE);
        }
        else game_menu_screen.setVisibility(View.VISIBLE);

        myview = v;
        return v;

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (MyStringListener) context;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        Log.d("game menu frag", "my fragment attached");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(GameServer.clientConnections != null && GameServer.clientConnections.size() > 0) {
            if (view == view.findViewById(R.id.laps_spinner) || view == view.findViewById(R.id.gametype_spinner))
                Log.d("Game Menu", "Spinner item selected");
            Game.total_laps = Integer.valueOf(laps_spinner.getSelectedItem().toString());
            GameServer.updateGameDetails();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public interface MyStringListener{  //customize this for sending information between fragment and activity
        public Integer computeSomething(String myString);
    }

    @Override
    public void onClick(View v) {
        Log.d("game menu frag", "on click");
        int laps = valueOf(((EditText) myview.findViewById(R.id.laps_edit_text)).getText().toString());
        String playername = ((EditText) myview.findViewById(R.id.player_name_edit)).getText().toString();
        Tutorial3.mainactivity.playername_bar.setText(playername);
        SharedPreferences sharedPref = getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("player_name", playername);
        editor.commit();
        switch (v.getId()) {
            case R.id.quit_game_btn:
                Tutorial3.mainactivity.cleanupGame();
                setAllMyViewsInvisible();
                game_menu_screen.setVisibility(View.VISIBLE);
                break;
            case R.id.solo_button:
                setAllMyViewsInvisible();
                Tutorial3.mainactivity.black_overlay.setVisibility(View.VISIBLE);
                solo_game_screen.setVisibility(View.VISIBLE);
                break;
            case R.id.solo_start_btn:
                Tutorial3.mainactivity.cleanupGame();
                Game.my_player = new Player(getIpAddress(), playername);
                ((Tutorial3)getActivity()).soloRace(laps);
                Tutorial3.mainactivity.game_status_string = "Solo Race";
                Tutorial3.mainactivity.game_status.setText(Tutorial3.mainactivity.game_status_string);
                break;
            case R.id.solo_cancel_btn:
                Tutorial3.mainactivity.cleanupGame();
                setAllMyViewsInvisible();
                game_menu_screen.setVisibility(View.VISIBLE);
                break;
            case R.id.join_multiplayer_btn:
                setAllMyViewsInvisible();
                join_multiplayer_screen.setVisibility(View.VISIBLE);
                Tutorial3.mainactivity.cleanupGame();
                Tutorial3.mainactivity.black_overlay.setVisibility(View.VISIBLE);
                Game.my_player = new Player(getIpAddress(), playername);
                break;
            case R.id.refresh_btn:
                refresh_hosts_btn.setText("Searching...");
                refresh_hosts_btn.setEnabled(false);
                if(search != null){
                    search.cancel(true);
                    Log.d("game_menu", "Our async task was already running or stuck.  Shutting it down.");
                }
                Log.d("game_menu", "About to search for hosts.");
                // = new searchForHosts().execute();
                new searchForHosts().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Log.d("game_menu", "Hopefully our background search just completed");
                break;
            case R.id.lobby_quit_btn:
                setAllMyViewsInvisible();
                Tutorial3.mainactivity.cleanupGame();
                game_menu_screen.setVisibility(View.VISIBLE);
                break;
            case R.id.host_multiplayer_btn:
                setAllMyViewsInvisible();
                host_multiplayer_screen.setVisibility(View.VISIBLE);
                Tutorial3.mainactivity.cleanupGame();
                Tutorial3.mainactivity.black_overlay.setVisibility(View.VISIBLE);
                Game.my_player = new Player(getIpAddress(), playername);
                Game.game_host = Game.my_player;
                new GameServer(Tutorial3.mainactivity);
                break;
            case R.id.host_lobby_ready_btn:
                setAllMyViewsInvisible();
                GameServer.startGame();
                break;
            case R.id.host_quit_btn:
                Tutorial3.mainactivity.cleanupGame();
                setAllMyViewsInvisible();
                game_menu_screen.setVisibility(View.VISIBLE);
                break;
        }
    }



    public static class GameHost {  //for indexing hosts found from game client
        public String ip;
//        public String players;
//        public String gametype;
        public JSONObject game_details;
        public static ArrayList<GameHost> hosts = new ArrayList<GameHost>();

        public GameHost(String ip, JSONObject details){
            this.ip = ip;
            this.game_details = details;
            hosts.add(this);
        }

        public static void clearList(){
            hosts = null;
            hosts = new ArrayList<GameHost>();
        }
    }

    public class searchForHosts extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... args) {
            Log.d("GAME_CLIENT", "Searching for hosts");
            GameHost.clearList();
            String ip_base = Tutorial3.MY_IP;
            ip_base = ip_base.substring(0, ip_base.lastIndexOf(".") + 1); //keep only the network portion of ip address
            for(int x = 20; x <= 25; x++) { //iterate through each possible ip address on the network  CHANGE TO 0-255
                String ip = "";
                ip = ip_base + x;
                String host_ip = ip;
                Log.d("GAME_CLIENT", "Attempting connection to: " + host_ip);
                JSONObject game_details = null;
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, GameClient.GAME_SERVER_PORT), 1000);  //try 20ms timeout
                    game_details = GameClient.request("GAME_DETAILS_REQUEST", socket);
                    socket.close();
                    Log.d("game_menu", "Closing connection.");
                    if(game_details != null){
                        new GameHost(host_ip,game_details); //add the found host
                        Log.d("game_menu", "Adding  " + host_ip + " to list of game hosts.");
                    }
                } catch (ConnectException ce) {
                    ce.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Void result) {
            Log.d("GAME_CLIENT", "Finished searching for hosts");
            try{
                for(final GameHost host : GameHost.hosts){
                    LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = vi.inflate(R.layout.game_host_item, null);

                    TextView ip_textView = (TextView) v.findViewById(R.id.host_list_game_details);
                    ip_textView.setText(host.game_details.get("GAME_TYPE").toString() + ": " + host.game_details.get("PLAYERS").toString());
                    final String host_name = host.game_details.get("HOST_NAME").toString();
                    Button join_button = (Button) v.findViewById(R.id.join_game_btn);
                    join_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d("game menu", "Join game button pressed.");
                            setAllMyViewsInvisible();
                            multiplayer_lobby_screen.setVisibility(View.VISIBLE);
                            GameClient mygameclient = new GameClient(host.ip, host_name);
                           // mygameclient.execute();
                        }
                    });
                    // insert into main view
                    LinearLayout insertPoint = (LinearLayout) myview.findViewById(R.id.game_hosts);
                    insertPoint.removeAllViews();
                    insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
                }
            }
            catch (Exception e){
            }
            refresh_hosts_btn.setEnabled(true);
            refresh_hosts_btn.setText("Refresh");

        }
    }

//


    private void setAllMyViewsInvisible(){
        game_in_progress_screen.setVisibility(View.INVISIBLE);
        game_menu_screen.setVisibility(View.INVISIBLE);
        solo_game_screen.setVisibility(View.INVISIBLE);
        join_multiplayer_screen.setVisibility(View.INVISIBLE);
        multiplayer_lobby_screen.setVisibility(View.INVISIBLE);
        host_multiplayer_screen.setVisibility(View.INVISIBLE);
    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

}
