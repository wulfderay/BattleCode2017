package astarrynight;

import Common.Broadcast;
import Common.Globals;
import Common.Util;
import battlecode.common.*;

public class BotGardener extends Globals {

	public static final int GROVE_GRID_SIZE = 3;
	public static final float MAX_GRID_DELTA = 0.001f;

	public static int treesPlanted = 0;
	public static RobotType[] buildOrderEarly = new RobotType[] {RobotType.SCOUT, RobotType.LUMBERJACK, RobotType.SOLDIER,RobotType.SCOUT};
	public static RobotType[] buildOrderMid = new RobotType[] { RobotType.SOLDIER,RobotType.LUMBERJACK,RobotType.SCOUT, RobotType.LUMBERJACK, RobotType.SOLDIER};
	public static RobotType[] buildOrderLate = new RobotType[] { RobotType.SOLDIER,RobotType.TANK,RobotType.SOLDIER};
	public static int buildIndex = 0;
	//public static Boolean builtGrove = false;
	public static Direction spawnLocation = null;
	
	public static RobotInfo[] nearbyBots;
	public static TreeInfo[] nearbyTrees;

	public enum GardenerStates {
		BUILD_INITIAL_LUMBERJACK,
		BUILD_GROVE,
		SPAWN_BOTS
	}
	public static GardenerStates state = GardenerStates.BUILD_INITIAL_LUMBERJACK;
	
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
        if ( rc.getTeamBullets() > 10000 - rc.getTeamVictoryPoints()*10)
		{
			rc.donate(rc.getTeamBullets());
		}

		Broadcast.RollCall();

		nearbyBots = rc.senseNearbyRobots();
		nearbyTrees = rc.senseNearbyTrees(-1, us);
        
		waterTrees();
		if (rc.senseNearbyRobots(myType.sensorRadius, them).length > 0)
		{
			// alert for help
			// spawn a soldier
			spawnBot(RobotType.SOLDIER);
		}
		if ( EnsureEarlyGameEntitiesAreSpawned())
			spawnBots();


		buildGrove();
	}

	public static boolean EnsureEarlyGameEntitiesAreSpawned() throws GameActionException{
		if (Broadcast.GetNumberOfRobots(RobotType.SCOUT) < 1) {
			spawnBot(RobotType.SCOUT);
			return false;
		}
		if (Broadcast.GetNumberOfRobots(RobotType.SOLDIER) < 1)
		{
			spawnBot(RobotType.SOLDIER);
			return false;
		}
		if (Broadcast.GetNumberOfRobots(RobotType.LUMBERJACK) < 2)
		{
			return spawnBot(RobotType.LUMBERJACK);
		}
		return true;
	}

	// to be refactored.
	public static boolean spawnBot(RobotType robotType) throws GameActionException {
		spawnLocation = Util.getClearDirection(towardsEnemySpawn().opposite(), 7, robotType.bodyRadius, false);
		if (spawnLocation != null) rc.setIndicatorDot(here.add(spawnLocation, 1), 50,50,50);
		if (spawnLocation == null)
		{
			rc.setIndicatorDot(here.add(spawnLocation, 1), 250,50,50);
			System.out.println("I'm Fucking Stuck! WTF?!");
			// we can't even move because we are stuck.
		}
		if (!rc.hasRobotBuildRequirements(robotType)) {
			return false;
		}

		if (rc.canBuildRobot(robotType, spawnLocation)) {
			rc.buildRobot(robotType, spawnLocation);
			System.out.println("Spawned a new "+robotType);
			return true;
		}
		return false;
	}

	public static void waterTrees() throws GameActionException {
		float maxDamage = 0f;
		TreeInfo worstTree = null;
		for (TreeInfo tree : nearbyTrees) {
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

		spawnLocation = Util.getClearDirection(towardsEnemySpawn().opposite(), 7, 1, false);
		if (spawnLocation != null) rc.setIndicatorDot(here.add(spawnLocation, 1), 50,50,50);
		if (spawnLocation == null)
		{
			rc.setIndicatorDot(here.add(spawnLocation, 1), 250,50,50);
			System.out.println("I'm Fucking Stuck! WTF?!");
			// we can't even move because we are stuck.
		}
		RobotType[] buildOrder = Util.isEarlyGame() && rc.getTreeCount() < 10? buildOrderEarly: buildOrderMid;
		RobotType nextBot = buildOrder[buildIndex % buildOrder.length];
		if (!rc.hasRobotBuildRequirements(nextBot)) {
			return false;
		}
		
		if (rc.canBuildRobot(nextBot, spawnLocation)) {
			rc.buildRobot(nextBot, spawnLocation);
			System.out.println("Spawned a new "+nextBot);
			buildIndex++;
			if (buildIndex >= buildOrder.length) {
				buildIndex = 0;
			}
			return true;
		}
		
		return false;
	}

	public static void buildGrove() throws GameActionException {

        TreeInfo nearestTree = nearbyTrees != null && nearbyTrees.length > 0? nearbyTrees[0] : null;
        if (nearestTree != null && nearestTree.location.distanceTo(here) < 2) {
        	System.out.println("Near a tree so going to start planting");
        	if (!plantTree()) {
        		state = GardenerStates.SPAWN_BOTS;
        	}
        } else {
        	RobotInfo nearestArchon = findNearestFriendlyArchon();
        	if (nearestArchon != null && nearestArchon.location.distanceTo(here) < 3) {
        		System.out.println("Need to move away from Archon");
        		if (!Util.tryMove(nearestArchon.location.directionTo(here), 5, 10)) {
        			System.out.println("Couldn't so building anyways");
        			plantTree();
        		}
        	} else {
        		System.out.println("Far enough away from Archons to start building");
        		if (!plantTree()) {
					state = GardenerStates.SPAWN_BOTS;
        		}
        	}
        }
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
		//if (rc.getID() % 2 == 0)
		//	return plantTreeLegacy();
		System.out.println("Trying to build a new tree. Trees so far:"+treesPlanted);
		Direction plantDirection = Util.getClearDirection(towardsEnemySpawn(), 30, 1f, false);
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


	public static Boolean plantTreeLegacy() throws GameActionException {
		System.out.println("Trying to build a new tree. Trees so far:"+treesPlanted);
		switch (treesPlanted) {
			case 0:
				return tryPlantTree(towardsEnemySpawn(), 5);
			case 1:
				return tryPlantTree(towardsEnemySpawn().rotateRightDegrees(60), 5);
			case 2:
				return tryPlantTree(towardsEnemySpawn().rotateLeftDegrees(60), -5);
			case 3:
				return tryPlantTree(towardsEnemySpawn().rotateRightDegrees(60), 5);
			case 4:
				return tryPlantTree(towardsEnemySpawn().rotateLeftDegrees(60), -5);
			default:
				return tryPlantTree(Direction.getNorth(), 10);
		}
	}

	public static Boolean tryPlantTree(Direction dir, float offset) throws GameActionException {
		if (!rc.hasTreeBuildRequirements() || rc.getBuildCooldownTurns() > 0) {
			return true;
		}

		if (rc.canPlantTree(dir)) {
			rc.plantTree(dir);
			treesPlanted++;
			System.out.println("Horray! Planted a tree!"+treesPlanted);
			return true;
		}

		float cumilativeOffset = offset;

		System.out.println("Something in the way of Direction"+dir);

		while (cumilativeOffset < 360 && cumilativeOffset > -360) {
			if (rc.canPlantTree(dir.rotateRightDegrees(cumilativeOffset))) {
				rc.plantTree(dir.rotateRightDegrees(cumilativeOffset));
				treesPlanted++;
				System.out.println("Horay! Was able to plant tree at offset"+cumilativeOffset+" tree #"+treesPlanted);
				return true;
			}
			cumilativeOffset += offset;
		}

		System.out.println("Couldn't plant a tree... :("+treesPlanted);
		return false;
	}
	
	public static RobotInfo findNearestFriendlyArchon() {
		float distance = 999f;
		RobotInfo closest = null;
		for (RobotInfo bot : nearbyBots) {
			if (bot.type == RobotType.ARCHON && bot.team == us) {
				return bot;
			}
		}
		return closest;
	}
	
	public static Direction towardsEnemySpawn() {
		return here.directionTo(rc.getInitialArchonLocations(them)[0]);
	}
	
	public static void initGardener() {
		
	}
	
}
