package Common;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/**
 * Common spawning helper functions
 */
public class UtilSpawn extends Globals {


    public static Direction getClearDirection(Direction dir, float resolution, float radius, boolean strict) {
        return getClearDirection(dir,resolution,radius,strict,false);
    }

    /**
     * find a direction to move or spawn in. It will return the first such direction that is clear for at least one radius.
     * @param radius
     * @return
     */
    public static Direction getClearDirection(Direction dir,  float resolution, float radius, boolean strict, boolean avoidStartingDirection) {
        try {

            if (dir == null) rc.setIndicatorDot(here.add(Direction.getNorth(), 1), 255, 255, 255);
            float distanceToCenter = myType.bodyRadius + radius;
            if (!avoidStartingDirection && !rc.isCircleOccupied(here.add(dir, distanceToCenter), radius) && rc.onTheMap(here.add(dir, distanceToCenter), radius))
                return dir;

            float cumulativeOffset = resolution;

            while (cumulativeOffset < 360 && cumulativeOffset > -360) {
                Direction testDir = dir.rotateLeftDegrees(cumulativeOffset);
                MapLocation testLoc = here.add(testDir, distanceToCenter);
                //rc.setIndicatorDot(here.add(testDir, distanceToCenter), 255, 255, 255);
                if (!rc.isCircleOccupiedExceptByThisRobot(testLoc, radius) && rc.onTheMap(testLoc, radius)) {
                    if (!avoidStartingDirection || !MapLocation.doCirclesCollide(testLoc, radius, here.add(dir, distanceToCenter), radius))
                        return testDir;
                }
                cumulativeOffset += resolution;
            }

        } catch (GameActionException e) {
            UtilDebug.debug_exceptionHandler(e,"sensing failed in getClearDirection");
        }

        return null;
    }

    /**
     *
     * Try to find a clear spot with no gardeners or enemies or bullets so I can spawn some gardeners in peace.
     * @throws GameActionException
     */
    public static void MoveToAClearerLocation(float maxradius) throws GameActionException {

        // hmm doesn't check for bullets.
        if ( !rc.isCircleOccupiedExceptByThisRobot(here, maxradius)) // we're in a good spot already.
            return;
        Direction dir = here.directionTo(rc.getInitialArchonLocations(them)[0]);
        Direction happyDir = null;

        float radius = maxradius;
        while (happyDir == null && radius > 0) {
            happyDir = getClearDirection(dir, 5, radius, false);
            radius--;
        }
        if (happyDir != null)
            UtilMove.tryMove(happyDir);
        // Move randomly
        UtilMove.tryMove(Util.randomDirection());
    }

}
