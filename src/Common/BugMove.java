package Common;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/**
 * Created by User on 1/24/2017.
 */
public class BugMove extends Globals {

    static boolean inBugMode = false;
    static Direction lastDirection = null;
    static final float BUG_ROTATE_INCREMENT = 5.0f;
    static boolean bugRotateLeft = true;

    public static boolean simpleBug(MapLocation target)
    {
        if (rc.hasMoved())
            return false;

        System.out.println("Trying to bug move to "+target);
        Direction dirToTarget = here.directionTo(target);
        if (!inBugMode) {
            if (!UtilMove.tryMove(dirToTarget)) {
                // This will trigger if there isn't a safe movement even if movement is possible.
                inBugMode = true;
                lastDirection = dirToTarget;
            } else {
                return true;
            }
        }

        if (inBugMode) {
            Direction bugDirection = bugGetNextDirection(dirToTarget, lastDirection);
            try {
                MapLocation temp = here;
                rc.setIndicatorDot(target, 255, 255, 0);
                rc.setIndicatorLine(temp, target, 255, 255, 0);
                rc.setIndicatorLine(temp, temp.add(lastDirection), 100, 100, 0);

                rc.move(bugDirection);
                here = rc.getLocation();
                lastDirection = bugDirection;
            } catch (GameActionException e) {
                UtilDebug.debug_exceptionHandler(e, "Failed to bug move direction "+bugDirection);
                return false;
            }
            return true;
        }
        return false;
    }

    public static Direction bugRotate(Direction dir, boolean positive) {
        if (bugRotateLeft == positive) {
            return dir.rotateLeftDegrees(BUG_ROTATE_INCREMENT);
        } else {
            return dir.rotateRightDegrees(BUG_ROTATE_INCREMENT);
        }
    }

    public static Direction bugGetNextDirection(Direction targetDirection, Direction lastDirection) {
        boolean hasBumped = false;
        Direction moveDir = lastDirection;
        float rotated = 0f;
        if (rc.canMove(moveDir)) { //Way is open so rewind
            System.out.println("BugMove: Hugging edge");
            Direction testDir = bugRotate(moveDir, false);
            while (rc.canMove(testDir)) {
                rc.setIndicatorDot(here.add(testDir, 2), 0,100,0);
                if (targetDirection.degreesBetween(testDir) <= BUG_ROTATE_INCREMENT &&
                        rc.canMove(targetDirection)) {
                    inBugMode = false;
                    System.out.println("BugMove: Freed from bugging");
                    return targetDirection;
                }
                testDir = bugRotate(testDir, false);
                rotated += BUG_ROTATE_INCREMENT;
                if (rotated > 360) {
                    System.out.println("BugMove: Unable to find a free location");
                    inBugMode = false;
                    return targetDirection;
                }
            }
            System.out.println("BugMove: Found best hug location");
            return bugRotate(testDir, true); //Back up one step to last clear direction
        } else { //Way blocked, rotate until find an opening.
            System.out.println("BugMove: Deflecting from obstacle");
            while (!rc.canMove(moveDir)) {
                rc.setIndicatorDot(here.add(moveDir, 2), 100,0,0);
                if (!hasBumped && isEdgeOfMap(moveDir)) {
                    //Bumped map edge, going other way
                    System.out.println("Bumped edge of map, rotating other way");
                    hasBumped = true;
                    bugRotateLeft = !bugRotateLeft;
                }
                moveDir = bugRotate(moveDir, true);
                if (rotated > 720) {
                    System.out.println("BugMove: Unable to find a free location");
                    inBugMode = false;
                    return targetDirection;
                }
            }
            System.out.println("BugMove: Found deflection angle");
            return moveDir;
        }
    }

    public static boolean isEdgeOfMap(Direction dir) {
        try {
            return !rc.onTheMap(here.add(dir, rc.getType().strideRadius + rc.getType().bodyRadius));
        } catch (GameActionException e) {
            System.out.println("Caught exception in isEdgeOfMap"+e);
        }
        return false;
    }

}
