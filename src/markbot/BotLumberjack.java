package markbot;

import battlecode.common.*;

public class BotLumberjack extends Globals {

    public static int currentDirection = 1;
    public static MapLocation whereIwasBorn = here;
    public static int clearingRadius = 5; // totally arbitrary
    public static TreeInfo mostHatedTree = null;
	public static void loop() throws GameActionException {
        System.out.println("I'm a Lumberjack and I'm ok. I chop all night and strike all day!");
        // The code you want your robot to perform every round should be in this loop
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

	    AvoidBullets();

	    AttackofOpportunity();

	   // if (Util.isEarlyGame() )
            clearForest();
      //  else
       //     murderArchonsAndGardeners();
	}

    private static void AvoidBullets() {
    }

    private static void AttackofOpportunity() {
	    // if I'm near an enemy but not an ally, strike.

    }

    private static void clearForest() throws GameActionException {
        // and remember it so I can kill it next turn.
        // else, circlestrafe from my position at the correct radius in the current direction.
        // if I hit a wall, change direction and increase the radius
        // if there's an enemy tree or neutral tree next to me, shake or chop it.
        if (mostHatedTree != null && rc.canInteractWithTree(mostHatedTree.getID())) {
            if (mostHatedTree.getContainedBullets() > 0)
                rc.shake(mostHatedTree.getID());
            if (rc.getAttackCount() < 1)
                rc.chop(mostHatedTree.getID());
        }
        if( mostHatedTree != null && !rc.canInteractWithTree(mostHatedTree.getID()) && here.distanceTo(mostHatedTree.getLocation()) <=  myType.strideRadius )
        {// tree is dead. move on.
            mostHatedTree = null;
        }

        if (!Util.CircleStrafe(whereIwasBorn, clearingRadius, currentDirection)) {
            currentDirection *= -1;
            clearingRadius+=myType.strideRadius;
        }

        if (mostHatedTree == null)
        {
            mostHatedTree = getMostHatedTree(them);
        }
        if (mostHatedTree == null)
        {
            mostHatedTree = getMostHatedTree(Team.NEUTRAL);
        }

    }


    private static void murderArchonsAndGardeners() {

    }

    private static TreeInfo getMostHatedTree(Team team)
    {
        TreeInfo hated = null;
        // Not sure this is working... I think that it's fixating on any old tree, but it should be the nearest one so it doensn't get stuck
        MapLocation where = whereIwasBorn.add(whereIwasBorn.directionTo(here), clearingRadius);
        for (TreeInfo tree: rc.senseNearbyTrees(where, myType.strideRadius, team) )
        {
            if (hated == null || here.distanceTo(tree.getLocation()) < here.distanceTo(hated.getLocation()))
            {
                hated = tree;
            }

        }
        return hated;
    }

    public void oldBehavior() throws GameActionException {
        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, them);

        if(robots.length > 0 && !rc.hasAttacked()) {
            // Use strike() to hit all nearby robots!
            rc.strike();
        } else {
            // No close robots, so search for robots within sight radius
            robots = rc.senseNearbyRobots(-1,them);

            // If there is a robot, move towards it
            if(robots.length > 0) {
                MapLocation enemyLocation = robots[0].getLocation();
                Direction toEnemy = here.directionTo(enemyLocation);

                Util.tryMove(toEnemy);
            } else {
                // Move Randomly
                Util.tryMove(Util.randomDirection());
            }
        }
    }
	
}
