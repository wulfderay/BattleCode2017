package Common;

import battlecode.common.*;

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
	
	public static MapLocation helpTarget;
	public static boolean helpTargetExists = true;

	public static Direction hexMoveA = Direction.NORTH;
	public static Direction hexMoveB = Direction.NORTH.rotateRightDegrees(60);
	public static Direction hexMoveC = Direction.NORTH.rotateRightDegrees(120);
	public static Direction hexMoveD = Direction.NORTH.rotateRightDegrees(180);
	public static Direction hexMoveE = Direction.NORTH.rotateRightDegrees(240);
	public static Direction hexMoveF = Direction.NORTH.rotateRightDegrees(300);

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
		scanForHelpTurnUpdate();
	}

	public static void scanForHelpTurnUpdate() throws GameActionException {
		System.out.println( "Globals.scanForHelpTurnUpdate()");
		here = rc.getLocation();
		roundNum = rc.getRoundNum();
		helpTarget = Broadcast.ReadHelpEnemyLocation();
		if ( helpTarget == null )
		{
			helpTargetExists = false;
		} else {
			helpTargetExists = true;
			System.out.println("Help target exists!  Woo" + helpTarget);
			if ( rc.canSenseAllOfCircle(helpTarget, myType.sensorRadius*0.7f)) //Leave a buffer so we're not hunting down a tree
			{
				System.out.println("Sensing helpTarget...");
				if ( rc.senseNearbyRobots(-1, them).length == 0 )
				{
					System.out.println("Nothing there!");
					Broadcast.ClearHelpEnemyLocation();
				}
			}
			rc.setIndicatorLine(helpTarget.add(Direction.NORTH), helpTarget.add(Direction.SOUTH), 255, 0, 0);
			rc.setIndicatorLine(helpTarget.add(Direction.EAST), helpTarget.add(Direction.WEST), 255, 0, 0);
		}
	}
}
