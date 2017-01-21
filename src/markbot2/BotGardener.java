package markbot2;

import Common.Broadcast;
import Common.Globals;
import Common.Util;
import battlecode.common.*;

public class BotGardener extends Globals {

	public static final int GROVE_GRID_SIZE = 3;
	public static final float MAX_GRID_DELTA = 0.001f;

	public static int treesPlanted = 0;
	public static BuildListItem[]  buildOrderEarly = new BuildListItem[] {
			new BuildListItem(RobotType.SCOUT, rc.getInitialArchonLocations(them).length *2),
			new BuildListItem(RobotType.LUMBERJACK, 5),
			new BuildListItem(RobotType.SOLDIER,8)};
	public static BuildListItem[]  buildOrderMid = new BuildListItem[] {
			new BuildListItem(RobotType.SCOUT, rc.getInitialArchonLocations(them).length *3),
			new BuildListItem(RobotType.LUMBERJACK, 7),
			new BuildListItem(RobotType.SOLDIER,10),
			new BuildListItem(RobotType.TANK,5)};
	public static BuildListItem[]  buildOrderLate = new BuildListItem[] {
			new BuildListItem(RobotType.TANK, 5),
			new BuildListItem(RobotType.LUMBERJACK, 5),
			new BuildListItem(RobotType.SOLDIER,20)};
	public static int buildIndex = 0;
	//public static Boolean builtGrove = false;
	public static Direction spawnLocation = null;

	public static boolean spawnedAtleastOneScout = false;

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

	//TODO: If there's enemies nearby, spawn some soldiers , and spawn a tree in the way of bullets.
	public static void turn() throws GameActionException {
		Util.BuyVPIfItWillMakeUsWin();

		Broadcast.RollCall();
		senseSurroundings();

		waterTrees();
		if ( EnsureEarlyGameBotsAreSpawned()) {
			spawnBots();
			//buildGrove();
			plantTree();
		}

		for (TreeInfo tree : nearbyNeutralTrees)
		{
			if (Clock.getBytecodesLeft() >100)
			{
				Broadcast.INeedATreeChopped(tree.getLocation());
			}
		}
	}

	public static void senseSurroundings() throws GameActionException {
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

	public static boolean EnsureEarlyGameBotsAreSpawned() throws GameActionException{
		rc.setIndicatorDot(here, 200, 100, 00);

		int numLumberjacks = Broadcast.GetNumberOfRobots(RobotType.LUMBERJACK);
		int numSoldiers = Broadcast.GetNumberOfRobots(RobotType.SOLDIER);
		int numScouts = Broadcast.GetNumberOfRobots(RobotType.SCOUT);
		int numGardeners = Broadcast.GetNumberOfRobots(RobotType.GARDENER);

		if (enemyAttackUnitsNearby > friendlyAttackUnitsNearby) { //Under attack. Spawn soldiers. NOTE: this may be a bad idea when we are getting overrun.
			System.out.println("Spawn: Under attack, spawning soldier");
			spawnBot(RobotType.SOLDIER);
			return false;
		}

		if (numScouts < 1) {
			System.out.println("Spawn: Go go scout");
			spawnedAtleastOneScout = spawnBot(RobotType.SCOUT);
			return false;
		}

		if (rc.getTreeCount() < numGardeners * 2)
		{
			plantTree();
		}

		if (numScouts < 1) {
			System.out.println("Spawn: Go go scout");
			spawnedAtleastOneScout = spawnBot(RobotType.SCOUT);
			return false;
		}
		if (nearbyNeutralTrees != null && nearbyNeutralTrees.length > 1 && numLumberjacks < 2 ) { //Gotta cut down these trees
			spawnBot(RobotType.LUMBERJACK);
			return false;
		}

		rc.setIndicatorDot(here, 000, 200, 00);
		if (numSoldiers == 0) { //Need soldiers.
			System.out.println("Spawn: Defensive soldier");
			spawnBot(RobotType.SOLDIER);
			return false;
		}

		if (numLumberjacks < 2) { //Gotta cut down these trees
			spawnBot(RobotType.LUMBERJACK);
			return false;
		}

		return true;
	}

	public static Direction refreshSpawnDirection() throws GameActionException {
		return Util.getClearDirection(spawnLocation != null? spawnLocation: Direction.NORTH, 7, 1, false);
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
		rc.setIndicatorDot(here, 100, 00, 100);
		spawnLocation = Util.getClearDirection(Direction.NORTH, 15, 1, false);
		rc.setIndicatorDot(here.add(spawnLocation, 1), 0, 50, 50);
		if (spawnLocation == null)
		{
			rc.setIndicatorDot(here, 250, 50, 50);
			System.out.println("I'm Fucking Stuck! WTF?!");
			Broadcast.IamAStuckGardener();
			return false;
		}
		rc.setIndicatorDot(here.add(spawnLocation, 1), 50,50,50);
		RobotType nextBot = getNextBotToBuild();

		if (nextBot == null) // we don't need to build
			return null;
		if (!rc.hasRobotBuildRequirements(nextBot)) {
			return false;
		}

		if (rc.canBuildRobot(nextBot, spawnLocation)) {
			rc.buildRobot(nextBot, spawnLocation);
			System.out.println("Spawned a new "+nextBot);
			buildIndex++;
			return true;
		}

		return false;
	}

	public static RobotType getNextBotToBuild() throws GameActionException {
		BuildListItem[] buildOrder = Util.isEarlyGame() && rc.getTreeCount() < 10? buildOrderEarly: buildOrderMid;
		BuildListItem nextBot = null;
		int offset = 0;
		buildIndex = buildIndex % buildOrder.length;
		while(offset < buildOrder.length)
		{

			if ( Broadcast.GetNumberOfRobots(nextBot.type) <buildOrder[(buildIndex + offset) % buildOrder.length].max) // we don't need to spawn any more. go on to the next one.
			{

				return buildOrder[(buildIndex + offset) % buildOrder.length].type;
			}
			offset++;
		}
		return null;
	}

	public static boolean plantTreeOnGrid() throws  GameActionException {
		// look for a location starting from top left
		// location should be on a grid of mapLocation.x,y % GROVE_GRID_SIZE == 0
		MapLocation loc_offset = fitToGroveGrid(here);

		for (int i = -GROVE_GRID_SIZE;i < GROVE_GRID_SIZE; i+=GROVE_GRID_SIZE)
		{
			for (int j = -GROVE_GRID_SIZE;j < GROVE_GRID_SIZE; j+=GROVE_GRID_SIZE) {
				MapLocation whereIwantToPlant = loc_offset.translate(i,j);
				if (rc.isCircleOccupiedExceptByThisRobot(whereIwantToPlant,1) || !rc.canPlantTree(here.directionTo(whereIwantToPlant)))
				{
					continue;
				}
				rc.setIndicatorDot(whereIwantToPlant, 100, 255, 30);
				float distance = here.distanceTo(whereIwantToPlant) + 0.5f;
				if (Math.abs(distance - myType.bodyRadius) < MAX_GRID_DELTA ) {
					rc.plantTree(here.directionTo(whereIwantToPlant));
					treesPlanted++;
					return true;
				}
				else if (!rc.hasMoved())
				{
					if (!rc.canMove(here.directionTo(whereIwantToPlant).opposite(), distance - myType.bodyRadius))
						continue;
					rc.move(here.directionTo(whereIwantToPlant).opposite(), distance - myType.bodyRadius);
					rc.plantTree(here.directionTo(whereIwantToPlant));
					treesPlanted++;
					return true;
				}
			}
		}

		return false;
	}

	private static MapLocation fitToGroveGrid(MapLocation loc) {
		return loc.translate(GROVE_GRID_SIZE - (loc.x % GROVE_GRID_SIZE), GROVE_GRID_SIZE -(loc.y % GROVE_GRID_SIZE));
	}

	public static Boolean plantTree() throws GameActionException {

		if (spawnLocation == null)
			spawnLocation = refreshSpawnDirection();
		System.out.println("Trying to build a new tree. Trees so far:"+treesPlanted);
		Direction plantDirection = Util.getClearDirection(spawnLocation != null? spawnLocation:Direction.NORTH, 15, 1f, false, true);
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

	public static Direction towardsEnemySpawn() {
		return here.directionTo(rc.getInitialArchonLocations(them)[0]);
	}

	public static void initGardener() {

	}

	// we might put flags in it too, if we want to monitor some other eent than number of units.
	public static class BuildListItem
	{
		public BuildListItem( RobotType type, int max)
		{
			this.type = type;
			this.max = max;
		}
		public static RobotType type;
		public static int max;
	}
}
