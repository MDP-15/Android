package sg.mdp.ntu.MDP15;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

public class RobotDialog extends AppCompatDialogFragment {
    EditText x,y;
    Button setRobotCoor;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_robot,null);
        builder.setView(view);

        x = (EditText)view.findViewById(R.id.etrX);
        y = (EditText)view.findViewById(R.id.etrY);

        setRobotCoor = (Button)view.findViewById(R.id.btnSetRobotCoor);
        setRobotCoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mazeX = Integer.parseInt(x.getText().toString());
                int mazeY = Integer.parseInt(y.getText().toString());
                MainActivity.robotManager.setRobotCoordinates(mazeX,mazeY);
                dismiss();
            }
        });

        return builder.create();
    }
}