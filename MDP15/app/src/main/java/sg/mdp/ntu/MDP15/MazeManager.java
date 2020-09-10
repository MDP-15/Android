package sg.mdp.ntu.MDP15;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MazeManager {
    private static RelativeLayout[][] maze;
    private Context context;
    static int curx;
    static int cury;


    public MazeManager(RelativeLayout[][] maze, Context context) {
        this.maze = maze;
        this.context = context;
    }

    public void reset() {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                setGrid(j, i, context.getString(R.string.maze_unexplored));
            }
        }
    }

    public static void setGrid(RelativeLayout layout, Context context, String label, int finalK, int finalJ) {
        if (layout.getChildCount() > 1) {  // remove text views if exist
            layout.removeViewAt(1);
        }
        if (label.length() == 1) {
            setText(layout, context, label);  // if it is a single character string, directly put it
        } else {
            if (label.equals(context.getString(R.string.maze_go))) {
                setGo(layout, context);
            } else if (label.equals(context.getString(R.string.maze_up))
                    || label.equals(context.getString(R.string.maze_down))
                    || label.equals(context.getString(R.string.maze_left))
                    || label.equals(context.getString(R.string.maze_right))) {
                setArrow(layout, context, label);
            } else if (label.equals(context.getString(R.string.maze_waypoint))) {
                setWaypoint(layout, context);
                MainActivity.btDialog.senddata("{\"Waypoint\":\"X:"+finalK+"\""+"\"Y:"+finalJ+"\"}");
            } else if (label.equals(context.getString(R.string.maze_obstacle))) {
                setObstacle(layout, context);
            } else if (label.equals(context.getString(R.string.maze_empty))) {
                setEmpty(layout, context);
            } else if (label.equals(context.getString(R.string.maze_unexplored))) {
                setUnexplored(layout, context);
            } else if (label.equals(context.getString(R.string.maze_stop))) {
                setStop(layout, context);
            }
        }
    }

    public static void setGrid(RelativeLayout layout, Context context, String label) {
        if (layout.getChildCount() > 1) {  // remove text views if exist
            layout.removeViewAt(1);
        }
        if (label.length() == 1) {
            setText(layout, context, label);  // if it is a single character string, directly put it
        } else {
            if (label.equals(context.getString(R.string.maze_go))) {
                setGo(layout, context);
            } else if (label.equals(context.getString(R.string.maze_up))
                    || label.equals(context.getString(R.string.maze_down))
                    || label.equals(context.getString(R.string.maze_left))
                    || label.equals(context.getString(R.string.maze_right))) {
                setArrow(layout, context, label);
            } else if (label.equals(context.getString(R.string.maze_waypoint))) {
                setWaypoint(layout, context);
            } else if (label.equals(context.getString(R.string.maze_obstacle))) {
                setObstacle(layout, context);
            } else if (label.equals(context.getString(R.string.maze_empty))) {
                setEmpty(layout, context);
            } else if (label.equals(context.getString(R.string.maze_unexplored))) {
                setUnexplored(layout, context);
            } else if (label.equals(context.getString(R.string.maze_stop))) {
                setStop(layout, context);
            }
        }
    }

    public void setGrid(int x, int y, String label) {
        RelativeLayout layout = maze[y][x];
        setGrid(layout, context, label);
    }

    private static void setUnexplored(RelativeLayout layout, Context context) {
        ImageView grid = (ImageView) layout.getChildAt(0);
        grid.setBackground(context.getDrawable(R.drawable.border_unknown));
        grid.setImageResource(android.R.color.transparent);
    }

    private static void setEmpty(RelativeLayout layout, Context context) {
        ImageView grid = (ImageView) layout.getChildAt(0);
        grid.setBackground(context.getDrawable(R.drawable.border));
        grid.setImageResource(android.R.color.transparent);
    }

    private static void setObstacle(RelativeLayout layout, Context context) {
        ImageView grid = (ImageView) layout.getChildAt(0);
        grid.setBackground(context.getDrawable(R.drawable.border_obstacle));
        grid.setImageResource(android.R.color.transparent);
    }

    private static void setArrow(RelativeLayout layout, Context context, String direction) {
        ImageView grid = (ImageView) layout.getChildAt(0);
        grid.setBackground(context.getDrawable(R.drawable.border_obstacle));
        if (direction.equals(context.getString(R.string.maze_up))) {
            grid.setImageResource(R.drawable.arrow_up);
        } else if (direction.equals(context.getString(R.string.maze_down))) {
            grid.setImageResource(R.drawable.arrow_down);
        } else if (direction.equals(context.getString(R.string.maze_left))) {
            grid.setImageResource(R.drawable.arrow_left);
        } else if (direction.equals(context.getString(R.string.maze_right))) {
            grid.setImageResource(R.drawable.arrow_right);
        }
    }

    private static void setGo(RelativeLayout layout, Context context) {
        ImageView grid = (ImageView) layout.getChildAt(0);
        grid.setBackground(context.getDrawable(R.drawable.border_obstacle));
        grid.setImageResource(R.drawable.go);
    }

    private static void setWaypoint(RelativeLayout layout, Context context) {
        ImageView grid = (ImageView) layout.getChildAt(0);
        grid.setBackground(context.getDrawable(R.drawable.border));
        grid.setImageResource(R.drawable.waypoint);

    }


    private static void setText(RelativeLayout layout, Context context, String label) {
        ImageView grid = (ImageView) layout.getChildAt(0);
        grid.setBackground(context.getDrawable(R.drawable.border_obstacle));
        grid.setImageResource(android.R.color.transparent);
        TextView textView = new TextView(context);
        textView.setWidth(25);
        textView.setHeight(25);
        textView.setText(label);
        textView.setTextColor(context.getColor(android.R.color.white));
        textView.setTextSize(14);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        layout.addView(textView);
    }

    private static void setStop(RelativeLayout layout, Context context) {
        ImageView grid = (ImageView) layout.getChildAt(0);
        grid.setBackground(context.getDrawable(R.drawable.border_obstacle));
        grid.setImageResource(R.drawable.stop);
    }

}
