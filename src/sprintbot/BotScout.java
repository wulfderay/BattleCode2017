package sprintbot;

import Common.Broadcast;
import Common.Globals;
import Common.Util;
import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;

public class BotScout extends Globals {

    static public final RobotInfo[] emptyRobotInfoList = {};
    static public TreeInfo[] nearbyTrees;
    static public RobotInfo[] nearbyEnemyBots;
    static public RobotInfo nearestEnemyGardener;
    static public RobotInfo nearestEnemySoldier;
    static public RobotInfo nearestEnemyJack;
    static public RobotInfo nearestEnemyTank;
    static public RobotInfo nearestEnemyArchon;
    static public RobotInfo nearestEnemyScout;
    static public int friendlyGardenersNearby = 0;
    static public int friendlyAttackUnitsNearby = 0;
    static public int enemyAttackUnitsNearby = 0;

    static public boolean shouldMove = true;

    static public Direction exploreDirection = new Direction((float)Math.random() * 2 * (float)Math.PI);

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

        shouldMove = true;

	    senseNearbyBots();
	    senseNearbyTrees();

		if ( nearestEnemyArchon != null ) {
            System.out.println("Scout found an archon!  Broadcasting global target..."+nearestEnemyArchon.location);
            Broadcast.WriteEnemyLocation(nearestEnemyArchon.location);
		}
		
        if (enemyAttackUnitsNearby == 0) // nothing evil around
        {
            AttackNearbyGardenersAndArchons();
        } else if (enemyAttackUnitsNearby < friendlyAttackUnitsNearby + 2) { // harass
            Util.AvoidBullets();
            AttackNearbyGardeners();
            //TODO: Better harass code
        } else { // Run away
            Util.AvoidBullets();
            //TODO: Implement runaway code
        }

        TreeHop();

        Explore();

        AttackOfOpportunity();
	}

	public static void senseNearbyBots() {
	    nearbyEnemyBots = rc.senseNearbyRobots();
        nearestEnemyGardener = null;
	    nearestEnemyScout = null;
	    nearestEnemyArchon = null;
	    nearestEnemyJack = null;
	    nearestEnemySoldier = null;
	    nearestEnemyTank = null;
        enemyAttackUnitsNearby = 0;
        friendlyAttackUnitsNearby = 0;
        friendlyGardenersNearby = 0;

	    for (RobotInfo bot : nearbyEnemyBots) {
	        if (bot.team == us) {
	            if (bot.type == RobotType.GARDENER) {
	                friendlyGardenersNearby++;
                } else if (bot.type.canAttack()) {
	                friendlyAttackUnitsNearby++;
                }
            } else {
	            if (bot.type.canAttack()) {
	                enemyAttackUnitsNearby++;
                }
                switch (bot.type) {
                    case ARCHON:
                        if (nearestEnemyArchon == null) {
                            nearestEnemyArchon = bot;
                        }
                        break;
                    case GARDENER:
                        if (nearestEnemyGardener == null) {
                            nearestEnemyGardener = bot;
                        }
                        break;
                    case LUMBERJACK:
                        if (nearestEnemyJack == null) {
                            nearestEnemyJack = bot;
                        }
                        break;
                    case SOLDIER:
                        if (nearestEnemySoldier == null) {
                            nearestEnemySoldier = bot;
                        }
                        break;
                    case TANK:
                        if (nearestEnemyTank == null) {
                            nearestEnemyTank = bot;
                        }
                        break;
                    case SCOUT:
                        if (nearestEnemyScout == null) {
                            nearestEnemyScout = bot;
                        }
                        break;
                }
            }
        }
    }

    public static void senseNearbyTrees() {
	    nearbyTrees = rc.senseNearbyTrees();
    }

    private static void AttackOfOpportunity() throws GameActionException {
        if (rc.getTeamBullets() < 200) {
            return;
        }

	    RobotInfo[] robots = rc.senseNearbyRobots(-1, them);

        // If there are some...
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
            }
        }
    }


    private static void Explore() throws GameActionException {
	     if (rc.hasMoved() || !shouldMove)
            return;
	     if (globalTargetExists) {
	         Util.moveToFarTarget(globalTarget);
         } else {
	         System.out.println("No global target so going to explore in a random direction"+exploreDirection);
             if (!Util.tryMove(exploreDirection)) {
                 exploreDirection = exploreDirection.rotateLeftDegrees(90);
                 Util.tryMove(exploreDirection);
             }
         }
    }

    private static void TreeHop() throws GameActionException  {
	    if (rc.hasMoved() || !shouldMove) {
	        return;
        }
        for (TreeInfo tree : nearbyTrees) {
            if (tree.containedBullets > 0) {
                if (rc.canShake() && rc.canInteractWithTree(tree.getID())) {
                    rc.shake(tree.getID());
                    rc.setIndicatorDot(tree.location, 0, 255, 100);
                    System.out.println("Shaking tree");
                } else {
                    Util.moveToFarTarget(tree.location);
                }
                return;
            }
        }
    }

    private static void AttackNearbyGardenersAndArchons() throws GameActionException {
        if (nearestEnemyGardener != null) {
            Util.pursueAndDestroy(nearestEnemyGardener);
            shouldMove = false;
        } else if (nearestEnemyArchon != null) {
            if (rc.getTeamBullets() > 500) {
                Util.pursueAndDestroy(nearestEnemyArchon);
            } else {
                Util.moveToNearTarget(nearestEnemyArchon.location);
            }
            shouldMove = false;
        }
    }

    private static void AttackNearbyGardeners() throws GameActionException {
        if (nearestEnemyGardener != null) {
            Util.pursueAndDestroy(nearestEnemyGardener);
            shouldMove = false;
        }
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
            Trees.putIfAbsent(tree.getID(), new TreeVisit(tree, false));

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
