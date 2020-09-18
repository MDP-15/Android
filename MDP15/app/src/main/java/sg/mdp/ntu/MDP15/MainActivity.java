package sg.mdp.ntu.MDP15;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;

import java.math.BigInteger;
import java.util.ArrayList;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //Maze stuff
    GridLayout gridLayout;
    ImageView robot;
    TextView startZone;
    TextView goalZone;
    static MazeManager mazeManager;
    RelativeLayout[][] maze;

    //Bluetooth stuff
    static BluetoothDialog btDialog;
    private Toolbar toolbar;
    Button btnSetStartPoint;
    Button autoManual;
    Button refresh;
    ImageButton btnUP;
    ImageButton btnDown;
    ImageButton btnLeft;
    ImageButton btnRight;
    TextView tvStatus;
    TextView tvmotion;
    TextView bluetoothStatus;
    SharedPreferences pref;
    String function1,function2;
    static RobotManager robotManager;

    //Accelerometer stuff
    boolean motionONOFF = false;
    SensorManager sensorManager;
    Sensor sensor;
    boolean sending = false;


    //Waypoint stuff
    int curx;
    int cury;
    String oldBg;
    String oldRes;

    SharedPreferences mdpAM;
    SharedPreferences.Editor mdpAMEditor;

    SharedPreferences mdfpref;
    SharedPreferences.Editor mdfprefeditor;

    boolean maprefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //connected = false;
        btDialog = new BluetoothDialog();
        setContentView(R.layout.activity_main);
        curx = 0;
        cury = 0;
        oldBg = "";
        oldRes = "";
        maprefresh = true;
        //Accelerometer
        //initialize sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        //Shared preference
        mdpAM = getSharedPreferences("MDPAM",Context.MODE_PRIVATE);
        mdpAMEditor = getSharedPreferences("MDPAM",Context.MODE_PRIVATE).edit();

        mdfprefeditor = getSharedPreferences("MDP_MDF", Context.MODE_PRIVATE).edit();
        mdfpref = getSharedPreferences("MDP_MDF",Context.MODE_PRIVATE);

        maprefresh = mdpAM.getBoolean("MDP_REFRESH",false);
        

        // robot
        robot = findViewById(R.id.robot);
        robotManager = new RobotManager(robot, getApplicationContext());
        robot.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {
                btDialog.senddata("tr");
                robotManager.rotateRight();
            }
            @Override
            public void onDoubleClick(View view) {
                btDialog.senddata("tl");
                robotManager.rotateLeft();
            }
        }));

        // prepare maze grids
        gridLayout = findViewById(R.id.maze);
        maze = new RelativeLayout[20][15];
        createMap();

        // initialize maze manager
        mazeManager = new MazeManager(maze, getApplicationContext());
        mazeManager.reset();


        //Toolbar
        toolbar = (Toolbar)findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        btnSetStartPoint = (Button)findViewById(R.id.btnSetStartPoint);
        btnSetStartPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btDialog.senddata("f");
            }
        });

        //Get saved stuff
        pref = getSharedPreferences("MDP_FUNCTIONS",Context.MODE_PRIVATE);

        //Buttons and textviews
        btnUP = findViewById(R.id.btnUP);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        tvStatus = findViewById(R.id.tbRobotStatus);
        tvmotion = findViewById(R.id.tbMotion);
        bluetoothStatus = findViewById(R.id.tbBT);
        autoManual = findViewById(R.id.autoManaualbtn);
        refresh = findViewById(R.id.refreshbtn);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(broadcastReceiver, filter);
    }

    private void start() {
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_FASTEST);
    }

    //when app minimize, turn off sensor
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        tvmotion.setText("Motion Sensor: OFF");
    }

    //when app reopen, turn back on sensor
    @Override
    protected void onResume() {
        super.onResume();
    }

    //when app close, turn off sensor
    //Might not need this?
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        tvmotion.setText("Motion Sensor: OFF");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    public void openWaypoint(final int finalK, final int finalJ){
        new AlertDialog.Builder(this)
                .setTitle("Waypoint")
                .setMessage("Set waypoint at X="+finalK+", Y= "+finalJ+"?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mazeManager.setGrid(cury,curx,getString(R.string.maze_empty));
                        Log.d("MAINMAZE",oldRes);
                        Log.d("MAINMAZE",oldBg);
                        mazeManager.setGrid(finalK,finalJ,getString(R.string.maze_waypoint));
                        btDialog.senddata("{\"Waypoint\":\"X\":"+finalK+","+"\"Y\":"+finalJ+"}");
                        curx = finalJ;
                        cury = finalK;
                    }
                })
                .setNegativeButton("No",null).show();
    }

    public void openBluetooth(MenuItem item){
        btDialog.show(getSupportFragmentManager(),"Bluetooth");
    }

    public void openReconfigure(MenuItem item){
        ReconfigureDialog rfDialog = new ReconfigureDialog();
        rfDialog.show(getSupportFragmentManager(),"Reconfigure");
    }

    public void motionONOFF(MenuItem item){
        if(!motionONOFF){
            start();
            motionONOFF = true;
            tvmotion.setText("Motion Sensor: ON");
        }
        else{
            sensorManager.unregisterListener(this);
            motionONOFF = false;
            tvmotion.setText("Motion Sensor: OFF");
        }
    }

    public void setWaypoint(View view){
        WaypointDialog wpDialog = new WaypointDialog();
        wpDialog.show(getSupportFragmentManager(),"Waypoint");
    }

    public void startExplore(View view0){

    }

    public void up(View v){
            robotManager.moveForward();
            btDialog.senddata("f");

    }
    public void down(View v){
            robotManager.moveBack();
            btDialog.senddata("r");

    }
    public void left(View v){
            robotManager.rotateLeft();
            btDialog.senddata("tl");

    }
    public void right(View v){
            robotManager.rotateRight();
            btDialog.senddata("tr");

    }

    public void sendfunction1(View view){
        //Get stored value
        function1 = pref.getString("Function1","f");

            btDialog.senddata(function1);


    }

    public void sendfunction2(View view){
        //Get stored value
        function2 = pref.getString("Function2","r");

            btDialog.senddata(function2);

    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        BluetoothDevice device;
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MAIN","IN BR");
            String action = intent.getAction();
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Device is now Connected", Toast.LENGTH_SHORT).show();
                bluetoothStatus.setText("Bluetooth: Connected");
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Device is disconnected",       Toast.LENGTH_SHORT).show();
                bluetoothStatus.setText("Bluetooth: Disconnected");
               // btDialog.reconnectBT();
            }
        }
    };

    public void FastestPath(View view) {
            btDialog.senddata("beginExplore");
    }

    private void createMap() {
        int j = 19;
        int k = 0;
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            final View layout = gridLayout.getChildAt(i);
            maze[j][k] = (RelativeLayout) layout;
            final int finalJ = j;
            final int finalK = k;
            layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Log.d("MainMaze","X = "+motionEvent.getRawX()+ "Y = " + motionEvent.getRawY());


                    final PopupWindow popupWindow = new PopupWindow(getApplicationContext());
                    ArrayList<String> sortList = new ArrayList<String>();
                    sortList.add("Move robot here");
                    sortList.add("Place waypoint here");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, sortList);
                    ListView options = new ListView(getApplicationContext());
                    options.setAdapter(adapter);
                    options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView parent, View view, int position, long id) {
                            if (position == 0) {
                                robotManager.setRobotCoordinates(finalK, finalJ);
                                btDialog.senddata("{\"RobotPos\":\"X\":"+finalK+","+"\"Y:"+finalJ+"\"}");
                            } else {
                                openWaypoint(finalK,finalJ);
                            }
                            popupWindow.dismiss();
                        }
                    });
                    popupWindow.setFocusable(true);
                    popupWindow.setWidth(175);
                    popupWindow.setBackgroundDrawable(getDrawable(R.color.popupWindowColor));
                    popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                    popupWindow.setContentView(options);
                    popupWindow.showAsDropDown(findViewById(R.id.screen), (int) motionEvent.getRawX(), (int) (motionEvent.getRawY() - 95));
                    return false;
                }
            });
            k++;
            if (k >= 15) {
                k = 0;
                j--;
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x=0 ,y=0 ,z = 0;
        //get the values from the sensor
        if(event.sensor.getType() == 1){
            x = event.values[0];
            y = event.values[1];
        }
        //i did all the testing already
        if(x > 4){
            if(!sending){
                sending = true;
                Log.d("Accel","Left");
                //send data
                robotManager.rotateLeft();
                btDialog.senddata("tl");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sending = false;
                    }
                },1000);
           }

        }
        if(x < -4){
            if(!sending){
                sending = true;
                //send data
                robotManager.rotateRight();
                Log.d("Accel","Right");
                btDialog.senddata("tr");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sending = false;
                    }
                },1000);
            }

        }
        if(y > 4){
            if(!sending){
                sending = true;
                //send data
                robotManager.moveBack();
                btDialog.senddata("r");
                Log.d("Accel","Backward");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sending = false;
                    }
                },1000);
            }

        }
        if(y < -4){
            if(!sending){
                sending = true;
                //send data
                robotManager.moveForward();
                btDialog.senddata("f");
                Log.d("Accel","Front");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sending = false;
                    }
                },1000);
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void autoManual(View view) {
        if(!maprefresh){ //Manual to Auto
            autoManual.setText("Auto");
            refresh.setEnabled(false);
            refresh.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_button_clicked));
            maprefresh = true;
            mdpAMEditor.putBoolean("MDF_REFRESH",true); //Auto refresh map
            mdpAMEditor.commit();
            Log.d("AutoManual","true");

        }
        else{// Auto to Manual
            autoManual.setText("Manual");
            refresh.setEnabled(true);
            refresh.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_button));
            maprefresh = false;
            mdpAMEditor.putBoolean("MDF_REFRESH",false); //Manually refresh map
            mdpAMEditor.commit();
            Log.d("AutoManual","false");
        }
    }

    public void refreshMap(View view) {
        Log.d("MDF","BUTTON IS PRESSED");
        plotMap();
    }

    private void plotMap(){
        Log.d("MDF","IT BEGINS");
        String mapdesriptor = mdfpref.getString("AMDMDF","");
        Log.d("MDF",mapdesriptor);
        if(mapdesriptor.equals("")){
            return;
        }
        String curr;
        String mdf = "",mdfbin;
        for(int j = 0; j < 75; j++){
            curr = String.valueOf(mapdesriptor.charAt(j));
            mdfbin = new BigInteger(curr,16).toString(2);
            if(mdfbin.length() == 1)
                mdfbin = "000" + mdfbin;
            if(mdfbin.length() == 2)
                mdfbin = "00" + mdfbin;
            if(mdfbin.length() == 3)
                mdfbin = "0" + mdfbin;
            mdf = mdf + mdfbin;
        }
        Log.d("Maze",mdf);
        //Plot Map
        int column = 19, row = 0;
        for(int k = 0; k < mdf.length(); k++){
            curr = String.valueOf(mdf.charAt(k));
            if(curr.equals("1")){
                MainActivity.mazeManager.setGrid(row,column,"Obstacle");

            }
            else{
                MainActivity.mazeManager.setGrid(row,column,"Empty");

            }
            row++;
            if(row > 14){
                row = 0;
                column--;
            }
        }
    }

    public void setRobotCoordinates(View view) {
        RobotDialog rDialog = new RobotDialog();
        rDialog.show(getSupportFragmentManager(),"Robot");
    }
}