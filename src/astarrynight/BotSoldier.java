package astarrynight;

import Common.Globals;
import Common.Util;
import battlecode.common.*;

public class BotSoldier extends Globals {

    public static void loop() throws GameActionException {
        System.out.println("I'm a "+rc.getType().toString());

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                //Update common data
                turnUpdate();

                //Do some stuff
                turn();

            } catch (Exception e) {
                System.out.println(rc.getType().toString()+" Exception");
                e.printStackTrace();
            }

            //Test that we completed within bytecode limit
            if (rc.getRoundNum() != roundNum) {
                System.out.println(rc.getType().toString()+" over bytecode limit");
            }

            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();

        }
    }

    public static void turn() throws GameActionException {
        //Scan
        //Head towards enemy archon
        System.out.println("SOLDIER EXECUTING!!!!");
        MapLocation target = getPriorityTarget();
        Util.moveToFarTarget(target);

        //Alright, we'll just fire one bullet... i guess...
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
        if(enemies.length > 0) {
        	Util.tryShootTarget(enemies[0].location);
        }
        
        //Kill trees:
        Direction dir = here.directionTo(target);
        MapLocation oneMoveLocation = here.add(dir, rc.getType().bodyRadius + rc.getType().strideRadius);
        TreeInfo obstacleTree = rc.senseTreeAtLocation(oneMoveLocation);
        if ( obstacleTree != null )
        {
        	Util.tryShootTarget(obstacleTree.location);
        }
    }

    private static boolean moveTowards(MapLocation location) throws GameActionException {
        Direction dir = here.directionTo(location);

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Sense what I'm up against:
        //here.directionTo(location)
        MapLocation oneMoveLocation = here.add(dir, rc.getType().bodyRadius + rc.getType().strideRadius);
        RobotInfo obstacleRobot = rc.senseRobotAtLocation(oneMoveLocation);
        TreeInfo obstacleTree = rc.senseTreeAtLocation(oneMoveLocation);
        MapLocation obstacleLocation = oneMoveLocation;
        float obstacleRadius = 1;
        if ( obstacleRobot != null )
        {
            obstacleLocation = obstacleRobot.getLocation();
            obstacleRadius = obstacleRobot.getRadius();
        }
        if ( obstacleTree != null )
        {
            obstacleLocation = obstacleTree.getLocation();
            obstacleRadius = obstacleTree.getRadius();
        }
        Direction obstacleToEdge = obstacleLocation.directionTo(here);
        MapLocation obstacleEdge = obstacleLocation.add(obstacleLocation.directionTo(here), obstacleRadius);

        Direction tangent = obstacleToEdge.rotateLeftDegrees(90);
        if (rc.canMove(tangent)) {
            rc.move(tangent);
            return true;
        }

        // Move Randomly
        Util.tryMove(Util.randomDirection());

        return false;
    }

    public static MapLocation getPriorityTarget()
    {
        // See if there are any nearby enemy robots
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
        if(enemies.length > 0) {
            //Find closest enemy:
            return enemies[0].getLocation();
        }
        // Otherwise, head towards enemy archon location:
        return globalTarget;
    }

}
