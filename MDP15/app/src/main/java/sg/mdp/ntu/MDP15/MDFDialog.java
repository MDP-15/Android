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
import android.widget.TextView;

import org.w3c.dom.Text;

import androidx.appcompat.app.AppCompatDialogFragment;

public class MDFDialog extends AppCompatDialogFragment {


    SharedPreferences pref;
    TextView tvMDF1;
    TextView tvMDF2;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_mdf,null);
        builder.setView(view);

        pref = getActivity().getSharedPreferences("MDP_MDF", Context.MODE_PRIVATE);

        tvMDF1 = (TextView)view.findViewById(R.id.tvMDF1bin);
        tvMDF2 = (TextView)view.findViewById(R.id.tvMDF2bin);

        String MDF1 = pref.getString("MDF1","00000000000000000000000000000000000000000000000000000000000000000000");
        String MDF2 = pref.getString("MDF2","00000000000000000000000000000000000000000000000000000000000000000000");

        tvMDF1.setText(MDF1);
        tvMDF2.setText(MDF2);
        return builder.create();
    }
}