package markbot;

import battlecode.common.*;

public class Robot {

	public static RobotController rc;
	public static MapLocation here;
	public static Team us;
	public static Team them;
	public static int myID;
	public static RobotType myType;
	public static int roundNum;
	
	public static void init(RobotController _RC) {
		rc = _RC;
		us = rc.getTeam();
		them = us.opponent();
		myID = rc.getID();
		myType = rc.getType();
		here = rc.getLocation();
	}
	
	public static void turnUpdate() {
		here = rc.getLocation();
		roundNum = rc.getRoundNum();		
	}


	/**
	 * A slightly more complicated example function, this returns true if the given bullet is on a collision
	 * course with the current robot. Doesn't take into account objects between the bullet and this robot.
	 *
	 * @param bullet The bullet in question
	 * @return True if the line of the bullet's path intersects with this robot's current position.
	 */
	static boolean willCollideWithMe(BulletInfo bullet) {
		MapLocation myLocation = rc.getLocation();

		// Get relevant bullet information
		Direction propagationDirection = bullet.dir;
		MapLocation bulletLocation = bullet.location;

		// Calculate bullet relations to this robot
		Direction directionToRobot = bulletLocation.directionTo(myLocation);
		float distToRobot = bulletLocation.distanceTo(myLocation);
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

		return (perpendicularDist <= rc.getType().bodyRadius);
	}
	
}
