/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.herbcleaner;

import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import java.util.concurrent.TimeUnit;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.GameTick;
import java.time.Instant;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Player;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import static net.runelite.client.plugins.herbcleaner.HerbCleanerState.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import net.runelite.api.NPC;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "c17 - Herb cleaner",
	enabledByDefault = false,
	description = "Cleans herbs for you",
	tags = {"Clean", "Herb", "Bot"},
	type = PluginType.SKILLING
)
@Slf4j
public class HerbCleanerPlugin extends Plugin {


	@Inject
	private Client client;

	@Inject
	private BotUtils utils;

	@Inject
	private HerbCleanerConfig config;

	@Inject
	PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	HerbCleanerOverlay overlay;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	@Inject
	ExecutorService executorService;


	HerbCleanerState state;
	Instant botTimer;
	MenuEntry targetMenu;
	Player player;


	int bankerID = -1;
	int grimyherbID = -1;
	int opCODE = -1;
	int timeout = 0;
	long sleepLength = 0;
	boolean startHerbCleaner;
	boolean busy_cleaning;
	private final Set<Integer> grimyherb = new HashSet<>();
	private final Set<Integer> cleanherbIDS = new HashSet<>();


	@Override
	protected void startUp() {
		bankerID = config.bankerID();
		grimyherbID = config.grimyherbID();
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown() {
		resetVals();
		chinBreakHandler.unregisterPlugin(this);
	}

	@Provides
	HerbCleanerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(HerbCleanerConfig.class);
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("HerbCleaner")) {
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		switch (configButtonClicked.getKey()) {
			case "startButton":
				if (!startHerbCleaner) {
					startHerbCleaner = true;
					chinBreakHandler.startPlugin(this);
					botTimer = Instant.now();
					state = null;
					targetMenu = null;
					botTimer = Instant.now();
					initVals();
					overlayManager.add(overlay);
					bankerID = config.bankerID();
					grimyherbID = config.grimyherbID();
					cleanherbIDS.addAll(utils.stringToIntList(",265,267,249,255,259,263,2481,201,257,253,2998"));
					busy_cleaning = false;
				} else {
					resetVals();
				}
				break;
		}
	}


	public void initVals() {
		bankerID = config.bankerID();
		opCODE = config.opCODE();
		grimyherbID = config.grimyherbID();
		cleanherbIDS.addAll(utils.stringToIntList("249,251,253,255,257,2998,259,261,263,3000,265,2481,267,269"));
		busy_cleaning = false;
	}

	public void resetVals() {
		overlayManager.remove(overlay);
		chinBreakHandler.stopPlugin(this);
		startHerbCleaner = false;
		botTimer = null;
		bankerID = -1;
		grimyherbID = -1;
		opCODE = -1;
		grimyherb.clear();
		timeout = 0;
		busy_cleaning = false;

	}


	@Subscribe
	private void onConfigChanged(ConfigChanged event) {
		if (event.getGroup() != "HerbCleaner") {
			return;
		}
		switch (event.getKey()) {
			case "bankerID":
				bankerID = config.bankerID();
				break;
			case "grimyherbID":
				grimyherb.clear();
				grimyherbID = config.grimyherbID();
				break;
			case "opCODE":
				opCODE = config.opCODE();
				break;
		}
	}

	private void cleanHerbs() {
		busy_cleaning = true;
		utils.inventoryItemsInteract(utils.stringToIntList("" + config.grimyherbID()), opCODE, false, true, config.sleepMin(), config.sleepMax());
	}

	private void openBank() {
		NPC banker = utils.findNearestNpc(bankerID);
		if (banker != null) {
			targetMenu = new MenuEntry("", "",
					banker.getIndex(), MenuOpcode.NPC_THIRD_OPTION.getId(), 0, 0, false);
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(banker.getConvexHull().getBounds(), sleepDelay());
		} else {
			utils.sendGameMessage("Bank not found, stopping");
			startHerbCleaner = false;
		}
	}

	private void withdrawHerb() {
		utils.withdrawAllItem(grimyherbID);
	}

	private void deposit() {
		utils.depositAllOfItems(cleanherbIDS);
	}


	public HerbCleanerState getState()
	{
		if (timeout > 0)
		{
			utils.handleRun(20, 20);
			return TIMEOUT;
		}
		Widget lvlup = client.getWidget(WidgetInfo.LEVEL_UP_SKILL);
		if (lvlup !=null)
		{
			if (utils.inventoryContains(grimyherbID))
			{
				return CLEANING_HERBS;
			}
			else
			{
				return OPEN_BANK;
			}
		}
		if (utils.isBankOpen())
		{
			if (!utils.bankContainsAnyOf(grimyherbID) && !utils.inventoryContains(grimyherbID))
			{
				return OUT_OF_GRIMY_HERBS;
			}
			if (utils.inventoryFull() && !utils.inventoryContains(grimyherbID))
			{
				return DEPOSIT;
			}
			if (!utils.inventoryFull() && !utils.inventoryContains(grimyherbID))
			{
				return WITHDRAWING_GRIMY_HERBS;
			} else
				{
				return CLOSE_BANK;
			}

		}
		if (chinBreakHandler.shouldBreak(this))
		{
			return HANDLE_BREAK;
		}
		if (utils.inventoryContains(grimyherbID) && !utils.isBankOpen())
		{
			return CLEANING_HERBS;
		}
		else
		{
			return OPEN_BANK;
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (busy_cleaning && utils.inventoryContains(grimyherbID))
		{
			return;
		}
		else
		{
			busy_cleaning = false;
		}
		if (!startHerbCleaner || chinBreakHandler.isBreakActive(this))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
			if (!client.isResized())
			{
				utils.sendGameMessage("c17 - client must be set to resizable");
				startHerbCleaner = false;
				return;
			}
			state = getState();
			log.debug(state.name());
			switch (state)
			{
				case TIMEOUT:
					timeout--;
					break;
				case ITERATING:
					break;
				case WITHDRAWING_GRIMY_HERBS:
					withdrawHerb();
					timeout = 0 + tickDelay();
					break;
				case CLOSE_BANK:
					utils.closeBank();
					timeout = 0 + tickDelay();
					break;
				case CLEANING_HERBS:
					cleanHerbs();
					timeout = 0 + tickDelay();
					break;
				case DEPOSIT:
					deposit();
					timeout = 0 + tickDelay();
					break;
				case OPEN_BANK:
					openBank();
					timeout = 0 + tickDelay();
					break;
				case HANDLE_BREAK:
					chinBreakHandler.startBreak(this);
					timeout = 5;
					break;
				case OUT_OF_GRIMY_HERBS:
					startHerbCleaner = false;
					if (config.logout())
					{
					utils.logout();
					}
				resetVals();
				break;
			}
		}
	}


	private long sleepDelay()
	{
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}


	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (!startHerbCleaner || event.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		state = TIMEOUT;
		timeout = 2;
	}
}