
package com.barrowspath;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

class BarrowsPathDebugOverlay extends Overlay
{
    private final Client client;
    private final BarrowsPathPlugin plugin;
    private final BarrowsPathConfig config;

    @Inject
    private BarrowsPathDebugOverlay(Client client, BarrowsPathPlugin plugin, BarrowsPathConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(!config.showDebugOverlay()) return null;

        Player player = client.getLocalPlayer();
        WorldPoint playerLocation = player.getWorldLocation();

        if (BarrowsPathPlugin.tunnels.contains(playerLocation)) {
            for (Room room : plugin.getMaze().getRooms()) {
                OverlayUtil.renderPolygon(graphics, room.getZone().getPolygon(client), room.getColor());
            }

            // Render the current room's name under the player
            Room currentRoom = plugin.getMaze().getRoom(playerLocation);
            if (currentRoom != null) {
                OverlayUtil.renderTextLocation(graphics,
                        player.getCanvasTextLocation(graphics, currentRoom.getName(), 0),
                        currentRoom.getName(),
                        Color.white);
            }
        }
        return null;
    }
}