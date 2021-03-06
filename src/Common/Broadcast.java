package Common;

import battlecode.common.*;

public class Broadcast extends Globals {
	public static final int ALPHA_ARCHON_ALERT_CHANNEL = 11;

	public static final int BOSS_ARCHON_LOCATION_X_CHANNEL = 98;
	public static final int BOSS_ARCHON_LOCATION_Y_CHANNEL = 99;

	public static final int ENEMY_TARGET_X_CHANNEL = 100;
	public static final int ENEMY_TARGET_Y_CHANNEL = 101;

	public static final int NEED_HELP_X_CHANNEL = 102;
	public static final int NEED_HELP_Y_CHANNEL = 103;
	
	public static final int ALPHA_ARCHON_LAST_CHECK_IN_CHANNEL = 200;
	public static final int ALPHA_ARCHON_ID_CHANNEL = 201;

	public static final int GARDENER_STUCK_ACCUMULATOR_CHANNEL = 202;
	public static final int GARDENER_STUCK_TALLY_CHANNEL = 203;

	public static final int TREEBUFFER_SIZE = 20; // gives us 10 trees to play with.
	public static final int TREES_TO_CHOP_START = 500;
	public static final int TREES_TO_CHOP_END = TREES_TO_CHOP_START + TREEBUFFER_SIZE;
	public static final int TOTAL_TREES_TO_CHOP_CHANNEL = TREES_TO_CHOP_END + 1;

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

	public static final int SPAWNED_ARCHONS = 700; // this is basically useless..
	public static final int SPAWNED_GARDENERS = 701;
	public static final int SPAWNED_LUMBERJACKS= 702;
	public static final int SPAWNED_SOLDIERS = 703;
	public static final int SPAWNED_SCOUTS = 704;
	public static final int SPAWNED_TANKS = 705;

	public static final int BULLSHIT_ERROR_CHANNEL = 9990;

	public static final int BroadcastBuffer_StartIndex_Channel = 2098;
	public static final int BroadcastBuffer_EndIndex_Channel = 2099;
	private static final int BroadcastBuffer_StartChannel = 1900; //Bottom of the ring
	public static final int BroadcastBuffer_EndChannel = 1999; //Top of the ring //Max channel is 1000 (apparently if i set this to 1000 it breaks so woops)
	public static int BroadcastBuffer_StartIndex = 0; //Current start index (for this robot only - must be initialized with PrepareToUse)
	public static int BroadcastBuffer_EndIndex = 0; //Current end index (for this robot only - must be initialized with PrepareToUse)
	
	
	
	public static void IHaveSpawnedA(RobotType bot) throws GameActionException
	{
		int accumulatorChannel = getBroadcastChannel(bot, SPAWNED_ARCHONS);
		System.out.println("gonna ask about " + bot.toString()+ " "+accumulatorChannel);
		rc.broadcast(accumulatorChannel,rc.readBroadcast(accumulatorChannel) +1);
	}

	/*
	Note, this won't be accurate until the spawned bot has had a chance to execute code. UNtil then it will seem as though our losses are greater than they are.
	 */
	public static float GetAttritionRateAllGame(RobotType bot) throws GameActionException {
		int spawned = GetNumberOfSpawned(bot);
		int alive = GetNumberOfLive(bot);
		if (spawned == 0)
			return -1;
		return alive/spawned;
	}

	public static int GetNumberOfSpawned(RobotType bot) throws GameActionException {
		int num = rc.readBroadcast(getBroadcastChannel(bot, SPAWNED_ARCHONS));
		System.out.println("Getting number of spawned " +bot+ " "+getBroadcastChannel(bot, SPAWNED_ARCHONS)+" "+num);
		return num;
	}

	public static void RollCall()
	{
		int accumulatorChannel = getBroadcastChannel(myType, ROBOT_ACCUM_ARCHONS);
		try {
			rc.broadcast(accumulatorChannel, rc.readBroadcast(accumulatorChannel) + 1);
		} catch (GameActionException e) {
			UtilDebug.debug_exceptionHandler(e, "Broadcast exception");
		}
	}

	public static void TallyRollCalls()
	{
		try {
			for (int i = ROBOT_ACCUM_ARCHONS; i <= ROBOT_ACCUM_TANKS; i++) {
				rc.broadcast(i - 10, rc.readBroadcast(i));
				rc.broadcast(i, 0);
			}
		} catch (GameActionException e) {
			UtilDebug.debug_exceptionHandler(e, "Broadcast exception");
		}
	}

	public static int GetNumberOfLive(RobotType type) throws GameActionException {
		return rc.readBroadcast(getBroadcastChannel(type, ROBOT_TALLY_ARCHONS));
	}



	private static int getBroadcastChannel(RobotType type, int initialChannel)
	{
		switch (type)
		{
			case ARCHON:
				return initialChannel;
			case GARDENER:
				return initialChannel+1;
			case LUMBERJACK:
				return initialChannel+2;
			case SOLDIER:
				return initialChannel+3;
			case SCOUT:
				return initialChannel+4;
			case TANK:
				return initialChannel+5;
		}
		return BULLSHIT_ERROR_CHANNEL;
	}



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

	public static void IamAStuckGardener() {
		try {
			rc.broadcast(GARDENER_STUCK_ACCUMULATOR_CHANNEL, rc.readBroadcast(GARDENER_STUCK_ACCUMULATOR_CHANNEL) + 1);
		} catch (GameActionException e) {
			UtilDebug.debug_exceptionHandler(e, "Broadcast exception");
		}
	}

	public static int TallyStuckGardeners() {
		try {
			int tally = rc.readBroadcast(GARDENER_STUCK_ACCUMULATOR_CHANNEL);
			rc.broadcast(GARDENER_STUCK_ACCUMULATOR_CHANNEL, 0);
			rc.broadcast(GARDENER_STUCK_TALLY_CHANNEL, tally);
			return tally;
		} catch (GameActionException e) {
			UtilDebug.debug_exceptionHandler(e, "Broadcast exception");
		}
		return 0;
	}

	public static MapLocation[] GetTreesToChop() {
		int totalTrees = 0;
		try {
			totalTrees = rc.readBroadcast(TOTAL_TREES_TO_CHOP_CHANNEL);
		} catch (GameActionException e) {
			UtilDebug.debug_exceptionHandler(e, "Broadcast exception");

		}
		if (totalTrees == 0 ) // we're already trying to chop the max trees. Please don't ask again.
			return new MapLocation[0];
		MapLocation [] treesToChop = new MapLocation[TREEBUFFER_SIZE /2];
		int treesfound = 0;
		for (int i = 0; i < TREEBUFFER_SIZE /2; i++  )
		{
			try {
				int x = rc.readBroadcast(TREES_TO_CHOP_START + (i * 2));
				int y = rc.readBroadcast(TREES_TO_CHOP_START + (i * 2) + 1);
				if (x != 0 || y != 0) // yes that means we won't chop trees at 0,0 but it lowers the number of reads we need, and it maked the lumberjack simpler.
				{
					treesToChop[treesfound] = new MapLocation(x, y);
					treesfound++;
				}
			} catch (GameActionException e) {
				UtilDebug.debug_exceptionHandler(e, "Broadcast exception");
			}
		}
		return treesToChop;
	}

	public static void ClearTreeList() throws GameActionException
	{
		for (int i = 0; i < TREEBUFFER_SIZE /2; i++  )
		{
			rc.broadcast(TREES_TO_CHOP_START + (i*2), 0);
			rc.broadcast(TREES_TO_CHOP_START + (i*2) +1,0);
		}
		rc.broadcast(TOTAL_TREES_TO_CHOP_CHANNEL, 0);
	}
	public static boolean INeedATreeChopped(MapLocation where) {
		try {
			int totalTrees = rc.readBroadcast(TOTAL_TREES_TO_CHOP_CHANNEL);
			if (totalTrees >= TREEBUFFER_SIZE / 2) // we're already trying to chop the max trees. Please don't ask again.
				return false;
			for (int i = 0; i < TREEBUFFER_SIZE / 2; i++) {
				int x = rc.readBroadcast(TREES_TO_CHOP_START + (i * 2));
				int y = rc.readBroadcast(TREES_TO_CHOP_START + (i * 2) + 1);
				if (x == (int) where.x && y == (int) where.y) // that tree is already to be chopped.
					return true;
				if (x == 0 && y == 0) // we found a place for our tree
				{
					rc.broadcast(TREES_TO_CHOP_START + (i * 2), (int) where.x);
					rc.broadcast(TREES_TO_CHOP_START + (i * 2) + 1, (int) where.y);
					rc.broadcast(TOTAL_TREES_TO_CHOP_CHANNEL, totalTrees + 1);
					return true;
				}
			}
		} catch (GameActionException e) {
			UtilDebug.debug_exceptionHandler(e, "Broadcast exception");
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
	
	
	//Help assistance requests!
	//Help the enemy!  With BULLETS!!!
	public static MapLocation ReadHelpEnemyLocation() throws GameActionException
	{
		int x = rc.readBroadcast(NEED_HELP_X_CHANNEL);
		int y = rc.readBroadcast(NEED_HELP_Y_CHANNEL);
		if ( x == 0 && y == 0 )
			return null;
		return new MapLocation( (float)x, (float)y);
	}
	
	public static void HelpHelpINeedAnAdult(RobotInfo[] nearbyEnemies) throws GameActionException {
		if ( nearbyEnemies.length == 0 )
			return; //No, you don't need an adult.  Go home bot, you're drunk
		for( RobotInfo enemy : nearbyEnemies ) {
			if ( enemy.type.canAttack() )
			{
				System.out.println("Help! The doll's trying to kill me and the toaster's been laughing at me!");
				Broadcast.WriteHelpEnemyLocation(nearbyEnemies[0].location);				
			}
		}
	}
	public static void WriteHelpEnemyLocation(MapLocation location) throws GameActionException
	{
		rc.broadcast(NEED_HELP_X_CHANNEL, (int) location.x);
		rc.broadcast(NEED_HELP_Y_CHANNEL, (int) location.y);
	}
	public static void ClearHelpEnemyLocation() throws GameActionException
	{
		rc.broadcast(NEED_HELP_X_CHANNEL, 0);
		rc.broadcast(NEED_HELP_Y_CHANNEL, 0);
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
