package template;

import battlecode.common.*;
import Common.*;
public class BotArchon extends Globals{

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

		}
	
}
