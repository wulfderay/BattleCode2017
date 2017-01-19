package Common;

import battlecode.common.*;

public class Broadcast extends Globals {

	public static final int ROBOT_TALLY_ARCHONS = 600;
	public static final int ROBOT_TALLY_GARDENERS = 601;
	public static final int ROBOT_TALLY_LUMBERJACKS= 602;
	public static final int ROBOT_TALLY_SOLDIERS = 603;
	public static final int ROBOT_TALLY_SCOUTS = 604;
	public static final int ROBOT_TALLY_TANKS = 605;

	public static final int ROBOT_ACCUM_ARCHONS = 610;
	public static final int ROBOT_ACCUM_GARDENERS = 611;
	public static final int ROBOT_ACCUM_LUMBERJACKS= 612;
	public static final int ROBOT_ACCUM_SOLDIERS = 613;
	public static final int ROBOT_ACCUM_SCOUTS = 614;
	public static final int ROBOT_ACCUM_TANKS = 615;

	public static final int ENEMY_TARGET_X_CHANNEL = 100;
	public static final int ENEMY_TARGET_Y_CHANNEL = 101;

	public static final int ALPHA_ARCHON_ALERT_CHANNEL = 11;

	public static void RollCall() throws GameActionException
	{
		int accumulatorChannel = 599;
		switch (myType)
		{
			case ARCHON:
				accumulatorChannel = ROBOT_ACCUM_ARCHONS;
				break;
			case GARDENER:
				accumulatorChannel = ROBOT_ACCUM_GARDENERS;
				break;
			case SCOUT:
				accumulatorChannel = ROBOT_ACCUM_SCOUTS;
				break;
			case SOLDIER:
				accumulatorChannel = ROBOT_ACCUM_SOLDIERS;
				break;
			case LUMBERJACK:
				accumulatorChannel = ROBOT_ACCUM_LUMBERJACKS;
				break;
			case TANK:
				accumulatorChannel = ROBOT_ACCUM_TANKS;
				break;
		}
		rc.broadcast(accumulatorChannel,rc.readBroadcast(accumulatorChannel) +1);
	}

	public static void TallyRollCalls() throws GameActionException
	{
		for (int i = ROBOT_ACCUM_ARCHONS; i <= ROBOT_ACCUM_TANKS; i++)
		{
			rc.broadcast(i-10, rc.readBroadcast(i));
			rc.broadcast(i, 0);
		}
	}

	public static int GetNumberOfRobots(RobotType type) throws GameActionException {
		int tallyChannel = 599;
		switch (type)
		{
			case ARCHON:
				tallyChannel = ROBOT_TALLY_ARCHONS;
				break;
			case GARDENER:
				tallyChannel = ROBOT_TALLY_GARDENERS;
				break;
			case SCOUT:
				tallyChannel = ROBOT_TALLY_SCOUTS;
				break;
			case SOLDIER:
				tallyChannel = ROBOT_TALLY_SOLDIERS;
				break;
			case LUMBERJACK:
				tallyChannel = ROBOT_TALLY_LUMBERJACKS;
				break;
			case TANK:
				tallyChannel = ROBOT_TALLY_TANKS;
				break;
		}
		return rc.readBroadcast(tallyChannel);
	}


	public static final int BOSS_ARCHON_LOCATION_X_CHANNEL = 98;
	public static final int BOSS_ARCHON_LOCATION_Y_CHANNEL = 99;

	public static void BroadcastBossArchonLocation(MapLocation location) throws GameActionException
	{
		rc.broadcast(BOSS_ARCHON_LOCATION_X_CHANNEL, (int) location.x);
		rc.broadcast(BOSS_ARCHON_LOCATION_Y_CHANNEL, (int) location.y);
	}

	public static MapLocation RetrieveBossArchonLocation() throws GameActionException
	{
		int x = rc.readBroadcast(BOSS_ARCHON_LOCATION_X_CHANNEL);
		int y = rc.readBroadcast(BOSS_ARCHON_LOCATION_Y_CHANNEL);
		if ( x == 0 && y == 0 )
			return null;
		return new MapLocation( (float)x, (float)y);
	}

	public static final int ALPHA_ARCHON_LAST_CHECK_IN_CHANNEL = 200;
	public static final int ALPHA_ARCHON_ID_CHANNEL = 201;

	public static boolean AmIAlphaArchon() throws GameActionException {
		if (myType != RobotType.ARCHON)
			return false;

		if (rc.readBroadcast(ALPHA_ARCHON_ID_CHANNEL) == rc.getID())
		{
			rc.broadcast(ALPHA_ARCHON_LAST_CHECK_IN_CHANNEL, rc.getRoundNum());
			return true;
		}
		int lastTimeArchonSeen = rc.readBroadcast(ALPHA_ARCHON_LAST_CHECK_IN_CHANNEL);
		if (lastTimeArchonSeen == 0 || rc.getRoundNum() - lastTimeArchonSeen > 2 )
		{
			rc.broadcast(ALPHA_ARCHON_ID_CHANNEL, rc.getID());
			rc.broadcast(ALPHA_ARCHON_LAST_CHECK_IN_CHANNEL, rc.getRoundNum());
			return true;
		}
		return false;
	}

	public static int GARDENER_STUCK_ACCUMULATOR_CHANNEL = 202;
	public static int GARDENER_STUCK_TALLY_CHANNEL = 202;

	public static void IamAStuckGardener() throws GameActionException {
		rc.broadcast(GARDENER_STUCK_ACCUMULATOR_CHANNEL, rc.readBroadcast(GARDENER_STUCK_ACCUMULATOR_CHANNEL)+1);
	}

	public static int TallyStuckGardeners() throws GameActionException{
		int tally = rc.readBroadcast(GARDENER_STUCK_ACCUMULATOR_CHANNEL);
		rc.broadcast(GARDENER_STUCK_ACCUMULATOR_CHANNEL, 0);
		rc.broadcast(GARDENER_STUCK_TALLY_CHANNEL, tally);
		return tally;
	}

	public static final int TREEBUFFER_SIZE = 20; // gives us 10 trees to play with.
	public static final int TREES_TO_CHOP_START = 500;
	public static final int TREES_TO_CHOP_END = TREES_TO_CHOP_START + TREEBUFFER_SIZE;
	public static final int TOTAL_TREES_TO_CHOP_CHANNEL = TREES_TO_CHOP_END + 1;

	public static MapLocation[] GetTreesToChop() throws GameActionException {
		int totalTrees = rc.readBroadcast(TOTAL_TREES_TO_CHOP_CHANNEL);
		if (totalTrees == 0 ) // we're already trying to chop the max trees. Please don't ask again.
			return new MapLocation[0];
		MapLocation [] treesToChop = new MapLocation[TREEBUFFER_SIZE /2];
		int treesfound = 0;
		for (int i = 0; i < TREEBUFFER_SIZE /2; i++  )
		{
			int x = rc.readBroadcast(TREES_TO_CHOP_START + (i*2));
			int y = rc.readBroadcast(TREES_TO_CHOP_START + (i*2) +1);
			if (x !=0 || y != 0 ) // yes that means we won't chop trees at 0,0 but it lowers the number of reads we need, and it maked the lumberjack simpler.
			{
				treesToChop[treesfound] = new MapLocation(x,y);
				treesfound++;
			}
		}
		return treesToChop;
	}


	public static boolean INeedATreeChopped(MapLocation where) throws GameActionException {
		int totalTrees = rc.readBroadcast(TOTAL_TREES_TO_CHOP_CHANNEL);
		if (totalTrees >= TREEBUFFER_SIZE/2) // we're already trying to chop the max trees. Please don't ask again.
			return false;
		for (int i = 0; i < TREEBUFFER_SIZE /2; i++  )
		{
			int x = rc.readBroadcast(TREES_TO_CHOP_START + (i*2));
			int y = rc.readBroadcast(TREES_TO_CHOP_START + (i*2) +1);
			if ( x ==(int)where.x && y == (int)where.y) // that tree is already to be chopped.
				return true;
			if (x ==0 && y == 0 ) // we found a place for our tree
			{
				rc.broadcast(TREES_TO_CHOP_START + (i*2), (int)where.x);
				rc.broadcast(TREES_TO_CHOP_START + (i*2) +1, (int)where.y);
				rc.broadcast(TOTAL_TREES_TO_CHOP_CHANNEL, totalTrees+1);
				return true;
			}
		}
		return false;
	}

	public static boolean IChoppedATree(MapLocation where) throws GameActionException {// hmm.. linear search.. don't like that.
		if (where == null)
			return false;
		for (int i = 0; i < TREEBUFFER_SIZE /2; i++  )
		{
			int x = rc.readBroadcast(TREES_TO_CHOP_START + (i*2));
			int y = rc.readBroadcast(TREES_TO_CHOP_START + (i*2) +1);
			if (x ==(int)where.x && y == (int)where.y ) // we found our tree
			{
				rc.broadcast(TREES_TO_CHOP_START + (i*2), 0);
				rc.broadcast(TREES_TO_CHOP_START + (i*2) +1,0);
				rc.broadcast(TOTAL_TREES_TO_CHOP_CHANNEL, rc.readBroadcast(TOTAL_TREES_TO_CHOP_CHANNEL) -1);
				return true;
			}
		}
		return false;
	}

	public static MapLocation GetNearestBotInTrouble() throws GameActionException {
		for (int i = 0; i < rc.getInitialArchonLocations(us).length; i++)
		{
			if (rc.readBroadcast(3+(i*4)) == 1)
			{
				return new MapLocation(rc.readBroadcast(0+i*4), rc.readBroadcast(1+i*4));
			}
		}
		return null;
	}

	/*
	Algorithm:

	Enemy position channel:
	Initialize channel to enemy archon location
	Scout code: if see an archon, update enemy position
	If you can see the location and there's nothing there, zero the location
	If the location is zero and you see an enemy, update the location
	 */
	public static MapLocation ReadEnemyLocation() throws GameActionException
	{
		int x = rc.readBroadcast(ENEMY_TARGET_X_CHANNEL);
		int y = rc.readBroadcast(ENEMY_TARGET_Y_CHANNEL);
		if ( x == 0 && y == 0 )
			return null;
		return new MapLocation( (float)x, (float)y);
	}
	public static void WriteEnemyLocation(MapLocation location) throws GameActionException
	{
		rc.broadcast(ENEMY_TARGET_X_CHANNEL, (int) location.x);
		rc.broadcast(ENEMY_TARGET_Y_CHANNEL, (int) location.y);
	}
	public static void ClearEnemyLocation() throws GameActionException
	{
		rc.broadcast(ENEMY_TARGET_X_CHANNEL, 0);
		rc.broadcast(ENEMY_TARGET_Y_CHANNEL, 0);
	}

	public static void setAlphaArchonAlert()
	{
		try {
			rc.broadcast(ALPHA_ARCHON_ALERT_CHANNEL, 1);
		} catch (GameActionException e) {
			System.out.println("Exception in setAlphaArchonAlert"+e);
		}
	}

	public static boolean getAlphaArchonAlert()
	{
		try {
			return rc.readBroadcast(ALPHA_ARCHON_ALERT_CHANNEL) > 0;
		} catch (GameActionException e) {
			System.out.println("Exception in setAlphaArchonAlert"+e);
		}
		return false;
	}


	//Helpful Broadcast tools:
	public class BroadcastFlag
	{
		public static final int ENEMY_ARCHON_LOCATION = -1024;
		public static final int ENEMY_BOT_SPOTTED = -1025;
	}


	public static void BroadcastBuffer_BroadcastEnemySpotted(RobotInfo robot) throws GameActionException
	{
		BroadcastBuffer_Send(BroadcastFlag.ENEMY_BOT_SPOTTED);
		BroadcastBuffer_Send(robot.ID);
		BroadcastBuffer_Send(robot.getType().ordinal());
		BroadcastBuffer_Send((int) robot.location.x);
		BroadcastBuffer_Send((int) robot.location.y);
	}

	public static RobotInfo BroadcastBuffer_ReadEnemySpotted() throws GameActionException
	{
		if ( BroadcastBuffer_ChannelsRemaining() < 4 )
		{
			return null;
		}
		int id = BroadcastBuffer_ReadNext();
		int typeOrdinal = BroadcastBuffer_ReadNext();
		int x = BroadcastBuffer_ReadNext();
		int y = BroadcastBuffer_ReadNext();
		return new RobotInfo(id, them, RobotType.values()[typeOrdinal], new MapLocation(x,y), 1, 1, 1);
	}





	/*
  ____  _____   ____          _____   _____           _____ _______ 
 |  _ \|  __ \ / __ \   /\   |  __ \ / ____|   /\    / ____|__   __|
 | |_) | |__) | |  | | /  \  | |  | | |       /  \  | (___    | |   
 |  _ <|  _  /| |  | |/ /\ \ | |  | | |      / /\ \  \___ \   | |   
 | |_) | | \ \| |__| / ____ \| |__| | |____ / ____ \ ____) |  | |   
 |____/|_|__\_\\____/_/____\_\_____/ \_____/_/___ \_\_____/   |_|   
         |  _ \| |  | |  ____|  ____|  ____|  __ \                  
         | |_) | |  | | |__  | |__  | |__  | |__) |                 
         |  _ <| |  | |  __| |  __| |  __| |  _  /                  
         | |_) | |__| | |    | |    | |____| | \ \                  
         |____/ \____/|_|    |_|    |______|_|  \_\    
                      .-  _           _  -.
                  /   /             \   \
                 (   (  (` (-o-) `)  )   )
                  \   \_ `  -+-  ` _/   /
                   `-       -+-       -`
                            -+-
                            _|_
                            |/|
                            |\|
                            |/|
                            L\J
                           J/_\L 
                           |/ \|
                           |\_/|
                           |/ \|
                           |\_/|
                      _____L/ \J_____
                     /|___J/\|/\L___|\
                    //    |\/`\/|    \\
                   //      `-.-`      \\
                  //___________________\\
                  \  ________ ________  /
                   \ \      | |      / /
                    \ \_____| |_____/ /
                     \  _____ _____  /
                      \ \___] [___/ /
                       \           /
                        \ \`] [`/ /
                         \ `   ` /
                          \ O O /
                ___________\: :/____NDT____
                            \n/ 
     */
	public static final int BroadcastBuffer_StartIndex_Channel = 98;
	public static final int BroadcastBuffer_EndIndex_Channel = 99;
	private static final int BroadcastBuffer_StartChannel = 900; //Bottom of the ring
	public static final int BroadcastBuffer_EndChannel = 999; //Top of the ring //Max channel is 1000 (apparently if i set this to 1000 it breaks so woops)
	public static int BroadcastBuffer_StartIndex = 0; //Current start index (for this robot only - must be initialized with PrepareToUse)
	public static int BroadcastBuffer_EndIndex = 0; //Current end index (for this robot only - must be initialized with PrepareToUse)
	
	/*
	Functionality of the buffer:
	nnnn
	D  
	1nnn
	SE
	12ES
	123D
	1234
	
	 */

	public static void BroadcastBuffer_PrepareToUse() throws GameActionException
	{
		BroadcastBuffer_StartIndex = rc.readBroadcast(BroadcastBuffer_StartIndex_Channel);
		BroadcastBuffer_EndIndex = rc.readBroadcast(BroadcastBuffer_EndIndex_Channel);
		if ( BroadcastBuffer_StartIndex == 0 )
		{
			//uninitialized - empty: start index at the very end of the ring, and the end index at the start of the ring
			BroadcastBuffer_StartIndex = BroadcastBuffer_StartChannel;
			BroadcastBuffer_EndIndex = BroadcastBuffer_StartChannel;
		}

	}

	public static boolean BroadcastBuffer_ContainsData()
	{
		if ( BroadcastBuffer_StartIndex == BroadcastBuffer_EndIndex
//				|| BroadcastBuffer_StartIndex == BroadcastBuffer_EndIndex-1
			//Loop condition:
//	      || (BroadcastBuffer_StartIndex == BroadcastBuffer_EndChannel && BroadcastBuffer_EndChannel == BroadcastBuffer_EndIndex)
				)
		{
			return false;
		}
		return true;
	}



	public static void BroadcastBuffer_Send( int data ) throws GameActionException
	{
		rc.broadcast(BroadcastBuffer_EndIndex, data);
		BroadcastBuffer_EndIndex++;
		//Loop logic:
		if ( BroadcastBuffer_EndIndex == BroadcastBuffer_EndChannel+1)
			BroadcastBuffer_EndIndex = BroadcastBuffer_StartChannel;
		//Overflow logic:
		if ( BroadcastBuffer_EndIndex == BroadcastBuffer_StartIndex )
			BroadcastBuffer_StartIndex++;

		if ( BroadcastBuffer_StartIndex == BroadcastBuffer_EndChannel+1)
			BroadcastBuffer_StartIndex = BroadcastBuffer_StartChannel;
	}

	//This will increment the start index as well - you could not do this instead, and just read it yourself.
	//Also, if you want multiple bots to read what's stored, don't use this method.
	public static int BroadcastBuffer_ReadNext() throws GameActionException
	{
		if ( BroadcastBuffer_EndIndex == BroadcastBuffer_StartIndex )
			throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "BroadcastBuffer_ReadNext - read out of range!");
		int data = rc.readBroadcast(BroadcastBuffer_StartIndex);
		BroadcastBuffer_StartIndex++;
		if ( BroadcastBuffer_StartIndex == BroadcastBuffer_EndChannel+1)
			BroadcastBuffer_StartIndex = BroadcastBuffer_StartChannel;
		return data;
	}
	public static int BroadcastBuffer_Peek() throws GameActionException
	{
		if ( BroadcastBuffer_EndIndex == BroadcastBuffer_StartIndex )
			throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "BroadcastBuffer_ReadNext - read out of range!");
		int data = rc.readBroadcast(BroadcastBuffer_StartIndex);
		return data;
	}
	public static void BroadcastBuffer_ClearAllData()
	{
		BroadcastBuffer_StartIndex = BroadcastBuffer_EndIndex;
	}
	private static int BroadcastBuffer_ChannelsRemaining()
	{
		if ( BroadcastBuffer_StartIndex < BroadcastBuffer_EndIndex )
			return BroadcastBuffer_StartIndex - BroadcastBuffer_EndIndex;
		if ( BroadcastBuffer_StartIndex > BroadcastBuffer_EndIndex )
			return BroadcastBuffer_EndIndex + (BroadcastBuffer_EndChannel - BroadcastBuffer_StartChannel) - BroadcastBuffer_StartIndex;
		return 0;
	}

	public static void BroadcastBuffer_Finalize() throws GameActionException
	{
		rc.broadcast(BroadcastBuffer_StartIndex_Channel, BroadcastBuffer_StartIndex);
		rc.broadcast(BroadcastBuffer_EndIndex_Channel, BroadcastBuffer_EndIndex);
	}
}
