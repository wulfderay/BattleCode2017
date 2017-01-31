package abaselinebot;

import Common.*;
import battlecode.common.*;

public class BotScout extends Globals {

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
                UtilDebug.debug_exceptionHandler(e,"Scout Exception");
            }

            //Test that we completed within bytecode limit
            if (rc.getRoundNum() != roundNum) {
                UtilDebug.alert();
                System.out.println("Scout over bytecode limit");
            }

            Clock.yield();

        }
	}

    public static void turn() throws GameActionException {

        shouldMove = true;

        Util.BuyVPIfItWillMakeUsWin();
        Broadcast.RollCall();
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
            UtilMove.AvoidBullets();
            AttackNearbyGardeners();
            //TODO: Better harass code
        } else { // Run away
            UtilMove.AvoidBullets();
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
            UtilMove.moveToFarTarget(globalTarget);
        } else {
            System.out.println("No global target so going to explore in a random direction"+exploreDirection);
            if (!UtilMove.tryMove(exploreDirection)) {
                exploreDirection = exploreDirection.rotateLeftDegrees(90);
                UtilMove.tryMove(exploreDirection);
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
                    UtilMove.moveToFarTarget(tree.location);
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
                UtilMove.moveToNearTarget(nearestEnemyArchon.location);
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

}