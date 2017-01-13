package shaunbot;

import battlecode.common.*;

public class BotSoldier extends Globals {

	public static void loop() throws GameActionException {
        System.out.println("I'm a soldier!");

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
        // See if there are any nearby enemy robots
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);

        // If there is an enemy, move towards it
        if(enemies.length > 0) {
            MapLocation location = enemies[0].getLocation();
            Direction toDir = here.directionTo(location);
            Util.tryMove(toDir);
        } else {
        	// Move Randomly
        	Util.tryMove(Util.randomDirection());
        }

        // If there are enemies, shoot them...
        if (enemies.length > 0) {
            //Alright, we'll just fire one bullet... i guess...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(enemies[0].location));
            }
        }
        
	}
}
