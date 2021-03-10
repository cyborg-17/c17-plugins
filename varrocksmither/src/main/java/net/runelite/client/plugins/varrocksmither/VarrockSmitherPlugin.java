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
package net.runelite.client.plugins.varrocksmither;

import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import java.time.Instant;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
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
import static net.runelite.client.plugins.varrocksmither.VarrockSmitherState.*;



@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "c17 - VarrockSmither",
	enabledByDefault = false,
	description = "Smithing in varrock for you",
	tags = {"Smithing", "skill", "bot",}
)
@Slf4j
public class VarrockSmitherPlugin extends Plugin {
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
	private VarrockSmitherConfig config;

	@Inject
	PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	VarrockSmitherOverlay overlay;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	Items selectedItem;
	VarrockSmitherState state;
	Instant botTimer;
	MenuEntry targetMenu;
	Player player;
	GameObject anvil;
	List<Integer> ITEMS_TO_KEEP = new ArrayList<>();

	int npcID = -1;
	int barID = -1;
	int timeout = 0;
	int failureCount = 0;
	int bars_per_item = 1;
	long sleepLength = 0;
	boolean startVarrockSmither;
	boolean can_smith;
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
		startVarrockSmither = false;
		selectedItem = null;
		botTimer = null;
		failureCount = 0;
		npcID = -1;
		barID = -1;
		timeout = 0;
		can_smith = false;
		bars_per_item = 1;
		ITEMS_TO_KEEP = List.of(2347);
		targetMenu = null;
	}

	@Provides
	VarrockSmitherConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(VarrockSmitherConfig.class);
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("VarrockSmither")) {
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		switch (configButtonClicked.getKey()) {
			case "startButton":
				if (!startVarrockSmither) {
					startVarrockSmither = true;
				//	chinBreakHandler.startPlugin(this);
					botTimer = Instant.now();
					state = null;
					targetMenu = null;
					timeout = 0;
					botTimer = Instant.now();
					initVals();
					overlayManager.add(overlay);
					can_smith = false;
					bars_per_item = 1;
					ITEMS_TO_KEEP = List.of(2347, barID);

				}
				else
				{
					resetVals();
				}
		}
	}


	public void initVals() {
		selectedItem = config.getItem();
		barID = config.barID();
		can_smith = false;
		bars_per_item = 1;
	}



	@Subscribe
	private void onConfigChanged(ConfigChanged event) {
		if (event.getGroup() != "VarrockSmither") {
			return;
		}
		switch (event.getKey()) {
			case "barID":
				barID = config.barID();
				ITEMS_TO_KEEP = List.of(2347, barID);
				break;
			case "getItem":
				selectedItem = config.getItem();
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


	private void clicktoSmith() {
		Widget smithingwidget = client.getWidget(WidgetInfo.SMITHING_INVENTORY_ITEMS_CONTAINER);
		if (smithingwidget != null) {
			bars_per_item = getRequiredBarCount(selectedItem.getItem());
			Widget smithingwidgetitem = client.getWidget(selectedItem.getItem());
			if (smithingwidgetitem != null) {
				targetMenu = new MenuEntry("", "",
						1, MenuAction.CC_OP.getId(), -1, selectedItem.getItem().getId(), false);
				menu.setEntry(targetMenu);
				mouse.delayMouseClick(smithingwidgetitem.getBounds(), sleepDelay());
			}
		} else {
			utils.sendGameMessage("Smithing UI not found");
			startVarrockSmither = false;
		}
	}



	private void openBank()
	{
		bankNPC = npc.findNearestNpc(2897);
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
			startVarrockSmither = false;
		}
	}


	private void withdraw()
	{
		bank.withdrawAllItem(barID);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		if (client.getLocalPlayer().getWorldLocation().getX() == (3188) &&
				(client.getLocalPlayer().getWorldLocation().getY() == (3427))) {
			if (inventory.getItemCount(barID, false) >= bars_per_item)
			{
				can_smith = true;
			}
			else
				{
				can_smith = false;
			}
		}
	}


	private int getRequiredBarCount(WidgetInfo wi) {
		int result = -1;
		Widget w = client.getWidget(wi);
		if (w != null) {
			result = Character.getNumericValue(w.getChild(1).getText().charAt(0));
		}
		return result;
	}


	public VarrockSmitherState getState()
	{
		if (timeout > 0)
		{
			return TIMEOUT;
		}

		if (client.getLocalPlayer().getWorldLocation().getX() == (3185) &&
			(client.getLocalPlayer().getWorldLocation().getY() == (3436) && !bank.isOpen() &&
					(client.getVarpValue(173) == 0) && (client.getEnergy() > 20)))
		{
			return RUN;
		}

		if (!can_smith && client.getLocalPlayer().getWorldLocation().getX() == (3185) &&
				(client.getLocalPlayer().getWorldLocation().getY() == (3436) && !bank.isOpen()))
		{
			can_smith = true;
			return BANKING;
		}
		if (bank.isOpen() && client.getLocalPlayer().getPoseAnimation() == 808 || client.getLocalPlayer().getPoseAnimation() == 813)
		{
			if (client.getWidget(WidgetInfo.BANK_CONTAINER) == null)
			{
				return TIMEOUT;
			}
			if (!bank.containsAnyOf(barID))
			{
				return OUT_OF_BARS;
			}
			if (inventory.containsExcept(ITEMS_TO_KEEP))
			{
				return DEPOSIT_ALL_EXCEPT;
			}
			if (!inventory.containsExcept(ITEMS_TO_KEEP) && bank.contains(barID, 1) && !inventory.isFull())
			{
				return WITHDRAW;
			}
			if (!bank.containsAnyOf(barID) && !inventory.containsItem(barID))
			{
				return OUT_OF_BARS;
			}
			if (inventory.containsItemAmount(config.barID(),27,false,true))
			{
				can_smith = true;
				return CLOSE_BANK;
			}

		}
	//	if (chinBreakHandler.shouldBreak(this) &&(client.getLocalPlayer().getWorldLocation().getX() == (3185) &&
	//		(client.getLocalPlayer().getWorldLocation().getY() == (3436))))
	//	{
	//		return HANDLE_BREAK;
	//	}
		if (!bank.isOpen() && can_smith && (client.getLocalPlayer().getWorldLocation().getX() == (3185) &&
				(client.getLocalPlayer().getWorldLocation().getY() == (3436) && inventory.getItemCount(barID, false) == 27)))
		{
			return SMITHING;
		}
		Widget smithingwidget = client.getWidget(WidgetInfo.SMITHING_INVENTORY_ITEMS_CONTAINER);
		if (smithingwidget !=null)
		{
			return SMITH;
		}
		Widget lvlup = client.getWidget(WidgetInfo.LEVEL_UP_SKILL);
		if (lvlup !=null && (inventory.getItemCount(barID, false) >= bars_per_item))
		{
			return SMITHING;
		}
		if (lvlup !=null || lvlup ==null && (inventory.getItemCount(barID, false) < bars_per_item) &&
				(client.getLocalPlayer().getWorldLocation().getX() == (3188) &&
						(client.getLocalPlayer().getWorldLocation().getY() == (3427))))
		{
			return WALK;
		}

		Widget chat = client.getWidget(WidgetInfo.DIALOG_NOTIFICATION_TEXT);
		if (chat !=null)
		{
			return WALK;
		}
		return IDLING;
	}


	@Subscribe
	private void onGameTick(GameTick tick) {
		if (!startVarrockSmither || chinBreakHandler.isBreakActive(this)) {
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
			if (!client.isResized()) {
				utils.sendGameMessage("c17 - client must be set to resizable");
				startVarrockSmither = false;
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
				case IS_SMITHING:
					break;
				case SMITHING:
					anvil = object.findNearestGameObject(2097);
					targetMenu = new MenuEntry("", "", anvil.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
							anvil.getSceneMinLocation().getX(), anvil.getSceneMinLocation().getY(), false);
					menu.setEntry(targetMenu);
					mouse.delayMouseClick(anvil.getConvexHull().getBounds(), sleepDelay());
					timeout = 1 +tickDelay();
					break;
				case RUN:
					targetMenu = new MenuEntry("", "", 1, 57, -1, 10485782, false);
					menu.setEntry(targetMenu);
					mouse.delayClickRandomPointCenter(50,50,sleepDelay());
					timeout = 0 +tickDelay();
					break;
				case BANKING:
					openBank();
					timeout = 0 +tickDelay();
					break;
				case WALK:
					walk.sceneWalk(new WorldPoint(3185, 3436, 0), 0, sleepDelay());
					timeout = 0+ tickDelay();
					break;
				case DEPOSIT_ALL_EXCEPT:
					bank.depositAllExcept(ITEMS_TO_KEEP);
					timeout = 0 +tickDelay();
					break;
				case WITHDRAW:
					withdraw();
					timeout = 0 +tickDelay();
					break;
				case CLOSE_BANK:
					bank.close();
					timeout = 0 + tickDelay();
					break;
				case SMITH:
					clicktoSmith();
					timeout = 0 + tickDelay();
					break;
				case OUT_OF_BARS:
					if (config.logout())
					{
						interfaceUtils.logout();
					}
					startVarrockSmither = false;
					resetVals();
					break;
			//	case HANDLE_BREAK:
			//		chinBreakHandler.startBreak(this);
			//		timeout = 8;
			}
		}
	}


		@Subscribe
		private void onGameStateChanged (GameStateChanged event)
		{
			if (!startVarrockSmither) {
				return;
			}
			if (event.getGameState() == GameState.LOGGED_IN) {
				state = IDLING;
				timeout = 2;
			}
		}
	}