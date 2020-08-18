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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;

public class BluetoothDialog extends AppCompatDialogFragment {

    private EditText editusername;
    private EditText editpassword;
    private final String TAG = "BluetoothDialog";
    private BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    ListView lvNewDevices;
    public DeviceListAdapter mDeviceListAdapter;

    Button btnONOFF;
    Button btnScan;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_bluetooth,null);


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

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
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

    private BroadcastReceiver receiver3 = new BroadcastReceiver() {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        requireActivity().unregisterReceiver(receiver);
    }
}
