package sg.mdp.ntu.MDP15;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;

public class WaypointDialog extends AppCompatDialogFragment {
    EditText x,y;
    Button setWay;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_waypoint,null);
        builder.setView(view);

        x = (EditText)view.findViewById(R.id.etX);
        y = (EditText)view.findViewById(R.id.etY);



        setWay = (Button)view.findViewById(R.id.btnSetWay);
        System.out.println("WAYPOINTX: "+x.getText().toString()+"d");
        setWay.setOnClickListener(new View.OnClickListener() {
            int mazeX = 0;
            int mazeY = 0;
            @Override
            public void onClick(View v) {
                if(!x.getText().toString().equals("") && !y.getText().toString().equals("")) {
                    System.out.println("WAYPOINTX: inside IF loop");
                    mazeX = Integer.parseInt(x.getText().toString());
                    mazeY = Integer.parseInt(y.getText().toString());
                }else if(x.getText().toString().equals("") && y.getText().toString().equals("")){
                    mazeX = 1;
                    mazeY = 1;
                }
                else if(x.getText().toString().equals("")){
                    System.out.println("WAYPOINTX: INSIDE X");
                    mazeX = 0;
                    mazeY = Integer.parseInt(y.getText().toString());
                    System.out.println("WAYPOINTX: "+mazeX);
                }
                else if(y.getText().toString().equals("")){
                    System.out.println("WAYPOINTX: INSIDE Y");
                    mazeY = 0;
                    mazeX = Integer.parseInt(x.getText().toString());
                    System.out.println("WAYPOINTX: "+mazeY);
                }
                if(mazeX == 0 && mazeY == 0){
                    mazeX = 1;
                    mazeY = 1;
                }
                MainActivity.mazeManager.setGrid(mazeX, mazeY, "Waypoint");
                MainActivity.btDialog.senddata("{\"MDP15\":\"W\",\"X\":" + mazeX + ",\"Y\":" + mazeY + "}\n");
                dismiss();
            }
        });

        return builder.create();
    }
}