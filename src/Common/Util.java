package Common;

import java.util.ArrayList;

import battlecode.common.*;

public class Util extends Globals {

	/**
	 * Returns a random Direction
	 * @return a random Direction
	 */
	public static Direction randomDirection() {
		return new Direction((float)Math.random() * 2 * (float)Math.PI);
	}

	public static boolean isEarlyGame() {
		return rc.getRoundNum() < 400 && rc.getTeamBullets() < 500;
	}
	
	public static RobotInfo getRobotInfoFromList(RobotInfo [] list, int id)
	{
		if (list == null || list.length == 0)
			return null;
		for (RobotInfo robot : list)
		{
			if (robot.getID() == id)
				return robot;
		}
		return null;
	}

	public static boolean pursueAndDestroy(RobotInfo target, MapLocation projectedLocation) throws GameActionException {
		boolean moved = UtilMove.moveToNearBot(target);
		boolean shot = UtilAttack.maximumFirepowerAtSafeTarget(projectedLocation);
		return moved || shot;
	}

	//Combat utility functions (previously in Soldier):
	public static boolean pursueAndDestroy(RobotInfo target) throws GameActionException {
		return pursueAndDestroy(target, target.location);
	}

	//This is a really bad picker:  Still bad, but whatevs.
	public static RobotInfo pickPriorityTarget(RobotInfo[] enemies)
	{
		if ( enemies.length == 0 )
			return null;
		boolean isEarlyGame = Util.isEarlyGame();
		RobotInfo ArchonAsLastResort = null;
		for ( RobotInfo enemy : enemies )
		{
			if ( enemy.getType() != RobotType.ARCHON)
				return enemy;
			else {
				if (!isEarlyGame)
					ArchonAsLastResort = enemy;
			}

		}
		return ArchonAsLastResort;
	}

	public static void BuyVPIfItWillMakeUsWin() {
		try {
			if (rc.getTeamBullets() > (1000 - rc.getTeamVictoryPoints()) * getVpCostThisRound() || rc.getRoundLimit() - rc.getRoundNum() < 5) {
				rc.donate(rc.getTeamBullets());
			}
			if (rc.getTeamBullets() > 1200)
				rc.donate(10 * getVpCostThisRound());
		} catch (GameActionException e) {
			UtilDebug.debug_exceptionHandler(e, "Donation exception");
		}
	}

	public static float getVpCostThisRound()
	{
		return rc.getVictoryPointCost();
		//return (float) (7.5 + (rc.getRoundNum()*12.5 / rc.getRoundLimit()));
	}

	

	public static MapLocation[] GenerateLocations( int numLocations, float distance)
	{
		MapLocation[] mapLocations = new MapLocation[numLocations];
		Direction direction = Direction.NORTH;
		for(int ii = 0; ii < numLocations; ii++)
		{
			mapLocations[ii] = here.add(direction, distance);
			direction = direction.rotateRightDegrees(45);
			rc.setIndicatorDot(mapLocations[ii], 255, 255, 0);
		}
		return mapLocations;
	}

}
