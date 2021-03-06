package abaselinebot;

import Common.*;
import battlecode.common.*;

public class BotLumberjack extends Globals {

    public static final int LAST_TREE_SEEN_TTL_MAX = 20;

    public static int currentDirection = 1;
    public static MapLocation whereIwasBorn = here;
    public static int clearingRadius = 5; // totally arbitrary
    public static int lastTreeSeenTTL = LAST_TREE_SEEN_TTL_MAX;

  //  public static TreeInfo mostHatedTree = null;
	public static void loop() throws GameActionException {
        System.out.println("I'm a Lumberjack and I'm ok. I chop all night and strike all day!");

        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

            	//Update common data
            	turnUpdate();
            		            	
                //Do some stuff
            	turn();

            } catch (Exception e) {
                UtilDebug.debug_exceptionHandler(e,"General Exception");
            }

            //Test that we completed within bytecode limit
            if (rc.getRoundNum() != roundNum) {
                UtilDebug.alert();
                System.out.println("***OVER BYTECODE LIMIT***");
            }

            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();

        }			
	}
	
	public static void turn() throws GameActionException {
        Util.BuyVPIfItWillMakeUsWin();

        Broadcast.RollCall();

	    UtilMove.AvoidBullets();

	    AttackofOpportunity();

        MapLocation nearestTreeINeedToChop = GetNearestTreeToChop();
	    if (nearestTreeINeedToChop != null &&!( (int)nearestTreeINeedToChop.x == 0 && (int)nearestTreeINeedToChop.y == 0))
        {
            ChopTreeDownForGardner(nearestTreeINeedToChop);
        }
        else if (Util.isEarlyGame() && lastTreeSeenTTL > 0)
            clearForest();
        else
            murderArchonsAndGardeners();
	}

	private static int bugMoveForAWhile = 0;

    private static void ChopTreeDownForGardner(MapLocation nearestTreeINeedToChop) {
	    rc.setIndicatorDot(nearestTreeINeedToChop, 0,100,0);
	    System.out.println("Need to chop tree at" + nearestTreeINeedToChop);

        if (ThereIsATreeINeedToMurder()) // come back next turn and finish killing it
        {
            rc.setIndicatorDot(here, 0,0,0);
            return; // i.e., don't move
        }

        // else move on.

        //if (nearestTreeINeedToChop.add(Direction.EAST,0.1f).distanceTo(here) > myType.sensorRadius)
        //    UtilMove.moveToFarTarget(nearestTreeINeedToChop);
        //else

        if (bugMoveForAWhile <= 0) {
            if (!UtilMove.moveToNearTarget(nearestTreeINeedToChop)) {
                System.out.println("Need to bug move for a while");
                bugMoveForAWhile = 20;
            } else {
                rc.setIndicatorDot(here, 0, 255, 0);
            }
        }

        if (bugMoveForAWhile > 0) {
            UtilMove.moveToFarTarget(nearestTreeINeedToChop);
            --bugMoveForAWhile;
            rc.setIndicatorDot(here, 0, 0, 255);
        }

    }

    private static MapLocation GetNearestTreeToChop() throws GameActionException {

	    MapLocation [] TreesThatNeedChopping = Broadcast.GetTreesToChop();
	    if (TreesThatNeedChopping == null || TreesThatNeedChopping.length == 0)
	        return null;

	    MapLocation closest = null;
	    for (MapLocation loc : TreesThatNeedChopping)
        {
            if (loc == null || (int)loc.x == 0 && (int)loc.y == 0)
                continue;
            if (closest == null)
            {
                closest = loc;
                continue;
            }
            if (here.distanceTo(closest) > here.distanceTo(loc))
                closest = loc;
        }
        return closest;
    }

    private static void AttackofOpportunity() throws GameActionException {
	    // if I'm near an enemy but not an ally, strike.
        RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS, them);

        if(robots.length > 0 && !rc.hasAttacked()) { // this needs to better gauge the cost/benefit of striking.
            // Use strike() to hit all nearby robots!
            rc.strike();
        }
    }

    /**  clearForest
     * if there's an enemy tree or neutral tree next to me, shake or chop it.
     * and remember it so I can kill it next turn.
     * else, circlestrafe from my position at the correct radius in the current direction.
     * if I hit a wall, change direction and increase the radius
     * @throws GameActionException
     */
    private static void clearForest() throws GameActionException {
        if (ThereIsATreeINeedToMurder()) // come back next turn and finish killing it
        {
            lastTreeSeenTTL = LAST_TREE_SEEN_TTL_MAX;
            return;
        }

        lastTreeSeenTTL--;
        // else move on.
        if (!UtilMove.CircleStrafe(whereIwasBorn, clearingRadius, currentDirection)) {
            currentDirection *= -1;
            clearingRadius+=myType.strideRadius;
        }
    }


    /**
     * murderArchonsAndGardeners actually just moves closer to gardeners and archons, clearing a path towards them...
     * attacks actually happen in AttackOfOpportunity, but I really like this name :).
     */
    private static void murderArchonsAndGardeners() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1 , them);
        RobotInfo mostHated = null;
        for (RobotInfo robot : enemies)
        {

            if (robot.getType() == RobotType.GARDENER)
            {
                if (mostHated == null || mostHated.getType() != RobotType.GARDENER || robot.getHealth() < mostHated.getHealth())
                    mostHated = robot;
            }
            if (robot.getType() == RobotType.ARCHON)
            {
                if (mostHated == null || (mostHated.getType() != RobotType.GARDENER && robot.getHealth() < mostHated.getHealth()))
                    mostHated = robot;
            }
        }
        if (mostHated != null )
        {
            if ( here.distanceTo(mostHated.getLocation()) < myType.strideRadius *2)
                UtilMove.moveToNearTarget(mostHated.location);
            // move towards them
            if (!UtilMove.moveToFarTarget(mostHated.getLocation())) {
            	//couldn't move that way...
            	
            }
        }

        if (ThereIsATreeINeedToMurder() && mostHated == null) // come back next turn and finish killing it
            return;

        UtilMove.moveToFarTarget(globalTarget);
    }

    /**
     * Looks for trees within chop/shake distance, shakes and chops any it finds,
     * and returns whether we need to stay here to keep killing the tree :)
     * @return True if we found a tree to fell
     * @throws GameActionException
     */
    public static boolean ThereIsATreeINeedToMurder() {
        boolean foundATreeToHate = false;
        for (TreeInfo tree: rc.senseNearbyTrees(-1) ) {
            if (tree != null && rc.canInteractWithTree(tree.getID())) {
                if (tree.getContainedBullets() > 0 && rc.canShake()) {
                    try {
                        rc.shake(tree.getID());
                    } catch (GameActionException e) {
                        UtilDebug.debug_exceptionHandler(e, "Shake exception");
                    }
                }

                if (tree.getTeam() != us && rc.canChop(tree.getID())) {
                    System.out.println("Gonna chop a tree!");
                    foundATreeToHate = true;
                    try {
                        rc.chop(tree.getID());
                        rc.setIndicatorLine(here, tree.location, 0, 200,0);
                    } catch (GameActionException e) {
                        UtilDebug.debug_exceptionHandler(e, "Chop exception");
                    }
                }
            }
        }
        return foundATreeToHate;
    }

}
