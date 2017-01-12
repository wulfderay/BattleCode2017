package markbot;

import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;

public class BotScout extends Globals {

    static TreeInfo nearestUnvisitedTree = null;
    static Map<Integer,TreeVisit> Trees = new HashMap<>();
	public static void loop() throws GameActionException {
        System.out.println("I'm a scout!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

            	//Update common data
            	turnUpdate();
            		            	
                //Do some stuff
            	turn();

            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }

            //Test that we completed within bytecode limit
            if (rc.getRoundNum() != roundNum) {
            	System.out.println("Scout over bytecode limit");
            }
            System.out.println("Bytecodes left at end of turn:" + Clock.getBytecodesLeft());
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();

        }			
	}
	
	public static void turn() throws GameActionException {
        for (TreeInfo tree: rc.senseNearbyTrees(RobotType.SCOUT.sensorRadius)) {
            if (Clock.getBytecodesLeft() < RobotType.SCOUT.bytecodeLimit /2) // don't waste too many bytecodes.
                break;
            Trees.putIfAbsent(tree.getID(), new TreeVisit(tree, false));

            if (!Trees.get(tree.getID()).haveVisited &&
                    (nearestUnvisitedTree == null ||
                            tree.getLocation().distanceTo(here) < nearestUnvisitedTree.getLocation().distanceTo(here))) {
                nearestUnvisitedTree = tree;

            }
            if (rc.canShake() &&  rc.canInteractWithTree(tree.getID()) )
            {

                if ( tree.getContainedBullets() > 0){
                    System.out.println("Tree "+tree.getID()+ " has "+ tree.getContainedBullets() +" bullets.");
                    rc.shake(tree.getID());
                }

                Trees.get(tree.getID()).haveVisited = true; // if we've shaken it, our job is done.
            }

        }
        if (nearestUnvisitedTree != null)
        {
            if (rc.canMove(nearestUnvisitedTree.getLocation()))
                rc.move(nearestUnvisitedTree.getLocation());
            else {
                Util.tryMove(Util.randomDirection());
            }

        }
        else // out of trees.. Time to go visit again.
        {
            if (Trees.size() >0 ) {
                for (TreeVisit treeVisit : Trees.values()) {
                    treeVisit.haveVisited = false; // this will make the scout go visit all the trees again.. not necessarily good, dunno yet.
                }
            }
            Util.tryMove(Util.randomDirection());
        }



        if ( nearestUnvisitedTree != null && nearestUnvisitedTree.getLocation().isWithinDistance(here, 0.2f))
        {
            if (!rc.canSenseTree(nearestUnvisitedTree.getID())) // tree has been cut down! oh no!
            {
                Trees.remove(nearestUnvisitedTree.getID());
            }
            nearestUnvisitedTree = null; // already visited. Time to move on.
        }

	}

	public static class TreeVisit{
        public TreeInfo tree;
        public boolean haveVisited;

        public TreeVisit( TreeInfo tree, boolean haveVisited)
        {
            this.haveVisited = haveVisited;
            this.tree = tree;
        }
    }
}
