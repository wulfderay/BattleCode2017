package astarrynight;

import Common.*;
import battlecode.common.*;

public class BotArchon extends Globals {

	public static int IamArchonNumber = getArchonNumber();
	public static boolean iAmAlphaArchon = getArchonNumber() == 0;
	public static int stuckGardeners = 0;

	public static RobotInfo[] nearbyBots;
	public static TreeInfo[] nearbyTrees;
	public static RobotInfo closestGardener;

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

			rc.setIndicatorDot(here, 0, 255, 0);

			Broadcast.TallyRollCalls();
			Broadcast.ClearTreeList();
			stuckGardeners = Broadcast.TallyStuckGardeners();
			//UtilSpawn.MoveToAClearerLocation(3);

			moveToSafeLocation();

			HireGardnerMaybe();

			BroadCastIfEmergency();
		}
		else
		{
			// Actions for beta archons
			//UtilSpawn.MoveToAClearerLocation(3);
			moveToSafeLocation();

			//HireGardnerMaybe();
		}
		for (TreeInfo tree : nearbyTrees)
		{
			if ( Clock.getBytecodesLeft() < 100)
				return;
			if ( tree.getTeam() != us && tree.location.distanceTo(here) < 4)
				Broadcast.INeedATreeChopped(tree.getLocation());
		}


	}

	public static void senseSurroundings() throws GameActionException {
		nearbyBots = rc.senseNearbyRobots();
		nearbyTrees = rc.senseNearbyTrees(-1,null);

		friendlyAttackUnitsNearby = 0;
		friendlyGardenersNearby = 0;
		enemyAttackUnitsNearby = 0;

		closestGardener = null;

		for (RobotInfo bot : nearbyBots) {
			if (bot.team == us) {
				if (bot.type.canAttack()) {
					friendlyAttackUnitsNearby++;
				}
				if (bot.type == RobotType.GARDENER) {
					friendlyGardenersNearby++;
					if (closestGardener == null)
						closestGardener = bot;
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

	private static void moveToSafeLocation() {
		TreeInfo closestTree = null;
		if (nearbyTrees.length > 0)
			closestTree = nearbyTrees[0];
		RobotInfo closestRobot = null;
		if (nearbyBots.length > 0)
			closestRobot = nearbyBots[0];

		MapLocation closestEntity = null;
		if (closestRobot != null) {
			closestEntity = closestRobot.location;
		}
		if (closestTree != null) {
			if (closestEntity == null) {
				closestEntity = closestTree.location;
			} else if (here.distanceTo(closestEntity) > here.distanceTo(closestTree.location)) {
				closestEntity = closestTree.location;
			}
		}

		if (closestGardener != null && Math.abs(here.directionTo(closestGardener.location).degreesBetween(here.directionTo(globalTarget))) < 100) {
			//Gardener is between us and the enemy, therefor we are going to get stuck in the wall
			UtilMove.moveToFarTarget(globalTarget);
		}

		if (closestEntity != null) {
			if (here.distanceTo(closestEntity) < 4) {
				UtilMove.tryMove(closestEntity.directionTo(here).rotateLeftDegrees((float)Math.random()*10 - 5));
				return;
			}
			if (here.distanceTo(closestEntity) > 9) {
				UtilMove.tryMove(here.directionTo(closestEntity).rotateLeftDegrees((float)Math.random()*10 - 5));
				return;
			}
			UtilMove.tryMove(Util.randomDirection());
		}

	}

	private static int turnsWithoutBeingAbleToSpawn = 0;


	private static void HireGardnerMaybe() throws GameActionException {
		Direction dir = UtilSpawn.getClearDirection(UtilSpawn.towardsEnemySpawn().opposite(), 7, 1, false);
		if (dir == null) {
			System.out.println("Spawning blocked");
			turnsWithoutBeingAbleToSpawn++;
			if (turnsWithoutBeingAbleToSpawn > 10) {
				System.out.println("Giving up trying to spawn. Call down the other archons!");
				Broadcast.setAlphaArchonAlert();
			}
			return;
		}

		int totalGardeners = Broadcast.GetNumberOfLive(RobotType.GARDENER);
		if (rc.canHireGardener(dir)) {
			if ((totalGardeners == 0 || stuckGardeners >= totalGardeners )&& enemyAttackUnitsNearby == 0) {
				System.out.println("No gardeners around who can spawn so lets get some.");
				rc.hireGardener(dir);
				return;
			}

			if (totalGardeners == 0 && rc.getTeamBullets() > 150) { //enough to spawn gardener and soldier
				System.out.println("Under attack but going to try and build a gardener+soldier combo");
				rc.hireGardener(dir);
				return;
			}

			System.out.println("Bullets"+rc.getTeamBullets()+"Gardeners"+totalGardeners);

			// this has to be better. Take into account military units.
			if (    (totalGardeners == 1 && rc.getTeamBullets() > 160) ||
					(totalGardeners == 2 && rc.getTeamBullets() > 160) ||
					(totalGardeners == 3 && rc.getTeamBullets() > 200) ||
					(totalGardeners == 4 && rc.getTeamBullets() > 250) ||
					(totalGardeners == 5 && rc.getTeamBullets() > 300)) {
				System.out.println("Got enough bullets for more gardeners"+totalGardeners+" "+rc.getTeamBullets());
				rc.hireGardener(dir);
				return;
			}

			if (rc.getTreeCount() < 50 && rc.getTeamBullets() > 400) {
				System.out.println("Got bullets to spare");
				rc.hireGardener(dir);
				return;
			}

		}
	}


}
