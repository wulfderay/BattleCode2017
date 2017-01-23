package markbot2;

import Common.Broadcast;
import Common.Globals;
import Common.Util;
import battlecode.common.*;

public class BotSoldier extends Globals {

	public static RobotInfo[] enemies;
	public static RobotInfo unitToDefend;

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
		Util.BuyVPIfItWillMakeUsWin();
		Broadcast.RollCall();
		//Enemy list:
		enemies = rc.senseNearbyRobots(-1, them);

		if (Util.isEarlyGame()) {
			PickAndDefendAnEconUnit();
		}
		PursueAndDestroyPriorityEnemy();

	}

	public static void PursueAndDestroyPriorityEnemy() throws GameActionException {
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
			currentTarget = Util.pickPriorityTarget(enemies);
			MapLocation target = currentTarget.location;
			turnsSinceLastSawCurrentTarget = 0;

			Util.pursueAndDestroy(currentTarget);

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
	public static void PickAndDefendAnEconUnit() throws GameActionException {
		unitToDefend = getUnitToDefend();

		Util.defend(unitToDefend);

		RobotInfo enemy = Util.pickPriorityTarget(enemies);
		if (enemy != null) {
			Util.moveToNearTarget(enemy.location);
			Util.fireStormTrooperStyle(enemy.location);
		}
	}

	// warning, this can return null.
	public static RobotInfo getUnitToDefend() throws GameActionException {
		// make sure the unit is still alive (and in tthe ssame relative are I left it in..)
		if (unitToDefend != null && rc.canSenseLocation(unitToDefend.getLocation())) // we have a previous unit. Take care of it if we can.
		{
			if (rc.canSenseRobot(unitToDefend.getID())) // he ded.
			{
				unitToDefend = rc.senseRobot(unitToDefend.getID()); // update the location of it so we don't lose track.
				return unitToDefend;
			}
			// he ded.
			unitToDefend = null;
		}
		if (unitToDefend !=  null) // he'se still out there. Get back to him!
			return unitToDefend;
		// if we're here, it's cause the guy we were defending is dead, gone or never existed. find a new person to help.
		RobotInfo[] friendlies = rc.senseNearbyRobots(-1,us);
		RobotInfo nearestArchon = null;
		RobotInfo nearestGardener = null;
		for (RobotInfo bot : friendlies) {
			if (bot.type == RobotType.ARCHON && nearestArchon == null){
				nearestArchon = bot;
			}
			else if (bot.type == RobotType.GARDENER && nearestGardener == null) {
				nearestGardener = bot;
			}
			if (nearestArchon != null && nearestGardener != null)
				break;
		}
		if (nearestGardener != null) {
			return nearestGardener;
		} else {
			return nearestArchon;
		}
	}
}
