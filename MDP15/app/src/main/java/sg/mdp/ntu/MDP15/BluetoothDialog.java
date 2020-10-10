package sg.mdp.ntu.MDP15;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;

public class BluetoothDialog extends AppCompatDialogFragment {
    private static final UUID mdpUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private EditText editusername;
    private EditText editpassword;
    private final String TAG = "BluetoothDialog";
    private BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    ListView lvNewDevices;
    public DeviceListAdapter mDeviceListAdapter;
    BluetoothDevice mBTDevice;
    BluetoothConnectionService mBluetoothConnection;
    static Handler mHandler;

    boolean loadmap = false;

    Button btnONOFF;
    Button btnScan;
    TextView tvStatus;
    TextView bluetoothStatus;


    //MDF
    SharedPreferences.Editor editor;
    SharedPreferences pref;

    //Auto Manual
    SharedPreferences mdpAM;
    SharedPreferences.Editor mdpAMEditor;

    boolean mdpautomanaual;

    String data;
    int x,y;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_bluetooth,null);
        tvStatus = (TextView)getActivity().findViewById(R.id.tbRobotStatus);
        bluetoothStatus = (TextView)getActivity().findViewById(R.id.tbBT);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnONOFF = (Button)view.findViewById(R.id.btnONOFF);
        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enabling/disabling BT");
                enableDisableBT();
            }
        });


        editor = getActivity().getSharedPreferences("MDP_MDF", Context.MODE_PRIVATE).edit();
        pref = getActivity().getSharedPreferences("MDP_MDF",Context.MODE_PRIVATE);

        mdpAM = getActivity().getSharedPreferences("MDPAM",Context.MODE_PRIVATE);
        mdpAMEditor = getActivity().getSharedPreferences("MDPAM",Context.MODE_PRIVATE).edit();
        mdpautomanaual = mdpAM.getBoolean("MDF_REFRESH",true);

        lvNewDevices = (ListView)view.findViewById(R.id.lvNewDevices);
        lvNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                //tvStatus.setText("Status Nothing");
                btnScan.setText("Scan");

                Log.d(TAG,"onItemClick: You clicked on a device");
                String deviceName = mBTDevices.get(position).getName();
                String deviceAddress = mBTDevices.get(position).getAddress();

                Log.d(TAG,"onItemClick: Device Name: "+deviceName);
                Log.d(TAG,"onItemClick: Device Address: "+deviceAddress);

                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                    Log.d(TAG,"Trying to pair with "+deviceName);
                    mBTDevices.get(position).createBond();
                    mBTDevice = mBTDevices.get(position);

                    mHandler = new Handler() {

                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            switch (msg.what) {
                                case 1: //Get robot status and setting it onto the textview
                                    try {
                                        data = msg.obj.toString();
                                        JSONObject jobj = new JSONObject(data);
                                        data = jobj.getString("status");
                                        tvStatus.setText("Status: "+data);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 2://Android not receiving Waypoint only sending so this is not needed.
                                    //Update Map waypoint
                                    //Get map coordinates
                                    MainActivity.mazeManager.setGrid(4,4,"Waypoint");
                                    break;
                                case 3:
                                    //Move robot according to exploration
                                    try {
                                        data = msg.obj.toString();
                                        JSONObject jobj = new JSONObject(data);
                                        data = jobj.getString("RI");
                                        switch (data){
                                            case "F1": MainActivity.robotManager.moveForward(); break;
                                            case "F2": MainActivity.robotManager.moveForward();
                                                        MainActivity.robotManager.moveForward();
                                                        break;
                                            case "F3": MainActivity.robotManager.moveForward();
                                                        MainActivity.robotManager.moveForward();
                                                        MainActivity.robotManager.moveForward();
                                                        break;
                                            case "L": MainActivity.robotManager.rotateLeft(); break;
                                            case "R":MainActivity.robotManager.rotateRight(); break;
                                        }
                                    }catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 7: //Receiving mdf to update gridview
                                    try {
                                        data = msg.obj.toString();
                                        JSONObject jobj = new JSONObject(data);
                                        data = jobj.getString("MDF");
                                        String mapmap = data;
                                        String MDF1 = mapmap;
                                        String MDF2 = mapmap;
                                        String MDF3 = mapmap;

                                        MDF1 = convertMDF1(MDF1);
                                        MDF2 = convertMDF2(MDF2);
                                        MDF3 = convertMDF3(MDF3);

                                        editor.putString("MDF1",MDF1);
                                        editor.putString("MDF2",MDF2);
                                        editor.putString("AMDMDF",mapmap);
                                        editor.commit();
                                        mdpautomanaual = mdpAM.getBoolean("MDF_REFRESH",true);
                                        Log.d("MDF","boolean: "+ mdpautomanaual);
                                        Log.d("AutoManaual",mdpautomanaual+"");
                                        if(mdpautomanaual){

                                            maptest2(data);
                                        }
                                        Log.d("BTMAZE",data);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 8: //Receiving mdf to update gridview
                                    try {
                                        data = msg.obj.toString();
                                        JSONObject jobj = new JSONObject(data);
                                        data = jobj.getString("grid");
                                        String mapmap = data;
                                        String MDF1 = mapmap;
                                        String MDF2 = mapmap;
                                        String MDF3 = mapmap;

                                        MDF1 = convertMDF1(MDF1);
                                        MDF2 = convertMDF2(MDF2);
                                        MDF3 = convertMDF3(MDF3);

                                        editor.putString("MDF1",MDF1);
                                        editor.putString("MDF2",MDF2);
                                        editor.putString("AMDMDF",MDF3);
                                        editor.commit();
                                        mdpautomanaual = mdpAM.getBoolean("MDF_REFRESH",true);
                                        Log.d("MDF","boolean: "+ mdpautomanaual);
                                        Log.d("AutoManaual",mdpautomanaual+"");
                                        if(mdpautomanaual){

                                            maptest(data);
                                        }
                                        Log.d("BTMAZE",data);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 9: //Receiving mdf to update gridview
                                    try {
                                        data = msg.obj.toString();
                                        JSONObject jobj = new JSONObject(data);
                                        data = jobj.getString("grid");
                                        editor.putString("AMDMDF",data);
                                        editor.commit();
                                        mdpautomanaual = mdpAM.getBoolean("MDF_REFRESH",true);
                                        Log.d("MDF","boolean: "+ mdpautomanaual);
                                        Log.d("AutoManaual",mdpautomanaual+"");
                                        if(mdpautomanaual){

                                            maptest(data);
                                        }
                                        Log.d("BTMAZE",data);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 10: //Image Rec
                                    try {
                                        data = msg.obj.toString();
                                        JSONObject jobj = new JSONObject(data);
                                        data = jobj.getString("ID");
                                        x = jobj.getInt("x");
                                        y = jobj.getInt("y");
                                        MainActivity.mazeManager.setGrid(x,y,data);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 11: //Image Rec
                                    try {
                                        data = msg.obj.toString();
                                        JSONObject jobj = new JSONObject(data);
                                        data = jobj.getString("Name");
                                        String name = jobj.getString("Image");
                                        x = jobj.getInt("x");
                                        y = jobj.getInt("y");
                                        MainActivity.mazeManager.setGrid(x,y,name);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                default:
                                    break;

                            }
                        }
                    };

                    mBluetoothConnection = new BluetoothConnectionService(getActivity(),mHandler,BluetoothDialog.this);
                    if(mBTDevices.get(position).getBondState() == BluetoothDevice.BOND_BONDING){
                        // tvStatus.setText("Pairing");
                        Log.d(TAG,"BroadcastReceiver4: Pairing");
                    }
                    if(mBTDevices.get(position).getBondState() == BluetoothDevice.BOND_NONE){
                        Log.d(TAG,"BroadcastReceiver4: No Pair");
                    }
                    if(mBTDevices.get(position).getBondState() == BluetoothDevice.BOND_BONDED){
                        // tvStatus.setText("Paired but not connected");
                        Log.d(TAG,"BroadcastReceiver4: Paired");
                        Log.d(TAG,"Connection Codes should come here");
                        startBTConnection(mBTDevice,mdpUUID);
                        Toast.makeText(getActivity(),"Connected with "+deviceName,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



        btnScan = (Button)view.findViewById(R.id.btnDiscover);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"btnDiscover: Looking for unpaired devices");
                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG,"btnDiscover: Cancelling discovery");
                    btnScan.setText("Scan");
                    //tvStatus.setText("Status:Nothing");

                }
                else if(!mBluetoothAdapter.isDiscovering()){
                    mBTDevices.clear();
                    //btnScan.setText("Stop");
                    Log.d(TAG,"btnDiscover: Starting discovery");
                    checkBTPermissions();
                    mBluetoothAdapter.startDiscovery();
                    Toast.makeText(getActivity(), "Scanning...", Toast.LENGTH_SHORT).show();
                    IntentFilter discoverDevicesIntent = new IntentFilter((BluetoothDevice.ACTION_FOUND));
                    requireActivity().registerReceiver(receiver3,discoverDevicesIntent);
                }
            }
        });

        builder.setView(view);
        return builder.create();
    }



    private String convertMDF1(String mdf) {
        String MDF1 = mdf;
        MDF1 = MDF1.replace("1", "1");
        MDF1 = MDF1.replace("0", "1");
        MDF1 = MDF1.replace("2", "0");
        MDF1 = "11" + MDF1 + "11";
        MDF1 = convertMDFHex(MDF1);
        return MDF1;
    }
    private String convertMDF2(String mdf) {
        String MDF2 = mdf;
        MDF2 = MDF2.replace("2","");
        while(MDF2.length()%8 != 0) {
            MDF2 = MDF2 +"0";
        }
        MDF2 = convertMDFHex(MDF2);
        return MDF2;

    }
    private String convertMDF3(String mdf) {
        String MDF3 = mdf;
        MDF3 = MDF3.replace("2","0");
        MDF3 = convertMDFHex(MDF3);
        return MDF3;
    }

    private String convertMDFHex(String mdf) {
        int decimal;
        String hexStr = "";
        String mdffour = "";
        String mdfString = "";

        for(int i = 0; i < mdf.length(); i = i + 4){
            mdffour = "";
            hexStr = "";
            for(int k = 0; k < 4 ; k++){
                mdffour = mdffour + mdf.charAt(i+k);
            }
            decimal = Integer.parseInt(mdffour,2);
            hexStr = Integer.toString(decimal,16);
            mdfString = mdfString + hexStr;
        }
        return mdfString;
    }


    public void maptest(String str){
        String mapdesriptor = pref.getString("AMDMDF",str);
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

    public void maptest2(String str){
        String mapdesriptor = pref.getString("AMDMDF",str);
        String curr;
//        String mdf = "",mdfbin;
//        for(int j = 0; j < 75; j++){
//            curr = String.valueOf(mapdesriptor.charAt(j));
//            mdfbin = new BigInteger(curr,16).toString(2);
//            if(mdfbin.length() == 1)
//                mdfbin = "000" + mdfbin;
//            if(mdfbin.length() == 2)
//                mdfbin = "00" + mdfbin;
//            if(mdfbin.length() == 3)
//                mdfbin = "0" + mdfbin;
//            mdf = mdf + mdfbin;
//        }
//        Log.d("Maze",mdf);
        //Plot Map
        String mdf = mapdesriptor;
        int column = 19, row = 0;
        for(int k = 0; k < mdf.length(); k++){
            curr = String.valueOf(mdf.charAt(k));
            if(curr.equals("2")){
                MainActivity.mazeManager.setGrid(row,column,"Unexplored");
            }
            else if(curr.equals("1")){
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


    /* Here is ON OFF BLUETOOTH */

    private void enableDisableBT() {
        if(mBluetoothAdapter == null){
            Log.d(TAG,"enableDisableBT: Does not have BT capabilities");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG,"enableDisableBT: enabling BT");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            requireActivity().registerReceiver(receiver,BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG,"enableDisableBT: disabling BT");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            requireActivity().registerReceiver(receiver,BTIntent);
        }
    }

    public final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"onReceive:STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"onReceive:STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"onReceive:STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"onReceive:STATE TURNING ON");
                        break;
                }
            }
        }
    };
    /* Here ends ON OFF BLUETOOTH */

    public void btnDiscover(View view) {
        Log.d(TAG,"btnDiscover: Looking for unpaired devices");
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG,"btnDiscover: Cancelling discovery");
            btnScan.setText("Scan");
            //tvStatus.setText("Status:Nothing");

        }
        else if(!mBluetoothAdapter.isDiscovering()){
            mBTDevices.clear();
            btnScan.setText("Stop");
            Log.d(TAG,"btnDiscover: Starting discovery");
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            Toast.makeText(getActivity(), "Scanning...", Toast.LENGTH_SHORT).show();
            IntentFilter discoverDevicesIntent = new IntentFilter((BluetoothDevice.ACTION_FOUND));
            requireActivity().registerReceiver(receiver3,discoverDevicesIntent);
        }
        //tvStatus.setText("Status:Nothing");
    }

    public BroadcastReceiver receiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG,"ACTION FOUND");
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName() != null){
                    if(device.getName().equals("rpi-grp-15") || device.getName().equals("DESKTOP-42ULPT8")) {
                        mBTDevices.add(device);
                    }
                }
                Log.d(TAG,"onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context,R.layout.device_adapter_view,mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){

            int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),"Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += ContextCompat.checkSelfPermission(getActivity(),"Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        mBluetoothConnection.startClient(device,uuid);
    }

    public void reconnectBT(){
        mBluetoothConnection.startClient(mBTDevice,mdpUUID);
    }

    public void senddata(String msg){
        Log.d("BTDIALOG","trying to send data");
        if(mBluetoothConnection != null){
            if(mBluetoothConnection.getConnection()){
                Log.d("BTDIALOG","trying to sending data");
                mBluetoothConnection.write(msg.getBytes(Charset.defaultCharset()));
            }
        }


    }
}
