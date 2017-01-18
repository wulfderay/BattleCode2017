package robbot2;

import Common.Broadcast;
import Common.Globals;
import Common.Util;
import battlecode.common.*;

public class BotArchon extends Globals {

	public static int gardenersHired = 0;
	public static MapLocation WhereIwasBorn = here;
	public static int IamArchonNumber = getArchonNumber();
	public static boolean iAmAlphaArchon = getArchonNumber() == 0;

	public static RobotInfo[] nearbyBots;
	public static int friendlyAttackUnitsNearby;
	public static int friendlyGardenersNearby;
	public static int enemyAttackUnitsNearby;

	public static int turnsWithoutBeingAbleToSpawn = 0;

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
		System.out.println("I'm an archon!");

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

		senseSurroundings();

		Util.AvoidBullets();

		if (iAmAlphaArchon || Broadcast.getAlphaArchonAlert()) { // btw we need to broadcast this so that we can have another take over if I die.
			System.out.println("I'm alpha so lets do something!");

			HireGardnerMaybe();

			MoveToABetterLocation();

			BroadCastIfEmergency();

		}
		else
		{
			Util.tryMove(Util.randomDirection()); // I guess... maybe hide somewhere..
		}

	}

	public static void senseSurroundings() throws GameActionException {
		nearbyBots = rc.senseNearbyRobots();

		friendlyAttackUnitsNearby = 0;
		friendlyGardenersNearby = 0;
		enemyAttackUnitsNearby = 0;

		for (RobotInfo bot : nearbyBots) {
			if (bot.team == us) {
				if (bot.type.canAttack()) {
					friendlyAttackUnitsNearby++;
				}
				if (bot.type == RobotType.GARDENER) {
					friendlyGardenersNearby++;
				}
			} else {
				if (bot.type.canAttack()) {
					enemyAttackUnitsNearby++;
				}
			}
		}
	}

	/**
	 * If I'm being attacked , broadcast for help. Scale up level of emergency depending on my health.
	 * @throws GameActionException
	 */
	private static void BroadCastIfEmergency() throws GameActionException {
		if (enemyAttackUnitsNearby > friendlyAttackUnitsNearby + 3 //being overrun
				|| rc.getHealth() < myType.maxHealth / 2) { // dying
			Broadcast.setAlphaArchonAlert();
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
		Direction dir = Util.getClearDirection(Direction.NORTH, 7, 1, false);
		if (dir == null) {
			System.out.println("Spawning blocked");
			turnsWithoutBeingAbleToSpawn++;
			if (turnsWithoutBeingAbleToSpawn > 10) {
				System.out.println("Giving up trying to spawn. Call down the other archons!");
				Broadcast.setAlphaArchonAlert();
			}
			return;
		}

		turnsWithoutBeingAbleToSpawn = 0;

		if (rc.canHireGardener(dir)) {
			if (friendlyGardenersNearby == 0 && enemyAttackUnitsNearby == 0) {
				System.out.println("No gardeners around so lets get some.");
				for (RobotInfo bot : nearbyBots) {
					System.out.print("Nearby bot"+bot.type+" "+bot.getID());
				}
				rc.hireGardener(dir);
				return;
			}

			if (friendlyGardenersNearby == 0 && rc.getTeamBullets() > 150) { //enough to spawn gardener and soldier
				System.out.println("Under attack but going to try and build a gardener+soldier combo");
				rc.hireGardener(dir);
				return;
			}

			System.out.println("Bullets"+rc.getTeamBullets()+"Gardeners"+friendlyGardenersNearby);

			if (    (friendlyGardenersNearby == 1 && rc.getTeamBullets() > 150) ||
					(friendlyGardenersNearby == 2 && rc.getTeamBullets() > 200) ||
					(friendlyGardenersNearby == 3 && rc.getTeamBullets() > 250) ||
					(friendlyGardenersNearby == 4 && rc.getTeamBullets() > 300) ||
					(friendlyGardenersNearby == 5 && rc.getTeamBullets() > 350)) {
				System.out.println("Got enough bullets for more gardeners"+friendlyGardenersNearby+" "+rc.getTeamBullets());
				rc.hireGardener(dir);
				return;
			}

			if (rc.getTreeCount() < 30 && rc.getTeamBullets() > 400) {
				System.out.println("Got bullets to spare");
				rc.hireGardener(dir);
				return;
			}

		}
	}

}
