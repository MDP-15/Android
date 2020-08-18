package sg.mdp.ntu.MDP15;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
    BluetoothDialog btDialog;
    private Toolbar toolbar;
    Button btnSetStartPoint;
    Button btnUP;
    Button btnDown;
    Button btnLeft;
    Button btnRight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btDialog = new BluetoothDialog();
        setContentView(R.layout.activity_main);
        //Toolbar
        toolbar = (Toolbar)findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        btnSetStartPoint = (Button)findViewById(R.id.btnSetStartPoint);
        btnSetStartPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btDialog.senddata("f");
            }
        });

        //Buttons
        btnUP = findViewById(R.id.btnUP);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    public void openBluetooth(MenuItem item){
        btDialog.show(getSupportFragmentManager(),"Bluetooth");
    }

    public void openReconfigure(MenuItem item){
        ReconfigureDialog rfDialog = new ReconfigureDialog();
        rfDialog.show(getSupportFragmentManager(),"Reconfigure");
    }

    public void up(View v){
        btDialog.senddata("f");
    }
    public void down(View v){
        btDialog.senddata("r");
    }
    public void left(View v){
        btDialog.senddata("tl");
    }
    public void right(View v){
        btDialog.senddata("tr");
    }

}
