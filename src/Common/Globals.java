package Common;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Globals {

	public static RobotController rc;
	public static MapLocation here;
	public static Team us;
	public static Team them;
	public static int myID;
	public static RobotType myType;
	public static int roundNum;
	public static MapLocation globalTarget;
	public static boolean globalTargetExists = true;
	
	public static void init(RobotController _RC) {
		rc = _RC;
		us = rc.getTeam();
		them = us.opponent();
		myID = rc.getID();
		myType = rc.getType();
		here = rc.getLocation();
	}
	
	public static void turnUpdate() throws GameActionException {
		System.out.println( "Globals.turnUpdate()");
		here = rc.getLocation();
		roundNum = rc.getRoundNum();
		globalTarget = Broadcast.ReadEnemyLocation();
		if ( globalTarget == null )
		{
			System.out.println("Updating GlobalTarget - no global target exists");
			RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, them);
			if ( nearbyEnemies.length > 0 )
			{
				System.out.println("Updating GlobalTarget - but I found an enemy!  Broadcasting!");
				globalTarget = nearbyEnemies[0].location;
				Broadcast.WriteEnemyLocation(globalTarget);
			}
			else
			{
				System.out.println("Updating GlobalTarget - but no enemy exists.  Personal target: enemy archon location");
				globalTargetExists = false;
				MapLocation [] archons = rc.getInitialArchonLocations(them);
				if (archons != null && archons.length > 0)
					globalTarget = archons[0];
				else
					globalTarget = here;
			}
		} else {
			globalTargetExists = true;
			System.out.println("Global target exists!  Woo" + globalTarget);
			//if ( rc.canSenseLocation(globalTarget) )
			if ( rc.canSenseAllOfCircle(globalTarget, rc.getType().sensorRadius*0.7f)) //Leave a buffer so we're not hunting down a tree
			{
				System.out.println("Sensing global target...");
				if ( rc.senseNearbyRobots(-1, them).length == 0 )
				{
					System.out.println("Nothing there!");
					Broadcast.ClearEnemyLocation();
				}
			}
		}
		rc.setIndicatorDot(globalTarget, 0, 0, 255);
	}
	
}
