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

    Button btnONOFF;
    Button btnScan;
    TextView tvStatus;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_bluetooth,null);
        tvStatus = (TextView)getActivity().findViewById(R.id.tbRobotStatus);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnONOFF = (Button)view.findViewById(R.id.btnONOFF);
        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enabling/disabling BT");
                enableDisableBT();
            }
        });



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
                                case 1:
                                    try {
                                        String str = msg.obj.toString();
                                        JSONObject jobj = new JSONObject(str);
                                        str = jobj.getString("status");
                                        tvStatus.setText("Status: "+str);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 2:
                                    //Update Map waypoint
                                    //Get map coordinates
                                    MainActivity.mazeManager.setGrid(4,4,"Waypoint");
                                    break;
                                case 3:
                                    //Update Robot forward
                                    MainActivity.robotManager.moveForward();
                                    break;
                            }
                        }
                    };

                    mBluetoothConnection = new BluetoothConnectionService(getActivity(),mHandler);
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
                    mBTDevices.add(device);
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

    public void senddata(String msg){
        Log.d("BTDIALOG","trying to send data");
        if(mBluetoothConnection.getConnection()){
            Log.d("BTDIALOG","trying to sending data");
            mBluetoothConnection.write(msg.getBytes(Charset.defaultCharset()));
        }

    }
}
