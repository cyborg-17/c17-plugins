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
package net.runelite.client.plugins.cballmaker;

import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import java.time.Instant;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import static net.runelite.client.plugins.cballmaker.CballMakerState.*;



@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "c17 - CballMaker",
	enabledByDefault = false,
	description = "Makes cannonballs in Edgeville for you.",
	tags = {"Smithing", "skill", "bot",}
)
@Slf4j
public class CballMakerPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private iUtils utils;

	@Inject
	private MouseUtils mouse;

	@Inject
	private PlayerUtils playerUtils;

	@Inject
	private InventoryUtils inventory;

	@Inject
	private InterfaceUtils interfaceUtils;

	@Inject
	private CalculationUtils calc;

	@Inject
	private MenuUtils menu;

	@Inject
	private ObjectUtils object;

	@Inject
	private BankUtils bank;

	@Inject
	private NPCUtils npc;

	@Inject
	private WalkUtils walk;

	@Inject
	private CballMakerConfig config;

	@Inject
	PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	CballMakerOverlay overlay;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	CballMakerState state;
	Instant botTimer;
	MenuEntry targetMenu;
	Player player;
	List<Integer> ITEMS_TO_KEEP = new ArrayList<>();

	int timeout = 0;
	long sleepLength = 0;
	boolean startCballMaker;
	NPC bankNPC;

	@Override
	protected void startUp() {

		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown() {
		resetVals();
		chinBreakHandler.unregisterPlugin(this);
	}

	public void resetVals() {
		overlayManager.remove(overlay);
		chinBreakHandler.stopPlugin(this);
		startCballMaker = false;
		botTimer = null;
		timeout = 0;
		ITEMS_TO_KEEP = List.of(2347);
		targetMenu = null;
	}

	@Provides
	CballMakerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(CballMakerConfig.class);
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("CballMaker")) {
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		switch (configButtonClicked.getKey()) {
			case "startButton":
				if (!startCballMaker) {
					startCballMaker = true;
					chinBreakHandler.startPlugin(this);
					botTimer = Instant.now();
					state = null;
					targetMenu = null;
					timeout = 0;
					overlayManager.add(overlay);
					initVals();

				}
				else
				{
					resetVals();
				}
		}
	}


	public void initVals() {


	}



	@Subscribe
	private void onConfigChanged(ConfigChanged event) {
		if (event.getGroup() != "CballMaker") {
			return;
		}
		switch (event.getKey()) {
			case "barID":
				ITEMS_TO_KEEP = List.of(2347);
				break;
		}
	}

	private long sleepDelay() {
		sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay() {
		int tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}



	private void clickBallWidget()
	{
		Widget cballwidget = client.getWidget(WidgetInfo.MULTI_SKILL_MENU);
		if (cballwidget != null)
		{
			targetMenu = new MenuEntry("","", 1 , MenuAction.CC_OP.getId(),
					-1, 17694734,false);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-200, 200, sleepDelay());
		}
		else
		{
			utils.sendGameMessage("Widget not found.");
			startCballMaker = false;
		}
	}



	private void clickFurnace()
	{
		GameObject furnace = object.findNearestGameObject(ObjectID.FURNACE_16469);
		if (furnace !=null)
		{
			targetMenu = new MenuEntry("","", furnace.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION.getId(),
					furnace.getLocalLocation().getSceneX(), furnace.getLocalLocation().getSceneY(),false);
			menu.setEntry(targetMenu);
			mouse.delayMouseClick(furnace.getConvexHull().getBounds(),sleepDelay());
		}
		else
		{
			utils.sendGameMessage("Furnace not found.");
			startCballMaker = false;
		}
	}

	private void openBank()
	{
		bankNPC = npc.findNearestNpc(1613);
		if (npc != null)
		{
			targetMenu = new MenuEntry("", "",
					bankNPC.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0, false);
			menu.setEntry(targetMenu);
			mouse.delayMouseClick(bankNPC.getConvexHull().getBounds(), sleepDelay());
		}
		else
		{
			utils.sendGameMessage("Bank not found.");
			startCballMaker = false;
		}
	}

	private void withdrawBars()
	{
		bank.withdrawAllItem(ItemID.STEEL_BAR);
	}

	private void withdrawMould()
	{

		bank.withdrawAllItem(ItemID.AMMO_MOULD);
	}

	private void depositcBalls()
	{
		bank.depositAllOfItem(ItemID.CANNONBALL);
	}


	public CballMakerState getState()
	{
		if (timeout > 0)
		{
			playerUtils.handleRun(20, 30);
			return TIMEOUT;
		}

		if (player.getPoseAnimation() == 819 || (player.getPoseAnimation() == 824) ||
				(player.getPoseAnimation() == 1205) || (player.getPoseAnimation() == 1210))
		{
			return MOVING;
		}

		if (player.getAnimation() == 899 || player.getAnimation() == 827) {
			return SMELTING;
		}

		if (bank.isOpen())
		{
			if (!inventory.containsItem(ItemID.AMMO_MOULD))

			{
				return WITHDRAW_MOULD;
			}
			if (!bank.containsAnyOf(ItemID.STEEL_BAR) && !inventory.containsItem(ItemID.STEEL_BAR))
			{
				return OUT_OF_BARS;
			}
			if (inventory.containsItem(ItemID.CANNONBALL))
			{
				return DEPOSIT_BALLS;
			}
			if (chinBreakHandler.shouldBreak(this) && !inventory.containsItem(ItemID.CANNONBALL))
			{
				return HANDLE_BREAK;
			}

			if (bank.containsAnyOf(ItemID.STEEL_BAR) && !inventory.isFull())
			{
				return WITHDRAW_BAR;
			}
			if (inventory.containsItem(ItemID.STEEL_BAR))
			{
				return CLOSE_BANK;
			}
		}

		Widget cballwidget = client.getWidget(WidgetInfo.MULTI_SKILL_MENU);
		if (cballwidget != null)
		{
			return CLICK_BALL_WIDGET;
		}

		if (inventory.containsItem(ItemID.STEEL_BAR) && client.getLocalPlayer().getWorldLocation().getX() == 3098 &&
				client.getLocalPlayer().getWorldLocation().getY() == 3494)
		{
			return FURNACE;
		}

		Widget lvlup = client.getWidget(WidgetInfo.LEVEL_UP_SKILL);
		if (lvlup !=null)
		{
			if (inventory.containsItem(ItemID.STEEL_BAR))
			{
				return FURNACE;
			}
			else
			{
				return OPEN_BANK;
			}
		}


		if ((player.getPoseAnimation() == 813 || (player.getPoseAnimation() == 808)) && (inventory.isEmpty() || !inventory.containsItem(ItemID.STEEL_BAR)))
		{
			return OPEN_BANK;
		}

		return IDLING;
	}


	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (!startCballMaker || chinBreakHandler.isBreakActive(this))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
			if (!client.isResized()) {
				utils.sendGameMessage("c17 - client must be set to resizable");
				startCballMaker = false;
				return;
			}
			playerUtils.handleRun(40, 20);
			state = getState();
			switch (state)
			{
				case TIMEOUT:
					timeout--;
					break;
				case ITERATING:
					break;
				case IDLING:
					timeout = 1;
					break;
				case MOVING:
					timeout = 1;
					break;
				case SMELTING:
					timeout = 1;
					break;
				case OPEN_BANK:
					openBank();
					timeout = 0 +tickDelay();
					break;
				case CLOSE_BANK:
					bank.close();
					timeout = 0 +tickDelay();
					break;
				case DEPOSIT_BALLS:
					depositcBalls();
					timeout = 0 +tickDelay();
					break;
				case WITHDRAW_BAR:
					withdrawBars();
					timeout = 0 +tickDelay();
					break;
				case WITHDRAW_MOULD:
					withdrawMould();
					timeout = 0 +tickDelay();
					break;
				case FURNACE:
					clickFurnace();
					timeout = 0 +tickDelay();
					break;
				case CLICK_BALL_WIDGET:
					clickBallWidget();
					timeout = 0 +tickDelay();
					break;
				case OUT_OF_BARS:
					if (config.logout())
					{
						interfaceUtils.logout();
					}
					startCballMaker = false;
					resetVals();
					break;
				case HANDLE_BREAK:
					chinBreakHandler.startBreak(this);
					timeout = 8;
			}
		}
	}


		@Subscribe
		private void onGameStateChanged (GameStateChanged event)
		{
			if (!startCballMaker) {
				return;
			}
			if (event.getGameState() == GameState.LOGGED_IN) {
				state = IDLING;
				timeout = 2;
			}
		}
	}