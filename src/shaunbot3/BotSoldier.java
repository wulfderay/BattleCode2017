package shaunbot3;

import Common.*;
import battlecode.common.*;

public class BotSoldier extends Globals {

	public static RobotInfo[] enemiesLastTurn;
	public static RobotInfo[] enemies;
	public static RobotInfo unitToDefend;

	public static void loop() throws GameActionException {
		System.out.println("I'm a Soldier!");
		
		// The code you want your robot to perform every round should be in this loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your robot to explode
			try {

				//Update common data
				turnUpdate();

				//Do some stuff
				turn();

			} catch (Exception e) {
				UtilDebug.debug_exceptionHandler(e,"Soldier Exception");
			}

			//Test that we completed within bytecode limit
			if (rc.getRoundNum() != roundNum) {
				UtilDebug.alert();
				System.out.println("Soldier over bytecode limit");
			}

			// Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
			Clock.yield();

		}
	}

	public static RobotInfo currentTarget = null;
	public static int turnsSinceLastSawCurrentTarget = 0;
	public static final int TURNS_TO_PURSUE_CURRENT_TARGET = 5;
	
	public static void turn() throws GameActionException 
	{
		// Initial basic stuff
		Util.BuyVPIfItWillMakeUsWin();
		Broadcast.RollCall();
		
		// Enemy list:
		enemiesLastTurn = enemies;
		enemies = rc.senseNearbyRobots(-1, them);
		
		// Dodge if we need to
		UtilMove.AvoidBullets();
		
		if ( Util.isEarlyGame() ) {
			//Defend in the early game
			PickAndDefendAnEconUnit();
		} else {
			PursueAndDestroyPriorityEnemy();
			/*
			// Fight fight fight!
			RobotInfo priorityTarget = Util.pickPriorityTarget(enemies);
			if ( priorityTarget != null )
			{
				
			}
			else
			{
				UtilMove.Explore();
				//UtilMove.moveToFarTarget(globalTarget);
			}*/
		}
	}

	public static void PursueAndDestroyPriorityEnemy() throws GameActionException {
		if(enemies.length == 0) {
			//Some (simple) pursuit code
			if ( currentTarget == null  )
			{
				UtilMove.Explore();
			} else {
				UtilMove.moveToNearTarget(currentTarget.location);
				turnsSinceLastSawCurrentTarget++;
				if ( turnsSinceLastSawCurrentTarget > TURNS_TO_PURSUE_CURRENT_TARGET)
				{
					currentTarget = null;
				}
			}
		} else {
			currentTarget = Util.pickPriorityTarget(enemies);
			if (currentTarget == null)
				return;
			turnsSinceLastSawCurrentTarget = 0;

			Util.pursueAndDestroy(currentTarget, projectTrajectory(currentTarget.location, getRobotInfoFromList(enemiesLastTurn, currentTarget.getID()).getLocation()));
		}
	}

	public static void PickAndDefendAnEconUnit() throws GameActionException {
		unitToDefend = getUnitToDefend();

		UtilMove.defend(unitToDefend);

		RobotInfo enemy = Util.pickPriorityTarget(enemies);
		if (enemy != null) {
			UtilMove.maintainDistanceWith(enemy, myType.sensorRadius, 2.1f,unitToDefend.getLocation());
			if (enemies.length <2){
				if ( enemiesLastTurn == null )
					UtilAttack.fireStormTrooperStyle(projectTrajectory(enemy.location, enemy.getLocation())); // deter
				else
					UtilAttack.fireStormTrooperStyle(projectTrajectory(enemy.location, getRobotInfoFromList(enemiesLastTurn, enemy.getID()).getLocation())); // deter
			}else
				UtilAttack.maximumFirepowerAtSafeTarget(enemy, enemies); //ohshiohshitohshit
		}
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

	public static MapLocation projectTrajectory(MapLocation enemyLocation, MapLocation oldEnemyLocation)
	{
		//I think the angle should be (sin(Vbullet(time))/vEnemyTime)
		if ( oldEnemyLocation == null || enemyLocation.equals(oldEnemyLocation))
			return enemyLocation;
		return (enemyLocation.add(oldEnemyLocation.directionTo(enemyLocation),oldEnemyLocation.distanceTo(enemyLocation)*2)); // this needs actual math..
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
