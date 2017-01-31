package Common;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/**
 * Created by User on 1/24/2017.
 */
public class UtilDebug extends Globals {

    public static void debug_exceptionHandler(GameActionException e, String message) {
        System.out.println("*********EXCEPTION*******");
        alert();
        System.out.println(message);
        e.printStackTrace();
    }

    public static void debug_exceptionHandler(Exception e, String message) {
        System.out.println("*********EXCEPTION*******");
        MapLocation a = here.add(Direction.NORTH).add(Direction.EAST);
        MapLocation b = here.add(Direction.NORTH).add(Direction.WEST);
        MapLocation c = here.add(Direction.SOUTH).add(Direction.WEST);
        MapLocation d = here.add(Direction.SOUTH).add(Direction.EAST);
        rc.setIndicatorLine(a,b,255,100,100);
        rc.setIndicatorLine(b,c,255,100,100);
        rc.setIndicatorLine(c,d,255,100,100);
        rc.setIndicatorLine(d,a,255,100,100);
        System.out.println(message);
        e.printStackTrace();
    }

    // Flags an alert to call attention to the robot
    public static void alert() {
        MapLocation a = here.add(Direction.NORTH).add(Direction.EAST);
        MapLocation b = here.add(Direction.NORTH).add(Direction.WEST);
        MapLocation c = here.add(Direction.SOUTH).add(Direction.WEST);
        MapLocation d = here.add(Direction.SOUTH).add(Direction.EAST);
        rc.setIndicatorLine(a,b,255,0,0);
        rc.setIndicatorLine(b,c,255,0,0);
        rc.setIndicatorLine(c,d,255,0,0);
        rc.setIndicatorLine(d,a,255,0,0);
    }


}
