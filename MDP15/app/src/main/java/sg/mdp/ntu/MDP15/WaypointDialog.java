package sg.mdp.ntu.MDP15;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
        setWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mazeX = Integer.parseInt(x.getText().toString());
                int mazeY = Integer.parseInt(y.getText().toString());
                MainActivity.mazeManager.setGrid(mazeY,mazeX,"Waypoint");
                dismiss();
            }
        });

        return builder.create();
    }
}