package org.freedesktop.gstreamer.tutorials.tutorial_3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.freedesktop.gstreamer.GStreamer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

public class Tutorial3 extends AppCompatActivity implements SurfaceHolder.Callback, SharedPreferences.OnSharedPreferenceChangeListener, game_menu.OnFragmentInteractionListener {
    private static final String TAG = Tutorial3.class.getSimpleName();

    private native void nativeInit();     // Initialize native code, build pipeline, etc

    private native void nativeFinalize(); // Destroy pipeline and shutdown native code

    private native void nativePlay();     // Set pipeline to PLAYING

    private native void nativePause();    // Set pipeline to PAUSED

    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks

    private native void nativeSurfaceInit(Object surface);

    private native void nativeSurfaceFinalize();

    private long native_custom_data;      // Native code will use this to keep private data


    //views
    private DrawerLayout mDrawer;
    private NavigationView nvDrawer;
    SeekBar steering_seekbar;
    SeekBar brightnessSeekBar;
    SeekBar bitrateSeekBar;
    private SeekBar gas_seek;
    private SeekBar brake_seek;
    private ProgressBar motor_progress;
    private FrameLayout settings_view;
    private FrameLayout gameview_layout;
    private TextView elapsed_time_textview;
    private TextView game_alert_tv;
    public static TextView game_summary;
    private Button free_roam;
    private View dev_buttons;
    private Button ready_button;
    private Button leave_button;
    public View black_overlay; //preferencesfragment has a transparent background
    private TextView speedometer_text;
    private TextView servo_text;
    private TextView motor_text;
    private ImageView game_item_box;
    private TextView socket_text;
    public TextView game_display;
    public static TextView game_status;
    private TextView link_quality;
    private View video_filter;
    public TextView playername_bar;

    //assorted vars
    private Timer heartbeat_timer;
    private long last_heartbeat;
    private int motor_output_value = 0;
    private boolean gas_pressed;
    private boolean braking;
    private int brake_input = 0;
    private int acceleration_input = 0;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Socket car_link_socket;
    private String incomingMessageFromCar = "";
    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING
    SharedPreferences prefs;
    public String socketOutString;
    private AlphaAnimation buttonClick = new AlphaAnimation(0.4F, 0.8F);
    long elapsedTime = 0;
    //preferences and settings
    private int top_speed;  //max is 1000
    private int min_speed;
    private int rev_top_speed;  //max is 1000
    private int rev_min_speed;
    private boolean boost_pref;
    private int deceleration_rate;
    private int acceleration_rate;
    private int brake_rate;
    private int servo;
    private int servo_center;
    int servo_max;
    int servo_min;
    private int control_loop_time = 50; //ms
    private int CONTROL_SERVER_PORT = 5001;
    private String CONTROL_SERVER_IP = "192.168.0.14";
    //public String GAME_SERVER_IP = "192.168.0.19";
    public static String MY_IP = "";
    GameServer gameserver;
    SoundPool soundpool;
    MediaPlayer mediaplayer;
    private Handler timer_handler; //timer
    static boolean vehicle_connected = false;
    public static String game_status_string = "Free Roam";
    public boolean getLinkQuality = false;
    private int min_video_bitrate;
    private int max_video_bitrate;
    private int video_bitrate;

    int soundIds[] = new int[20];
    final int RACE_START = 0;
    final int RACE_FINISH = 1;
    final int START_LINE = 2;
    final int FINAL_LAP = 3;
    final int CONNECTED = 4;
    final int DISCONNECTED = 5;
    final int ITEM_BOX = 6;
    final int NITROUS = 7;
    final int LAP = 8;
    final int GATE = 9;
    final int LASER = 10;
    final int SHIELD = 11;
    final int SLOW = 12;
    final int CHAINS = 13;
    final int TIRE_POP = 14;
    final int RACE_IS_STARTING = 15;
//increase array size

    public static Tutorial3 mainactivity;

    //    public ColorSensor finish_line = null;
//    public ColorSensor gate = null;
//    public ColorSensor boost = null;
//    public ColorSensor mystery_box = null;
    public int currentColor[] = new int[3];
    public float currentHSV[] = new float[3];


    // Called when the activity is first created.
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainactivity = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectDiskReads()
//                .detectDiskWrites()
//                .detectNetwork()   // or .detectAll() for all detectable problems
//                .penaltyLog()
//                .penaltyFlashScreen()
//                .build());
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectLeakedSqlLiteObjects()
//                .detectLeakedClosableObjects()
//                .penaltyLog()
//                .penaltyDeath()
//                .build());


        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        socketOutString = "";

//        finish_line = new ColorSensor();
//        gate = new ColorSensor();
//        boost = new ColorSensor();
//        mystery_box = new ColorSensor();

        PreferenceManager.setDefaultValues(this, R.xml.default_prefs, false);

        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        loadPrefs();
        setupViews();
        loadViewPrefs();

        timer_handler = new Handler();


        Button startbutton = (Button) findViewById(R.id.start_btn);
        startbutton.setText("Btn");
        startbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        ready_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readyToStartEvent(5);
            }
        });

        Button button1 = (Button) findViewById(R.id.button1);
        //   button1.setText("Cal:Fin");
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                finish_line.calibrate(currentColor);
//                finish_line.save("finish_line");
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setText("Button2");
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        Button button3 = (Button) findViewById(R.id.button3);
        button3.setText("Gate");
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Game.mygame != null)Game.gate(Game.my_player);

            }
        });

        Button button4 = (Button) findViewById(R.id.button4);
        button4.setText("Lap");
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Game.mygame != null)Game.lap(Game.my_player);

            }
        });

        Button button5 = (Button) findViewById(R.id.button5);
        //       button5.setText("Cal:Boost");
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                boost.calibrate(currentColor);
//                boost.save("boost");
            }
        });

        Button button6 = (Button) findViewById(R.id.button6);
        button6.setText("Pickup");
        button6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mysteryBoxPickup();
            }
        });


        free_roam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                free_roam.setVisibility(View.GONE);
                cleanupGame();
            }
        });

        leave_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                free_roam.setVisibility(View.GONE);
                cleanupGame();
            }
        });

        steering_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                servo = progress;
                ImageView steering_wheel_image = findViewById(R.id.steering_wheel_image);
                steering_wheel_image.setRotation(-(progress - servo_center) / 5);
                servo_text.setText("Servo output: " + servo);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            //   seekBar.setProgress(servo_center);
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(servo_center);
                ImageView steering_wheel_image = findViewById(R.id.steering_wheel_image);
                steering_wheel_image.setRotation(0);
            }
        });

        final int dead_zone_end = 20; //slow to minimum speed if gas pedal progress below this value
        gas_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > dead_zone_end)
                    motor_output_value = (progress * (top_speed - min_speed)) / 100 + min_speed;
                else motor_output_value = min_speed;
                if (progress == 0) motor_output_value = 0;
//                if(progress >= 75)acceleration_input = ((progress - 74) * acceleration_rate) / 100;
//                else if(progress >= 40)acceleration_input = ((progress - 39) * acceleration_rate) / 150; //speed up
//                else if(progress >= 20)acceleration_input = 0;  //maintain speed
//                else if(progress > 0)acceleration_input = deceleration_rate / -progress; //slow down
                // else acceleration_input = 0;

                motor_progress.setProgress((motor_output_value - min_speed));
                int percent = progress;
                if (progress <= dead_zone_end) percent = dead_zone_end;
                if (percent < 0) percent = 0;

                motor_text.setText("Motor output: " + motor_output_value);
                speedometer_text.setText(percent + "%");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                gas_pressed = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                gas_pressed = false;
                motor_output_value = 0;
                seekBar.setProgress(0);
                motor_text.setText("Motor output: " + motor_output_value);
                speedometer_text.setText("0%");
            }
        });

        brake_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // brake_input = (progress * brake_rate) / 100;
                motor_output_value = -((progress * (top_speed - min_speed)) / 100 + min_speed);
                if (progress == 0) motor_output_value = 0;
                motor_text.setText("Motor output: " + motor_output_value);

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                braking = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                braking = false;
                motor_output_value = 0;
                seekBar.setProgress(0);
                motor_text.setText("Motor output: " + motor_output_value);
                speedometer_text.setText("0%");
            }
        });

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                socketOutString = socketOutString + "U=B_" + String.valueOf(progress + 30) + "&";
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        bitrateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                socketOutString = socketOutString + "U=V_" + String.valueOf(progress + min_video_bitrate) + "&";
                video_bitrate = progress + min_video_bitrate;

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        createPowerUps();


        mDrawer.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );


        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundpool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attrs)
                .build();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundIds[RACE_START] = soundpool.load(mainactivity, R.raw.race_start, 1);
        soundIds[RACE_FINISH] = soundpool.load(mainactivity, R.raw.win, 1);
        soundIds[START_LINE] = soundpool.load(mainactivity, R.raw.get_to_start_line, 1);
        soundIds[FINAL_LAP] = soundpool.load(mainactivity, R.raw.final_lap, 1);
        soundIds[CONNECTED] = soundpool.load(mainactivity, R.raw.connected_to_vehicle, 1);
        soundIds[DISCONNECTED] = soundpool.load(mainactivity, R.raw.vehicle_disconnected, 1);
        soundIds[ITEM_BOX] = soundpool.load(mainactivity, R.raw.twinkle, 1);
        soundIds[NITROUS] = soundpool.load(mainactivity, R.raw.nitrous, 1);
        soundIds[LAP] = soundpool.load(mainactivity, R.raw.bonus_twinkle, 1);
        soundIds[GATE] = soundpool.load(mainactivity, R.raw.bonus_sound, 1);
        soundIds[LASER] = soundpool.load(mainactivity, R.raw.laser, 1);
        soundIds[SHIELD] = soundpool.load(mainactivity, R.raw.shield, 1);
        soundIds[SLOW] = soundpool.load(mainactivity, R.raw.cartoon_down, 1);
        soundIds[CHAINS] = soundpool.load(mainactivity, R.raw.chains, 1);
        soundIds[TIRE_POP] = soundpool.load(mainactivity, R.raw.tire_pop, 1);
        soundIds[RACE_IS_STARTING] = soundpool.load(mainactivity, R.raw.the_race_is_starting, 1);


//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        SurfaceView sv = (SurfaceView) this.findViewById(R.id.surface_video);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        MY_IP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());


//        if (savedInstanceState != null) {
//            is_playing_desired = savedInstanceState.getBoolean("playing");
//            Log.i("GStreamer", "Activity created. Saved state is playing:" + is_playing_desired);
//        } else {
//            is_playing_desired = false;
//            Log.i("GStreamer", "Activity created. There is no saved state, playing: false");
//        }

        is_playing_desired = true; //genix
        nativeInit();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("GStreamer", "Saving state, playing:" + is_playing_desired);
        outState.putBoolean("playing", is_playing_desired);
    }

    protected void onDestroy() {
        nativeFinalize();
        super.onDestroy();
        soundpool.release();
        if (mediaplayer != null) mediaplayer.release();
        gameserver.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        loadPrefs();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        sensorManager.unregisterListener(this);
    }


    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
        final TextView tv = (TextView) this.findViewById(R.id.textview_message);
        runOnUiThread(new Runnable() {
            public void run() {
                tv.setText(message);
            }
        });
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized() {
        Log.i("GStreamer", "Gst initialized. Restoring state, playing:" + is_playing_desired);
        // Restore previous playing state
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }

        // Re-enable buttons, now that GStreamer is initialized
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            public void run() {
//                activity.findViewById(R.id.button_play).setEnabled(true);
//                activity.findViewById(R.id.button_stop).setEnabled(true);
                if (!vehicle_connected) connectToCar();
            }
        });
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("tutorial-3");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit(holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize();
    }

    public void connectToCar() {
        try {
            new Thread(new CarLink()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromCar(){
        if(car_link_socket != null){
            try {
                car_link_socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            car_link_socket = null;
            carDisconnected();
        }

    }


    class CarLink implements Runnable {

        @Override
        public void run() {
            Log.d(TAG, "CarLink started");

            try {
                InetAddress serverAddr = InetAddress.getByName(CONTROL_SERVER_IP);
                car_link_socket = new Socket(serverAddr, CONTROL_SERVER_PORT);
                car_link_socket.setTcpNoDelay(true);
                CarControl.run();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    Thread CarControl = new Thread() {
        PrintWriter controller;
        BufferedReader input_from_car;

        @Override
        public void run() {
            vehicle_connected = true;
            carConnected();
            try {
                controller = new PrintWriter(new BufferedWriter(new OutputStreamWriter(car_link_socket.getOutputStream())), true);
                input_from_car = new BufferedReader(new InputStreamReader(car_link_socket.getInputStream()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                while (vehicle_connected) { //control loop
                    sleep(control_loop_time);
                    socketOutString = socketOutString + "M=" + String.valueOf((motor_output_value / 10)) + "&";  //add motor control to output string
                    socketOutString = socketOutString + "S=" + String.valueOf(servo) + "&";
                    if(getLinkQuality)socketOutString = socketOutString + "U=W&"; //controlled by a timer
                    if (socketOutString != "") {
                        controller.println(socketOutString);
                        socketOutString = ""; //clear the string
                        getLinkQuality = false;
                    }
                    String incomingMessage = "";
                    try {
                        if (input_from_car.ready()) {
                            incomingMessage = input_from_car.readLine();
                            if (incomingMessage != "" && incomingMessage != null) {
                                incomingMessageFromCar = incomingMessage;
                                last_heartbeat = System.currentTimeMillis();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        socket_text.setText("Socket: " + incomingMessageFromCar);
                                        Log.d(TAG, "message from car: " + incomingMessageFromCar);
                                        if (incomingMessageFromCar.contains("LAP") && Game.mygame != null)
                                            Game.lap(Game.my_player);
                                        if (incomingMessageFromCar.contains("GATE") && Game.mygame != null)
                                            Game.gate(Game.my_player);
                                        if (incomingMessageFromCar.contains("BOOST")) boost();
                                        if (incomingMessageFromCar.contains("POWERUP"))
                                            mysteryBoxPickup();
                                        if (incomingMessageFromCar.contains("Link Quality")){
                                            processWifiQualityMessage(incomingMessageFromCar);
                                        }
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        carDisconnected();
                        e.printStackTrace();
                    }


                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Interupted exception: " + e);
                e.printStackTrace();
            }
        }
    };


    public void carConnected() {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "CAR CONNECTED");
                soundpool.play((soundIds[CONNECTED]), 1, 1, 1, 0, 1.0f);
                brake_rate = (int) Math.round((float) brake_rate * ((float) control_loop_time / 50.0));
                deceleration_rate = (int) Math.round((float) deceleration_rate * ((float) control_loop_time / 50.0));
                acceleration_rate = (int) Math.round((float) acceleration_rate * ((float) control_loop_time / 50.0));
                last_heartbeat = System.currentTimeMillis();
                game_status.setText(game_status_string);
                heartbeat_timer = new Timer();
                heartbeat_timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getLinkQuality = true;
                        if(last_heartbeat < System.currentTimeMillis() - 20000){
                            Log.d(TAG, "Heartbeat timer expired!  Disconnecting");
                            disconnectFromCar(); //it's been too long since we've talked to server, so disconnect
                        }
                    }

                }, 0, 10000);

            }
        });

    }

    public void carDisconnected() {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "CAR DISCONNECTED");
                vehicle_connected = false;
                soundpool.play((soundIds[DISCONNECTED]), 1, 1, 1, 0, 1.0f);
                game_status.setText("DISCONNECTED");
                link_quality.setText("0%");
                if(heartbeat_timer != null){
                    heartbeat_timer.cancel();
                    heartbeat_timer = null;
                }

            }
        });
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(false);
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Boolean drawer_open = true;
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = null;
        switch (menuItem.getItemId()) {
            case R.id.game_menu:
                black_overlay.setVisibility(View.VISIBLE); //to show black background behind preference fragment
                fragmentClass = game_menu.class;
                break;
            case R.id.nav_vehicle:
                black_overlay.setVisibility(View.VISIBLE); //to show black background behind preference fragment
                fragmentClass = prefs_vehicle.class;
                menuItem.setChecked(true);
                break;
            case R.id.nav_connection:
                black_overlay.setVisibility(View.VISIBLE); //to show black background behind preference fragment
                fragmentClass = prefs_connection.class;
                menuItem.setChecked(true);
                break;
            case R.id.nav_development:
                black_overlay.setVisibility(View.VISIBLE); //to show black background behind preference fragment
                fragmentClass = prefs_development.class;
                menuItem.setChecked(true);
                break;
            case R.id.nav_close:
                black_overlay.setVisibility(View.GONE); //to show black background behind preference fragment
                mDrawer.setDrawerLockMode(LOCK_MODE_UNLOCKED);
                mDrawer.closeDrawers();
                menuItem.setChecked(false);
                drawer_open = false;
                loadPrefs();
                loadViewPrefs();
                break;
            default:
                black_overlay.setVisibility(View.GONE); //to show black background behind preference fragment
                mDrawer.setDrawerLockMode(LOCK_MODE_UNLOCKED);
                mDrawer.closeDrawers();
                menuItem.setChecked(false);
                drawer_open = false;
                loadPrefs();
                loadViewPrefs();
        }


        if (drawer_open) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mDrawer.setDrawerLockMode(LOCK_MODE_LOCKED_OPEN);
            mDrawer.setScrimColor(Color.TRANSPARENT); //disable transparency

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.settings_view, fragment, "SETTINGS_FRAGMENT").commit();
        } else { //remove the settings fragment when drawer closed
            black_overlay.setVisibility(View.GONE); //to show black background behind preference fragment
            Fragment current_fragment = getSupportFragmentManager().findFragmentByTag("SETTINGS_FRAGMENT");
            if (current_fragment != null)
                getSupportFragmentManager().beginTransaction().remove(current_fragment).commit();
        }
    }


    @Override
    public void onBackPressed() {
//        if (!shouldAllowBack()) {
//            doSomething();
//        } else {
        //           super.onBackPressed();
        //       }
    }

    public void loadPrefs() {
        prefs = getDefaultSharedPreferences(this);
//vehicle prefs
        top_speed = Integer.parseInt(prefs.getString("max_speed", "0"));  //(string key, default) PreferenceFragmentCompat does not support integer from edittext
        min_speed = Integer.parseInt(prefs.getString("min_speed", "0"));
        acceleration_rate = Integer.parseInt(prefs.getString("accel_rate", "0"));
        deceleration_rate = Integer.parseInt(prefs.getString("decel_rate", "0"));
        brake_rate = Integer.parseInt(prefs.getString("brake_rate", "0"));
        rev_min_speed = Integer.parseInt(prefs.getString("rev_min_speed", "0"));
        rev_top_speed = Integer.parseInt(prefs.getString("rev_max_speed", "0"));
        servo_center = Integer.parseInt(prefs.getString("servo_center", Integer.toString(1500)));
        servo_min = Integer.parseInt(prefs.getString("servo_min", Integer.toString(800)));
        servo_max = Integer.parseInt(prefs.getString("servo_max", Integer.toString(2200)));

        control_loop_time = Integer.parseInt(prefs.getString("control_loop_time", Integer.toString(50)));
        CONTROL_SERVER_IP = prefs.getString("vehicle_ip", CONTROL_SERVER_IP);
        CONTROL_SERVER_PORT = Integer.parseInt(prefs.getString("vehicle_control_port", Integer.toString(CONTROL_SERVER_PORT)));
        boost_pref = prefs.getBoolean("boost_pref", false);
        playername_bar = (TextView) findViewById(R.id.player_name_bar);
        playername_bar.setText(prefs.getString("player_name", "Player Name"));

        max_video_bitrate = Integer.parseInt(prefs.getString("max_video_bitrate", Integer.toString(5000000)));
        min_video_bitrate = Integer.parseInt(prefs.getString("min_video_bitrate", Integer.toString(500000)));
        video_bitrate = Integer.parseInt(prefs.getString("video_bitrate", Integer.toString(500000)));
    }

    public void loadViewPrefs() {
        if (prefs.getBoolean("development_buttons", false) == false)
            dev_buttons.setVisibility(View.GONE);
        else dev_buttons.setVisibility(View.VISIBLE);
        motor_progress.setMax(top_speed - min_speed);
        bitrateSeekBar.setMax(max_video_bitrate - min_video_bitrate); //max-min
        bitrateSeekBar.setProgress(video_bitrate - min_video_bitrate);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        //what to do if connection settings changed?

    }

    public void setupViews() {
        mDrawer = findViewById(R.id.drawer_layout);
        nvDrawer = findViewById(R.id.nav_view);
        setupDrawerContent(nvDrawer);
        settings_view = (FrameLayout) this.findViewById(R.id.settings_view);
        black_overlay = (View) findViewById(R.id.black_overlay_view);

        steering_seekbar = (SeekBar) this.findViewById(R.id.steering_seek);
        steering_seekbar.setMax(servo_max);
        steering_seekbar.setMin(servo_min);
        steering_seekbar.setRotation(180);
        steering_seekbar.setProgress(servo_center);

        brightnessSeekBar = (SeekBar) this.findViewById(R.id.BrightnessSeekBar);
        brightnessSeekBar.setMax(40); //max-min
        brightnessSeekBar.setProgress(20); //max/2 for 50%
        brightnessSeekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#ff6d00"), PorterDuff.Mode.MULTIPLY));

        bitrateSeekBar = (SeekBar) this.findViewById(R.id.bitrateSeekBar);
        bitrateSeekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#ff6d00"), PorterDuff.Mode.MULTIPLY));


        dev_buttons = (View) this.findViewById(R.id.development_buttons);
        gas_seek = (SeekBar) this.findViewById(R.id.gas_seek);
        brake_seek = (SeekBar) this.findViewById(R.id.brake_seek);
        motor_progress = (ProgressBar) this.findViewById(R.id.motor_progress_bar);
        speedometer_text = (TextView) this.findViewById(R.id.speedometer_text);
        servo_text = (TextView) this.findViewById(R.id.servo_text);
        motor_text = (TextView) this.findViewById(R.id.motor_text);
        //motor_progress.setRotation(180);
        game_item_box = (ImageView) this.findViewById(R.id.game_item_box);
        socket_text = (TextView) this.findViewById(R.id.socket_text);


        gameview_layout = (FrameLayout) this.findViewById(R.id.GameLayer);
        elapsed_time_textview = (TextView) this.findViewById(R.id.elapsed_time);
        game_display = (TextView) this.findViewById(R.id.game_display);
        game_summary = (TextView) this.findViewById(R.id.game_summary);
        game_alert_tv = (TextView) findViewById(R.id.game_alert_tv);
        game_status = (TextView) findViewById(R.id.game_status_bar);
        link_quality = (TextView) findViewById(R.id.link_quality);

        free_roam = findViewById(R.id.free_roam);
        ready_button = findViewById(R.id.ready_button);
        leave_button = findViewById(R.id.leave_button);
        video_filter = findViewById(R.id.video_filter);

    }

    public void resetView(TextView tv) {
        tv.setText("");
        tv.setScaleX(1);
        tv.setScaleY(1);
        tv.setAlpha(1);
    }

    public void startGameEvent() {
        mDrawer.setDrawerLockMode(LOCK_MODE_UNLOCKED);
        mDrawer.closeDrawers();
        Fragment current_fragment = getSupportFragmentManager().findFragmentByTag("SETTINGS_FRAGMENT");
        if (current_fragment != null)
            getSupportFragmentManager().beginTransaction().remove(current_fragment).commit();
        gameview_layout.setVisibility(View.VISIBLE);
        black_overlay.setVisibility(View.GONE);
        resetView(game_summary);
        resetView(game_alert_tv);
        soundpool.play((soundIds[RACE_IS_STARTING]), 1, 1, 1, 0, 1.0f);
        resetView(game_alert_tv);
        game_alert_tv.setText("Get to the start line");
        ready_button.setVisibility(View.VISIBLE);
        leave_button.setVisibility(View.VISIBLE);
        elapsedTime = 0;
        elapsed_time_textview.setVisibility(View.VISIBLE);
        elapsed_time_textview.setText(Game.formatTime(elapsedTime));
        //wait for ready_button to go to readyToStartEvent
    }

    public void readyToStartEvent(long seconds_to_start) {
        resetView(game_alert_tv);
        ready_button.setVisibility(View.GONE);
        leave_button.setVisibility(View.GONE);
        new CountDownTimer((seconds_to_start * 1000), 1000) {
            public void onTick(long millisUntilFinished) {
                resetView(game_alert_tv);
                game_alert_tv.setText("" + (millisUntilFinished + 1000) / 1000);
                if(millisUntilFinished <= 3500 && millisUntilFinished >= 2500) {
                    soundpool.play((soundIds[RACE_START]), 1, 1, 1, 0, 1.0f);
                }
            }

            public void onFinish() {
                game_alert_tv.setText("GO!");
                game_alert_tv.animate().scaleX(2f).scaleY(2f).alpha(0).setDuration(1000);
                Game.mygame.Start();
                game_summary.setAlpha(0.4f);
                timer_handler.removeCallbacks(incrementElapsedTimer);
                timer_handler.postDelayed(incrementElapsedTimer, 1000);
            }
        }.start();
    }

    private Runnable incrementElapsedTimer = new Runnable() {
        public void run() {
            elapsedTime += 1000;
            elapsed_time_textview.setText(Game.formatTime(elapsedTime));
            timer_handler.postDelayed(incrementElapsedTimer, 1000);
        }
    };


    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }

    public static void updatePostGameScreen(String player_stats) {
        String new_text = "";
        if (!Game.allFinished()) {
            new_text = "Waiting for " + Game.players_not_finished + " players to finish...\n\n";
            new_text += player_stats;
            game_summary.setText(new_text);
        } else {
            new_text = "RACE FINISHED\n\n";
            new_text += player_stats;
            game_summary.setText(new_text);
        }
    }

    public void myplayerGateEvent(Player player) {
        soundpool.play((soundIds[GATE]), 0.7f, 0.7f, 1, 0, 1.0f);
    }

    public void myplayerLapEvent(Player player) {
        resetView(game_alert_tv);
        game_alert_tv.setText("Lap " + player.current_lap + " of " + Game.mygame.total_laps);
        game_alert_tv.animate().scaleX(1f).scaleY(1f).alpha(0).setDuration(2000);
        game_display.setText("Lap " + player.current_lap + "/" + Game.mygame.total_laps);
        if (player.current_lap == Game.mygame.total_laps) {  //final lap
            soundpool.play((soundIds[FINAL_LAP]), 1, 1, 1, 0, 1.0f);
        } else {
            soundpool.play((soundIds[LAP]), 1, 1, 1, 0, 1.0f);
        }
    }

    public void myplayerFinishedEvent(Player player) {
        resetView(game_alert_tv);
        game_summary.setAlpha(0);
        game_display.setVisibility(View.INVISIBLE);
        game_alert_tv.setText("FINISHED!");
        game_alert_tv.animate().scaleX(2f).scaleY(2f).alpha(0).setDuration(2000);
        soundpool.play((soundIds[RACE_FINISH]), 1, 1, 1, 0, 1.0f);
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                game_summary.animate().alpha(1f).setDuration(3000);
            }
        }.start();
    }

    public void raceEndEvent(Player player) {
        mediaplayer = MediaPlayer.create(getBaseContext(), R.raw.bensound_moose);
        mediaplayer.start();
        free_roam.setVisibility(View.VISIBLE);
        elapsedTime = 0;
        timer_handler.removeCallbacks(incrementElapsedTimer);
    }

    public void cleanupGame() {
        Log.d(TAG, "Cleanup game.");
        Game.mygame = null;
        Game.destroyPlayers();
        elapsedTime = 0;
        timer_handler.removeCallbacks(incrementElapsedTimer);
        gameview_layout.setVisibility(View.GONE);
        black_overlay.setVisibility(View.GONE);
        if (mediaplayer != null) {
            try {
                mediaplayer.stop();
                mediaplayer.release();
            } catch (IllegalStateException e) {

            }
        }
        GameServer.cleanup();
        GameClient.cleanup();
        game_status_string = "Free Roam";
        game_status.setText(game_status_string);
        game_item_box.setImageResource(0);
        mystery_box_available = true;
        game_item_box.setOnTouchListener(null);
    }

    public void soloRace(int laps) {
        Game.mygame = new Game(mainactivity);
        Game.total_laps = laps;
        Game.multiplayer = false;
        startGameEvent();
    }

//    public void processColorData(String colorReadings[]){
//        for(int i = 0; i<colorReadings.length;i++) {
//            int value1 = Integer.valueOf(colorReadings[i].substring(0, colorReadings[i].indexOf("|")));
//            int value2 = Integer.valueOf(colorReadings[i].substring(colorReadings[i].indexOf("|") + 1, colorReadings[i].indexOf("$")));
//            int value3 = Integer.valueOf(colorReadings[i].substring(colorReadings[i].indexOf("$") + 1));
//            if (Game.mygame != null && finish_line.matchWithRatio(value1, value2, value3))
//                Game.lap(Game.my_player);
//            if (Game.mygame != null && gate.matchWithRatio(value1, value2, value3))
//                Game.gate(Game.my_player);
//            if (boost.matchWithRatio(value1, value2, value3)) {
//                boost();
//            }
//            if (mystery_box.matchWithRatio(value1, value2, value3)) {
//                mysteryBoxPickup();
//            }
//            currentColor[0] = value1;
//            currentColor[1] = value2;
//            currentColor[2] = value3;
//
//            //display color on screen
//            currentHSV = ColorSensor.convertToHSV(value1, value2, value3);
//            TextView hue_tv = findViewById(R.id.hue);
//            hue_tv.setText("Hue: " + String.valueOf(currentHSV[0]) + "  Sat: " + currentHSV[1] + "  R:" + value1 + " G:" + value2 + " B:" + value3);
//            View v = findViewById(R.id.color_view);
//            float display_hsv[] = currentHSV;
//            display_hsv[1] = display_hsv[1] * 1.5f; //boost saturation a bit for display purposes only
//            v.getBackground().setColorFilter(Color.HSVToColor(display_hsv), PorterDuff.Mode.SRC_IN);
//            v.invalidate();
//
//        }
//
//    }

    public boolean boost_available = true;
    public final int boost_speed = 3500;

    public void boost() {
        if (boost_available && boost_pref) {
            soundpool.play((soundIds[NITROUS]), 1, 1, 1, 0, 1.0f);
            boost_available = false;
            final int min_speed_old = min_speed;
            final int max_speed_old = top_speed;
            min_speed = boost_speed;
            if (top_speed < boost_speed) {
                top_speed = boost_speed;
            }
            motor_output_value = boost_speed;

            new CountDownTimer(250, 100) {
                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    min_speed = min_speed_old;
                    top_speed = max_speed_old;
                    boost_available = true;
                }
            }.start();
        }
    }


    public boolean mystery_box_available = true;

    public void mysteryBoxPickup() {
        if (mystery_box_available) {
            mystery_box_available = false;
            soundpool.play((soundIds[ITEM_BOX]), 1, 1, 1, 0, 1.0f);
            final Random rand = new Random();
            new CountDownTimer(300, 50) {
                public void onTick(long millisUntilFinished) {
                    //cycle through images randomly, ignoring their actual weight
                    PowerUp random_item = PowerUp.getByItem(rand.nextInt(PowerUp.number_of_items) + 1); //our item numbers start at 1
                    game_item_box.setImageResource(random_item.image);
                    game_item_box.setColorFilter(random_item.color_filter, PorterDuff.Mode.MULTIPLY);
                }

                public void onFinish() {
                    //"land" on a powerup using their weight
                    final PowerUp readyPowerUp = PowerUp.randomWeightedPowerUp();
                    game_item_box.setImageResource(readyPowerUp.image);
                    game_item_box.setColorFilter(readyPowerUp.color_filter, PorterDuff.Mode.MULTIPLY);
                    game_item_box.setOnTouchListener(new ImageView.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            game_item_box.setImageResource(0);
                            mystery_box_available = true;
                            readyPowerUp.method.run();
                            game_item_box.setOnTouchListener(null);
                            return true;
                        }
                    });
                }
            }.start();
        }

    }

    public void createPowerUps() {
        new PowerUp(R.drawable.fast_forward, 0.25, Color.parseColor("#6400ff00"), new Runnable() {
            @Override
            public void run() {
                boostPowerUp();
            }
        });
        new PowerUp(R.drawable.missile, 0.00, Color.parseColor("#64ff5d00"), null);
        new PowerUp(R.drawable.shield, 0.25, Color.parseColor("#640000ff"), new Runnable() {
            @Override
            public void run() {
                shieldApplied();
            }
        });
        new PowerUp(R.drawable.magnet, 0.25, Color.parseColor("#64ff0000"), new Runnable() {
            @Override
            public void run() {
                fireEMP();
            }
        });
        new PowerUp(R.drawable.spikeball, 0.25, Color.parseColor("#64ff00ff"), new Runnable() {
            @Override
            public void run() {
                setTrap();
            }
    });
    }

    public void processWifiQualityMessage(String message){
        //let's factor in the phone's wifi link later
    //    sample: "Link Quality=53/70 Signal level=-57 dBm"
            float quality = 0.0f;
            String quality_str = "";
            quality = (Float.valueOf(message.substring(13,15)))/70.0f;
            quality_str = String.valueOf(Math.round(quality * 100));
            link_quality.setText(quality_str + "%");
    }

    public void boostPowerUp() {
        int boost_speed = 3500;
        soundpool.play((soundIds[NITROUS]), 1, 1, 1, 0, 1.0f);
        final int min_speed_old = min_speed;
        final int max_speed_old = top_speed;
        min_speed = boost_speed;
        if (top_speed < boost_speed) {
            top_speed = boost_speed;
        }
        motor_output_value = boost_speed;
        ColorDrawable viewColor = (ColorDrawable) video_filter.getBackground();
        final int old_color = viewColor.getColor();
        final float old_alpha = video_filter.getAlpha();
        video_filter.setBackgroundColor(Color.parseColor("#00FF00"));
        video_filter.setAlpha(0.15f);

        new CountDownTimer(250, 100) {
            public void onTick(long millisUntilFinished) {            }
            public void onFinish() {
                min_speed = min_speed_old;
                top_speed = max_speed_old;
                video_filter.setBackgroundColor(old_color);
                video_filter.setAlpha(old_alpha);
            }
        }.start();
    }

    public void shieldApplied(){
        if(Game.my_player != null){
            Game.my_player.has_shield = true;
        }
        soundpool.play((soundIds[SHIELD]), 1, 1, 1, 0, 1.0f);
        ColorDrawable viewColor = (ColorDrawable) video_filter.getBackground();
        final int old_color = viewColor.getColor();
        final float old_alpha = video_filter.getAlpha();
        video_filter.setBackgroundColor(Color.parseColor("#0000FF"));
        video_filter.setAlpha(0.15f);

        new CountDownTimer(200, 100) {
            public void onTick(long millisUntilFinished) {            }
            public void onFinish() {
                video_filter.setBackgroundColor(old_color);
                video_filter.setAlpha(old_alpha);
            }
        }.start();
    }

    public void hitWithEMP(){
        ColorDrawable viewColor = (ColorDrawable) video_filter.getBackground();
        final int old_color = viewColor.getColor();
        final float old_alpha = video_filter.getAlpha();
        if(Game.my_player.has_shield){
            soundpool.play((soundIds[SHIELD]), 1, 1, 1, 0, 1.0f);
            Game.my_player.has_shield = false;
            resetView(game_alert_tv);
            game_alert_tv.setText("EMP BLOCKED!");
            game_alert_tv.animate().scaleX(1f).scaleY(1f).alpha(0).setDuration(1500);
            return;
        }
        int slow_speed = 500;
        soundpool.play((soundIds[SLOW]), 1, 1, 1, 0, 1.0f);
        final int min_speed_old = min_speed;
        final int max_speed_old = top_speed;
        video_filter.setBackgroundColor(Color.parseColor("#FF0000"));
        video_filter.setAlpha(0.3f);
        top_speed = slow_speed;
        min_speed = slow_speed;
        motor_output_value = slow_speed;
        resetView(game_alert_tv);
        game_alert_tv.setText("HIT WITH EMP!");
        game_alert_tv.animate().scaleX(1f).scaleY(1f).alpha(0).setDuration(1500);

        new CountDownTimer(500, 100) {
            public void onTick(long millisUntilFinished) {            }
            public void onFinish() {
                min_speed = min_speed_old;
                top_speed = max_speed_old;
                video_filter.setBackgroundColor(old_color);
                video_filter.setAlpha(old_alpha);
            }
        }.start();
    }

    public void setTrap(){
        soundpool.play((soundIds[CHAINS]), 1, 1, 1, 0, 1.0f);
        if(Game.mygame != null){
            Game.multiplayerTrap(Game.my_player, true);
            resetView(game_alert_tv);
            game_alert_tv.setText("SPIKE STRIP SET");
            game_alert_tv.animate().scaleX(1f).scaleY(1f).alpha(0).setDuration(1500);
        }
    }

    public void fireEMP(){
        soundpool.play((soundIds[LASER]), 1, 1, 1, 0, 1.0f);
        if(Game.mygame != null){
            Game.multiplayerEMP(Game.my_player, true);
        }
    }

    public void hitTrap(){
        if(Game.mygame != null){
            Game.multiplayerTrapCancel(Game.my_player, true);
        }
        int slow_speed = 0;
        soundpool.play((soundIds[TIRE_POP]), 1, 1, 1, 0, 1.0f);
        final int min_speed_old = min_speed;
        final int max_speed_old = top_speed;
        ColorDrawable viewColor = (ColorDrawable) video_filter.getBackground();
        final int old_color = viewColor.getColor();
        final float old_alpha = video_filter.getAlpha();
        video_filter.setBackgroundColor(Color.parseColor("#FF0000"));
        video_filter.setAlpha(0.5f);
        top_speed = slow_speed;
        min_speed = slow_speed;
        motor_output_value = slow_speed;
        resetView(game_alert_tv);
        game_alert_tv.setText("HIT SPIKE STRIP!");
        game_alert_tv.animate().scaleX(1f).scaleY(1f).alpha(0).setDuration(1500);

        new CountDownTimer(3000, 100) {
            public void onTick(long millisUntilFinished) {            }
            public void onFinish() {
                min_speed = min_speed_old;
                top_speed = max_speed_old;
                video_filter.setBackgroundColor(old_color);
                video_filter.setAlpha(old_alpha);
            }
        }.start();
    }

}


