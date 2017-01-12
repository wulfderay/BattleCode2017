package markbot;

import battlecode.common.*;

public class BotGardener extends Globals {

    static boolean hasBuiltScout = false;
	public static void loop() throws GameActionException {
        System.out.println("I'm an archon!");

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

        if (hasBuiltScout)
            rc.disintegrate();
        // Randomly attempt to build a soldier or lumberjack in this direction
        if (rc.canBuildRobot(RobotType.SCOUT, dir) && !hasBuiltScout) {
            rc.buildRobot(RobotType.SCOUT, dir);
            hasBuiltScout = true;
        }

        // Move randomly
        Util.tryMove(Util.randomDirection());
	}
	
}
