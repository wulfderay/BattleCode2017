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
	
	public static final int BOSS_ARCHON_ACTIVE_CHANNEL = 97;
	public static void SetBossArchonActive() throws GameActionException
	{
		rc.broadcast(BOSS_ARCHON_ACTIVE_CHANNEL, 1);
	}
	public static int GetBossArchonActive() throws GameActionException
	{
		return rc.readBroadcast(BOSS_ARCHON_ACTIVE_CHANNEL);
	}
	
	
	/*
	Algorithm:
	
	Enemy position channel:
	Initialize channel to enemy archon location
	Scout code: if see an archon, update enemy position
	If you can see the location and there's nothing there, zero the location
	If the location is zero and you see an enemy, update the location
	 */
	public static final int ENEMY_TARGET_X_CHANNEL = 100;
	public static final int ENEMY_TARGET_Y_CHANNEL = 101;
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
    public static int BroadcastBuffer_ChannelsRemaining()
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
