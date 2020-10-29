package sg.mdp.ntu.MDP15;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

public class ReconfigureDialog extends AppCompatDialogFragment {

    EditText etfunction1;
    EditText etfunction2;
    Button btnSave;
    String function1,function2;
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_reconfigure,null);

        builder.setView(view);
        etfunction1 = (EditText)view.findViewById(R.id.etf1);
        etfunction2 = (EditText)view.findViewById(R.id.etf2);
        btnSave = (Button)view.findViewById(R.id.btnSave);

        editor = getActivity().getSharedPreferences("MDP_FUNCTIONS", Context.MODE_PRIVATE).edit();
        pref = getActivity().getSharedPreferences("MDP_FUNCTIONS",Context.MODE_PRIVATE);

        //Get stored value from sharedpreference
        function1 = pref.getString("Function1","f");
        function2 = pref.getString("Function2","r");
        //Set stored value into edit text
        etfunction1.setText(function1);
        etfunction2.setText(function2);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save to shared preference
                function1 = etfunction1.getText().toString();
                function2 = etfunction2.getText().toString();

                editor.putString("Function1",function1);
                editor.putString("Function2",function2);
                editor.apply();
                dismiss();
            }
        });


        return builder.create();
    }
}
