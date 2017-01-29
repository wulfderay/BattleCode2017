package shaunbot3;

import Common.*;
import battlecode.common.*;

public class BotTank extends Globals {

	public static void loop() throws GameActionException {
        System.out.println("I'm a tank!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

            	//Update common data
            	turnUpdate();
            		            	
                //Do some stuff
            	turn();

            } catch (Exception e) {
                UtilDebug.debug_exceptionHandler(e,"Tank Exception");
            }

            //Test that we completed within bytecode limit
            if (rc.getRoundNum() != roundNum) {
                UtilDebug.alert();
            	System.out.println("Tank's over bytecode limit");
            }
            
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();

        }			
	}

    public static void turn() throws GameActionException {
        Util.BuyVPIfItWillMakeUsWin();
        Broadcast.RollCall();

	    //Scan
        //Head towards enemy archon
        RobotInfo target = getPriorityTarget();
        moveTowards(target);
        
        //Alright, we'll just fire one bullet... i guess...
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
        RobotInfo priorityTarget = Util.pickPriorityTarget(enemies);
        if(enemies.length > 0) {
        	UtilAttack.maximumFirepowerAtSafeTarget(priorityTarget, enemies);
        }
    }

    private static boolean moveTowards(RobotInfo target) throws GameActionException {
        MapLocation location = target.getLocation();
        Direction dir = here.directionTo(location);

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Sense what I'm up against:
        //If it's a tree, MOVE TO IT!  WOO BULLDOZE
        MapLocation oneMoveLocation = here.add(dir, rc.getType().bodyRadius + rc.getType().strideRadius);
        TreeInfo obstacleTree = rc.senseTreeAtLocation(oneMoveLocation);
        if ( obstacleTree != null )
        {
            rc.move(dir);
            return true;
        }
        UtilMove.moveToFarTarget(target.location);
        return false;
    }

    public static RobotInfo getPriorityTarget()
    {
        // See if there are any nearby enemy robots
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
        if(enemies.length > 0) {
            //Find closest enemy:
            return getClosestRobot(enemies);
        }
        // Otherwise, head towards enemy archon location:
        return new RobotInfo(-1, them, myType, globalTarget, 1, 1, 1);

    }

    public static RobotInfo getClosestRobot(RobotInfo[] robots)
    {
        if ( robots.length == 0 )
            return null;
        RobotInfo closestRobot = null;
        float closestRobotDistance = 1200; //Maps are max 100, so that should be safe maybe?
        float robotDistance;
        for(RobotInfo robot : robots)
        {
            robotDistance = robot.location.distanceTo(here);
            if ( robotDistance < closestRobotDistance )
            {
                closestRobot = robot;
                closestRobotDistance = robotDistance;
            }
        }
        return closestRobot;
    }
	
}
