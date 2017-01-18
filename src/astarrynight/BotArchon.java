package astarrynight;

import Common.Broadcast;
import Common.Globals;
import Common.Util;
import battlecode.common.*;

public class BotArchon extends Globals {

	public static int gardenersHired = 0;
	public static MapLocation WhereIwasBorn = here;
	public static int IamArchonNumber = getArchonNumber();
	public static boolean iAmAlphaArchon = getArchonNumber() == 0;

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
		Broadcast.RollCall();

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

		Util.AvoidBullets();

		if (iAmAlphaArchon) { // btw we need to broadcast this so that we can have another take over if I die.
			HireGardnerMaybe();

			MoveToABetterLocation();
			Broadcast.TallyRollCalls();
		}
		else
		{
			Util.tryMove(Util.randomDirection()); // I guess... maybe hide somewhere..
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
		if (rc.senseNearbyRobots(myType.sensorRadius, them).length > 0);
		{
			MapLocation myLocation = rc.getLocation();
			rc.broadcast(0+IamArchonNumber*3,(int)myLocation.x);
			rc.broadcast(1+IamArchonNumber*3,(int)myLocation.y);
			rc.broadcast(2+IamArchonNumber*3,(int)rc.getHealth());
		}

	}

	/**
	 * Try to find a clear spot with no gardeners or enemies or bullets so I can spawn some gardeners in peace.
	 * @throws GameActionException
	 */
	private static void MoveToABetterLocation() throws GameActionException {

		// Move randomly
		Util.tryMove(Util.randomDirection());
	}

	//TODO: TAke into account how many archons there are, and how many bots we have.
	private static void HireGardnerMaybe() throws GameActionException {
		// Generate a random direction
		//Direction dir = Util.getClearDirection(RobotType.GARDENER.bodyRadius);
		Direction dir = Util.randomDirection();
		// Randomly attempt to build a gardener in this direction
		if (rc.canHireGardener(dir)) {
			if (rc.getTreeCount() == 0) {
				rc.hireGardener(dir);
				gardenersHired++;
			} //else if (rc.getTreeCount() > gardenersHired * 2) {
			else if (rc.getRobotCount() > 2 + rc.getInitialArchonLocations(us).length){
				rc.hireGardener(dir);
				gardenersHired++;
			} else if (rc.getTreeCount() < 30 && rc.getTeamBullets() > 400) {
				rc.hireGardener(dir);
				gardenersHired++;
			}
		}
	}

}
