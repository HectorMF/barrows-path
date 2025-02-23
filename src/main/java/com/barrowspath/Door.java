package com.barrowspath;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;

import java.util.*;

@Slf4j
public class Door {
    private boolean isOpen;
    private final int id1;
    private final int id2;
    private WallObject wallObject1;
    private WallObject wallObject2;
    private final Set<Room> connectedRooms = new LinkedHashSet<>();

    public Door(int id1, int id2, Room room1, Room room2) {
        this.id1 = id1;
        this.id2 = id2;
        connectRoom(room1);
        connectRoom(room2);
    }

    protected void connectRoom(Room room) {
        connectedRooms.add(room);
        room.addDoor(this);
    }

    // Given a room, return the other rooms connected by this door.
    public Set<Room> getOtherRooms(Room current) {
        Set<Room> others = new HashSet<>(connectedRooms);
        others.remove(current);
        return others;
    }

    public boolean hasId(int id) {
        return id == id1 || id == id2;
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

    public Set<WallObject> getWallObjects() {
        Set<WallObject> wallObjects = new HashSet<>();
        if (wallObject1 != null) {
            wallObjects.add(wallObject1);
        }
        if (wallObject2 != null) {
            wallObjects.add(wallObject2);
        }
        return wallObjects;
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
