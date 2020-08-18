package sg.mdp.ntu.MDP15;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar
        toolbar = (Toolbar)findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    public void openBluetooth(MenuItem item){
        BluetoothDialog btDialog = new BluetoothDialog();
        btDialog.show(getSupportFragmentManager(),"Bluetooth");
    }

    public void openReconfigure(MenuItem item){
        ReconfigureDialog rfDialog = new ReconfigureDialog();
        rfDialog.show(getSupportFragmentManager(),"Reconfigure");
    }
}
