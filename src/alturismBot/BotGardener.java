package alturismBot;

import battlecode.common.*;

public class BotGardener extends Globals {

	public static int treesPlanted = 0;
	//public static RobotType[] buildOrder = new RobotType[] {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.LUMBERJACK, RobotType.SOLDIER, RobotType.SOLDIER};
	//public static int buildIndex = 0;
	public static Boolean builtGrove = false;
	public static Direction spawnLocation = null;
	
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
            	System.out.println("Archon over bytecode limit");
            }
            
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();

        }			
	}
	
	public static void turn() throws GameActionException {
        nearbyBots = rc.senseNearbyRobots();
		nearbyTrees = rc.senseNearbyTrees();
        
		waterTrees();
		
		buildGrove();
		
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

	public static void buildGrove() throws GameActionException {
        TreeInfo nearestTree = findNearestTree();
        if (nearestTree != null && nearestTree.location.distanceTo(here) < 2) {
        	System.out.println("Near a tree so going to start planting");
        	plantTree();
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
        		plantTree();
        	}
        }
	}
	
	public static Boolean plantTree() throws GameActionException {
		System.out.println("Trying to build a new tree. Trees so far:"+treesPlanted);

		if (rc.getTreeCount() < 10) { //Build as fast as possible to some magic number
			return tryPlantTree();		
		}
		
		if (rc.getTeamBullets() > 200) { //Always build if you have excess bullets
			return tryPlantTree();
		}
		
		if (rc.getTeamBullets() > (50 + treesPlanted * 20)) {
			return tryPlantTree();
		}
		
		return false;
	}
	
	public static Boolean tryPlantTree() throws GameActionException {
		return tryPlantTree(towardsEnemySpawn(),5 * (-1 * (treesPlanted + 1)));
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
