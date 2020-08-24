package sg.mdp.ntu.MDP15;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

public class MainActivity extends AppCompatActivity {
    GridLayout gridLayout;
    ImageView robot;
    TextView startZone;
    TextView goalZone;
    static MazeManager mazeManager;
    
    BluetoothDialog btDialog;
    private Toolbar toolbar;
    Button btnSetStartPoint;
    ImageButton btnUP;
    ImageButton btnDown;
    ImageButton btnLeft;
    ImageButton btnRight;
    TextView tvStatus;
    SharedPreferences pref;
    String function1,function2;
    static RobotManager robotManager;
    private boolean connected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connected = false;
        btDialog = new BluetoothDialog();
        setContentView(R.layout.activity_main);

        // robot
        robot = findViewById(R.id.robot);
        robotManager = new RobotManager(robot, getApplicationContext());
        robot.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {}
            @Override
            public void onDoubleClick(View view) {
                robotManager.rotateLeft();
            }
        }));

        // prepare maze grids
        gridLayout = findViewById(R.id.maze);
        RelativeLayout[][] maze = new RelativeLayout[20][15];
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
                            } else {
                                MazeManager.setGrid((RelativeLayout) layout, getApplicationContext(), getString(R.string.maze_waypoint));
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

        pref = getSharedPreferences("MDP_FUNCTIONS",Context.MODE_PRIVATE);

        //Buttons
        btnUP = findViewById(R.id.btnUP);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        tvStatus = findViewById(R.id.tbRobotStatus);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(broadcastReceiver, filter);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    public void openBluetooth(MenuItem item){
        btDialog.show(getSupportFragmentManager(),"Bluetooth");
    }

    public void openReconfigure(MenuItem item){
        ReconfigureDialog rfDialog = new ReconfigureDialog();
        rfDialog.show(getSupportFragmentManager(),"Reconfigure");
    }

    public void up(View v){
        if( connected){
            btDialog.senddata("f");
        }
    }
    public void down(View v){
        if( connected){
            btDialog.senddata("r");
        }
    }
    public void left(View v){
        if( connected){
            btDialog.senddata("tl");
        }
    }
    public void right(View v){
        if( connected){
            btDialog.senddata("tr");
        }
    }

    public void sendfunction1(View view){
        //Get stored value
        function1 = pref.getString("Function1","f");
        if( connected){
            btDialog.senddata(function1);
        }

    }

    public void sendfunction2(View view){
        //Get stored value
        function2 = pref.getString("Function2","r");
        if( connected){
            btDialog.senddata(function2);
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        BluetoothDevice device;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Device is now Connected",    Toast.LENGTH_SHORT).show();
                connected = true;
                tvStatus.setText("Bluetooth is connected");
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Device is disconnected",       Toast.LENGTH_SHORT).show();
                connected = false;
                tvStatus.setText("Bluetooth died");
            }
        }
    };

    public void FastestPath(View view) {
        if( connected){
            btDialog.senddata("beginExplore");
        }
    }

}