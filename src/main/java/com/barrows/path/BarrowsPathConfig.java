package com.barrows.path;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("barrowsMaze")
public interface BarrowsPathConfig extends Config
{
	@ConfigItem(
			keyName = "Barrows Path",
			name = "Overlay Color",
			description = "Color of the highlighted path overlay.",
			position = 3
	)
	default Color doorColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			keyName = "showDebugOverlay",
			name = "Show Debug Overlay",
			description = "Shows debug zones",
			hidden = true,
			position = Integer.MAX_VALUE
	)
	default boolean showDebugOverlay()
	{
		return false;
	}
}
