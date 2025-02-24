/*
 * Copyright (c) 2019, Trevor <https://github.com/Trevor159>
 * All rights reserved.
 *
 * Copyright (c) 2021, Zoinkwiz <https://github.com/Zoinkwiz>
 * All rights reserved. (getPolygon, addToPoly)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.barrows.path;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.awt.Polygon;

public class Zone
{
    @Getter
    private final int minX;
    @Getter
    private final int maxX;
    @Getter
    private final int minY;
    @Getter
    private final int maxY;
    private int minPlane = 0;
    private int maxPlane = 2;

    // Constants for polygon corner indices
    private static final int SW = 0;
    private static final int NW = 3;
    private static final int NE = 2;
    private static final int SE = 1;

    public Zone(WorldPoint p1, WorldPoint p2)
    {
        assert(p1 != null);
        assert(p2 != null);
        minX = Math.min(p1.getX(), p2.getX());
        maxX = Math.max(p1.getX(), p2.getX());
        minY = Math.min(p1.getY(), p2.getY());
        maxY = Math.max(p1.getY(), p2.getY());
        minPlane = Math.min(p1.getPlane(), p2.getPlane());
        maxPlane = Math.max(p1.getPlane(), p2.getPlane());
    }

    public boolean contains(WorldPoint worldPoint)
    {
        return minX <= worldPoint.getX()
                && worldPoint.getX() <= maxX
                && minY <= worldPoint.getY()
                && worldPoint.getY() <= maxY
                && minPlane <= worldPoint.getPlane()
                && worldPoint.getPlane() <= maxPlane;
    }

    public WorldPoint getMinWorldPoint()
    {
        return new WorldPoint(minX, minY, minPlane);
    }

    public Polygon getPolygon(Client client)
    {
        Polygon areaPoly = new Polygon();
        if (client == null)
        {
            return areaPoly;
        }
        // Use the plane from the zone's minimum world point.
        int plane = getMinWorldPoint().getPlane();

        // Top edge: from minX (inclusive) to maxX (exclusive)
        for (int x = minX; x < maxX; x++)
        {
            addToPoly(client, areaPoly, new WorldPoint(x, maxY, plane), NW);
        }

        // NE corner of the zone
        addToPoly(client, areaPoly, new WorldPoint(maxX, maxY, plane), NW, NE, SE);

        // Right edge: from maxY - 1 down to minY + 1 (exclusive minY)
        for (int y = maxY - 1; y > minY; y--)
        {
            addToPoly(client, areaPoly, new WorldPoint(maxX, y, plane), SE);
        }

        // SE corner of the zone
        addToPoly(client, areaPoly, new WorldPoint(maxX, minY, plane), SE, SW);

        // Bottom edge: from maxX - 1 down to minX (exclusive minX)
        for (int x = maxX - 1; x > minX; x--)
        {
            addToPoly(client, areaPoly, new WorldPoint(x, minY, plane), SW);
        }

        // SW corner of the zone
        addToPoly(client, areaPoly, new WorldPoint(minX, minY, plane), SW, NW);

        // Left edge: from minY + 1 up to maxY (exclusive maxY)
        for (int y = minY + 1; y < maxY; y++)
        {
            addToPoly(client, areaPoly, new WorldPoint(minX, y, plane), NW);
        }

        return areaPoly;
    }

    private static void addToPoly(Client client, Polygon areaPoly, WorldPoint wp, int... points)
    {
        LocalPoint localPoint = LocalPoint.fromWorld(client.getTopLevelWorldView(), wp);
        if (localPoint == null)
        {
            return;
        }
        Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);
        if (poly != null)
        {
            for (int point : points)
            {
                // Only add if the index is valid
                if (point >= 0 && point < poly.npoints)
                {
                    areaPoly.addPoint(poly.xpoints[point], poly.ypoints[point]);
                }
            }
        }
    }
}