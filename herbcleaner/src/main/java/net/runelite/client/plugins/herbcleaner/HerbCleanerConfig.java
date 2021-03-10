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

import java.time.Instant;
import java.util.function.Consumer;
import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("HerbCleaner")
public interface HerbCleanerConfig extends Config
{
	@ConfigItem(
		keyName = "bankerID",
		name = "BANK NPC ID",
		description = "Provide ID of the BANK you want to use",
		position = 1
	)
	default int bankerID()	{ return 1634;	}

	@ConfigItem(
		keyName = "grimyherbID",
		name = "Grimy Herb ID",
		description = "Provide ID of Grimy hearb you want to clean",
		position = 2
	)
	default int grimyherbID()	{ return 0;	}

	@ConfigItem(
			keyName = "opCode",
			name = "Op Code ID",
			description = "Provide Op code to use",
			position = 3
	)
	default int opCODE()	{ return 33;	}


	@ConfigSection(
		keyName = "delayConfig",
		name = "Sleep Delay Configuration",
		description = "Configure how the bot handles sleep delays",
		position = 5
	)
	String delayConfig = "delayConfig";

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepMin",
		name = "Sleep Min",
		description = "",
		position = 6,
		section = "delayConfig"
	)
	default int sleepMin()
	{
		return 60;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepMax",
		name = "Sleep Max",
		description = "",
		position = 7,
		section = "delayConfig"
	)
	default int sleepMax()
	{
		return 350;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepTarget",
		name = "Sleep Target",
		description = "",
		position = 8,
		section = "delayConfig"
	)
	default int sleepTarget()
	{
		return 100;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepDeviation",
		name = "Sleep Deviation",
		description = "",
		position = 9,
		section = "delayConfig"
	)
	default int sleepDeviation()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "sleepWeightedDistribution",
		name = "Sleep Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 10,
		section = "delayConfig"
	)
	default boolean sleepWeightedDistribution()
	{
		return false;
	}

	@ConfigSection(
		keyName = "delayTickConfig",
		name = "Game Tick Configuration",
		description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
		position = 11
	)
	String delayTickConfig = "delayTickConfig";

	@Range(
		min = 0,
		max = 25
	)
	@ConfigItem(
		keyName = "tickDelayMin",
		name = "Game Tick Min",
		description = "",
		position = 12,
		section = "delayTickConfig"
	)
	default int tickDelayMin()
	{
		return 1;
	}

	@Range(
		min = 0,
		max = 30
	)
	@ConfigItem(
		keyName = "tickDelayMax",
		name = "Game Tick Max",
		description = "",
		position = 13,
		section = "delayTickConfig"
	)
	default int tickDelayMax()
	{
		return 3;
	}

	@Range(
		min = 0,
		max = 30
	)
	@ConfigItem(
		keyName = "tickDelayTarget",
		name = "Game Tick Target",
		description = "",
		position = 14,
		section = "delayTickConfig"
	)
	default int tickDelayTarget()
	{
		return 2;
	}

	@Range(
		min = 0,
		max = 30
	)
	@ConfigItem(
		keyName = "tickDelayDeviation",
		name = "Game Tick Deviation",
		description = "",
		position = 15,
		section = "delayTickConfig"
	)
	default int tickDelayDeviation()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "tickDelayWeightedDistribution",
		name = "Game Tick Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 16,
		section = "delayTickConfig"
	)
	default boolean tickDelayWeightedDistribution()
	{
		return false;
	}

	@ConfigItem(
			keyName = "logout",
			name = "Logout when out of herbs",
			description = "Enable to logout when out of herbs.",
			position = 24
	)
	default boolean logout() { return true; }

	@ConfigItem(
		keyName = "enableUI",
		name = "Enable UI",
		description = "Enable to turn on in game UI",
		position = 25
	)
	default boolean enableUI()
	{
		return true;
	}

	@ConfigItem(
		keyName = "startButton",
		name = "Start/Stop",
		description = "Start or stop the bot",
		position = 30
	)
	default Button startButton()
	{
		return null;
	}
}
