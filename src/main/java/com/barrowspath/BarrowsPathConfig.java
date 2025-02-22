package com.barrowspath;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("barrowsMaze")
public interface BarrowsPathConfig extends Config
{
	@ConfigItem(
			keyName = "doorColor",
			name = "Correct door color",
			description = "Change the highlight color of correct door to take.",
			position = 3
	)
	default Color doorColor()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
			keyName = "pathColor",
			name = "Path color",
			description = "Change the color of the solution path.",
			position = 5
	)
	default Color pathColor()
	{
		return new Color(0, 255, 0, 50);
	}

	@ConfigItem(
			keyName = "showDebugOverlay",
			name = "Show Debug Overlay",
			description = "Shows the debug overlay",
			hidden = false,
			position = Integer.MAX_VALUE
	)
	default boolean showDebugOverlay()
	{
		return false;
	}
}
