
package com.barrowspath;

import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Room {
    private final String name;
    private final Zone zone;
    private final int length;
    private final Set<Door> doors = new HashSet<>();
    private final Color color;

    public Room(String name, WorldPoint point1, WorldPoint point2, Integer length) {
        this.name = name;
        this.zone = new Zone(point1, point2);
        this.length = length;

        Random random = new Random();

        // Generate random hue, saturation, and brightness values (each between 0.0 and 1.0)
        float hue = random.nextFloat();          // Hue: 0.0 <= hue < 1.0
        float saturation = random.nextFloat();   // Saturation: 0.0 <= saturation < 1.0
        float brightness = random.nextFloat();   // Brightness: 0.0 <= brightness < 1.0
        this.color = Color.getHSBColor(hue, saturation, brightness);
    }

    public void addDoor(Door door) {
        doors.add(door);
    }

    // Get an unmodifiable view of this room's doors.
    public Set<Door> getDoors() {
        return Collections.unmodifiableSet(doors);
    }

    public boolean contains(WorldPoint point) {
        return zone.contains(point);
    }

    public String getName() {
        return name;
    }

    public Integer getLength()
    {
        return length;
    }

    public Zone getZone()
    {
        return zone;
    }

    public Color getColor()
    {
        return color;
    }
}