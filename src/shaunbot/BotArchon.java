package shaunbot;

import battlecode.common.*;

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
            if ( rc.getTeamBullets() > 200 && Math.random() < 0.5 )
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
            
            //BroadcastPosition(myLocation, rc.getType());
            
		}
		
		public static int minTopLeftCoord = 0;
		public static int maxTopLeftCoord = 500;
		public static int maxBottomRightCoord = 600;
		public static void BroadcastInfo( RobotInfo info )
		{
			
			
			
		}
	
		public static final int BroadcastBuffer_StartChannel = 100;
		public static final int BroadcastBuffer_EndChannel = 200;
		public static final int BroadcastBuffer_StartIndex_Channel = 98;
		public static final int BroadcastBuffer_EndIndex_Channel = 99;
		public static int BroadcastBuffer_StartIndex = 0;
		public static int BroadcastBuffer_EndIndex = 0;
		
		public static void ReceiveBroadcastBuffer()
		{
			try
			{
				BroadcastBuffer_StartIndex = rc.readBroadcast(BroadcastBuffer_StartIndex_Channel);
				BroadcastBuffer_EndIndex = rc.readBroadcast(BroadcastBuffer_EndIndex_Channel);
	        } catch (Exception e) {
	            System.out.println("Receive Broadcast Buffer Exception");
	            e.printStackTrace();
	            //Setup start of buffer
	            BroadcastBuffer_StartIndex = BroadcastBuffer_StartChannel;
	            BroadcastBuffer_EndIndex = BroadcastBuffer_EndChannel;
	        }
		}
		
		public static void BufferBroadcast_Int( int data )
		{
			BroadcastBuffer_EndIndex++;
			if ( BroadcastBuffer_StartIndex == BroadcastBuffer_EndIndex)
			{
				
			}
			
			try
			{
				rc.broadcast(BroadcastBuffer_EndIndex, data);
			} catch (Exception e) {
	            System.out.println("Buffer Broadcast Int Exception");
	            e.printStackTrace();
	        }
			
			//rc.broadcast(, data);
		}
		
		public static void FinalizeBroadcastBuffer()
		{
			try
			{
				rc.broadcast(BroadcastBuffer_StartIndex_Channel, BroadcastBuffer_StartIndex);
				rc.broadcast(BroadcastBuffer_EndIndex_Channel, BroadcastBuffer_EndIndex);	
			} catch (Exception e) {
	            System.out.println("Finalize Broadcast Buffer Int Exception");
	            e.printStackTrace();
	        }
		}
		
}
