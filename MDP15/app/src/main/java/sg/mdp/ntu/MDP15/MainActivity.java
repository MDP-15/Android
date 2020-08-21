package sg.mdp.ntu.MDP15;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    GridLayout gridLayout;
    ImageView robot;
    TextView startZone;
    TextView goalZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // robot
        robot = findViewById(R.id.robot);
        final RobotManager robotManager = new RobotManager(robot, getApplicationContext());
        robot.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {}
            @Override
            public void onDoubleClick(View view) {
                robotManager.rotateLeft();
            }
        }));

        // prepare maze grids
        gridLayout = findViewById(R.id.maze);
        RelativeLayout[][] maze = new RelativeLayout[20][15];
        int j = 19;
        int k = 0;
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            final View layout = gridLayout.getChildAt(i);
            maze[j][k] = (RelativeLayout) layout;
            final int finalJ = j;
            final int finalK = k;
            layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    final PopupWindow popupWindow = new PopupWindow(getApplicationContext());
                    ArrayList<String> sortList = new ArrayList<String>();
                    sortList.add("Move robot here");
                    sortList.add("Place waypoint here");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, sortList);
                    ListView options = new ListView(getApplicationContext());
                    options.setAdapter(adapter);
                    options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView parent, View view, int position, long id) {
                            if (position == 0) {
                                robotManager.setRobotCoordinates(finalK, finalJ);
                            } else {
                                MazeManager.setGrid((RelativeLayout) layout, getApplicationContext(), getString(R.string.maze_waypoint));
                            }
                            popupWindow.dismiss();
                        }
                    });
                    popupWindow.setFocusable(true);
                    popupWindow.setWidth(175);
                    popupWindow.setBackgroundDrawable(getDrawable(R.color.popupWindowColor));
                    popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                    popupWindow.setContentView(options);
                    popupWindow.showAsDropDown(findViewById(R.id.screen), (int) motionEvent.getRawX(), (int) (motionEvent.getRawY() - 95));
                    return false;
                }
            });
            k++;
            if (k >= 15) {
                k = 0;
                j--;
            }
        }
        // initialize maze manager
        MazeManager mazeManager = new MazeManager(maze, getApplicationContext());
        mazeManager.reset();




    }

}