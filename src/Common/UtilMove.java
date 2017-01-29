package Common;

import battlecode.common.*;

/**
 * Created by User on 1/24/2017.
 */
public class UtilMove extends Globals {

    private static Direction exploreDirection = Direction.NORTH;

	public static boolean moveToFarTarget(MapLocation target) {
        return BugMove.simpleBug(target);
    }

    public static boolean moveToNearTarget(MapLocation target) {
        tryMove(here.directionTo(target),10,10);
        return true;
    }

    // Tries to stay nearby but move around randomly
    public static boolean defend(RobotInfo bot) {
        return defend(bot, 2,5);
    }

    public static boolean defend(RobotInfo bot, float minDistance, float maxDistance) {
        if (bot == null || rc.hasMoved())
            return false;

        float currentDistance = here.distanceTo(bot.location);

        if (currentDistance > maxDistance)
        {
            return tryMove(here.directionTo(bot.location));
        }

        if (currentDistance < minDistance)
        {
            return tryMove(bot.location.directionTo(here));
        }

        return tryMove(Util.randomDirection());

    }

    public static boolean moveToNearBot(RobotInfo target) {
    	return moveStraightTowardsBot(target);
    }
    public static boolean moveStraightTowardsBot(RobotInfo target)
    {
    	if (rc.hasMoved())
            return false;
        float minDist = myType.bodyRadius + target.type.bodyRadius;
        float targetDist = here.distanceTo(target.location);
        if (targetDist < myType.strideRadius + minDist) {
            if (targetDist - minDist < 0.002f) {
                System.out.println("Already adjacent to target");
                return true;
            }
            System.out.println("Moving adjacent to target");
            if (doMove(here.directionTo(target.location), targetDist - minDist - 0.001f)) {
                return true;
            }
            System.out.println("Failed to move adjacent...."+here.distanceTo(target.location)+" "+myType.bodyRadius+" "+target.type.bodyRadius);
        }
        return tryMove(here.directionTo(target.location),10,10);
        //moveFurthestDistancePossibleTowards(target);//simpleSlide(target);
    }
    
    public static boolean moveUnsafeTowardsBot(RobotInfo target)
    {
    	int checksPerSide = 90;
    	float degreeOffset = 1f;
    	Direction dir = here.directionTo(target.location);
    	if (rc.hasMoved())
            return false;
        // First, try intended direction
        if ( rc.canMove(dir) ) {
            doMove(dir);
            here = rc.getLocation();
            return true;
        }

        // Now try a bunch of similar angles
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if( rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck)) ) {
                doMove(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if( rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck)) ) {
                doMove(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }
    

    // circle strafes around a certain location at a particular radius. Direction can be switched by passing -1 or 1 to current direction
    // returns false if circle strafe failed because we ran into a wall.
    // direction should be clockwise or counter-clockwise... hmm
    public static boolean CircleStrafe(MapLocation locationToCircle, int clearingRadius, int currentDirection) {
        if (rc.hasMoved())
            return false;

        // figure out which direction we are going, then early-out if it's off the map
        if ( here == locationToCircle ) // we're just starting out. get some distance.
        {
            tryMove(Util.randomDirection());
            return true;
        }

        Direction tangent =here.directionTo(locationToCircle).rotateLeftDegrees(90 * currentDirection);
        try {
            if (!rc.onTheMap(here.add(tangent, myType.strideRadius)))
                return false;
        } catch (GameActionException e) {
            UtilDebug.debug_exceptionHandler(e, "Error sensing in circle strafe");
            return false;
        }

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

    public static boolean AvoidBullets() {
        if (rc.hasMoved())
            return false;

        BulletInfo[] bullets = rc.senseNearbyBullets(5);
        for (BulletInfo bullet : bullets)
        {
            if (willCollideWithMe(bullet))
            {
                return tryMove(bullet.getDir().rotateLeftDegrees(90), 5, 3);
            }
        }
        return false;
    }

    public static Direction maintainDistanceWith(RobotInfo friend, float MAX, float MIN, MapLocation centerOfCircle) {
        Direction directionToCenter = here.directionTo(centerOfCircle);
        Direction directionAwayFromCenter = centerOfCircle.directionTo(here);
        Direction friendDirection = here.directionTo(friend.location);
        float friendDistance = here.distanceTo(friend.location);
        if ( friendDistance > MAX )
        {
            return halfwayDirection(friendDirection, directionToCenter);
        }
        if ( friendDistance < MIN )
        {
            return halfwayDirection(friendDirection.opposite(), directionAwayFromCenter);
        }
        return null;
    }

    private static Direction halfwayDirection(Direction dir1, Direction dir2) {
        float dx = (dir1.getDeltaX(1) + dir2.getDeltaX(1)) / 2;
        float dy = (dir1.getDeltaY(1) + dir2.getDeltaY(1)) / 2;
        return new Direction(dx, dy);
    }
    
    public static boolean Explore(MapLocation target)
    {
    	if (rc.hasMoved())
            return false;
        if (globalTargetExists) {
            UtilMove.moveToFarTarget(target);
        } else {
            System.out.println("No global target so going to explore in a random direction"+exploreDirection);
            if (!UtilMove.tryMove(exploreDirection)) {
                exploreDirection = exploreDirection.rotateLeftDegrees(90);
                return tryMove(exploreDirection);
            }
        }
        return false;
    }
    public static boolean Explore()
    {
    	return Explore(globalTarget);
    }
    
    /**********************************************************************************************
     *   Basic movement functions                                                                 *
     **********************************************************************************************/

    public static boolean tryMove(Direction dir) {
        return tryMove(dir,20,3);
    }

    public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide, float distance) {
        //Can't move if we already have
        if (rc.hasMoved())
            return false;

        // First, try intended direction
        if (rc.canMove(dir, distance) && rc.senseNearbyBullets(here.add(dir), myType.bodyRadius).length == 0 ) {
            doMove(dir, distance);
            return true;
        }

        // Now try a bunch of similar angles
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if( canMoveSafely(dir.rotateLeftDegrees(degreeOffset*currentCheck), distance) ) {
                doMove(dir.rotateLeftDegrees(degreeOffset*currentCheck), distance);
                return true;
            }
            // Try the offset on the right side
            if(canMoveSafely(dir.rotateRightDegrees(degreeOffset*currentCheck), distance) ) {
                doMove(dir.rotateRightDegrees(degreeOffset*currentCheck), distance);
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) {
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
            if( canMoveSafely(dir.rotateLeftDegrees(degreeOffset*currentCheck)) ) {
                doMove(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if( canMoveSafely(dir.rotateRightDegrees(degreeOffset*currentCheck)) ) {
                doMove(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }



    public static boolean doMove(Direction dir) {
        try {
            rc.move(dir);
            rc.setIndicatorLine(here, here.add(dir, rc.getType().strideRadius), 0, 255, 0);
            here = rc.getLocation();
        } catch (GameActionException e) {
            UtilDebug.debug_exceptionHandler(e,"Failed to move direction "+dir);
            return false;
        }
        return true;
    }

    public static boolean doMove(Direction dir, float length) {
        try {
            rc.move(dir, length);
            MapLocation temp = here;
            here = rc.getLocation();
            rc.setIndicatorLine(here, temp, 0, 255, 0);
        } catch (GameActionException e) {
            UtilDebug.debug_exceptionHandler(e,"Failed to move direction "+dir+"length"+length);
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
            UtilDebug.debug_exceptionHandler(e,"Failed to move to target at "+target);
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
            UtilDebug.debug_exceptionHandler(e,"Failed to move to target at "+target);
            return false;
        }
        return true;
    }

    /**********************************************************************************************
     * Safety Checks
     **********************************************************************************************/

    public static boolean canMoveSafely(Direction dir) {
        if (!rc.canMove(dir))
            return false;
        return isSafeLocation(here.add(dir, myType.strideRadius));
    }

    public static boolean canMoveSafely(Direction dir, float distance) {
        if (!rc.canMove(dir, distance))
            return false;
        return isSafeLocation(here.add(dir, distance));
    }

    public static boolean isSafeLocation(MapLocation loc) {
        BulletInfo[] bullets = rc.senseNearbyBullets(loc, myType.bodyRadius + 4);
        float radius = myType.bodyRadius;
        for (int i = bullets.length; --i >= 0;) {
            if (willCollideWithLocation(bullets[i], loc, radius)) {
                return false;
            }
        }
        RobotInfo[] enemies = rc.senseNearbyRobots(loc, radius + GameConstants.LUMBERJACK_STRIKE_RADIUS, them);
        for (int i = enemies.length; --i >= 0;)
        {
            if (enemies[i].getType() == RobotType.LUMBERJACK)
                return false;
        }
        return true;
    }

    public static boolean willCollideWithMe(BulletInfo bullet) {
        return willCollideWithLocation(bullet, here, myType.bodyRadius);
    }

    public static boolean willCollideWithLocation(BulletInfo bullet, MapLocation loc, float radius) {

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

}