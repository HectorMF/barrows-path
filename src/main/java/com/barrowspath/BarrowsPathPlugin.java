package com.barrowspath;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Shows the path to the barrows chest"
)
public class BarrowsPathPlugin extends Plugin
{
	@Getter
	@Inject
	@Named("developerMode")
	private boolean developerMode = true;

	static final Zone barrowsZone = new Zone(new WorldPoint(3524, 9667, 0), new WorldPoint(3579, 9722, 0));

	@Inject
	private Client client;

	@Inject
	private BarrowsPathConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private BarrowsPathOverlay overlay;
	@Inject
	private BarrowsPathDebugOverlay debugOverlay;

	@Getter
	private Maze maze;

	@Getter
	private Solution solution;

	@Getter
	private final Set<Door> doors = new HashSet<>();
	private int delayTicksUntilLevelsInitialized = 2;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);

		if (developerMode && this.config.showDebugOverlay())
		{
			this.overlayManager.add(debugOverlay);
		}

		//Rooms
		Room se = new Room("South East Room", new WorldPoint(3574, 9672, 0), new WorldPoint(3563, 9683, 0), 9);
		Room s = new Room("South Room", new WorldPoint(3557, 9672, 0), new WorldPoint(3546, 9683, 0), 9);
		Room sw = new Room("South West Room", new WorldPoint(3540, 9672, 0), new WorldPoint(3529, 9683, 0), 9);
		Room w = new Room("West Room", new WorldPoint(3540, 9689, 0), new WorldPoint(3529, 9700, 0), 9);
		Room nw = new Room("North West Room", new WorldPoint(3540, 9706, 0), new WorldPoint(3529, 9717, 0), 9);
		Room n = new Room("North Room", new WorldPoint(3557, 9706, 0), new WorldPoint(3546, 9717, 0), 9);
		Room ne = new Room("North East", new WorldPoint(3574, 9706, 0), new WorldPoint(3563, 9717, 0), 9);
		Room e = new Room("East", new WorldPoint(3574, 9689, 0), new WorldPoint(3563, 9700, 0), 9);
		Room c = new Room("Center", new WorldPoint(3557, 9689, 0), new WorldPoint(3546, 9700, 0), 0);

		//Hallways
		Room se_to_s = new Room("South East to South Hallway", new WorldPoint(3562,9677, 0), new WorldPoint(3558, 9678, 0), 4);
		Room s_to_sw = new Room("South to South West Hallway", new WorldPoint(3545, 9677, 0), new WorldPoint(3541, 9678, 0), 4);
		Room sw_to_w = new Room("South West to West Hallway", new WorldPoint(3535, 9684, 0), new WorldPoint(3534, 9688, 0), 4);
		Room w_to_nw = new Room("West to North West Hallway", new WorldPoint(3535, 9701, 0), new WorldPoint(3534, 9705, 0), 4);
		Room nw_to_n = new Room("North West to North Hallway", new WorldPoint(3541, 9711, 0), new WorldPoint(3545, 9712, 0), 4);
		Room n_to_ne = new Room("North to North East Hallway", new WorldPoint(3558, 9711, 0), new WorldPoint(3562, 9712, 0), 4);
		Room ne_to_e = new Room("North to North East Hallway", new WorldPoint(3569, 9701, 0), new WorldPoint(3568, 9705, 0), 4);
		Room e_to_se = new Room("East to South East Hallway", new WorldPoint(3569, 9684, 0), new WorldPoint(3568, 9688, 0), 4);

		Room s_to_c = new Room("South to Center Hallway", new WorldPoint(3552, 9684, 0), new WorldPoint(3551, 9688, 0), 4);
		Room w_to_c = new Room("West to Center Hallway", new WorldPoint(3541, 9694, 0), new WorldPoint(3545, 9695, 0), 4);
		Room n_to_c = new Room("North to Center Hallway", new WorldPoint(3552, 9701, 0), new WorldPoint(3551, 9705, 0), 4);
		Room e_to_c = new Room("East to Center Hallway", new WorldPoint(3562, 9695, 0), new WorldPoint(3558, 9694, 0), 4);

		Room se_to_sw = new Room("South East to South West Hallway", new WorldPoint(3569, 9667, 0), new WorldPoint(3534, 9671, 0), 35);
		Room sw_to_nw = new Room("South West to North West Hallway", new WorldPoint(3528, 9712, 0), new WorldPoint(3524,9677, 0), 35);
		Room nw_to_ne = new Room("North West to North East Hallway", new WorldPoint(3534, 9722, 0), new WorldPoint(3569, 9718, 0), 35);
		Room ne_to_se = new Room("North East to South East Hallway", new WorldPoint(3579, 9712, 0), new WorldPoint(3575, 9677, 0), 35);
		ArrayList<PreciseWorldPoint> path = new ArrayList<>();
		// Doors
		Door se_n = new Door(13196943, 13200424, se, e_to_se);
		Door se_e = new Door(13220956, 13220897, se, ne_to_se);
		Door se_s = new Door(13199657, 13196176, se, se_to_sw);
		Door se_w = new Door(13175644, 13175703, se, se_to_s);

		Door s_n = new Door(13137766, 13141247, s, s_to_c);
		Door s_e = new Door(13161779, 13161720, s, se_to_s);
		Door s_w = new Door(13116467, 13116526, s, s_to_sw);

		Door sw_n = new Door(13078589, 13082070, sw, sw_to_w);
		Door sw_e = new Door(13102602, 13102543, sw, s_to_sw);
		Door sw_s = new Door(13081303, 13077822, sw, se_to_sw);
		Door sw_w = new Door(13057290, 13057349, sw, sw_to_nw);

		Door w_n = new Door(13079592, 13083073, w, w_to_nw);
		Door w_e = new Door(13103605, 13103546, w, w_to_c);
		Door w_s = new Door(13082306, 13078825, w, sw_to_w);

		Door nw_n = new Door(13080595, 13084076, nw, nw_to_ne);
		Door nw_e = new Door(13104608, 13104549, nw, nw_to_n);
		Door nw_s = new Door(13083309, 13079828, nw, w_to_nw);
		Door nw_w = new Door(13059296, 13059355, nw, sw_to_nw);

		Door n_e = new Door(13118473, 13118532, n, nw_to_n);
		Door n_s = new Door(13142486, 13139005, n, n_to_c);
		Door n_w = new Door(13163785, 13163726, n, n_to_ne);

		Door ne_n = new Door(13198949, 13202430, ne, nw_to_ne);
		Door ne_e = new Door(13222962, 13222903, ne, ne_to_se);
		Door ne_s = new Door(13201663, 13198182, ne, ne_to_e);
		Door ne_w = new Door(13177650, 13177709, ne, n_to_ne);

		Door e_n = new Door(13197946, 13201427, e, ne_to_e);
		Door e_s = new Door(13200660, 13197179, e, e_to_se);
		Door e_w = new Door(13176647, 13176706, e, e_to_c);

		Door c_n = new Door(13138769, 13142250, n_to_c, c);
		Door c_e = new Door(13162782, 13162723, e_to_c, c);
		Door c_s = new Door(13141483, 13138002, s_to_c, c);
		Door c_w = new Door(13117470, 13117529, w_to_c, c);

		// Add custom pathing for long hallways
		se_e.setPath(Arrays.asList(
				new PreciseWorldPoint(3569, 9678, -1),
				new PreciseWorldPoint(3576, 9678, -1),
				new PreciseWorldPoint(3579, 9681, -1),
				new PreciseWorldPoint(3579, 9694, -1)
		));

		se_s.setPath(Arrays.asList(
				new PreciseWorldPoint(3569, 9678, -1),
				new PreciseWorldPoint(3569, 9671, -1),
				new PreciseWorldPoint(3566, 9668, -1),
				new PreciseWorldPoint(3552, 9668, -1)
		));

		sw_s.setPath(Arrays.asList(
				new PreciseWorldPoint(3535, 9678, -1),
				new PreciseWorldPoint(3535, 9671, -1),
				new PreciseWorldPoint(3538, 9668, -1),
				new PreciseWorldPoint(3552, 9668, -1)
		));

		sw_w.setPath(Arrays.asList(
				new PreciseWorldPoint(3535, 9678, -1),
				new PreciseWorldPoint(3528, 9678, -1),
				new PreciseWorldPoint(3525, 9681, -1),
				new PreciseWorldPoint(3525, 9695, -1)
		));

		nw_n.setPath(Arrays.asList(
				new PreciseWorldPoint(3535, 9712, -1),
				new PreciseWorldPoint(3535, 9719, -1),
				new PreciseWorldPoint(3538, 9722, -1),
				new PreciseWorldPoint(3552, 9722, -1)
		));

		nw_w.setPath(Arrays.asList(
				new PreciseWorldPoint(3535, 9712, -1),
				new PreciseWorldPoint(3528, 9712, -1),
				new PreciseWorldPoint(3525, 9709, -1),
				new PreciseWorldPoint(3525, 9695, -1)
		));

		ne_n.setPath(Arrays.asList(
				new PreciseWorldPoint(3569, 9712, -1),
				new PreciseWorldPoint(3569, 9719, -1),
				new PreciseWorldPoint(3566, 9722, -1),
				new PreciseWorldPoint(3552, 9722, -1)
		));

		ne_e.setPath(Arrays.asList(
				new PreciseWorldPoint(3569, 9712, -1),
				new PreciseWorldPoint(3576, 9712, -1),
				new PreciseWorldPoint(3579, 9709, -1),
				new PreciseWorldPoint(3579, 9694, -1)
		));

		Set<Room> rooms = new HashSet<>();
		rooms.add(se);
		rooms.add(s);
		rooms.add(sw);
		rooms.add(w);
		rooms.add(nw);
		rooms.add(n);
		rooms.add(ne);
		rooms.add(e);
		rooms.add(c);

		rooms.add(se_to_s);
		rooms.add(s_to_sw);
		rooms.add(sw_to_w);
		rooms.add(w_to_nw);
		rooms.add(nw_to_n);
		rooms.add(n_to_ne);
		rooms.add(ne_to_e);
		rooms.add(e_to_se);

		rooms.add(s_to_c);
		rooms.add(w_to_c);
		rooms.add(n_to_c);
		rooms.add(e_to_c);

		rooms.add(se_to_sw);
		rooms.add(sw_to_nw);
		rooms.add(nw_to_ne);
		rooms.add(ne_to_se);

		maze = new Maze(rooms, c);
		doors.add(se_n);
		doors.add(se_e);
		doors.add(se_s);
		doors.add(se_w);
		doors.add(s_n);
		doors.add(s_e);
		doors.add(s_w);
		doors.add(sw_n);
		doors.add(sw_e);
		doors.add(sw_s);
		doors.add(sw_w);
		doors.add(w_n);
		doors.add(w_e);
		doors.add(w_s);
		doors.add(nw_n);
		doors.add(nw_e);
		doors.add(nw_s);
		doors.add(nw_w);
		doors.add(n_e);
		doors.add(n_s);
		doors.add(n_w);
		doors.add(ne_n);
		doors.add(ne_e);
		doors.add(ne_s);
		doors.add(ne_w);
		doors.add(e_n);
		doors.add(e_s);
		doors.add(e_w);
		doors.add(c_n);
		doors.add(c_e);
		doors.add(c_s);
		doors.add(c_w);

		solution = Solution.EMPTY;
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.overlayManager.remove(overlay);
		if (developerMode)
		{
			this.overlayManager.remove(debugOverlay);
		}
		solution = Solution.EMPTY;
		doors.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState().equals(GameState.LOGGING_IN) || gameStateChanged.getGameState().equals(GameState.HOPPING))
		{
			this.delayTicksUntilLevelsInitialized = 2;
			this.solution.invalidate();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		log.debug(event.getMessage());

		// Check that the chat message is a game message.
		if (event.getType() == ChatMessageType.GAMEMESSAGE)
		{
			String message = event.getMessage();
			// Check if the message contains your specific text.
			if (message.contains("catacombs moving around you"))
			{
				// Trigger a new maze solve.
				// You might want to log or do additional checks.
				log.info("Puzzle message detected; triggering new maze solve");

				delayTicksUntilLevelsInitialized = 1;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick) {
		// Get the player's current world location once.
		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		// Only proceed if the player is in the Barrows zone.
		if (!BarrowsPathPlugin.barrowsZone.contains(playerLocation)) {
			solution.invalidate();
			return;
		}

		// Decrement delay counter if needed and update door statuses when it reaches 0.
		if (delayTicksUntilLevelsInitialized > 0) {
			delayTicksUntilLevelsInitialized--;
			if (delayTicksUntilLevelsInitialized == 0) {
				log.debug("Two ticks have passed since the player logged in. Re-updating max pouch capacities (RC Level: {})");
				for (Door door : doors) {
					door.updateStatus(client);
				}
				log.debug("Solving Maze");
				solution = maze.solve(client.getLocalPlayer().getWorldLocation());
			}
		}else{
			if(solution.isInvalid()) {
				log.debug("Solving Maze");
				solution = maze.solve(client.getLocalPlayer().getWorldLocation());
			}
		}
	}

	@Provides
	BarrowsPathConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BarrowsPathConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged changedConfig)
	{
		if (changedConfig.getGroup().equals("barrowsMaze"))
		{
			if (changedConfig.getKey().equals("showDebugOverlay") && this.developerMode)
			{
				if (this.config.showDebugOverlay())
				{
					this.overlayManager.add(debugOverlay);
				}
				else
				{
					this.overlayManager.remove(debugOverlay);
				}
			}
		}
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		//if (!BarrowsPathPlugin.barrowsZone.contains(client.getLocalPlayer().getWorldLocation()))
		//	return;

		WallObject wallObject = event.getWallObject();
		Door spawnedDoor = findMatchingDoor(wallObject);
		if (spawnedDoor != null) {
			spawnedDoor.setWallObject(wallObject);
			spawnedDoor.updateStatus(client);
			solution.invalidate();
		}
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event)
	{
		//if (!BarrowsPathPlugin.barrowsZone.contains(client.getLocalPlayer().getWorldLocation()))
		//	return;
		log.debug("DESPAWNED");
		Door despawnedDoor = findMatchingDoor(event.getWallObject());
		if(despawnedDoor != null) {
			despawnedDoor.removeWallObject(event.getWallObject());
		}
	}

	private Door findMatchingDoor(WallObject wallObject) {
		// Get the target value (for example, the hash of the wall object's world location)
		int targetHash = wallObject.getWorldLocation().hashCode();

		// Iterate through the doors and check whether one of its ids equals the target
		for (Door door : doors) {
			if (door.hasId(targetHash)) {
				return door;  // found a match!
			}
		}

		// No door matched the wall object.
		return null;
	}
}
