package Common;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
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
		here = rc.getLocation();
		roundNum = rc.getRoundNum();
		globalTarget = Broadcast.ReadEnemyLocation();
		if ( globalTarget == null )
		{
			globalTargetExists = false;
			globalTarget = rc.getInitialArchonLocations(them)[0];
		} else {
			globalTargetExists = true;
		}
	}
	
}
