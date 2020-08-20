package sg.mdp.ntu.MDP15;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class RobotManager {
    private enum Orientation {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
    private ImageView robot;
    private Context context;
    private int curX;
    private int curY;
    private Orientation orientation;

    public RobotManager (ImageView robot, Context context) {
        this.robot = robot;
        this.context = context;
        this.curX = 0;
        this.curY = 0;
        this.orientation = Orientation.RIGHT;
    }

    public void setRobotCoordinates(int x, int y) {
        if (x < 1) {
            x = 1;
        }
        if (x > 13) {
            x = 13;
        }
        if (y < 1) {
            y = 1;
        }
        if (y > 18) {
            y = 18;
        }

        curX = x;
        curY = y;

        RelativeLayout.MarginLayoutParams params = (RelativeLayout.MarginLayoutParams) robot.getLayoutParams();
        params.leftMargin = (x - 1) * 25;
        params.bottomMargin = (y - 1) * 25;
        robot.setLayoutParams(params);
    }

    /*
    Display whether the robot is idle/moving/turning
     */
    public void setState(String state) {
        if (state.equals(context.getString(R.string.robot_idle))) {
            robot.setBackground(context.getDrawable(R.color.idleColor));
        } else if (state.equals(context.getString(R.string.robot_moving))) {
            robot.setBackground(context.getDrawable(R.color.movingColor));
        } else if (state.equals(context.getString(R.string.robot_turning))) {
            robot.setBackground(context.getDrawable(R.color.turningColor));
        }
    }

    public void moveForward() {
        if (orientation == Orientation.RIGHT) {
            setRobotCoordinates(curX + 1, curY);
        } else if (orientation == Orientation.LEFT) {
            setRobotCoordinates(curX - 1, curY);
        } else if (orientation == Orientation.UP) {
            setRobotCoordinates(curX , curY + 1);
        } else if (orientation == Orientation.DOWN) {
            setRobotCoordinates(curX , curY - 1);
        }
    }

    public void moveBack() {
        if (orientation == Orientation.RIGHT) {
            setRobotCoordinates(curX - 1, curY);
        } else if (orientation == Orientation.LEFT) {
            setRobotCoordinates(curX + 1, curY);
        } else if (orientation == Orientation.UP) {
            setRobotCoordinates(curX , curY - 1);
        } else if (orientation == Orientation.DOWN) {
            setRobotCoordinates(curX , curY + 1);
        }
    }

    public void rotateLeft() {
        if (orientation == Orientation.RIGHT) {
            orientation = Orientation.UP;
            robot.setImageResource(R.drawable.robot_up);
        } else if (orientation == Orientation.LEFT) {
            orientation = Orientation.DOWN;
            robot.setImageResource(R.drawable.robot_down);
        } else if (orientation == Orientation.UP) {
            orientation = Orientation.LEFT;
            robot.setImageResource(R.drawable.robot_left);
        } else if (orientation == Orientation.DOWN) {
            orientation = Orientation.RIGHT;
            robot.setImageResource(R.drawable.robot_right);
        }
    }

    public void rotateRight() {
        if (orientation == Orientation.RIGHT) {
            orientation = Orientation.DOWN;
            robot.setImageResource(R.drawable.robot_down);
        } else if (orientation == Orientation.LEFT) {
            orientation = Orientation.UP;
            robot.setImageResource(R.drawable.robot_up);
        } else if (orientation == Orientation.UP) {
            orientation = Orientation.RIGHT;
            robot.setImageResource(R.drawable.robot_right);
        } else if (orientation == Orientation.DOWN) {
            orientation = Orientation.LEFT;
            robot.setImageResource(R.drawable.robot_left);
        }
    }


}
