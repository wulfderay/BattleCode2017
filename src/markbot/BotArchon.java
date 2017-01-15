package markbot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import Common.*;

public class BotArchon extends Globals {

		static int gardnersBuilt = 0;
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
			// Generate a random direction
            Direction dir = Util.randomDirection();

            // Randomly attempt to build a gardener in this direction
            if (rc.canHireGardener(dir) && gardnersBuilt < rc.getRoundNum()/100 ) {
                rc.hireGardener(dir);
                gardnersBuilt ++;
            }

            // Move randomly
            Util.tryMove(Util.randomDirection());

            // Broadcast archon's location for other robots on the team to know
            MapLocation myLocation = rc.getLocation();
            rc.broadcast(0,(int)myLocation.x);
            rc.broadcast(1,(int)myLocation.y);
		}
	
}
