
package com.barrowspath;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Room {
    @Getter
    private final String name;
    @Getter
    private final Color color;
    @Getter
    private final int length;
    @Getter
    private final Zone zone;

    private final Set<Door> doors = new HashSet<>();

    public Room(String name, WorldPoint point1, WorldPoint point2, Integer length) {
        this.name = name;
        this.zone = new Zone(point1, point2);
        this.length = length;

        Random random = new Random();

        this.color = Color.getHSBColor(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }

    public void addDoor(Door door) {
        doors.add(door);
    }

    public Set<Door> getDoors() {
        return Collections.unmodifiableSet(doors);
    }

    public boolean contains(WorldPoint point) {
        return zone.contains(point);
    }
}