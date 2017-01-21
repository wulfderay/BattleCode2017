package markbot;

import battlecode.common.*;
import Common.*;
public class BotLumberjack extends Globals {

    public static int currentDirection = 1;
    public static MapLocation whereIwasBorn = here;
    public static int clearingRadius = 5; // totally arbitrary
  //  public static TreeInfo mostHatedTree = null;
	public static void loop() throws GameActionException {
        System.out.println("I'm a Lumberjack and I'm ok. I chop all night and strike all day!");
        // The code you want your robot to perform every round should be in this loop
        Util.setEnemyLoc(rc.getInitialArchonLocations(them)[(int)(Math.random() * rc.getInitialArchonLocations(them).length)]);
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

            	//Update common data
            	turnUpdate();
            		            	
                //Do some stuff
            	turn();

            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }

            //Test that we completed within bytecode limit
            if (rc.getRoundNum() != roundNum) {
            	System.out.println("Lumberjack over bytecode limit");
            }
            
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();

        }			
	}
	
	public static void turn() throws GameActionException {

	    Util.AvoidBullets();

	    AttackofOpportunity();

	    if (Util.isEarlyGame() )
            clearForest();
        else
            murderArchonsAndGardeners();
	}

    private static void AttackofOpportunity() throws GameActionException {
	    // if I'm near an enemy but not an ally, strike.
        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, them);

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
            return;
        // else move on.
        if (!Util.CircleStrafe(whereIwasBorn, clearingRadius, currentDirection)) {
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
            // move towards them
            Util.tryMove(here.directionTo(mostHated.getLocation()));
        }

        if (ThereIsATreeINeedToMurder() && mostHated == null) // come back next turn and finish killing it
            return;

        Util.tryMove(here.directionTo(Util.getEnemyLoc()));
    }

    /**
     * Looks for trees within chop/shake distance, shakes and chops any it finds,
     * and returns whether we need to stay here to keep killing the tree :)
     * @return True if we found a tree to fell
     * @throws GameActionException
     */
    public static boolean ThereIsATreeINeedToMurder() throws GameActionException {
        boolean foundATreeToHate = false;
        for (TreeInfo tree: rc.senseNearbyTrees( -1) ) {
            if (tree != null && rc.canInteractWithTree(tree.getID())) {
                if (tree.getContainedBullets() > 0 && rc.canShake())
                    rc.shake(tree.getID());
                if (tree.getTeam() != us && rc.canChop(tree.getID())) {
                    System.out.println("Gonna chop a tree!");
                    foundATreeToHate = true;
                    rc.chop(tree.getID());
                }
            }
        }
        return foundATreeToHate;
    }

}
