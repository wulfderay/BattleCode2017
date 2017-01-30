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
        	maximumFirepowerAtSafeTarget(priorityTarget, enemies);
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
        	BulldozeMove(dir);
        }
        UtilMove.moveToFarTarget(target.location);
        return false;
    }
    
    public static boolean BulldozeMove(Direction dir)
    {
    	int checksPerSide = 90;
    	float degreeOffset = 1f;
        try {
        	Direction testDirection = dir;

            // Now try a bunch of similar angles
            int currentCheck = 1;
            while(currentCheck<=checksPerSide) {
                // Try the offset of the left side
                if( rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck)) ) {
                    rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                    return true;
                }
                // Try the offset on the right side
                if( rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck)) ) {
                	rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                    return true;
                }
                // No move performed, try slightly further
                currentCheck++;
            }
        	
			rc.move(dir);
			
		} catch (GameActionException e) {
			UtilDebug.debug_exceptionHandler(e, "Exception while bulldoze moving");
		}
        return true;
    	
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
    
    
    
    //HACK FIRING CODE:

    public static boolean maximumFirepowerAtSafeTarget(MapLocation loc) {
        if ( safeToFire(here.directionTo(loc)))
            return maximumFirepowerAt(loc);
        return false;
    }
    public static boolean maximumFirepowerAtSafeTarget(RobotInfo currentTarget) {
        if ( safeToFireAtTarget(currentTarget) )
            return maximumFirepowerAt(currentTarget.location);
        System.out.println("Target not safe to shoot at!"+currentTarget.location);
        return false;
    }

    public static boolean maximumFirepowerAtSafeTarget(RobotInfo currentTarget, RobotInfo[] enemies) {
        if ( safeToFireAtTarget(currentTarget) )
            return maximumFirepowerAt(currentTarget.location);
        for( RobotInfo target : enemies )
        {
            if ( safeToFireAtTarget(target)) {
                System.out.println("Picking alternate safe target!");
                return maximumFirepowerAt(target.location);
            }
        }
        System.out.println("Can't find a safe target to shoot at!");
        return false;
    }

    public static boolean safeToFireAtTarget(RobotInfo target)
    {
        return safeToFire(here.directionTo(target.getLocation()));
    }

    public static boolean safeToFire(Direction dir)
    {
        Direction direction = dir;
        //Test the line towards the target:
        MapLocation testLocation;
        float distance = rc.getType().bodyRadius + 0.1f;
        float DISTANCE_INCREMENT = 0.3f; //Chosen by IEEE certified random dice roll
        float max_test_distance = rc.getType().sensorRadius;
        while ( distance < max_test_distance)
        {
            testLocation = here.add(direction, distance);
            try {
                if (rc.isLocationOccupiedByRobot(testLocation)) {
                    RobotInfo bot = rc.senseRobotAtLocation(testLocation);
                    if (bot.team == us)
                        return false;
                    return true;
                }
            } catch (GameActionException e) {
                System.out.println("Exception in safeToFireAtTarget"+e);
            }
            distance += DISTANCE_INCREMENT;
        }
        return true;
    }

    public static boolean maximumFirepowerAt(MapLocation target)
    {
        Direction direction = here.directionTo(target);
        float distance = here.add(direction, rc.getType().bodyRadius).distanceTo(target);
        if ( distance < 4.75f ) //Determined on the back of an official IEEE napkin
        {
            if (rc.canFirePentadShot()) {
                System.out.println("FIRING PENTAD SHOT!");
                try {
                    rc.firePentadShot(here.directionTo(target));
                } catch (GameActionException e) {
                    UtilDebug.debug_exceptionHandler(e, "Exception while firing pentad shot");
                }
                rc.setIndicatorLine(here, target, 50, 0, 0);
                return true;
            }
        }
        if (rc.canFireSingleShot()) {
            System.out.println("Firing single shot!");
            try {
                rc.fireSingleShot(here.directionTo(target));
            } catch (GameActionException e) {
                UtilDebug.debug_exceptionHandler(e, "Exception while firing single shot");
            }
            rc.setIndicatorLine(here, target, 255, 0, 0);
            return true;
        }
        return false;
    }

	
}
