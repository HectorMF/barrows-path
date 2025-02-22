
package com.barrowspath;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.geometry.Shapes;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import net.runelite.api.coords.LocalPoint;
import java.awt.geom.Line2D;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.List;
import java.util.Set;

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
    public Dimension render(Graphics2D graphics)
    {
        Player player = client.getLocalPlayer();
        WorldPoint playerLocation = player.getWorldLocation();

        if (BarrowsPathPlugin.barrowsZone.contains(playerLocation))
        {
            OverlayUtil.renderPolygon(graphics, QuestPerspective.getZonePoly(client, BarrowsPathPlugin.barrowsZone), Color.red);
            plugin.getMaze().drawZones(graphics, client);

            // Render the current room's name under the player
            Room currentRoom = plugin.getMaze().getRoom(playerLocation);
            if (currentRoom != null) {
                OverlayUtil.renderTextLocation(graphics, player.getCanvasTextLocation(graphics, currentRoom.getName(), 0), currentRoom.getName(), Color.white);
            }

            // Render the tile's world position on the curser
            LocalPoint p = client.getSelectedSceneTile().getLocalLocation();
            OverlayUtil.renderTextLocation(graphics, client.getMouseCanvasPosition() , String.valueOf(client.getSelectedSceneTile().getWorldLocation()) + " : " + String.valueOf(p), Color.red);

        }
        return null;
    }

    public double getDistanceToWall(WallObject wallObject)
    {
        if (wallObject == null || client.getLocalPlayer() == null)
        {
            return -1;
        }

        LocalPoint wallLocation = wallObject.getLocalLocation();
        LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();

        if (wallLocation == null || playerLocation == null)
        {
            return -1;
        }

        int dx = wallLocation.getX() - playerLocation.getX();
        int dy = wallLocation.getY() - playerLocation.getY();

        // Euclidean distance in local units (each tile = 128 units)
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static Line2D.Double getWorldLines(Client client, LocalPoint startLocation, LocalPoint endLocation)
    {
        final int plane = client.getPlane();

        final int startX = startLocation.getX();
        final int startY = startLocation.getY();
        final int endX = endLocation.getX();
        final int endY = endLocation.getY();

        final int sceneX = startLocation.getSceneX();
        final int sceneY = startLocation.getSceneY();

        if (sceneX < 0 || sceneY < 0 || sceneX >= Constants.SCENE_SIZE || sceneY >= Constants.SCENE_SIZE)
        {
            return null;
        }

        final int startHeight = Perspective.getTileHeight(client, startLocation, plane);
        final int endHeight = Perspective.getTileHeight(client, endLocation, plane);

        Point p1 = Perspective.localToCanvas(client, startX, startY, startHeight);
        Point p2 = Perspective.localToCanvas(client, endX, endY, endHeight);

        if (p1 == null || p2 == null)
        {
            return null;
        }

        return new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public static void drawLinesOnWorld(Graphics2D graphics, Client client, List<WorldPoint> linePoints,
                                        Color color)
    {
        for (int i = 0; i < linePoints.size() - 1; i++)
        {
            WorldPoint startWp = linePoints.get(i);
            WorldPoint endWp = linePoints.get(i+1);

            if (startWp == null || endWp == null) continue;
            if (startWp.equals(new WorldPoint(0, 0, 0))) continue;
            if (endWp.equals(new WorldPoint(0, 0, 0))) continue;
            if (startWp.getPlane() != endWp.getPlane()) continue;
            LocalPoint startLp = QuestPerspective.getInstanceLocalPointFromReal(client, startWp);
            LocalPoint endLp = QuestPerspective.getInstanceLocalPointFromReal(client, endWp);
            if (startLp == null && endLp == null)
            {
                continue;
            }

            int MAX_LP = 13056;

            if (endLp == null)
            {
                // Work out point of intersection of loaded area
                int xDiff = endWp.getX() - startWp.getX();
                int yDiff = endWp.getY() - startWp.getY();

                int changeToGetXToBorder;
                if (xDiff != 0)
                {
                    int goalLine = 0;
                    if (xDiff > 0) goalLine = MAX_LP;
                    changeToGetXToBorder = (goalLine - startLp.getX()) / xDiff;
                }
                else
                {
                    changeToGetXToBorder = Integer.MAX_VALUE;
                }
                int changeToGetYToBorder;
                if (yDiff != 0)
                {
                    int goalLine = 0;
                    if (yDiff > 0) goalLine = MAX_LP;
                    changeToGetYToBorder =(goalLine - startLp.getY()) / yDiff;
                }
                else
                {
                    changeToGetYToBorder = Integer.MAX_VALUE;
                }
                if (Math.abs(changeToGetXToBorder) < Math.abs(changeToGetYToBorder))
                {
                    endLp = new LocalPoint(startLp.getX() + (xDiff * changeToGetXToBorder), startLp.getY() + (yDiff * changeToGetXToBorder));
                }
                else
                {
                    endLp = new LocalPoint(startLp.getX() + (xDiff * changeToGetYToBorder), startLp.getY() + (yDiff * changeToGetYToBorder));
                }
            }

            if (startLp == null)
            {
                // Work out point of intersection of loaded area
                int xDiff = startWp.getX() - endWp.getX();
                int yDiff = startWp.getY() - endWp.getY();

                // if diff negative, go to 0?
                int changeToGetXToBorder;
                if (xDiff != 0)
                {
                    int goalLine = 0;
                    if (xDiff > 0) goalLine = MAX_LP;
                    changeToGetXToBorder = (goalLine - endLp.getX()) / xDiff;
                }
                else
                {
                    changeToGetXToBorder = 1000000000;
                }
                int changeToGetYToBorder;
                if (yDiff != 0)
                {
                    int goalLine = 0;
                    if (yDiff > 0) goalLine = MAX_LP;
                    changeToGetYToBorder = (goalLine - endLp.getY()) / yDiff;
                }
                else
                {
                    changeToGetYToBorder = 1000000000;
                }

                if (Math.abs(changeToGetXToBorder) < Math.abs(changeToGetYToBorder))
                {
                    startLp = new LocalPoint(endLp.getX() + (xDiff * changeToGetXToBorder), endLp.getY() + (yDiff * changeToGetXToBorder));
                }
                else
                {
                    startLp = new LocalPoint(endLp.getX() + (xDiff * changeToGetYToBorder), endLp.getY() + (yDiff * changeToGetYToBorder));
                }
            }

            // If one is in scene, find local point we intersect with

            Line2D.Double newLine = getWorldLines(client, startLp, endLp);
            if (newLine != null)
            {
                OverlayUtil.renderPolygon(graphics, newLine, color);
            }
        }
    }

    public void drawLines(Graphics2D graphics)
    {
        if (client.getLocalPlayer() == null)
        {
            return;
        }
        List<WorldPoint> linePoints = List.of(
                client.getLocalPlayer().getWorldLocation(),
                //new WorldPoint(3573, 9673, 0),
                new WorldPoint(3564, 9682, 0)
        );

        if (linePoints != null && linePoints.size() > 1)
        {
            drawLinesOnWorld(graphics, client, linePoints, Color.white);
        }
    }

}