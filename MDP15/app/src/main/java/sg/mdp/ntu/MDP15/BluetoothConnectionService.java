package sg.mdp.ntu.MDP15;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionService";
    private static final String appName = "MDP 15 Tablet";
    static Handler mhandler; // handler that gets info from Bluetooth service

    private static final UUID mdpUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;
    private BluetoothDialog btdialog;

    private ConnectedThread mConnectedThread;

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    public BluetoothConnectionService(Context context, Handler handler, BluetoothDialog btdialog){
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mhandler = handler;
        this.btdialog = btdialog;
        start();
    }

    public boolean getConnection(){
        return  mConnectedThread.mmSocket.isConnected();
    }

    //this thread runs while listening for incoming connections.
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,mdpUUID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"AccpetThread IO:"+e.getMessage());
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG,"run: AcceptThread running...");

            BluetoothSocket socket = null;

            try {
                Log.d(TAG,"run: AcceptThread RFCOM server socket start...");
                socket = mmServerSocket.accept();
                Log.d(TAG,"run: AcceptThread RFCOM server socket accepted connection...");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"AccpetThread IO:"+e.getMessage());
            }

            if(socket != null){
                connected(socket,mmDevice);
            }

            Log.i(TAG,"END Accept Thread");
        }

        public void cancel(){
            Log.d(TAG,"cancel: Cancelling AcceptThread");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"cancel: AccpetThread serversocket closure failed. "+e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG,"ConnectThread: started");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG,"RUN mConnectThread");

            //Get BTSocket for connection with the given device
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"ConnectThread: could not crate insecure connection "+e.getMessage());
            }

            mmSocket = tmp;

            mBluetoothAdapter.cancelDiscovery();

            try {
                if(!mmSocket.isConnected()){
                    mmSocket.connect();
                }
                Log.d(TAG,"run: ConnectThread connected");
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mmSocket.close();
                    Log.d(TAG,"run: Closed socket");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.e(TAG,"run: ConnectThread unable to close connection in socket. "+e.getMessage());
                }
                Log.d(TAG,"run: Could not connect to UUID " +mdpUUID);
            }

            connected(mmSocket,mmDevice);
        }

        public void cancel(){
            try {
                Log.d(TAG,"cancel: closing client socket");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"cancel: closure of mmsocket in connecthread failed "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG,"connected: Starting");
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }

    public synchronized void start(){
        Log.d(TAG,"start");

        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG,"startClient: started.");

       // mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth","Please wait...",true);

        mConnectThread = new ConnectThread(device,uuid);
        mConnectThread.start();
    }


    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG,"ConnectedThread: Starting...");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
                Log.d(TAG,"Progress error");
            }


            try {
                tmpIn = mmSocket.getInputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];

            int bytes;

            while(true){
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer,0,bytes);
                    //System.out.println(incomingMessage);
                    Log.d(TAG,"InputStream: "+incomingMessage);
                    if(incomingMessage.contains("STATUS"))
                        mhandler.obtainMessage(1,incomingMessage).sendToTarget();
                    if(incomingMessage.contains(("ROBOT_INSTRUCTION")))
                        mhandler.obtainMessage(3,incomingMessage).sendToTarget();
                    if(incomingMessage.contains(("MDF")))
                        mhandler.obtainMessage(7,incomingMessage).sendToTarget();
                    if(incomingMessage.contains(("MDF1")))
                        mhandler.obtainMessage(8,incomingMessage).sendToTarget();
                    if(incomingMessage.contains(("grid")))
                        mhandler.obtainMessage(9,incomingMessage).sendToTarget();
                    if(incomingMessage.contains("ID"))
                        mhandler.obtainMessage(10,incomingMessage).sendToTarget();
                    if(incomingMessage.contains("ImageRec")){
                        mhandler.obtainMessage(11,incomingMessage).sendToTarget();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"write: Error reading to inputstream "+e.getMessage());
                   //btdialog.startBTConnection(mmDevice,mdpUUID);
                    disconnect();
                    startNew();
                    break;

                    //Bluetooth connection died.


                }
            }
        }
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"write: Error writing to outputstream "+e.getMessage());
            }
        }

        public void cancel(){
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized void disconnect() {
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            if (mInsecureAcceptThread != null) {
                mInsecureAcceptThread.cancel();
                mInsecureAcceptThread = null;
            }
        }

        public synchronized  void startNew() {
            Log.d(TAG, "start");

            //Cancel any thread attempting to make a connection
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
            if (mInsecureAcceptThread == null) {
                Log.e(TAG, "Creating a new accept thread...");
                mInsecureAcceptThread = new AcceptThread();
                mInsecureAcceptThread.start();
            }
        }
    }
}
