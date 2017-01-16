package shaunbot;

import battlecode.common.*;
import Common.*;

public class BotArchon extends Globals{

		public static void loop() throws GameActionException {
	        System.out.println("I'm an archon!");

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
                	System.out.println("Archon over bytecode limit");
                }
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

	        }			
		}
		
		public static void turn() throws GameActionException {
			// Generate a random direction
            Direction dir = Util.randomDirection();

            // Randomly attempt to build a gardener in this direction
            if ( rc.getTeamBullets() / 10 >= 1000 - rc.getTeamVictoryPoints())
            {
            	rc.donate(rc.getTeamBullets());
            }
            if ( rc.getTeamBullets() > 200 && Math.random() < 1 )
            {
            	rc.donate(10);
            }
            if (rc.canHireGardener(dir) && (Math.random() < .01 || rc.getTeamBullets() > 180)) {
                rc.hireGardener(dir);
            }
            
            // Move randomly
	        Util.tryMove(Util.randomDirection());
            
            // Broadcast archon's location for other robots on the team to know
            MapLocation myLocation = rc.getLocation();
            rc.broadcast(0,(int)myLocation.x);
            rc.broadcast(1,(int)myLocation.y);
            
            Broadcast.BroadcastBuffer_PrepareToUse();
            
            System.out.println("------------------------------------------");
            System.out.println("Archon "+rc.getID()+" Reading from buffer: end index:"+Broadcast.BroadcastBuffer_EndIndex);
            System.out.println("Max broadcast channel:"+GameConstants.BROADCAST_MAX_CHANNELS);
            while ( Broadcast.BroadcastBuffer_ContainsData() )
            {
            	System.out.println(Broadcast.BroadcastBuffer_StartIndex + "->" + Broadcast.BroadcastBuffer_ReadNext());
            }
            
            System.out.println("Archon "+rc.getID()+" broadcasting position!");
            
            Broadcast.BroadcastBuffer_Send(rc.getID());
            Broadcast.BroadcastBuffer_Send((int)myLocation.x);
            Broadcast.BroadcastBuffer_Send((int)myLocation.y);
            Broadcast.BroadcastBuffer_Finalize();
            
            //BroadcastPosition(myLocation, rc.getType());
            
		}
		
		public static int minTopLeftCoord = 0;
		public static int maxTopLeftCoord = 500;
		public static int maxBottomRightCoord = 600;

		
		
		
		
}
