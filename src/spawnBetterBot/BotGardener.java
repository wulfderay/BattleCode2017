package spawnBetterBot;

import Common.*;
import battlecode.common.*;

import java.util.Map;

public class BotGardener extends Globals {


	public static int treesPlanted = 0;

	public static int buildIndex = 0;
	//public static Boolean builtGrove = false;
	public static Direction spawnLocation = null;
	public static int numSafeSpawnLocations = 0;

	//Spawn crap
	public static MapLocation[] spawnLocs = new MapLocation[] {null, null, null, null, null, null};
	public static int[] spawnLocStatus = new int[] {0,0,0,0,0,0};
	public static Direction[] spawnDirs = new Direction[] {Direction.NORTH, Direction.NORTH.rotateRightDegrees(60), Direction.NORTH.rotateRightDegrees(120), Direction.SOUTH, Direction.SOUTH.rotateRightDegrees(60), Direction.SOUTH.rotateRightDegrees(120)};
	public static MapLocation spawnLocHere;

	public static RobotInfo[] nearbyBots;
	public static TreeInfo[] nearbyFriendlyTrees;
	public static TreeInfo[] nearbyNeutralTrees;
	public static int enemyAttackUnitsNearby = 0;
	public static int friendlyAttackUnitsNearby = 0;
	public static int enemyScoutsNearby = 0;
	public static int friendlySoldiersNearby = 0;
	public static int friendlyLumberJacksNearby = 0;

	public static void loop() throws GameActionException {
		System.out.println("I'm an gardener!");

		initGardener();

		// The code you want your robot to perform every round should be in this loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your robot to explode
			try {

				//Update common data
				turnUpdate();

				//Do some stuff
				turn();

			} catch (Exception e) {
				UtilDebug.debug_exceptionHandler(e,"Gardener Exception");
			}

			//Test that we completed within bytecode limit
			if (rc.getRoundNum() != roundNum) {
				UtilDebug.alert();
				System.out.println("Gardener over bytecode limit");
			}

			// Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
			Clock.yield();

		}
	}

	//TODO: If there's enemies nearby, spawn some soldiers , and spawn a tree in the way of bullets.
	public static void turn() {

		// Basic initialization stuff
		Util.BuyVPIfItWillMakeUsWin();
		Broadcast.RollCall();
		senseSurroundings();

		// Water some trees
		waterTrees();

		Direction spawnDir = determineSpawnLocation();

		if (moveToSpawnLocation()) {
			spawnTrees(spawnDir);
		}

	}

	public static boolean moveToSpawnLocation() {
		if (nearbyFriendlyTrees.length > 0) {
			TreeInfo closestTree = nearbyFriendlyTrees[0];
			return UtilMove.moveAdjacentToTree(closestTree);
		} else {
			return true;
		}

	}

	public static boolean spawnTrees(Direction spawnDir) {
		// Spawn trees
		if (rc.getBuildCooldownTurns() > 0) {
			rc.setIndicatorDot(here, 0,0,0);
			return false;
		}
		if (rc.getTeamBullets() < 50) {
			rc.setIndicatorDot(here, 20,20,20);
			return false;
		}
		if (numSafeSpawnLocations > 1 && spawnDir != null) {
			if (rc.canPlantTree(spawnDir)) {
				try {
					rc.plantTree(spawnDir);
					return true;
				} catch (GameActionException e) {
					UtilDebug.debug_exceptionHandler(e, "Tree planting exception");
				}
			} else {
				System.out.println("Tried to plant in a safe location but failed");
				rc.setIndicatorDot(here, 255, 100,100);
			}
		}
		return false;
	}

	public static void senseSurroundings() {
		nearbyBots = rc.senseNearbyRobots();
		nearbyFriendlyTrees = rc.senseNearbyTrees(-1, us);

		nearbyNeutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);

		for (RobotInfo bot : nearbyBots) {
			if (bot.team == us) {
				if (bot.type.canAttack()) {
					friendlyAttackUnitsNearby++;
					if (bot.type == RobotType.SOLDIER) {
						friendlySoldiersNearby++;
					} else if (bot.type == RobotType.LUMBERJACK) {
						friendlyLumberJacksNearby++;
					}
				}
			} else {
				if (bot.type.canAttack()) {
					enemyAttackUnitsNearby++;
					if (bot.type == RobotType.SCOUT) {
						enemyScoutsNearby++;
					}
				}
			}
		}
	}

	public static void setupSpawnLocations() {
		if (here == spawnLocHere)
			return;
		spawnLocHere = here;

		for (int i = 6; --i >= 0; ) {
			spawnLocs[i] = here.add(spawnDirs[i], 2);
		}

	}

	static final int LOCATION_EMPTY = 0;
	static final int LOCATION_TREE = 1;
	static final int LOCATION_ROBOT = 2;
	static final int LOCATION_UNKNOWN = 3;

	public static int canSpawn(MapLocation loc, Direction dir) {
		TreeInfo[] trees = rc.senseNearbyTrees(loc, 1, null);
		if (trees.length == 0)
		{
			RobotInfo[] bots = rc.senseNearbyRobots(loc, 0.99f, null);
			if (bots.length == 0) {
				rc.setIndicatorDot(loc, 255, 255, 255);
				return LOCATION_EMPTY;
			} else {
				rc.setIndicatorDot(loc, 150, 150, 150);
				return LOCATION_ROBOT;
			}
		} else {
			if (trees[0].team == us) {
				rc.setIndicatorDot(loc, 0, 0, 255);
			} else {
				rc.setIndicatorDot(loc, 255, 0, 0);
				Broadcast.INeedATreeChopped(loc);
			}
			return LOCATION_TREE;
		}
	}

	public static Direction determineSpawnLocation() {
		setupSpawnLocations();
		Direction bestLocation = null;
		numSafeSpawnLocations = 0;
		for (int i = 6; --i >= 0;) {
			spawnLocStatus[i] = canSpawn(spawnLocs[i], spawnDirs[i]);
			if (spawnLocStatus[i] == LOCATION_EMPTY) {
				numSafeSpawnLocations++;
				bestLocation = spawnDirs[i];
			}
			if (spawnLocStatus[i] == LOCATION_ROBOT) {
				numSafeSpawnLocations++;
			}
		}
		return bestLocation;
	}



	// to be refactored.
	public static boolean spawnBot(RobotType robotType) throws GameActionException {
		// update spawnlocation if needed.
		//spawnLocation = refreshSpawnDirection();

		if (spawnLocation == null)
		{
			rc.setIndicatorDot(here.add(spawnLocation, 1), 250, 50, 50);
			System.out.println("I'm Fucking Stuck! WTF?!");
			return false;
		}

		rc.setIndicatorDot(here.add(spawnLocation, 1), 50,50,50);

		if (!rc.hasRobotBuildRequirements(robotType)) {
			return false;
		}

		if (rc.canBuildRobot(robotType, spawnLocation)) {
			rc.buildRobot(robotType, spawnLocation);
			System.out.println("Spawned a new "+robotType);
			return true;
		} else {
			//This is a shitty hack but sometimes Util.getClearDirection returns an invalid spawn location so going to try brute forcing it.
			float resolution = 3;
			float cumilativeOffset = resolution;

			while (cumilativeOffset < 360) {
				if (rc.canBuildRobot(robotType, spawnLocation.rotateLeftDegrees(cumilativeOffset))) {
					rc.buildRobot(robotType, spawnLocation);
					System.out.println("Spawned a new "+robotType);
					return true;
				}
				cumilativeOffset += resolution;
			}
		}
		return false;
	}

	public static void waterTrees() {
		float maxDamage = 0f;
		TreeInfo worstTree = null;
		for (TreeInfo tree : nearbyFriendlyTrees) {
			if (rc.canWater(tree.ID)) {
				float damage = tree.maxHealth - tree.health;
				if (damage > maxDamage) {
					maxDamage = damage;
					worstTree = tree;
				}
			}
		}
		if (worstTree != null) {
			try {
				rc.water(worstTree.ID);
				System.out.println("Watered tree " + worstTree.ID);
			} catch (GameActionException e) {
				UtilDebug.debug_exceptionHandler(e, "Watering tree exception");
			}
		}
	}


	public static Boolean plantTree() throws GameActionException {

		//if (spawnLocation == null)
		//	spawnLocation = refreshSpawnDirection();
		System.out.println("Trying to build a new tree. Trees so far:"+treesPlanted);
		Direction plantDirection = UtilSpawn.getClearDirection(spawnLocation != null? spawnLocation:Direction.NORTH, 15, 1f, false, true);
		if (plantDirection != null)
		{
			if (rc.canPlantTree(plantDirection) ) {
				rc.plantTree(plantDirection);
				treesPlanted++;
				return true;
			}
		}
		return false;
	}

	public static void initGardener() {

	}

}
