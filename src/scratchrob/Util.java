package scratchrob;

import battlecode.common.*;

public class Util extends Globals {

    static MapLocation enemyLoc = rc.getInitialArchonLocations(them)[(int)(Math.random() * rc.getInitialArchonLocations(them).length)];

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     * Don't move directly onto a bullet :D
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
        if (rc.hasMoved())
            return false;
        // First, try intended direction
        if (rc.canMove(dir) && rc.senseNearbyBullets(here.add(dir), myType.bodyRadius).length == 0 ) {
            doMove(dir);
            here = rc.getLocation();
            return true;
        }

        // Now try a bunch of similar angles
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck)) && rc.senseNearbyBullets(here.add(dir), myType.bodyRadius).length == 0  && notNearALumberJack(here.add(dir))) {
                doMove(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                here = rc.getLocation(); //here.add(dir.rotateLeftDegrees(degreeOffset*currentCheck),rc.getType().strideRadius);
                hasMoved = true;
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck)) && rc.senseNearbyBullets(here.add(dir), myType.bodyRadius).length == 0 ) {
                doMove(dir.rotateRightDegrees(degreeOffset*currentCheck));
                here = rc.getLocation(); //here.add(dir.rotateRightDegrees(degreeOffset*currentCheck),rc.getType().strideRadius);
                hasMoved = true;
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    private static boolean notNearALumberJack(MapLocation location) {
        for (RobotInfo enemy : rc.senseNearbyRobots(location, 1, them))
        {
            if (enemy.getType() == RobotType.LUMBERJACK)
                return false;
        }
        return true;
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollideWithMe(BulletInfo bullet) {
       return willCollideWithLocation(bullet, here, myType.bodyRadius);
    }


    static boolean willCollideWithLocation(BulletInfo bullet, MapLocation loc, float radius) {

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this location
        Direction directionToRobot = bulletLocation.directionTo(loc);
        float distToRobot = bulletLocation.distanceTo(loc);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= radius);
    }

    public static boolean isEarlyGame() {
        return (rc.getRoundLimit()/3) - rc.getRoundNum()> 0;
    }

    // circle strafes around a certain location at a particular radius. Direction can be switched by passing -1 or 1 to current direction
    // returns false if circle strafe failed because we ran into a wall.
    // direction should be clockwise or counter-clockwise... hmm
    public static boolean CircleStrafe(MapLocation locationToCircle, int clearingRadius, int currentDirection) throws GameActionException{
        // figure out which direction we are going, then early-out if it's off the map
        if ( here == locationToCircle ) // we're just starting out. get some distance.
        {
            Util.tryMove(here.directionTo(Util.getEnemyLoc()));
            return true;
        }

        Direction tangent =here.directionTo(locationToCircle).rotateLeftDegrees(90 * currentDirection);
        if (!rc.onTheMap(here.add(tangent, myType.strideRadius)))
            return false;

        // check if our radius is ok. and adjust if not.
        float currentRadius = Math.abs(here.distanceTo(locationToCircle));
        if ((currentRadius - clearingRadius) > myType.strideRadius)
        {
            System.out.println("Circle Strafe: Radius is too big. Fixing.");
            tryMove(tangent.rotateLeftDegrees (20 * -currentDirection), 40, 3);
            //tryMove(here.directionTo(locationToCircle), 40, 3);
            return true;
        }
        else if ((currentRadius - clearingRadius) < myType.strideRadius)
        {
            System.out.println("Circle Strafe: Radius is too small. Fixing.");
            tryMove(tangent.rotateLeftDegrees (20 * currentDirection), 40, 3);
           // tryMove(locationToCircle.directionTo(here), 40, 3);
            return true;
        }

        System.out.println("Circle Strafe: Radius is good. Strafing");
        // kk.. Actually circle-strafe
        tryMove(tangent, 40, 3);
        return true;
    }

    public static Boolean tryShootTarget(MapLocation target) throws GameActionException
    {
        if (rc.canFireSingleShot()) {
            rc.fireSingleShot(here.directionTo(target));
            rc.setIndicatorLine(here, target, 255, 0, 0);
            return true;
        }
        return false;
    }

    public static void AvoidBullets() throws GameActionException {
        BulletInfo [] bullets = rc.senseNearbyBullets();
        for (BulletInfo bullet : bullets)
        {
            if (Util.willCollideWithMe(bullet))
            {
                Util.tryMove(bullet.getDir(), -90, 3);
                break;
            }
        }
    }

    public static Boolean doMove(Direction dir) {
        try {
            rc.move(dir);
            rc.setIndicatorLine(here, here.add(dir, rc.getType().strideRadius), 0, 255, 0);
            here = rc.getLocation();
        } catch (GameActionException e) {
            System.out.println("Failed to move"+dir);
            return false;
        }
        return true;
    }

    public static Boolean doMove(Direction dir, float length) {
        try {
            rc.move(dir, length);
            MapLocation temp = here;
            here = rc.getLocation();
            rc.setIndicatorLine(here, temp, 0, 255, 0);
        } catch (GameActionException e) {
            System.out.println("Failed to move"+dir);
            return false;
        }
        return true;
    }

    public static boolean doMove(MapLocation target, boolean strict) {
        try {
            if (strict && here.distanceTo(target) > rc.getType().strideRadius) {
                return false;
            }
            rc.move(target);
            MapLocation temp = here;
            here = rc.getLocation();
            rc.setIndicatorLine(here, temp, 0, 255, 0);
        } catch (GameActionException e) {
            System.out.println("Failed to move to "+target);
            return false;
        }
        return true;
    }

    public static boolean doMove(MapLocation target) {
        try {
            rc.move(target);
            MapLocation temp = here;
            here = rc.getLocation();
            rc.setIndicatorLine(here, temp, 0, 255, 0);
        } catch (GameActionException e) {
            System.out.println("Failed to move to "+target);
            return false;
        }
        return true;
    }

    public static MapLocation getEnemyLoc()
    {
        return enemyLoc;
    }

    public static void setEnemyLoc(MapLocation loc)
    {
        enemyLoc = loc;
    }
}
