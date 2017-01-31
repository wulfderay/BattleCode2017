package astarrynight;

import Common.*;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public class BotGardener extends Globals {


	public static int treesPlanted = 0;
	public static BuildListItem[]  buildOrderEarly = new BuildListItem[] {
			new BuildListItem(RobotType.SCOUT, rc.getInitialArchonLocations(them).length, 0.60f ),
			new BuildListItem(RobotType.LUMBERJACK, 3, 0.60f),
			new BuildListItem(RobotType.TANK, 1, .2f),
			new BuildListItem(RobotType.SOLDIER,5)};
	public static BuildListItem[]  buildOrderMid = new BuildListItem[] {
			new BuildListItem(RobotType.SCOUT, rc.getInitialArchonLocations(them).length *3, 0.5f),
			new BuildListItem(RobotType.LUMBERJACK, 7),
			new BuildListItem(RobotType.SOLDIER,10),
			new BuildListItem(RobotType.TANK,5)};
	public static BuildListItem[]  buildOrderLate = new BuildListItem[] {
			new BuildListItem(RobotType.TANK, 5),
			new BuildListItem(RobotType.LUMBERJACK, 5),
			new BuildListItem(RobotType.SOLDIER,20)};
	public static int buildIndex = 0;
	//public static Boolean builtGrove = false;

	public static boolean spawnedAtleastOneScout = false;

	//Spawn crap
	public static Direction spawnLocation = null;
	public static int numSafeSpawnLocations = 0;
	public static MapLocation[] spawnLocs = new MapLocation[] {null, null, null, null, null, null};
	public static int[] spawnLocStatus = new int[] {0,0,0,0,0,0};
	public static Direction[] spawnDirs = new Direction[] {Direction.NORTH, Direction.NORTH.rotateRightDegrees(60), Direction.NORTH.rotateRightDegrees(120), Direction.SOUTH, Direction.SOUTH.rotateRightDegrees(60), Direction.SOUTH.rotateRightDegrees(120)};
	public static MapLocation spawnLocHere;
	public static boolean foundSpawnLocation = false;

	public static RobotInfo[] nearbyBots;
	public static RobotInfo nearbyFriendlyGardener;
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
	public static void turn() throws GameActionException {

		// Basic initialization stuff
		Util.BuyVPIfItWillMakeUsWin();
		Broadcast.RollCall();
		senseSurroundings();

		// Water some trees
		waterTrees();

		Direction spawnDir = determineSpawnLocation();

		// Spawn stuff
		if ( EnsureEarlyGameBotsAreSpawned() ) {
			spawnBots();
		}

		if (foundSpawnLocation || moveToSpawnLocation()) {
			spawnTrees(spawnDir);
		}


	}
	public static BodyInfo getNearestEntityToAvoid()
	{
		BodyInfo closestEntity = null;

		if (nearbyFriendlyTrees.length > 0 )
			closestEntity = nearbyFriendlyTrees[0];

		if (nearbyNeutralTrees != null && nearbyNeutralTrees.length > 0 && (closestEntity == null ||here.distanceTo(nearbyNeutralTrees[0].getLocation()) - nearbyNeutralTrees[0].getRadius() < here.distanceTo(closestEntity.getLocation()) - closestEntity.getRadius()))
			closestEntity = nearbyNeutralTrees[0];
		if (nearbyFriendlyGardener != null && (closestEntity == null || here.distanceTo(nearbyFriendlyGardener.getLocation()) - 6 < here.distanceTo(closestEntity.getLocation()) - closestEntity.getRadius()))
			closestEntity = nearbyFriendlyGardener;
		return closestEntity;
	}

	public static boolean moveToSpawnLocation() {
		if (nearbyFriendlyTrees.length > 0) {
			//Move to distance 4 from nearest friendly tree... this gives enough space to spawn a tree between
			//TODO: This needs to take into account off-map locations.
			BodyInfo closestEntity = getNearestEntityToAvoid();
			float distanceGross = here.distanceTo(closestEntity.getLocation());
			float difference = closestEntity instanceof RobotInfo? 6: (closestEntity.getRadius() +GameConstants.BULLET_TREE_RADIUS + myType.bodyRadius+ 1);
			float distanceNet =  distanceGross - difference;
			Direction dir = here.directionTo(closestEntity.getLocation());
			System.out.println("Trying to move distance 4 from tree" + closestEntity.getLocation());
			System.out.println("Distance" + distanceGross + " " + distanceNet);
			if (distanceNet < 0) {
				distanceNet = Math.abs(distanceNet);
				dir = dir.opposite();
			}
			if (distanceNet < 0.01) {
				return true;
			}
			if (distanceNet >= myType.strideRadius) {
				if (rc.canMove(dir)) {
					UtilMove.doMove(dir);
				} else {
					BugMove.simpleBug(closestEntity.getLocation());
				}
			} else {
				if (rc.canMove(dir, distanceNet)) {
					return UtilMove.doMove(dir, distanceNet);
				} else {
					BugMove.simpleBug(closestEntity.getLocation());
				}
			}
			return false;
		} else {
			//Find map edge
			for (int i = 6; --i >= 0;) {

			}


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
		if (numSafeSpawnLocations > 2 && spawnDir != null) {
			if (rc.canPlantTree(spawnDir)) {
				try {
					rc.plantTree(spawnDir);
					System.out.println("Found a good spawn location. Staying here!");
					foundSpawnLocation = true;
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
				if (bot.type == RobotType.GARDENER && nearbyFriendlyGardener == null)
					nearbyFriendlyGardener = bot;
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
		TreeInfo[] trees = rc.senseNearbyTrees(loc, 1, null); // 2 because of tanks
		if (trees.length == 0)
		{
			RobotInfo[] bots = rc.senseNearbyRobots(loc, 0.99f, null);
			if (bots.length == 0) {
				try {
					if (rc.onTheMap(loc, 1)) {
						rc.setIndicatorDot(loc, 255, 255, 255);
						return LOCATION_EMPTY;
					} else {
						rc.setIndicatorDot(loc, 0, 0, 0);
						return LOCATION_UNKNOWN;
					}
				} catch (GameActionException e) {
					UtilDebug.debug_exceptionHandler(e, "Sensing spawn location exception");
					rc.setIndicatorDot(loc, 255, 0, 0);
					return LOCATION_UNKNOWN;
				}
			} else {
				rc.setIndicatorDot(loc, 150, 150, 150);
				return LOCATION_ROBOT;
			}
		} else {
			if (trees[0].team == us) {
				rc.setIndicatorDot(loc, 0, 255, 0);
				return LOCATION_TREE;
			} else {
				rc.setIndicatorDot(loc, 255, 0, 0);
				Broadcast.INeedATreeChopped(trees[0].location);
				return LOCATION_UNKNOWN;
			}
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
			/*if (spawnLocStatus[i] == LOCATION_ROBOT) {
				numSafeSpawnLocations++;
			}*/
		}
		return bestLocation;
	}

	public static Direction getAnEmptyDir() {
		for (int i = 6; --i >= 0;) {
			if (spawnLocStatus[i] == LOCATION_EMPTY) {
				return spawnDirs[i];
			}
		}
		return null;
	}


	public static boolean EnsureEarlyGameBotsAreSpawned() throws GameActionException{
		rc.setIndicatorDot(here, 200, 100, 00);

		int numLumberjacks = Broadcast.GetNumberOfSpawned(RobotType.LUMBERJACK);
		int numSoldiers = Broadcast.GetNumberOfLive(RobotType.SOLDIER);
		int numScouts = Broadcast.GetNumberOfSpawned(RobotType.SCOUT);
		int numGardeners = Broadcast.GetNumberOfLive(RobotType.GARDENER);

		int numTrees = 0;
		for (int i = 6; --i >= 0;) {
			if (spawnLocStatus[i] == LOCATION_TREE)
				numTrees++;
		}

		System.out.println("Spawn check L" + numLumberjacks + "s" + numScouts + "S" + numSoldiers + "G" + numGardeners);

		if (enemyAttackUnitsNearby > friendlyAttackUnitsNearby) { //Under attack. Spawn soldiers. NOTE: this may be a bad idea when we are getting overrun.
			System.out.println("Spawn: Under attack, spawning soldier");
			spawnBot(RobotType.SOLDIER);
			return false;
		}

		if (numSoldiers == 0 && numTrees > 1) { //Need soldiers.
			System.out.println("Spawn: Defensive soldier");
			spawnBot(RobotType.SOLDIER);
			return false;
		}

		if (numScouts == 0 && numTrees > 2) {
			System.out.println("Spawn: Go go scout");
			spawnedAtleastOneScout = spawnBot(RobotType.SCOUT);
			return false;
		}

		if (nearbyNeutralTrees != null && nearbyNeutralTrees.length > 1 && numLumberjacks < 1 ) { //Gotta cut down these trees
			spawnBot(RobotType.LUMBERJACK);
			return false;
		}

		if (nearbyNeutralTrees != null && nearbyNeutralTrees.length > 5 && numLumberjacks < 2 ) { //Gotta cut down these trees
			spawnBot(RobotType.LUMBERJACK);
			return false;
		}

		rc.setIndicatorDot(here, 000, 200, 00);

		return true;
	}

	public static Direction refreshSpawnDirection() throws GameActionException {
		//return UtilSpawn.getClearDirection(spawnLocation != null? spawnLocation: Direction.NORTH, 7, 1, false);
		return getAnEmptyDir();
	}

	// to be refactored.
	public static boolean spawnBot(RobotType robotType) throws GameActionException {
		// update spawnlocation if needed.
		spawnLocation = refreshSpawnDirection();

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
			Broadcast.IHaveSpawnedA(robotType);
			System.out.println("Spawned a new "+robotType);
			return true;
		} else {
			//This is a shitty hack but sometimes Util.getClearDirection returns an invalid spawn location so going to try brute forcing it.
			float resolution = 1;
			float cumilativeOffset = resolution;

			while (cumilativeOffset < 360) {
				if (rc.canBuildRobot(robotType, spawnLocation.rotateLeftDegrees(cumilativeOffset))) {
					rc.buildRobot(robotType, spawnLocation);
					Broadcast.IHaveSpawnedA(robotType);
					System.out.println("Spawned a new "+robotType);
					return true;
				}
				cumilativeOffset += resolution;
			}
		}
		return false;
	}

	public static void waterTrees() throws GameActionException {
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
			rc.water(worstTree.ID);
			System.out.println("Watered tree "+worstTree.ID);
		}
	}

	//TODO:  Better early game building.
	//
	//TODO: take into account how many bots we already have/attrition level.

	public static Boolean spawnBots() throws GameActionException {
		RobotType nextBot = getNextBotToBuild();
		if (nextBot == null) // we don't need to build
			return false;
		if (spawnBot(nextBot)) {
			buildIndex++;
			return true;
		}
		return false;
	}

	// Here's where we need to add the genrealized attrition code.
	public static RobotType getNextBotToBuild() throws GameActionException {
		BuildListItem[] buildOrder = Util.isEarlyGame() && rc.getTreeCount() < 10 ? buildOrderEarly: buildOrderMid;
		BuildListItem nextBot = null;
		int offset = 0;
		buildIndex = buildIndex % buildOrder.length;
		while(offset < buildOrder.length)
		{
			BuildListItem botToBuild = buildOrder[(buildIndex + offset) % buildOrder.length];
			if ( Broadcast.GetNumberOfLive(nextBot.type) <botToBuild.max &&
					(botToBuild.maxAttrition == -1 ||Broadcast.GetAttritionRateAllGame(nextBot.type) < botToBuild.maxAttrition)) // we don't need to spawn any more. go on to the next one.
			{
				return botToBuild.type;
			}
			offset++;
		}
		return null;
	}

	public static void initGardener() {

	}

	// we might put flags in it too, if we want to monitor some other eent than number of units.
	public static class BuildListItem
	{
		public BuildListItem( RobotType type, int max, float maxAttrition)
		{
			this.type = type;
			this.max = max;
			this.maxAttrition = maxAttrition;
		}
		public BuildListItem( RobotType type, int max)
		{
			this.type = type;
			this.max = max;
		}
		public static RobotType type;
		public static int max;
		public static float maxAttrition = -1;
	}
}
