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
		return rc.getRoundNum() < 400;
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

	public static void BuyVPIfItWillMakeUsWin() throws GameActionException {
		if ( rc.getTeamBullets() > (1000 - rc.getTeamVictoryPoints())* getVpCostThisRound() || rc.getRoundLimit() -rc.getRoundNum() < 5)
		{
			rc.donate(rc.getTeamBullets());
		}
		if (rc.getTeamBullets() > 1200)
		rc.donate( 10 * getVpCostThisRound());
	}

	public static float getVpCostThisRound()
	{
		return (float) (7.5 + (rc.getRoundNum()*12.5 / rc.getRoundLimit()));
	}

}
