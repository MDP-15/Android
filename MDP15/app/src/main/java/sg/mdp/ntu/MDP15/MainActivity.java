package sg.mdp.ntu.MDP15;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // prepare maze world
        gridLayout = findViewById(R.id.maze);
        RelativeLayout[][] maze = new RelativeLayout[20][15];
        int j = 19;
        int k = 0;
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View layout = gridLayout.getChildAt(i);
            maze[j][k] = (RelativeLayout) layout;
            final int finalJ = j;
            final int finalK = k;
            layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Toast.makeText(getApplicationContext(), "Coordinates: " + finalJ + ", " + finalK, Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            k++;
            if (k >= 15) {
                k = 0;
                j--;
            }
        }

        MazeManager mazeManager = new MazeManager(maze, getApplicationContext());
        mazeManager.reset();
    }
}