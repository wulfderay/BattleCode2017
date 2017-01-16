package shaunbot;

import battlecode.common.*;
import Common.*;

public class BotLumberjack extends Globals {

	public static void loop() throws GameActionException {
        System.out.println("I'm a "+rc.getType().toString());

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

            	//Update common data
            	turnUpdate();
            		            	
                //Do some stuff
            	turn();

            } catch (Exception e) {
                System.out.println(rc.getType().toString()+" Exception");
                e.printStackTrace();
            }

            //Test that we completed within bytecode limit
            if (rc.getRoundNum() != roundNum) {
            	System.out.println(rc.getType().toString()+" over bytecode limit");
            }
            
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();

        }			
	}
	
	public static void turn() throws GameActionException {
        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, them);

        if(robots.length > 0 && !rc.hasAttacked()) {
            // Use strike() to hit all nearby robots!
            rc.strike();
        } else {
            // No close robots, so search for robots within sight radius
            robots = rc.senseNearbyRobots(-1,them);

            // If there is a robot, move towards it
            if(robots.length > 0) {
                MapLocation enemyLocation = robots[0].getLocation();
                Direction toEnemy = here.directionTo(enemyLocation);

                Util.tryMove(toEnemy);
            } else {
            	
            	chopTrees();
            	
                // Move Randomly
            	Util.tryMove(Util.randomDirection());
            	
            	
            }
        }
	}
	
	public static void chopTrees() throws GameActionException
	{
        chopTrees(them);
        chopTrees(Team.NEUTRAL);
	}
	public static void chopTrees(Team team) throws GameActionException
	{
		TreeInfo[] trees = rc.senseNearbyTrees(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, team);
		if(trees.length > 0 && !rc.hasAttacked()) {
            // Use strike() to hit all nearby robots!
			rc.chop(trees[0].ID);
		} else {
            // No close robots, so search for robots within sight radius
			trees = rc.senseNearbyTrees(-1,team);

            // If there is a robot, move towards it
            if(trees.length > 0) {
                MapLocation enemyLocation = trees[0].getLocation();
                Direction toEnemy = here.directionTo(enemyLocation);
                Util.tryMove(toEnemy);
            } else {
            	// Move Randomly
            	Util.tryMove(Util.randomDirection());
            }
        }
	}
}
