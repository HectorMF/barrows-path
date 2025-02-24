
package com.barrows.path;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.WallObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

import java.awt.*;

import javax.inject.Inject;
import java.util.Set;

@Slf4j
class BarrowsPathOverlay extends Overlay
{
    private final Client client;
    private final BarrowsPathPlugin plugin;
    private final BarrowsPathConfig config;
    @Inject
    private BarrowsPathOverlay(Client client, BarrowsPathPlugin plugin, BarrowsPathConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (BarrowsPathPlugin.tunnels.contains(client.getLocalPlayer().getWorldLocation()))
        {
            Solution solution = plugin.getSolution();

            if(solution == null) return null;

            int alpha = 255;

            for (int i = 0, n = Math.min(3, solution.getDoors().size()); i < n; i++) {
                Door door = solution.getDoors().get(i);
                Set<WallObject> wallObjects = door.getWallObjects();

                for(WallObject wallObject : wallObjects)
                {
                    Shape clickbox = wallObject.getClickbox();

                    if (clickbox == null)
                    {
                        continue;
                    }

                    Color color = ColorUtil.colorWithAlpha(config.doorColor(), alpha);
                    graphics.setColor(color);
                    graphics.draw(clickbox);
                    graphics.setColor(ColorUtil.colorWithAlpha(color, color.getAlpha() / 5));
                    graphics.fill(clickbox);
                }

                alpha /= 3;
            }
        }
        return null;
    }
}