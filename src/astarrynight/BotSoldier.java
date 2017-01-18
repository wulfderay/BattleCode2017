package astarrynight;

import Common.Broadcast;
import com.sun.jdi.Location;

import Common.Globals;
import Common.Util;
import battlecode.common.*;

public class BotSoldier extends Globals {
	
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
    
    
    public static RobotInfo currentTarget = null;
    public static int turnsSinceLastSawCurrentTarget = 0;
    public static final int TURNS_TO_PURSUE_CURRENT_TARGET = 5;
    
    public static void turn() throws GameActionException {

		Broadcast.RollCall();

    	//Enemy list:
    	RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
    	MapLocation target = getPriorityTarget();
    	if(enemies.length == 0) {
    		//Some (simple) pursuit code
    		if ( currentTarget == null  )
    		{
    			Util.moveToFarTarget(target);    			
    		} else { 
    			moveToNearTarget(currentTarget.location);
    			turnsSinceLastSawCurrentTarget++;
    			if ( turnsSinceLastSawCurrentTarget > TURNS_TO_PURSUE_CURRENT_TARGET)
    			{
    				currentTarget = null;
    			}
    		}
        } else {
        	turnsSinceLastSawCurrentTarget = 0;
        	currentTarget = pickPriorityTarget(enemies);
        	moveToNearTarget(currentTarget.location);
        	
        	currentTarget = pickPriorityTarget(enemies);
        	maximumFirepowerAtSafeTarget(currentTarget, enemies);
        	
	        //Kill trees:
	        Direction dir = here.directionTo(target);
	        MapLocation oneMoveLocation = here.add(dir, rc.getType().bodyRadius + rc.getType().strideRadius);
	        TreeInfo obstacleTree = rc.senseTreeAtLocation(oneMoveLocation);
	        if ( obstacleTree != null )
	        {
	        	maximumFirepowerAt(obstacleTree.location);
	        }
        }
    }
    
    private static boolean maximumFirepowerAtSafeTarget(RobotInfo currentTarget, RobotInfo[] enemies) throws GameActionException {
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
    	Direction direction = here.directionTo(target.location);
    	//Test the line towards the target:
    	MapLocation testLocation;
    	int sensedTeammates;
    	float distance = rc.getType().bodyRadius + 0.1f;
    	float DISTANCE_INCREMENT = 0.3f; //Chosen by IEEE certified random dice roll
    	float senseRadius = 0.01f;
    	float max_test_distance = rc.getType().sensorRadius;
    	while ( distance < max_test_distance)
    	{
    		testLocation = here.add(direction, distance);
    		sensedTeammates = rc.senseNearbyRobots(testLocation, senseRadius, us).length;
    		if ( sensedTeammates > 0 )
    			return false;
    		distance += DISTANCE_INCREMENT;
    	}
    	return true;
    }

	public static boolean maximumFirepowerAt(MapLocation target) throws GameActionException
    {
    	Direction direction = here.directionTo(target);
    	float distance = here.add(direction, rc.getType().bodyRadius).distanceTo(target);
    	if ( distance < 1.75f ) //Determined on the back of an official IEEE napkin 
    	{
    		if (rc.canFirePentadShot()) {
    			System.out.println("FIRING PENTAD SHOT!");
	            rc.firePentadShot(here.directionTo(target));
	            rc.setIndicatorLine(here, target, 100, 0, 0);
	            return true;
    		}
    	}
    	if ( distance < 2.5f )
    	{
    		if (rc.canFireTriadShot()) {
    			System.out.println("FIRING TRIAD SHOT!");
	            rc.fireTriadShot(here.directionTo(target));
	            rc.setIndicatorLine(here, target, 170, 0, 0);
	            return true;
    		}
    	}
    	
		if (rc.canFireSingleShot()) {
			System.out.println("Firing single shot!");
            rc.fireSingleShot(here.directionTo(target));
            rc.setIndicatorLine(here, target, 255, 0, 0);
            return true;
		}
        return false;
    }
    
    //This is a really bad picker:
    public static RobotInfo pickPriorityTarget(RobotInfo[] enemies)
    {
    	RobotInfo currentTarget = enemies[0];
    	if ( enemies[0].getType() == RobotType.ARCHON && enemies.length > 1)
    		return enemies[1];
    	return currentTarget;
    }
    

    public static boolean moveToNearTarget(MapLocation target) throws GameActionException
    {
    	Util.tryMove(here.directionTo(target),10,10);
    	//moveFurthestDistancePossibleTowards(target);//simpleSlide(target);
    	return true;
    }

    
    public static boolean moveFurthestDistancePossibleTowards(MapLocation target)
    {
    	float STEP_DISTANCE = 0.1f;
    	float distance;
    	float furthestDistance = 0f;
    	
    	float currentDegree = 0f;
    	float DEGREE_INCREMENT = 1f;
    	float degreesToSpan = 120f; //Total degrees in front of us (centered at target)
    	
    	
    	
    	return false;
    }
    
    
    public static boolean whiskerSlide(MapLocation target) throws GameActionException
    {
    	System.out.println("Trying to slide move to "+target);
		Direction dirToTarget = here.directionTo(target);
		float rotated = 0f;
		float STEP_DISTANCE = 0.1f;
		///TODO: Make "whisker" sensors and use them to "steer"
		return true;
    }
    
    public static boolean simpleSlide(MapLocation target) throws GameActionException
    {
		System.out.println("Trying to slide move to "+target);
		Direction dirToTarget = here.directionTo(target);
		float rotated = 0f;
		if (rc.canMove(dirToTarget))
		{
			rc.move(dirToTarget);
			return true;
		} else {
			//First (maybe) move partially forwards???
			//What am I running into?
			SenseResult closestObstacle = senseTowards(dirToTarget);
			
			
			
			
			return false;
		}
    }
        
    //SENSE CODE: - UNUSED - 
    public static SenseResult senseTowards(Direction dirToTarget) throws GameActionException
    {
    	float senseDistance = rc.getType().strideRadius;
		MapLocation center = here.add(dirToTarget, rc.getType().bodyRadius);
		MapLocation left = here.add(dirToTarget.rotateLeftDegrees(90), rc.getType().bodyRadius);
		MapLocation right = here.add(dirToTarget.rotateRightDegrees(90), rc.getType().bodyRadius);
		SenseResult obstacleCenter = senseObstacle( center, dirToTarget, senseDistance);
		SenseResult obstacleLeft = senseObstacle( left, dirToTarget, senseDistance);
		SenseResult obstacleRight = senseObstacle( right, dirToTarget, senseDistance);
		SenseResult[] results = new SenseResult[]{obstacleCenter, obstacleLeft, obstacleRight};
		SenseResult closestResult = results[0];
		for(SenseResult result : results)
		{
			if ( closestResult.distance < result.distance )
				closestResult = result;
		}
		return closestResult;
    }
    
    private static final float SENSE_OBSTACLE_INCREMENT = 0.1f; //Can increase
    public static class SenseResult
    {
    	public MapLocation center;
    	public float radius;
    	public RobotInfo robotInfo;
    	public TreeInfo treeInfo;
    	public float distance;
    	public SenseResult(RobotInfo info, float distance)
    	{
    		center = info.location;
    		radius = info.getRadius();
    		robotInfo = info;
    		this.distance = distance;
    	}
    	public SenseResult(TreeInfo info, float distance)
    	{
    		center = info.location;
    		radius = info.radius;
    		treeInfo = info;
    		this.distance = distance;
    	}
    }
    private static SenseResult senseObstacle(MapLocation start, Direction direction, float distance) throws GameActionException
    {
    	TreeInfo sensedTree = null;
    	RobotInfo sensedRobot = null;
    	float currentSenseDistance = SENSE_OBSTACLE_INCREMENT;
    	MapLocation testLocation;
    	while( currentSenseDistance < distance)
    	{
    		testLocation = start.add(direction, currentSenseDistance);
    		if ( !rc.canSenseLocation(testLocation))
    			break;
    		sensedRobot = rc.senseRobotAtLocation(testLocation);
    		if ( sensedRobot != null )
    			return new SenseResult(sensedRobot, currentSenseDistance);
    		sensedTree = rc.senseTreeAtLocation(testLocation);
    		if ( sensedTree != null )
    			return new SenseResult(sensedTree, currentSenseDistance);
    		currentSenseDistance += SENSE_OBSTACLE_INCREMENT;
    	}
    	return null;
    }
    
    
    

    private static boolean moveTowards(MapLocation location) throws GameActionException {
        Direction dir = here.directionTo(location);

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Sense what I'm up against:
        //here.directionTo(location)
        MapLocation oneMoveLocation = here.add(dir, rc.getType().bodyRadius + rc.getType().strideRadius);
        RobotInfo obstacleRobot = rc.senseRobotAtLocation(oneMoveLocation);
        TreeInfo obstacleTree = rc.senseTreeAtLocation(oneMoveLocation);
        MapLocation obstacleLocation = oneMoveLocation;
        float obstacleRadius = 1;
        if ( obstacleRobot != null )
        {
            obstacleLocation = obstacleRobot.getLocation();
            obstacleRadius = obstacleRobot.getRadius();
        }
        if ( obstacleTree != null )
        {
            obstacleLocation = obstacleTree.getLocation();
            obstacleRadius = obstacleTree.getRadius();
        }
        Direction obstacleToEdge = obstacleLocation.directionTo(here);
        MapLocation obstacleEdge = obstacleLocation.add(obstacleLocation.directionTo(here), obstacleRadius);

        Direction tangent = obstacleToEdge.rotateLeftDegrees(90);
        if (rc.canMove(tangent)) {
            rc.move(tangent);
            return true;
        }

        // Move Randomly
        Util.tryMove(Util.randomDirection());

        return false;
    }

    public static MapLocation getPriorityTarget()
    {
        // See if there are any nearby enemy robots
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
        if(enemies.length > 0) {
            //Find closest enemy:
            return enemies[0].getLocation();
        }
        // Otherwise, head towards enemy archon location:
        return globalTarget;
    }

}
