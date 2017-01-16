package AStarryJan15;

import battlecode.common.*;

public class BotGardener extends Globals {

	public static int treesPlanted = 0;
	public static RobotType[] buildOrder = new RobotType[] {RobotType.SCOUT, RobotType.LUMBERJACK, RobotType.SOLDIER,RobotType.SCOUT,RobotType.SOLDIER};
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
	
	public static void turn() throws GameActionException {
        if ( rc.getTeamBullets() > 10000 - rc.getTeamVictoryPoints()*10)
		{
			rc.donate(rc.getTeamBullets());
		}

		nearbyBots = rc.senseNearbyRobots();
		nearbyTrees = rc.senseNearbyTrees();
        
		waterTrees();

		switch (state)
		{
			case BUILD_INITIAL_LUMBERJACK:
				SpawnLumberJack();
				break;
			case BUILD_GROVE:
				buildGrove();
				break;
			default:
				spawnBots();
				break;
		}
		
	}

	private static void SpawnLumberJack() throws GameActionException {
		spawnBot(RobotType.LUMBERJACK);
		state = GardenerStates.BUILD_GROVE;
	}

	// to be refactored.
	public static boolean spawnBot(RobotType robotType) throws GameActionException {
		if (spawnLocation == null)
		{
			spawnLocation = towardsEnemySpawn().opposite();
		}

		RobotType nextBot = RobotType.LUMBERJACK;
		if (!rc.hasRobotBuildRequirements(nextBot) || rc.getBuildCooldownTurns() > 0) {
			return false;
		}

		Boolean canSpawn = true;
		if (!rc.canBuildRobot(nextBot, spawnLocation)) {
			System.out.println("SpawnLocation blocked - trying to find new location");
			if (rc.senseNearbyRobots(here.add(spawnLocation),1,null).length > 0) {
				System.out.println("Robot in the way. Waiting for it to move"+rc.senseNearbyRobots(here.add(spawnLocation),1,null)[0].ID);
			}
			canSpawn = false;
			//spawnLocation blocked. Find nearest spawnable location.
			for (int i = 100; --i != 0;) { //bytecode efficient loop
				spawnLocation = spawnLocation.rotateRightDegrees(7);
				if (rc.canBuildRobot(nextBot, spawnLocation))
				{
					canSpawn = true;
					System.out.println("Found new location after"+i+"iterations");
					break;
				}
			}
		}

		if (canSpawn) {
			rc.buildRobot(nextBot, spawnLocation);
			System.out.println("Spawned a new "+nextBot);
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
	
	public static Boolean spawnBots() throws GameActionException {
		if (rc.getTeamBullets() < 200) {
			return false;
		}
		
		if (spawnLocation == null)
		{
			spawnLocation = towardsEnemySpawn().opposite();
		}
		
		RobotType nextBot = buildOrder[buildIndex];
		if (!rc.hasRobotBuildRequirements(nextBot) || rc.getBuildCooldownTurns() > 0) {
			return false;
		}
		
		Boolean canSpawn = true;
		if (!rc.canBuildRobot(nextBot, spawnLocation)) {
			System.out.println("SpawnLocation blocked - trying to find new location");
			if (rc.senseNearbyRobots(here.add(spawnLocation),1,null).length > 0) {
				System.out.println("Robot in the way. Waiting for it to move"+rc.senseNearbyRobots(here.add(spawnLocation),1,null)[0].ID);
			}
			canSpawn = false;
			//spawnLocation blocked. Find nearest spawnable location.
			for (int i = 100; --i != 0;) { //bytecode efficient loop
				spawnLocation = spawnLocation.rotateRightDegrees(7);
				if (rc.canBuildRobot(nextBot, spawnLocation))
				{
					canSpawn = true;
					System.out.println("Found new location after"+i+"iterations");
					break;
				}
			}
		}
		
		if (canSpawn) {
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
        TreeInfo nearestTree = findNearestTree();
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
	
	public static Boolean plantTree() throws GameActionException {
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
				float d = here.distanceTo(bot.location);
				if (d < distance) {
					distance = d;
					closest = bot;
				}
			}
		}
		return closest;
	}
	
	public static TreeInfo findNearestTree() {
		float distance = 999f;
		TreeInfo closest = null;
		for (TreeInfo tree : nearbyTrees) {
			float d = here.distanceTo(tree.location);
			if (d < distance) {
				distance = d;
				closest = tree;
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
