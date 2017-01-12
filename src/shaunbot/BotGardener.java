package shaunbot;

import battlecode.common.*;

public class BotGardener extends Globals {

	public static void loop() throws GameActionException {
        System.out.println("I'm a gardener!");

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
        // Listen for home archon's location
        int xPos = rc.readBroadcast(0);
        int yPos = rc.readBroadcast(1);
        MapLocation archonLoc = new MapLocation(xPos,yPos);

        // Generate a random direction
        Direction dir = Util.randomDirection();

        // Randomly attempt to build a soldier or lumberjack in this direction
        if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
            rc.buildRobot(RobotType.SOLDIER, dir);
        } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
            rc.buildRobot(RobotType.LUMBERJACK, dir);
        } else if (rc.canPlantTree(dir) && Math.random() < .02 && rc.isBuildReady()) {
            rc.plantTree(dir);
        }
        
        //Trees!
        TreeInfo[] trees = rc.senseNearbyTrees(here, RobotType.GARDENER.sensorRadius, us);
        for(TreeInfo tree : trees)
        {
        	if ( !rc.canWater() )
        		break;
        	if ( rc.canShake(tree.ID))
        		rc.shake(tree.ID);
        	if ( tree.team == them)
        		continue;
        	if ( rc.canWater(tree.ID))
        		rc.water(tree.ID);
        	
        }
        
        // If there is a tree, move towards it
        if(trees.length > 0) {
            MapLocation treeLocation = trees[0].getLocation();
            Direction toTree = here.directionTo(treeLocation);
            Util.tryMove(toTree);
        } else {
        	// Move Randomly
        	Util.tryMove(Util.randomDirection());
        }
	}
	
}
