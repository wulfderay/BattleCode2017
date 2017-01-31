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
		UtilMove.CombatAvoidBullets();
		
		if ( Util.isEarlyGame() ) {
			//Defend in the early game
			System.out.println("Defending an econ unit!");
			PickAndDefendAnEconUnit();
		} else {
			System.out.println("Krush Kill n Destroy!");
			// Fight fight fight!
			PursueAndDestroyPriorityEnemy();
		}
	}
	
	public static boolean PursueAndDestroyPriorityEnemy() throws GameActionException {
		if(enemies.length == 0) {
			//Some (simple) pursuit code
			if ( currentTarget == null  )
			{
				if ( helpTargetExists && globalTargetExists )
				{
					if ( here.distanceTo(globalTarget) < here.distanceTo(helpTarget) )
						return UtilMove.moveToFarTarget(globalTarget);
					else {
						System.out.println("Help target detected closer to us!  Time to go help!");
						return UtilMove.moveToFarTarget(helpTarget);
					}
				}
				if (globalTargetExists)
				{
					return UtilMove.moveToFarTarget(globalTarget);
				}
				if (helpTargetExists)
				{
					System.out.println("Help target detected!  Time to go help!");
					return UtilMove.moveToFarTarget(helpTarget);
				}
				return UtilMove.ExploreInExploreDirection();
			}
			//Target != null
			turnsSinceLastSawCurrentTarget++;
			if ( turnsSinceLastSawCurrentTarget > TURNS_TO_PURSUE_CURRENT_TARGET)
			{
				currentTarget = null;
			}
			return UtilMove.moveToNearTarget(currentTarget.location);
		}
		//Enemies.length > 0:
		currentTarget = Util.pickPriorityTarget(enemies);
		turnsSinceLastSawCurrentTarget = 0;
		if (currentTarget == null) {
			return UtilMove.moveToFarTarget(globalTarget);
		}
		switch ( currentTarget.getType() )
		{
		case LUMBERJACK:
			float MIN = GameConstants.LUMBERJACK_STRIKE_RADIUS + RobotType.LUMBERJACK.strideRadius;
			float MAX = MIN + rc.getType().bodyRadius*2f; //Fudge factor (mmm... fudge...)
			UtilMove.maintainDistanceWith(currentTarget, MAX, MIN, currentTarget.location);
			UtilAttack.fireStormTrooperStyle(currentTarget.location);
			break;
		case SCOUT:
			UtilMove.moveToNearTarget(currentTarget.location);
			UtilAttack.fireStormTrooperStyle(currentTarget.location);
			break;
		default:
			UtilMove.moveToNearTarget(currentTarget.location);
			UtilAttack.maximumFirepowerAtSafeTarget(currentTarget, enemies);					
		}
		return true; // ehh, close enough
	}

	public static boolean PickAndDefendAnEconUnit() throws GameActionException {
		if ( enemies.length == 0 && helpTargetExists)
		{
			System.out.println("Help target detected!  Time to go help!");
			return UtilMove.moveToFarTarget(helpTarget);
		}
		
		unitToDefend = getUnitToDefend();
		
		UtilMove.defend(unitToDefend);

		RobotInfo enemy = Util.pickPriorityTarget(enemies);
		if (enemy != null ) {
			if ( unitToDefend != null )
			{
				UtilMove.maintainDistanceWith(enemy, myType.sensorRadius, 2.1f,unitToDefend.getLocation());
			}
			if (enemies.length <2){
				if ( enemiesLastTurn == null )
					UtilAttack.fireStormTrooperStyle(enemy.location); // deter
				else
					UtilAttack.fireStormTrooperStyle(enemy.location); // deter
			}else{
				UtilMove.moveToNearTarget(enemy.location); //RUN AT THE ENEMY!
				UtilAttack.maximumFirepowerAtSafeTarget(enemy, enemies); //ohshiohshitohshit
			}
		}
		return true; //-ish?!?
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
