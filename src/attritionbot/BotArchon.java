package attritionbot;

import Common.*;
import battlecode.common.*;

public class BotArchon extends Globals {

	public static int IamArchonNumber = getArchonNumber();
	public static boolean iAmAlphaArchon = getArchonNumber() == 0;
	public static int stuckGardeners = 0;

	public static RobotInfo[] nearbyBots;
	public static int friendlyAttackUnitsNearby;
	public static int friendlyGardenersNearby;
	public static int enemyAttackUnitsNearby;

	private static int getArchonNumber()  {
		MapLocation [] arconlocs = rc.getInitialArchonLocations(us);
		for (int i = 0; i < arconlocs.length; i++ )
		{
			if (arconlocs[i].equals(here))
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
				UtilDebug.debug_exceptionHandler(e,"Archon Exception");
			}

			//Test that we completed within bytecode limit
			if (rc.getRoundNum() != roundNum) {
				UtilDebug.alert();
				System.out.println("Archon over bytecode limit");
			}

			// Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
			Clock.yield();

		}
	}

	public static void turn() throws GameActionException {
		Util.BuyVPIfItWillMakeUsWin();
		Broadcast.RollCall();

		iAmAlphaArchon = Broadcast.AmIAlphaArchon();

		UtilMove.AvoidBullets();

		senseSurroundings();

		if (iAmAlphaArchon || Broadcast.getAlphaArchonAlert()) {
			// Actions for alpha archons

			Broadcast.TallyRollCalls();
			stuckGardeners = Broadcast.TallyStuckGardeners();
			UtilSpawn.MoveToAClearerLocation(3);

			HireGardnerMaybe();

			BroadCastIfEmergency();
		}
		else
		{
			// Actions for beta archons
			UtilSpawn.MoveToAClearerLocation(3);
			HireGardnerMaybe();
		}

		// Common actions
		UtilSpawn.MoveToAClearerLocation(3);

		for (TreeInfo tree : rc.senseNearbyTrees(myType.sensorRadius, Team.NEUTRAL))
		{
			if (Clock.getBytecodesLeft() >100)
			{
				Broadcast.INeedATreeChopped(tree.getLocation());
			}
			else
				break;
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
	private static void BroadCastIfEmergency() {
		if (enemyAttackUnitsNearby > friendlyAttackUnitsNearby + 3 //being overrun
				|| rc.getHealth() < myType.maxHealth / 2) { // dying
			Broadcast.setAlphaArchonAlert();
		}
	}

	private static void HireGardnerMaybe() throws GameActionException {
		Direction dir = UtilSpawn.getClearDirection(Direction.NORTH, 7, 1, false);
		if (dir == null) {
			System.out.println("Spawning blocked");
			return;
		}

		int totalGardeners = Broadcast.GetNumberOfLive(RobotType.GARDENER);
		if (rc.canHireGardener(dir)) {
			if ((totalGardeners == 0 || stuckGardeners >= totalGardeners )&& enemyAttackUnitsNearby == 0) {
				System.out.println("No gardeners around who can spawn so lets get some.");
				rc.hireGardener(dir);
				Broadcast.IHaveSpawnedA(RobotType.GARDENER);
				return;
			}

			if (totalGardeners == 0 && rc.getTeamBullets() > 150) { //enough to spawn gardener and soldier
				System.out.println("Under attack but going to try and build a gardener+soldier combo");
				rc.hireGardener(dir);
				Broadcast.IHaveSpawnedA(RobotType.GARDENER);
				return;
			}

			System.out.println("Bullets"+rc.getTeamBullets()+"Gardeners"+totalGardeners);

			if (    (totalGardeners == 1 && rc.getTeamBullets() > 150) ||
					(totalGardeners == 2 && rc.getTeamBullets() > 200) ||
					(totalGardeners == 3 && rc.getTeamBullets() > 250) ||
					(totalGardeners == 4 && rc.getTeamBullets() > 300) ||
					(totalGardeners == 5 && rc.getTeamBullets() > 350)) {
				System.out.println("Got enough bullets for more gardeners"+totalGardeners+" "+rc.getTeamBullets());
				rc.hireGardener(dir);
				Broadcast.IHaveSpawnedA(RobotType.GARDENER);
				return;
			}

			if (rc.getTreeCount() < 30 && rc.getTeamBullets() > 400) {
				System.out.println("Got bullets to spare");
				rc.hireGardener(dir);
				Broadcast.IHaveSpawnedA(RobotType.GARDENER);
				return;
			}

		}
	}


}
