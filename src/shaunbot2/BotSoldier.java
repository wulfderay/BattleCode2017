package shaunbot2;

import com.sun.jdi.Location;

import Common.Broadcast;
import Common.Globals;
import Common.Util;
import battlecode.common.*;

public class BotSoldier extends Globals {
	
	public static boolean defensive = false;
	
    public static void loop() throws GameActionException {
        System.out.println("I'm a "+rc.getType().toString());
        
        defensive = Math.random() > 0.5f;
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                //Update common data
                turnUpdate();
                
                //Do some stuff
                if ( defensive) 
                	defensiveTurn();
                else
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
    
    public static void defensiveTurn() throws GameActionException
    {
    	//Enemy list:
    	RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
    	
    	
    	if(enemies.length == 0) {
    		Util.makeAmoebaCircle();
        } else {
        	MapLocation target = Util.pickPriorityTarget(enemies).location;
        	turnsSinceLastSawCurrentTarget = 0;
        	currentTarget = Util.pickPriorityTarget(enemies);
        	Util.moveToNearTarget(currentTarget.location);
        	
        	currentTarget = Util.pickPriorityTarget(enemies);
        	Util.maximumFirepowerAtSafeTarget(currentTarget, enemies);
        	
	        //Kill trees:
	        Direction dir = here.directionTo(target);
	        MapLocation oneMoveLocation = here.add(dir, rc.getType().bodyRadius + rc.getType().strideRadius);
	        TreeInfo obstacleTree = rc.senseTreeAtLocation(oneMoveLocation);
	        if ( obstacleTree != null )
	        {
	        	Util.maximumFirepowerAt(obstacleTree.location);
	        }
        }
    }
    
    
    
    public static RobotInfo currentTarget = null;
    public static int turnsSinceLastSawCurrentTarget = 0;
    public static final int TURNS_TO_PURSUE_CURRENT_TARGET = 5;
    
    public static void turn() throws GameActionException {
        //Enemy list:
    	RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
    	if(enemies.length == 0) {
    		//Some (simple) pursuit code
    		if ( currentTarget == null  )
    		{
    			Util.moveToFarTarget(globalTarget);
    		} else { 
    			Util.moveToNearTarget(currentTarget.location);
    			turnsSinceLastSawCurrentTarget++;
    			if ( turnsSinceLastSawCurrentTarget > TURNS_TO_PURSUE_CURRENT_TARGET)
    			{
    				currentTarget = null;
    			}
    		}
        } else {
        	MapLocation target = Util.pickPriorityTarget(enemies).location;
        	turnsSinceLastSawCurrentTarget = 0;
        	currentTarget = Util.pickPriorityTarget(enemies);
        	Util.moveToNearTarget(currentTarget.location);
        	
        	currentTarget = Util.pickPriorityTarget(enemies);
        	Util.maximumFirepowerAtSafeTarget(currentTarget, enemies);
        	
	        //Kill trees:
	        Direction dir = here.directionTo(target);
	        MapLocation oneMoveLocation = here.add(dir, rc.getType().bodyRadius + rc.getType().strideRadius);
	        TreeInfo obstacleTree = rc.senseTreeAtLocation(oneMoveLocation);
	        if ( obstacleTree != null )
	        {
	        	Util.maximumFirepowerAt(obstacleTree.location);
	        }
        }
    }
    

}
