package markbot;

import battlecode.common.*;
import Common.*;
public class BotGardener extends Globals {

    static int scoutsBuilt = 0;
    static int lumberjacksbuilt = 0;
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
                System.out.println("gardener Exception");
                e.printStackTrace();
            }

            //Test that we completed within bytecode limit
            if (rc.getRoundNum() != roundNum) {
            	System.out.println("gardener over bytecode limit");
            }
            
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();

        }			
	}
	
	public static void turn() throws GameActionException {
        // Listen for home archon's location
        //int xPos = rc.readBroadcast(0);
        //int yPos = rc.readBroadcast(1);
        //MapLocation archonLoc = new MapLocation(xPos,yPos);

        // Generate a random direction
        Direction dir = Util.randomDirection();


        if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && lumberjacksbuilt < rc.getRoundNum()/25) {
            rc.buildRobot(RobotType.LUMBERJACK, dir);
            lumberjacksbuilt ++;
        }
        // Randomly attempt to build a soldier or lumberjack in this direction
        if (rc.canBuildRobot(RobotType.SCOUT, dir) && scoutsBuilt < rc.getRoundNum()/19 ) {
            rc.buildRobot(RobotType.SCOUT, dir);
            scoutsBuilt ++;
        }


        // Move randomly
        Util.tryMove(Util.randomDirection());
	}
	
}
