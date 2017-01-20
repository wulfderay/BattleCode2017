package markbot2;

import Common.Broadcast;
import Common.Globals;
import Common.Util;
import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;

public class BotScout extends Globals {

    static TreeInfo nearestUnvisitedTree = null;
    static Map<Integer,TreeVisit> Trees = new HashMap<>();
    static float Treedensity = 1 ; // used for adjusting the sense distance to use less bytecode.
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
        Util.BuyVPIfItWillMakeUsWin();

        Broadcast.RollCall();

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1);
        for(RobotInfo robot : nearbyRobots)
        {
            if (robot.getTeam() == them) {
                if (robot.getType() == RobotType.ARCHON) {
                    System.out.println("Scout found an archon!  Broadcasting global target..." + robot.location);
                    Broadcast.WriteEnemyLocation(robot.location);
                    break;
                }
            }
        }

        PopulateBestNextTree();

        Util.AvoidBullets();

        AttackNearbyGardenersAndArchons();

        TreeHop();

        CleanUpTreeList();

        Explore();

        if (rc.getRoundNum() > rc.getRoundLimit()/2)
            AttackOfOpportunity();
    }

    private static void AttackOfOpportunity() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, them);

        // If there are some...
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                Util.maximumFirepowerAtSafeTarget(robots[0], robots);
            }
        }
    }


    private static void Explore() throws GameActionException {
        if (rc.hasMoved())
            return;
        Util.setEnemyLoc(globalTarget);
        Util.tryMove(here.directionTo(Util.getEnemyLoc())); // maybe change this to circle-strafe?
	     /*
	     - move towards enemy broadcasts
 - move towards enemy start location
 - move randomly
	      */
    }

    private static void CleanUpTreeList() {
	    /* // this might be needed later, but...
        if (Trees.size() >0 ) {
            for (TreeVisit treeVisit : Trees.values()) {
                treeVisit.haveVisited = false; // this will make the scout go visit all the trees again.. not necessarily good, dunno yet.
            }
        }*/

        if ( shouldReplaceNextTree() && nearestUnvisitedTree != null)
        {
            if ( !rc.canSenseTree(nearestUnvisitedTree.getID())) // tree has been cut down! oh no!
            {
                Trees.remove(nearestUnvisitedTree.getID());
            }
            nearestUnvisitedTree = null; // already visited. Time to move on.
        }
    }

    private static void TreeHop() throws GameActionException  {
        if (nearestUnvisitedTree == null)
            return;

        if (!rc.hasMoved() && nearestUnvisitedTree != null)
        {
            if (!Util.doMove(nearestUnvisitedTree.getLocation(), true)) {
                Util.tryMove(here.directionTo(nearestUnvisitedTree.getLocation()));
            }
        }
        if (rc.canShake() && rc.canInteractWithTree(nearestUnvisitedTree.getID())) {

            if (nearestUnvisitedTree.getContainedBullets() > 0) {
                rc.shake(nearestUnvisitedTree.getID());
                System.out.println("Shaking tree");
                rc.setIndicatorDot(nearestUnvisitedTree.getLocation(), (int)(Math.random()* 255), (int)(Math.random()* 255), (int)(Math.random()* 255));
            }

            Trees.get(nearestUnvisitedTree.getID()).haveVisited = true;
        }
    }

    private static void AttackNearbyGardenersAndArchons() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1 , them);
        RobotInfo mostHated = null;
        for (RobotInfo robot : enemies)
        {
            if (robot.getType() == RobotType.GARDENER)
            {
                if (mostHated == null || mostHated.getType() != RobotType.GARDENER || robot.getHealth() < mostHated.getHealth())
                    mostHated = robot;
            }
        }
        if (mostHated == null) return;

        //BobandWeave(mostHated);
        Util.moveToNearTarget(mostHated.location);

        Util.maximumFirepowerAtSafeTarget(mostHated, enemies);
    }

    private static void BobandWeave(RobotInfo mostHated) throws GameActionException {
        // if in a tree, come out of it and shoot.
        // if out of a tree, shoot, then try to find cover or move
        if (rc.isLocationOccupiedByTree(here))
        {
            if (rc.getMoveCount() < 1)
                Util.tryMove(here.directionTo(mostHated.getLocation()),0, 4);
            if ( rc.getAttackCount() < 1 && rc.getTeamBullets() >1 && rc.isLocationOccupiedByTree(here))
                rc.fireSingleShot(here.directionTo(mostHated.getLocation()));
        }
        else
        {

            if ( rc.getAttackCount() < 1 && rc.getTeamBullets() >1 && rc.isLocationOccupiedByTree(here))
                rc.fireSingleShot(here.directionTo(mostHated.getLocation()));
            findCoverFrom(mostHated.getLocation());
        }
        if (rc.getMoveCount() < 1)
            Util.tryMove(here.directionTo(mostHated.getLocation()), 150, 4);
    }

    private static void findCoverFrom(MapLocation from) throws GameActionException {
        if (rc.getMoveCount() > 0) return;
        TreeInfo[] covertrees = rc.senseNearbyTrees(RobotType.SCOUT.strideRadius);
        if (covertrees.length > 0)
            rc.move(covertrees[0].getLocation());
        else
            Util.tryMove(here.directionTo(from),0, 4);

    }


    private static void PopulateBestNextTree() throws GameActionException {
        // if we haven't visited the nearest tree picked in an earlier round, don't replace it

        for (TreeInfo tree : rc.senseNearbyTrees(RobotType.SCOUT.sensorRadius/Treedensity)) {
            if (Clock.getBytecodesLeft() < RobotType.SCOUT.bytecodeLimit / 2) // don't waste too many bytecodes.
            {
                Treedensity+=0.5;
                break;
            }
            Trees.putIfAbsent(tree.getID(), new BotScout.TreeVisit(tree, false));

            if (shouldReplaceNextTree() && !Trees.get(tree.getID()).haveVisited) {
                if (nearestUnvisitedTree == null || TreeIsNearerEnemyTree(tree) || TreeHasMoreBulletsOrIsCloser(tree))
                    nearestUnvisitedTree = tree;
            }

        }
        if (Clock.getBytecodesLeft() >= RobotType.SCOUT.bytecodeLimit / 2)
        {
            Treedensity = Math.max(Treedensity/=2,1);
        }
    }

    private static boolean shouldReplaceNextTree()
    {
        return (nearestUnvisitedTree == null || Trees.get(nearestUnvisitedTree.getID()).haveVisited);
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
