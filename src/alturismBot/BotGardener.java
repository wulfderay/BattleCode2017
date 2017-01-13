package alturismBot;

import battlecode.common.*;

public class BotGardener extends Globals {

	public static MapLocation archonLoc = null;
	public static MapLocation nextTreeLoc = null;
	
	public static void loop() throws GameActionException {
        System.out.println("I'm a gardener!");

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
		if (here.isWithinDistance(nextTreeLoc, 0.1f)) {
			//buildTree();
		} else {
			//moveToLocation(nextTreeLoc);
		}
		
	}
	
	public static void initGardener() {
		RobotInfo friends[] = rc.senseNearbyRobots(-1, us);
		for (RobotInfo bot : friends) {
			if (bot.type == RobotType.ARCHON) {
				archonLoc = bot.location;
				break;
			}
		}
		
		Direction dir = Util.randomDirection();
		nextTreeLoc = archonLoc.add(dir, 5);
		
		return;
	}
	
	
	
}
