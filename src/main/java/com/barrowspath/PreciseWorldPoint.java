package com.barrowspath;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;

@Slf4j
public class PreciseWorldPoint {
    private final double x;
    private final double y;
    private final int plane;

    public PreciseWorldPoint(double x, double y, int plane) {
        this.x = x;
        this.y = y;
        this.plane = plane;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getPlane() {
        return plane;
    }

    public LocalPoint getLocalPoint(Client client)
    {
        WorldView wv = client.getTopLevelWorldView();
        // The client's base coordinates (in tile units)
        int baseX = wv.getBaseX();
        int baseY = wv.getBaseY();

        // 'this.x' and 'this.y' are in world (tile) coordinates with fractional parts.
        // Subtract the base so that the coordinate becomes relative to the current scene,
        // then multiply by 128 (the number of local units per tile) and round.

        int localX = (int) Math.round((this.x - baseX) * 128);
        int localY = (int) Math.round((this.y - baseY) * 128);
        // Create and return a new LocalPoint with the calculated local coordinates.
        return new LocalPoint(localX, localY, wv.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreciseWorldPoint)) return false;
        PreciseWorldPoint point = (PreciseWorldPoint) o;
        return x == point.x && y == point.y && plane == point.plane;
    }

    @Override
    public String toString() {
        return "PreciseWorldPoint{" +
                "x=" + x +
                ", y=" + y +
                ", plane=" + plane +
                '}';
    }
}