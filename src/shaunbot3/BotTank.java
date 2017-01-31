package shaunbot3;

import Common.*;
import battlecode.common.*;

public class BotTank extends Globals {

	public static RobotInfo[] enemies;
	
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
	
	public static RobotInfo currentTarget = null;
	public static int turnsSinceLastSawCurrentTarget = 0;
	public static final int TURNS_TO_PURSUE_CURRENT_TARGET = 5;
	
    public static void turn() throws GameActionException {
        Util.BuyVPIfItWillMakeUsWin();
        Broadcast.RollCall();

	    //Scan
		enemies = rc.senseNearbyRobots(-1, them);
		PursueAndDestroyPriorityEnemy();
        /*
		//Head towards enemy archon
        RobotInfo target = getPriorityTarget();
        UtilMove.CombatAvoidBullets();
        moveTowards(target);
        
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
        RobotInfo priorityTarget = Util.pickPriorityTarget(enemies);
        if(enemies.length > 0) {
        	maximumFirepowerAtSafeTarget(priorityTarget, enemies);
        }
        */
    }
    

	public static boolean PursueAndDestroyPriorityEnemy() throws GameActionException {
		if(enemies.length == 0) {
			//Some (simple) pursuit code
			if ( currentTarget == null  )
			{
				if ( helpTargetExists && globalTargetExists )
				{
					if ( here.distanceTo(globalTarget) < here.distanceTo(helpTarget) )
						return BulldozeMoveTowardsFarTarget(globalTarget);
					return BulldozeMoveTowardsFarTarget(helpTarget);
				}
				if (globalTargetExists)
				{
					return BulldozeMoveTowardsFarTarget(globalTarget);
				}
				if (helpTargetExists)
				{
					System.out.println("Help target detected!  Time to go help!");
					return BulldozeMoveTowardsFarTarget(helpTarget);
				}
				return BulldozeMoveTowardsFarTarget(globalTarget);
			}
			//Target != null
			turnsSinceLastSawCurrentTarget++;
			if ( turnsSinceLastSawCurrentTarget > TURNS_TO_PURSUE_CURRENT_TARGET)
			{
				currentTarget = null;
			}
			if ( currentTarget != null )
				return BulldozeMoveTowardsNearTarget(currentTarget.location);
			return BulldozeMoveTowardsFarTarget(globalTarget);
		}
		//Enemies.length > 0:
		currentTarget = Util.pickPriorityTarget(enemies);
		turnsSinceLastSawCurrentTarget = 0;
		if (currentTarget == null) {
			return BulldozeMoveTowardsFarTarget(globalTarget);
		}
		switch ( currentTarget.getType() )
		{
		/*
		//YOU CAN'T OUTRUN A LUMBERJACK
		case LUMBERJACK:
			float MIN = GameConstants.LUMBERJACK_STRIKE_RADIUS + RobotType.LUMBERJACK.strideRadius;
			float MAX = MIN + rc.getType().bodyRadius*2f; //Fudge factor (mmm... fudge...)
			UtilMove.maintainDistanceWith(currentTarget, MAX, MIN, currentTarget.location);
			maximumFirepowerAtSafeTarget(currentTarget, enemies);
			break;
			*/
		default:
			BulldozeMoveTowardsNearTarget(currentTarget.location);
			maximumFirepowerAtSafeTarget(currentTarget, enemies);
		}
		return true; // ehh, close enough
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
    
    public static boolean BulldozeMoveTowardsNearTarget(MapLocation location) throws GameActionException
    {
    	Direction dir = here.directionTo(location);
    	// Sense what I'm up against:
        //If it's a tree, MOVE TO IT!  WOO BULLDOZE
        MapLocation oneMoveLocation = here.add(dir, rc.getType().bodyRadius + rc.getType().strideRadius);
        TreeInfo obstacleTree = rc.senseTreeAtLocation(oneMoveLocation);
        if ( obstacleTree != null )
        {
        	return BulldozeMove(dir);
        }
		return UtilMove.moveToNearTarget(location);
    }
    public static boolean BulldozeMoveTowardsFarTarget(MapLocation location) throws GameActionException
    {
    	Direction dir = here.directionTo(location);
    	// Sense what I'm up against:
        //If it's a tree, MOVE TO IT!  WOO BULLDOZE
        MapLocation oneMoveLocation = here.add(dir, rc.getType().bodyRadius + rc.getType().strideRadius);
        TreeInfo obstacleTree = rc.senseTreeAtLocation(oneMoveLocation);
        if ( obstacleTree != null )
        {
        	return BulldozeMove(dir);
        }
		return UtilMove.moveToFarTarget(location);
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
			return true;
		} catch (GameActionException e) {
			UtilDebug.debug_exceptionHandler(e, "Exception while bulldoze moving");
		}
        return false;
    }

    public static RobotInfo getPriorityTarget()
    {
        // See if there are any nearby enemy robots
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
        if(enemies.length > 0) {
            //Find closest enemy:
            return enemies[0];
        }
        // Otherwise, head towards enemy archon location:
        return new RobotInfo(-1, them, myType, globalTarget, 1, 1, 1);
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
                return maximumFirepowerAt(target.location);
            }
            System.out.println("Picking alternate safe target!");
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
