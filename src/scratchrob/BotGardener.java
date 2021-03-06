package scratchrob;

import battlecode.common.*;

public class BotGardener extends Globals {

	public static int treesPlanted = 0;
	public static RobotType[] buildOrder = new RobotType[] {RobotType.SCOUT, RobotType.SOLDIER, RobotType.LUMBERJACK, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SCOUT};
	public static int buildIndex = 0;
	public static Boolean builtGrove = false;
	public static Direction spawnLocation = null;
	
	public static Boolean seenScout = false;
	
	public static RobotInfo[] nearbyBots;
	public static TreeInfo[] nearbyTrees;
	
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
            	System.out.println("Gardener over bytecode limit");
            }
            
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();

        }			
	}
	
	public static void turn() throws GameActionException {
        nearbyBots = rc.senseNearbyRobots();
		nearbyTrees = rc.senseNearbyTrees();
        
		waterTrees();
		
		if (!seenScout) {
			if (seeFriendlyScout()) {
				System.out.println("Spotted a scout");
				seenScout = true;
			} else if (treesPlanted == 1) {
				//try and build a scout
				spawnBot(RobotType.SCOUT);
				return;
			}
		}
		
		if (builtGrove) {
			spawnBots();
		} else {
			buildGrove();
		}
		
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
		

		
		RobotType nextBot = buildOrder[buildIndex];
		
		if (spawnBot(nextBot)) {
			buildIndex++;
			if (buildIndex >= buildOrder.length) {
				buildIndex = 0;
			}
			return true;
		}
		
		return false;
		
	}
	
	public static Boolean spawnBot(RobotType bot) throws GameActionException {

		if (!rc.hasRobotBuildRequirements(bot) || rc.getBuildCooldownTurns() > 0) {
			return false;
		}
		
		if (spawnLocation == null)
		{
			spawnLocation = towardsEnemySpawn().opposite();
		}		
		Boolean canSpawn = true;
		if (!rc.canBuildRobot(bot, spawnLocation)) {
			System.out.println("SpawnLocation blocked - trying to find new location");
			if (rc.senseNearbyRobots(here.add(spawnLocation),1,null).length > 0) {
				System.out.println("Robot in the way. Waiting for it to move"+rc.senseNearbyRobots(here.add(spawnLocation),1,null)[0].ID);
			}
			canSpawn = false;
			//spawnLocation blocked. Find nearest spawnable location.
			for (int i = 100; --i != 0;) { //bytecode efficient loop
				spawnLocation = spawnLocation.rotateRightDegrees(7);
				if (rc.canBuildRobot(bot, spawnLocation))
				{
					canSpawn = true;
					System.out.println("Found new location after"+i+"iterations");
					break;
				}
			}
		}
		
		if (canSpawn) {
			rc.buildRobot(bot, spawnLocation);
			System.out.println("Spawned a new "+bot);
			return true;
		}
		
		return false;
	}

	public static void buildGrove() throws GameActionException {
        TreeInfo nearestTree = findNearestTree();
        if (nearestTree != null && nearestTree.location.distanceTo(here) < 2) {
        	System.out.println("Near a tree so going to start planting");
        	if (!plantTree()) {
        		builtGrove = true;
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
        			builtGrove = true;
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
		
		if (treesPlanted * 10 + 50 > rc.getTeamBullets()) {
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
	
	public static Boolean seeFriendlyScout() {
		for (RobotInfo bot : nearbyBots) {
			if (bot.type == RobotType.SCOUT && bot.team == us) {
				return true;
			}
		}
		return false;
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
