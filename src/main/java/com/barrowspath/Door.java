package com.barrowspath;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

@Slf4j
public class Door {
    private final int id1;
    private final int id2;
    private final Set<Room> connectedRooms = new LinkedHashSet<>();
    private List<PreciseWorldPoint> path = new ArrayList<>();

    private WallObject wallObject1;
    private WallObject wallObject2;

    private boolean isOpen;

    public Door(int id1, int id2, Room room1, Room room2) {
        this.id1 = id1;
        this.id2 = id2;
        connectRoom(room1);
        connectRoom(room2);
        generateGeneralPathing();
    }

    public boolean hasId(int id) {
        return id == id1 || id == id2;
    }

    // Connect this door to a room.
    protected void connectRoom(Room room) {
        connectedRooms.add(room);
        // Also add this door to the room’s door list.
        room.addDoor(this);

    }

    // Get an unmodifiable set of connected rooms.
    public Set<Room> getConnectedRooms() {
        return Collections.unmodifiableSet(connectedRooms);
    }

    // Given a room, return the other rooms connected by this door.
    public Set<Room> getOtherRooms(Room current) {
        Set<Room> others = new HashSet<>(connectedRooms);
        others.remove(current);
        return others;
    }

    public int getId1() {
        return id1;
    }

    public int getId2() {
        return id2;
    }

    public void setWallObject(WallObject wallObject)
    {
        int hash = wallObject.getWorldLocation().hashCode();
        if(hash == id1)
            this.wallObject1 = wallObject;
        else if (hash == id2)
            this.wallObject2 = wallObject;
    }

    public void removeWallObject(WallObject wallObject)
    {
        int hash = wallObject.getWorldLocation().hashCode();
        if(hash == id1)
            this.wallObject1 = null;
        else if (hash == id2)
            this.wallObject2 = null;
    }

    public void drawOutline(Graphics2D graphics, Color color, Client client)
    {
        final Shape polygon1;
        final Shape polygon2;
        // final boolean isUnlockedDoor = impostor.getActions()[0] != null;
        //final Color color = isUnlockedDoor ? Color.green : Color.red;
        if(wallObject1 == null || wallObject2 == null) return;

        polygon1 = wallObject1.getClickbox();
        if (polygon1 != null)
        {
            graphics.setColor(color);
            graphics.draw(polygon1);
            graphics.setColor(ColorUtil.colorWithAlpha(color, color.getAlpha() / 5));
            graphics.fill(polygon1);

            //OverlayUtil.renderTextLocation(graphics, client.getLocalPlayer().get(), client.getLocalPlayer().getLocalLocation(), Color.white);
            //OverlayUtil.renderPolygon(graphics, polygon1, color);
            //OverlayUtil.renderTextLocation(graphics, door.getCanvasLocation() , String.valueOf(client.getSelectedSceneTile().getWorldLocation()), Color.white);
            //OverlayUtil.renderTextLocation(graphics, door.getCanvasLocation() , String.valueOf(door.getId()) + " : " +  String.valueOf(door.getWorldLocation().hashCode()), Color.white);
        }
        polygon2 = wallObject2.getClickbox();
        if (polygon2 != null)
        {
            //OverlayUtil.renderTextLocation(graphics, client.getLocalPlayer().get(), client.getLocalPlayer().getLocalLocation(), Color.white);
            graphics.setColor(color);
            graphics.draw(polygon2);
            graphics.setColor(ColorUtil.colorWithAlpha(color, color.getAlpha() / 5));
            graphics.fill(polygon2);


            //OverlayUtil.renderTextLocation(graphics, door.getCanvasLocation() , String.valueOf(door.getId()) + " : " +  String.valueOf(door.getWorldLocation().hashCode()), Color.white);
        }

        for(PreciseWorldPoint point: getPath())
        {
        //    OverlayUtil.renderTextLocation(graphics, Perspective.getCanvasTextLocation(client, graphics, point.getLocalPoint(client), point.toString(), 0), String.valueOf(point.toString() + " : " + point.getLocalPoint(client)), Color.white);
        }

        //BarrowsPathOverlay.drawLinesOnWorld3(graphics, client, getPath(), Color.BLUE);
    }

    public void updateStatus(Client client)
    {
        if(wallObject1 == null || wallObject2 == null) return;
        ObjectComposition objectComp = client.getObjectDefinition(this.wallObject1.getId());
        ObjectComposition impostor = objectComp.getImpostorIds() != null ? objectComp.getImpostor() : null;
        if (impostor != null) {
            this.isOpen = impostor.getActions()[0] != null;
        }
    }

    public boolean isOpen()
    {
        return isOpen;
    }

    public boolean connects(Room room)
    {
        return connectedRooms.contains(room);
    }

    public List<PreciseWorldPoint> getPath()
    {
        return path;
    }

    public void setPath(List<PreciseWorldPoint> list)
    {
        this.path = new ArrayList<>(list);
    }

    protected void generateGeneralPathing(){
        path = new ArrayList<>();
        for(Room room : connectedRooms)
            path.add(room.getZone().getCenter());
    }

    public WorldPoint getWorldLocation()
    {
        if(wallObject1 != null)
            return wallObject1.getWorldLocation();
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Door)) return false;
        Door door = (Door) o;
        return id1 == door.id1 && id2 == door.id2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }
}
