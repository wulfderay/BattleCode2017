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

        PopulateBestNextTree();

        AttackNearbyGardenersAndArchons();

        TreeHop();

        CleanUpTreeList();

        Explore();
	}

    private static void Explore() {
	    //  if (!rc.hasMoved())
    }

    private static void CleanUpTreeList() {
	    /* // this might be needed later, but...
        if (Trees.size() >0 ) {
            for (TreeVisit treeVisit : Trees.values()) {
                treeVisit.haveVisited = false; // this will make the scout go visit all the trees again.. not necessarily good, dunno yet.
            }
        }*/

        if ( nearestUnvisitedTree != null && nearestUnvisitedTree.getLocation().isWithinDistance(here, 0.2f))
        {
            if (!rc.canSenseTree(nearestUnvisitedTree.getID())) // tree has been cut down! oh no!
            {
                Trees.remove(nearestUnvisitedTree.getID());
            }
            nearestUnvisitedTree = null; // already visited. Time to move on.
        }
    }

    private static void TreeHop() throws GameActionException  {
        if (!rc.hasMoved() && nearestUnvisitedTree != null && rc.canMove(nearestUnvisitedTree.getLocation()))
            rc.move(nearestUnvisitedTree.getLocation());
    }

    private static void AttackNearbyGardenersAndArchons() {

    }


    private static void PopulateBestNextTree() throws GameActionException {
        for (TreeInfo tree : rc.senseNearbyTrees(RobotType.SCOUT.sensorRadius/2)) {
            if (Clock.getBytecodesLeft() < RobotType.SCOUT.bytecodeLimit / 2) // don't waste too many bytecodes.
                break;
            Trees.putIfAbsent(tree.getID(), new TreeVisit(tree, false));

            if (!Trees.get(tree.getID()).haveVisited) {
                if (nearestUnvisitedTree == null || TreeIsNearerEnemyTree(tree) || TreeHasMoreBulletsOrIsCloser(tree))
                    nearestUnvisitedTree = tree;
            }


            if (rc.canShake() && rc.canInteractWithTree(tree.getID())) {

                if (tree.getContainedBullets() > 0) {
                    rc.shake(tree.getID());
                    System.out.println("Shaking tree");
                    rc.setIndicatorDot(tree.getLocation(), (int)(Math.random()* 255), (int)(Math.random()* 255), (int)(Math.random()* 255));
                }

                Trees.get(tree.getID()).haveVisited = true; // if we've shaken it, our job is done.
            }

        }
    }

    private static boolean TreeHasMoreBulletsOrIsCloser(TreeInfo tree) {
	    return (tree.getTeam() == nearestUnvisitedTree.getTeam() &&
                (tree.getContainedBullets() > nearestUnvisitedTree.getContainedBullets() ||
                (tree.getContainedBullets() == nearestUnvisitedTree.getContainedBullets() && tree.getLocation().distanceTo(here) < nearestUnvisitedTree.getLocation().distanceTo(here)))
        );
    }

    private static boolean TreeIsNearerEnemyTree(TreeInfo tree) {
	    return (tree.getTeam() == them &&
                tree.getLocation().distanceTo(here) < nearestUnvisitedTree.getLocation().distanceTo(here));
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
