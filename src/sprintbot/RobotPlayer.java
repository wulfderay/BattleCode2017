package sprintbot;

import Common.Globals;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public strictfp class RobotPlayer extends Globals {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        init(rc);

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                BotArchon.loop();
                break;
            case GARDENER:
                BotGardener.loop();
                break;
            case SOLDIER:
                BotSoldier.loop();
                break;
            case LUMBERJACK:
                BotLumberjack.loop();
            case TANK:
                BotTank.loop();
                break;
            case SCOUT:
                BotScout.loop();
                break;
        }
	}

}
