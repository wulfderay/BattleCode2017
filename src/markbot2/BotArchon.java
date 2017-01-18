package markbot2;

import Common.Broadcast;
import Common.Globals;
import Common.Util;
import battlecode.common.*;

public class BotArchon extends Globals {

	public static int gardenersHired = 0;
	public static MapLocation WhereIwasBorn = here;
	public static int IamArchonNumber = getArchonNumber();
	public static boolean iAmAlphaArchon = getArchonNumber() == 0;
	public static int stuckGardeners = 0;

	private static int getArchonNumber()  {
		MapLocation [] arconlocs = rc.getInitialArchonLocations(us);
		for (int i = 0; i < arconlocs.length; i++ )
		{
			if (arconlocs[i].equals(WhereIwasBorn))
				return i;
		}
		try {
			throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE, "i am not in the list wtf");
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		return 4;
	}

	public static void loop() throws GameActionException {

		//Update target location!
		Broadcast.WriteEnemyLocation(rc.getInitialArchonLocations(them)[0]);

		// The code you want your robot to perform every round should be in this loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your robot to explode
			try {

				//Update common data
				turnUpdate();

				//Do some stuff
				turn();

			} catch (Exception e) {
				System.out.println("Archon Exception");
				e.printStackTrace();
			}

			//Test that we completed within bytecode limit
			if (rc.getRoundNum() != roundNum) {
				System.out.println("Archon over bytecode limit");
			}

			// Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
			Clock.yield();

		}
	}

	public static void turn() throws GameActionException {
		Broadcast.RollCall();

		iAmAlphaArchon = Broadcast.AmIAlphaArchon();

		Util.AvoidBullets();

		if (iAmAlphaArchon) { // btw we need to broadcast this so that we can have another take over if I die.
			Broadcast.TallyRollCalls();
			stuckGardeners = Broadcast.TallyStuckGardeners();
			Util.MoveToAClearerLocation(myType.sensorRadius/2);
			HireGardnerMaybe();
		}
		else
		{
			Util.MoveToAClearerLocation(myType.sensorRadius/2);
			HireGardnerMaybe();
		}
		BroadCastIfEmergency();
	}

	/**
	 * If I'm being attacked , broadcast for help. Scale up level of emergency depending on my health.
	 * @throws GameActionException
	 */
	private static void BroadCastIfEmergency() throws GameActionException {
		// Broadcast archon's location for other robots on the team to know
		// hmm.. no one cares yet.
		if (rc.senseNearbyRobots(myType.sensorRadius, them).length > 0)
		{
			MapLocation myLocation = rc.getLocation();
			rc.broadcast(0+IamArchonNumber*4,(int)myLocation.x);
			rc.broadcast(1+IamArchonNumber*4,(int)myLocation.y);
			rc.broadcast(2+IamArchonNumber*4,(int)rc.getHealth());
			rc.broadcast(3+IamArchonNumber*4,1);  // it's an emergency
		}
		else
		{
			rc.broadcast(3+IamArchonNumber*4,0);  // it's  not an emergency
		}

	}

	private static void HireGardnerMaybe() throws GameActionException {
		// Generate a random direction
		//Direction dir = Util.getClearDirection(RobotType.GARDENER.bodyRadius);
		Direction dir = Util.randomDirection();
		// Randomly attempt to build a gardener in this direction

		int numGardeners = Broadcast.GetNumberOfRobots(RobotType.GARDENER);

		if (rc.canHireGardener(dir)) {
			if (rc.getTreeCount() == 0 || stuckGardeners >= numGardeners) {
				rc.hireGardener(dir);
				gardenersHired++;
			} //else if (rc.getTreeCount() > gardenersHired * 2) {
			else if (rc.getRobotCount() > 1 + rc.getInitialArchonLocations(us).length + numGardeners ){
				rc.hireGardener(dir);
				gardenersHired++;
			} else if (rc.getTreeCount() < 30 && rc.getTeamBullets() > 400) {
				rc.hireGardener(dir);
				gardenersHired++;
			}
		}
	}

}
