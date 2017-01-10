package markbot;

import battlecode.common.*;
import scratchrob.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class BotGardener extends Robot {

    static Map<Integer, TreeInfo> MyTrees = new HashMap<>();

	public static void loop() throws GameActionException {
        System.out.println("I'm a gardener!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

            	//Update common data
            	turnUpdate();
            		            	
                //Do some stuff
            	turn();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }

            //Test that we completed within bytecode limit
            if (rc.getRoundNum() != roundNum) {
            	System.out.println("Gardener over bytecode limit");
            }
            
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();

        }			
	}
	
	public static void turn() throws GameActionException {
        // Listen for home archon's location
        int xPos = rc.readBroadcast(0);
        int yPos = rc.readBroadcast(1);
        MapLocation archonLoc = new MapLocation(xPos,yPos);

        TreeInfo[] trees = rc.senseNearbyTrees(here, RobotType.GARDENER.sensorRadius, us);
        for (TreeInfo tree : trees)
        {
            if (rc.canWater(tree.getID()))
                rc.water(tree.getID());
            MyTrees.put(tree.getID(), tree);
        }

        // next: build some trees and go between the neediest ones.

        // Generate a random direction
        Direction dir = Util.randomDirection();

        // Randomly attempt to build a soldier or lumberjack in this direction
        if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
            rc.buildRobot(RobotType.SOLDIER, dir);
        } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
            rc.buildRobot(RobotType.LUMBERJACK, dir);
        }

        // Move randomly
        Util.tryMove(Util.randomDirection());
	}

	static int GetNeediestTree()
    {
        TreeInfo needyTree = null;
        for(TreeInfo tree: MyTrees.values())
        {
            if (needyTree == null)
                needyTree = tree;
            else
                if (tree.getHealth() < needyTree.getHealth())
                    needyTree = tree;
        }
        return needyTree.getID();
    }
	
}
