package Common;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

/**
 * Common Attack code
 */
public class UtilAttack extends Globals {

    // sometimes firing sraight at an enemy is bad. if we want to scare em off or if they dodge too well, missing might help.
    public static boolean fireStormTrooperStyle(MapLocation loc) {
        Direction toFire = here.directionTo(loc).rotateLeftDegrees((float)(-5.0f + (Math.random() * 10)));
        if (rc.canFireSingleShot() && safeToFire(toFire) ) { // don't fire expensive bullets early game
            System.out.println("Firing single shot!");
            try {
                rc.fireSingleShot(toFire);
            } catch (GameActionException e) {
                UtilDebug.debug_exceptionHandler(e, "StormTrooperStyle shooting exception");
            }
            rc.setIndicatorLine(here, loc, 255, 100, 0); // it should be where we are firing to...
            return true;
        }
        return false;
    }

    public static boolean maximumFirepowerAtSafeTarget(MapLocation loc) {
        if ( safeToFire(here.directionTo(loc)))
            return maximumFirepowerAt(loc);
        return false;
    }
    public static boolean maximumFirepowerAtSafeTarget(RobotInfo currentTarget) {
        if ( safeToFireAtTarget(currentTarget) )
            return maximumFirepowerAt(currentTarget.location);
        System.out.println("Target not safe to shoot at!"+currentTarget.location);
        return false;
    }

    public static boolean maximumFirepowerAtSafeTarget(RobotInfo currentTarget, RobotInfo[] enemies) {
        if ( safeToFireAtTarget(currentTarget) )
            return maximumFirepowerAt(currentTarget.location);
        for( RobotInfo target : enemies )
        {
            if ( safeToFireAtTarget(target)) {
                System.out.println("Picking alternate safe target!");
                return maximumFirepowerAt(target.location);
            }
        }
        System.out.println("Can't find a safe target to shoot at!");
        return false;
    }

    public static boolean safeToFireAtTarget(RobotInfo target)
    {
        return safeToFire(here.directionTo(target.getLocation()));
    }

    public static boolean safeToFire(Direction dir)
    {
        Direction direction = dir;
        //Test the line towards the target:
        MapLocation testLocation;
        float distance = rc.getType().bodyRadius + 0.1f;
        float DISTANCE_INCREMENT = 0.3f; //Chosen by IEEE certified random dice roll
        float max_test_distance = rc.getType().sensorRadius;
        while ( distance < max_test_distance)
        {
            testLocation = here.add(direction, distance);
            try {
                if (rc.isLocationOccupiedByRobot(testLocation)) {
                    RobotInfo bot = rc.senseRobotAtLocation(testLocation);
                    if (bot.team == us)
                        return false;
                    return true;
                }
            } catch (GameActionException e) {
                System.out.println("Exception in safeToFireAtTarget"+e);
            }
            distance += DISTANCE_INCREMENT;
        }
        return true;
    }

    public static boolean maximumFirepowerAt(MapLocation target)
    {
        Direction direction = here.directionTo(target);
        float distance = here.add(direction, rc.getType().bodyRadius).distanceTo(target);
        if ( distance < 1.75f ) //Determined on the back of an official IEEE napkin
        {
            if (rc.canFirePentadShot()) {
                System.out.println("FIRING PENTAD SHOT!");
                try {
                    rc.firePentadShot(here.directionTo(target));
                } catch (GameActionException e) {
                    UtilDebug.debug_exceptionHandler(e, "Exception while firing pentad shot");
                }
                rc.setIndicatorLine(here, target, 100, 0, 0);
                return true;
            }
        }
        if ( distance < 2.75f )
        {
            if (rc.canFireTriadShot()) {
                System.out.println("FIRING TRIAD SHOT!");
                try {
                    rc.fireTriadShot(here.directionTo(target));
                } catch (GameActionException e) {
                    UtilDebug.debug_exceptionHandler(e, "Exception while firing triad shot");
                }
                rc.setIndicatorLine(here, target, 170, 0, 0);
                return true;
            }
        }

        if (rc.canFireSingleShot()) {
            System.out.println("Firing single shot!");
            try {
                rc.fireSingleShot(here.directionTo(target));
            } catch (GameActionException e) {
                UtilDebug.debug_exceptionHandler(e, "Exception while firing single shot");
            }
            rc.setIndicatorLine(here, target, 255, 0, 0);
            return true;
        }
        return false;
    }

}
