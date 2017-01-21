package Common;

import java.util.ArrayList;

import battlecode.common.*;

public class Util extends Globals {

	static MapLocation enemyLoc = rc.getInitialArchonLocations(them)[(int)(Math.random() * rc.getInitialArchonLocations(them).length)];





	/**
	 * Returns a random Direction
	 * @return a random Direction
	 */
	public static Direction randomDirection() {
		return new Direction((float)Math.random() * 2 * (float)Math.PI);
	}

	/**
	 * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	public static boolean tryMove(Direction dir) throws GameActionException {
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
	public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
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
				return true;
			}
			// Try the offset on the right side
			if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck)) && rc.senseNearbyBullets(here.add(dir), myType.bodyRadius).length == 0 ) {
				doMove(dir.rotateRightDegrees(degreeOffset*currentCheck));
				here = rc.getLocation(); //here.add(dir.rotateRightDegrees(degreeOffset*currentCheck),rc.getType().strideRadius);
				return true;
			}
			// No move performed, try slightly further
			currentCheck++;
		}

		// A move never happened, so return false.
		return false;
	}

	public static boolean notNearALumberJack(MapLocation location) {
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

	public static boolean isEarlyGame() {
		return rc.getRoundNum() < 400;
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

	public static void AvoidBullets() throws GameActionException {
		BulletInfo [] bullets = rc.senseNearbyBullets();
		for (BulletInfo bullet : bullets)
		{
			if (Util.willCollideWithMe(bullet))
			{
				Util.tryMove(bullet.getDir().rotateLeftDegrees(90), 5, 3);
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
			System.out.println("Failed to move direction "+dir);
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
			System.out.println("Failed to move direction "+dir+"length"+length);
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
			System.out.println("Failed to move to target at "+target);
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
			System.out.println("Failed to move to target at "+target);
			return false;
		}
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

	public static MapLocation getEnemyLoc()
	{
		return enemyLoc;
	}

	public static void setEnemyLoc(MapLocation loc)
	{
		enemyLoc = loc;
	}




	public static boolean moveToFarTarget(MapLocation target) throws GameActionException
	{
		return simpleBug(target);
	}

	static boolean inBugMode = false;
	static Direction lastDirection = null;
	static final float BUG_ROTATE_INCREMENT = 5.0f;

	public static boolean simpleBug(MapLocation target) throws GameActionException
	{
		System.out.println("Trying to bug move to "+target);
		Direction dirToTarget = here.directionTo(target);
		if (!inBugMode) {
			if (!tryMove(dirToTarget)) {

				inBugMode = true;
				lastDirection = dirToTarget;
			} else {
				return true;
			}
		}

		if (inBugMode) {
			Direction bugDirection = bugGetNextDirection(dirToTarget, lastDirection);
			try {
				rc.move(bugDirection);
				MapLocation temp = here;
				here = rc.getLocation();
				rc.setIndicatorDot(target, 255, 255, 0);
				rc.setIndicatorLine(temp, target, 255, 255, 0);
				rc.setIndicatorLine(temp, temp.add(lastDirection), 100, 100, 0);
				lastDirection = bugDirection;
			} catch (GameActionException e) {
				System.out.println("Failed to bug move direction "+bugDirection);
				return false;
			}
			return true;
		}
		return false;
	}

	public static Direction bugGetNextDirection(Direction targetDirection, Direction lastDirection) {
		Direction moveDir = lastDirection;
		float rotated = 0f;
		if (rc.canMove(moveDir)) {
			Direction testDir = moveDir.rotateLeftDegrees(BUG_ROTATE_INCREMENT);
			while (rc.canMove(testDir)) {
				if (targetDirection.degreesBetween(testDir) <= BUG_ROTATE_INCREMENT &&
						rc.canMove(targetDirection)) {
					inBugMode = false;
					return targetDirection;
				}
				testDir = testDir.rotateLeftDegrees(BUG_ROTATE_INCREMENT);
				rotated += BUG_ROTATE_INCREMENT;
				if (rotated > 360) {
					inBugMode = false;
					return targetDirection;
				}
			}
			return testDir.rotateRightDegrees(BUG_ROTATE_INCREMENT);
		} else {
			while (!rc.canMove(moveDir)) {
				moveDir = moveDir.rotateRightDegrees(BUG_ROTATE_INCREMENT);
				if (rotated > 360) {
					inBugMode = false;
					return targetDirection;
				}
			}
			return moveDir;
		}
	}

	public static Direction getClearDirection(Direction dir,  float resolution, float radius, boolean strict) throws GameActionException {
		return getClearDirection(dir,resolution,radius,strict,false);
	}
	/**
	 * find a direction to move or spawn in. It will return the first such direction that is clear for at least one radius.
	 * @param radius
	 * @return
	 */
	public static Direction getClearDirection(Direction dir,  float resolution, float radius, boolean strict, boolean avoidStartingDirection) throws GameActionException {
		if (dir == null) rc.setIndicatorDot(here.add(Direction.getNorth(), 1), 255,255,255);
		float distanceToCenter = myType.bodyRadius+radius;
		if (!avoidStartingDirection && !rc.isCircleOccupied(here.add(dir, distanceToCenter), radius) && rc.onTheMap(here.add(dir, distanceToCenter), radius))
			return dir;

		float cumulativeOffset = resolution;

		while (cumulativeOffset < 360 && cumulativeOffset > -360) {
			Direction testDir = dir.rotateLeftDegrees(cumulativeOffset);
			MapLocation testLoc = here.add(testDir, distanceToCenter);
			rc.setIndicatorDot(here.add(testDir, distanceToCenter), 255,255,255);
			if (!rc.isCircleOccupiedExceptByThisRobot(testLoc, radius) && rc.onTheMap(testLoc, radius))
			{
				if (!avoidStartingDirection || !MapLocation.doCirclesCollide(testLoc, radius,here.add(dir, distanceToCenter), radius ))
					return testDir;
			}
			cumulativeOffset += resolution;
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
		if ( !rc.isCircleOccupiedExceptByThisRobot(here, maxradius - myType.bodyRadius)) // we're in a good spot already.
			return;
		Direction dir = here.directionTo(rc.getInitialArchonLocations(them)[0]).opposite();
		Direction happyDir = null;

		float radius = maxradius - myType.bodyRadius;
		while (happyDir == null && radius > 0) {
			happyDir = Util.getClearDirection(dir, 5, radius, false);
			radius--;
		}
		if (happyDir != null)
			Util.tryMove(happyDir);
		// Move randomly
		Util.tryMove(Util.randomDirection());
	}

	//Combat utility functions (previously in Soldier):
	public static boolean pursueAndDestroy(RobotInfo target) throws GameActionException {
		boolean moved = Util.moveToNearBot(target);
		boolean shot = Util.maximumFirepowerAtSafeTarget(target);
		return moved || shot;
	}

	public static boolean maximumFirepowerAtSafeTarget(RobotInfo currentTarget) throws GameActionException {
		if ( safeToFireAtTarget(currentTarget) )
			return maximumFirepowerAt(currentTarget.location);
		System.out.println("Target not safe to shoot at!"+currentTarget.location);
		return false;
	}

	public static boolean maximumFirepowerAtSafeTarget(RobotInfo currentTarget, RobotInfo[] enemies) throws GameActionException {
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
		Direction direction = here.directionTo(target.location);
		//Test the line towards the target:
		MapLocation testLocation;
		int sensedTeammates;
		float distance = rc.getType().bodyRadius + 0.1f;
		float DISTANCE_INCREMENT = 0.3f; //Chosen by IEEE certified random dice roll
		float senseRadius = 0.01f;
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

	public static boolean maximumFirepowerAt(MapLocation target) throws GameActionException
	{
		Direction direction = here.directionTo(target);
		float distance = here.add(direction, rc.getType().bodyRadius).distanceTo(target);
		if ( distance < 1.75f ) //Determined on the back of an official IEEE napkin
		{
			if (rc.canFirePentadShot()) {
				System.out.println("FIRING PENTAD SHOT!");
				rc.firePentadShot(here.directionTo(target));
				rc.setIndicatorLine(here, target, 100, 0, 0);
				return true;
			}
		}
		if ( distance < 2.5f )
		{
			if (rc.canFireTriadShot()) {
				System.out.println("FIRING TRIAD SHOT!");
				rc.fireTriadShot(here.directionTo(target));
				rc.setIndicatorLine(here, target, 170, 0, 0);
				return true;
			}
		}

		if (rc.canFireSingleShot()) {
			System.out.println("Firing single shot!");
			rc.fireSingleShot(here.directionTo(target));
			rc.setIndicatorLine(here, target, 255, 0, 0);
			return true;
		}
		return false;
	}

	// sometimes firing sraight at an enemy is bad. if we want to scare em off or if they dodge too well, missing might help.
	public static boolean fireStormTrooperStyle(MapLocation loc) throws GameActionException {
		Direction toFire = here.directionTo(loc).rotateLeftDegrees((float)(-5.0f + (Math.random() * 10)));
		if (rc.canFireSingleShot()) { // don't fire expensive bullets early game
			System.out.println("Firing single shot!");
			rc.fireSingleShot(toFire);
			rc.setIndicatorLine(here, loc, 255, 100, 0); // it should be where we are firing to...
			return true;
		}
		return false;
	}

	//This is a really bad picker:  Still bad, but whatevs.
	public static RobotInfo pickPriorityTarget(RobotInfo[] enemies)
	{
		if ( enemies.length == 0 )
			return null;
		boolean isEarlyGame = Util.isEarlyGame();
		RobotInfo ArchonAsLastResort = null;
		for ( RobotInfo enemy : enemies )
		{
			if ( enemy.getType() != RobotType.ARCHON)
				return enemy;
			else {
				if (!isEarlyGame)
					ArchonAsLastResort = enemy;
			}

		}
		return ArchonAsLastResort;
	}

	public static boolean moveToNearBot(RobotInfo target) throws GameActionException
	{
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
		Util.tryMove(here.directionTo(target.location),10,10);
		//moveFurthestDistancePossibleTowards(target);//simpleSlide(target);
		return true;
	}

	public static boolean moveToNearTarget(MapLocation target) throws GameActionException
	{
		Util.tryMove(here.directionTo(target),10,10);
		//moveFurthestDistancePossibleTowards(target);//simpleSlide(target);
		return true;
	}
	/////////////////

	public static void makeAmoebaCircle() throws GameActionException {
		// TODO Auto-generated method stub
		//Friendly list:
		RobotInfo[] friends = rc.senseNearbyRobots(-1, us);
		MapLocation bossArchonLocation = Broadcast.RetrieveBossArchonLocation();

		//Determine goal position:
		Direction outsideOfCircle = bossArchonLocation.directionTo(here);

		float MAX_DISTANCE_TO_ADJACENT_FRIENDS = 3f + rc.getType().bodyRadius;
		float MIN_DISTANCE_TO_ADJACENT_FRIENDS = 2f + rc.getType().bodyRadius;
		float DISTANCE_TO_MAINTAIN_AROUND_GARDENER = 3f + rc.getType().bodyRadius;

		//First, find my friends:
		RobotInfo friend1 = null;
		RobotInfo friend2 = null;
		RobotInfo closestGardener = null;
		for(RobotInfo friend : friends)
		{
			if ( friend.type == RobotType.SOLDIER ) //And lumberjack, later...
			{
				if ( friend1 == null )
					friend1 = friend;
				else
				if ( friend2 == null )
					friend2 = friend;
			}
			if ( friend.type == RobotType.GARDENER )
				if ( closestGardener == null )
					closestGardener = friend;
		}

		if ( closestGardener != null ) {
			//Move to the other side of the gardener from the bossArchonLocation
			//Util.moveToNearTarget(closestGardener.location.add(outsideOfCircle, DISTANCE_TO_MAINTAIN_AROUND_GARDENER));
			//rc.setIndicatorLine(closestGardener.location, closestGardener.location.add(outsideOfCircle, DISTANCE_TO_MAINTAIN_AROUND_GARDENER), 0, 0, 255);
			//return;
		}
		if ((friend1 == null || friend2 == null ) )
		{
			System.out.println("I got no friends!");
			rc.setIndicatorDot(here, 125, 125, 125);
			if ( friend1 != null )
			{
				Direction moveDirFriend1 = maintainDistanceWith(friend1, MAX_DISTANCE_TO_ADJACENT_FRIENDS, MIN_DISTANCE_TO_ADJACENT_FRIENDS, bossArchonLocation);
				if ( moveDirFriend1 != null )
				{
					System.out.println("Too close to my only friend1!");
					Util.tryMove(moveDirFriend1,10,10, 0.1f);
					rc.setIndicatorLine(here, friend1.location, 255, 100, 255);
					return;
				}
				return;
			}
			return;
		}

		float moveSpeed = rc.getType().strideRadius;

		//Maintain distance from friend 1:
		Direction moveDirFriend1 = maintainDistanceWith(friend1, MAX_DISTANCE_TO_ADJACENT_FRIENDS, MIN_DISTANCE_TO_ADJACENT_FRIENDS, bossArchonLocation);
		Direction moveDirFriend2 = maintainDistanceWith(friend2, MAX_DISTANCE_TO_ADJACENT_FRIENDS, MIN_DISTANCE_TO_ADJACENT_FRIENDS, bossArchonLocation);
		if ( moveDirFriend1 != null && moveDirFriend2 != null )
		{
			System.out.println("Too close to both friends!");
			Direction friendlyDirection = halfwayDirection(moveDirFriend1, moveDirFriend2);
			Util.tryMove(friendlyDirection,10,10, moveSpeed);
			rc.setIndicatorLine(here, friend1.location, 255, 255, 255);
			rc.setIndicatorLine(here, friend2.location, 255, 255, 255);
			return;
		}
		if ( moveDirFriend1 != null )
		{
			System.out.println("Too close to friend1!");
			Util.tryMove(moveDirFriend1,10,10, moveSpeed/2);
			rc.setIndicatorLine(here, friend1.location, 255, 100, 255);
			return;
		}

		if ( moveDirFriend2 != null )
		{
			System.out.println("Too close to friend2!");
			Util.tryMove(moveDirFriend2,10,10, moveSpeed/2);
			rc.setIndicatorLine(here, friend2.location, 255, 255, 100);
			return;
		}

		System.out.println("AMOEBA!");

		//Okay, we've got distance, but have we got degrees?
		Direction towardsCenter = here.directionTo(bossArchonLocation);
		Direction towardsFriend1 = here.directionTo(friend1.location);
		float degreesBetween = towardsFriend1.degreesBetween(towardsCenter);
		float DEGREE_FUDGE = 5f;
		if ( degreesBetween > 0 )
		{
			if ( degreesBetween < 90 + DEGREE_FUDGE && degreesBetween > 90 - DEGREE_FUDGE)
				return;
			Util.tryMove(towardsCenter,10,10, moveSpeed/2);
		}
		else
		{
			degreesBetween *= -1;
			if ( degreesBetween < 90 + DEGREE_FUDGE && degreesBetween > 90 - DEGREE_FUDGE)
				return;
			Util.tryMove(towardsCenter.opposite(),10,10, moveSpeed/2);
		}

	}

	// Tries to stay nearby but move around randomly
	public static boolean defend(RobotInfo bot) throws GameActionException
	{
		float minDistance = 2;
		float maxDistance = 5;
		float optimalDistance = 3;

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


	//True if moved:
	public static Direction maintainDistanceWith(RobotInfo friend, float MAX, float MIN, MapLocation centerOfCircle) throws GameActionException
	{
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



	public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide, float distance) throws GameActionException {
		if (rc.hasMoved())
			return false;
		// First, try intended direction
		if (rc.canMove(dir, distance) && rc.senseNearbyBullets(here.add(dir), myType.bodyRadius).length == 0 ) {
			doMove(dir, distance);
			here = rc.getLocation();
			return true;
		}

		// Now try a bunch of similar angles
		int currentCheck = 1;

		while(currentCheck<=checksPerSide) {
			// Try the offset of the left side
			if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck), distance) && rc.senseNearbyBullets(here.add(dir), myType.bodyRadius).length == 0  && notNearALumberJack(here.add(dir))) {
				doMove(dir.rotateLeftDegrees(degreeOffset*currentCheck), distance);
				here = rc.getLocation(); //here.add(dir.rotateLeftDegrees(degreeOffset*currentCheck),rc.getType().strideRadius);
				return true;
			}
			// Try the offset on the right side
			if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck), distance) && rc.senseNearbyBullets(here.add(dir), myType.bodyRadius).length == 0 ) {
				doMove(dir.rotateRightDegrees(degreeOffset*currentCheck), distance);
				here = rc.getLocation(); //here.add(dir.rotateRightDegrees(degreeOffset*currentCheck),rc.getType().strideRadius);
				return true;
			}
			// No move performed, try slightly further
			currentCheck++;
		}

		// A move never happened, so return false.
		return false;
	}

	public static void BuyVPIfItWillMakeUsWin() throws GameActionException {
		if ( rc.getTeamBullets() > (1000 - rc.getTeamVictoryPoints())* getVpCostThisRound() || rc.getRoundLimit() -rc.getRoundNum() < 5)
		{
			rc.donate(rc.getTeamBullets());
		}
		if (rc.getTeamBullets() > 1200)
		rc.donate(rc.getTeamBullets() -1000);
	}

	public static float getVpCostThisRound()
	{
		return (float) (7.5 + (rc.getRoundNum()*12.5 / rc.getRoundLimit()));
	}
}
