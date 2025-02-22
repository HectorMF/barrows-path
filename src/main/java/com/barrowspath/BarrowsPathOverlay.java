
package com.barrowspath;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ObjectComposition;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.geometry.Shapes;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import net.runelite.api.Constants;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.awt.geom.Line2D;

import javax.inject.Inject;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        if (BarrowsPathPlugin.barrowsZone.contains(client.getLocalPlayer().getWorldLocation()))
        {
            renderDoors(graphics);
        }
        return null;
    }

    public static LocalPoint preciseToLocalPoint(PreciseWorldPoint p, boolean center) {
        final int TILE_SIZE = 128;
        // If you want to offset to the tile center, add half a tile
        double offset = center ? TILE_SIZE / 2.0 : 0;
        int localX = (int) ((p.getX() * TILE_SIZE) + offset);
        int localY = (int) ((p.getY() * TILE_SIZE) + offset);
        return new LocalPoint(localX, localY);
    }

    public static Line2D.Double getWorldLines2(Client client, LocalPoint startLocation, LocalPoint endLocation)
    {
        final int plane = client.getPlane();

        final int startX = startLocation.getX();
        final int startY = startLocation.getY();
        final int endX = endLocation.getX();
        final int endY = endLocation.getY();

        // Adjust scene coordinates using the client’s base
        int sceneX = (startX - (client.getBaseX() * 128)) >> 7;
        int sceneY = (startY - (client.getBaseY() * 128)) >> 7;

        System.out.println("Adjusted start sceneX: " + sceneX + ", sceneY: " + sceneY);

        // For debugging, comment out the bounds check to see if lines are drawn
        // if (sceneX < 0 || sceneY < 0 || sceneX >= Constants.SCENE_SIZE || sceneY >= Constants.SCENE_SIZE)
        // {
        //     System.out.println("Start location is outside scene bounds after adjustment.");
        //     return null;
        // }

        final int startHeight = Perspective.getTileHeight(client, startLocation, plane);
        final int endHeight = Perspective.getTileHeight(client, endLocation, plane);
        System.out.println("startHeight: " + startHeight + ", endHeight: " + endHeight);

        Point p1 = Perspective.localToCanvas(client, startX, startY, startHeight);
        Point p2 = Perspective.localToCanvas(client, endX, endY, endHeight);

        System.out.println("Canvas p1: " + p1);
        System.out.println("Canvas p2: " + p2);

        if (p1 == null || p2 == null)
        {
            System.out.println("One or both canvas points are null.");
            return null;
        }

        Line2D.Double newLine = new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        System.out.println("Drawing line: " + newLine);
        return newLine;
    }
    public static void drawLinesOnWorld2(Graphics2D graphics, Client client, List<PreciseWorldPoint> linePoints,
                                        Color color)
    {
        for (int i = 0; i < linePoints.size() - 1; i++)
        {
            PreciseWorldPoint startP = linePoints.get(i);
            PreciseWorldPoint endP = linePoints.get(i+1);

            if (startP == null || endP == null) continue;
            if (startP.getPlane() != endP.getPlane()) continue;

            // Convert to LocalPoint, using the center if desired.
            LocalPoint startLp = preciseToLocalPoint(startP, true);
            LocalPoint endLp   = preciseToLocalPoint(endP, true);

            // (You can keep your existing border intersection logic if needed.)
            // Now draw the line in world space.
            Line2D.Double line = getWorldLines2(client, startLp, endLp);
            if (line != null)
            {
                graphics.setColor(color);
                graphics.setStroke(new BasicStroke(3)); // Thicker line for testing
                graphics.draw(line);
            }
        }
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
    /**
     * Returns a list of canvas points that represent an interpolated line
     * from start to end, using linearly interpolated tile heights.
     *
     * @param client   the client instance
     * @param start    the starting LocalPoint
     * @param end      the ending LocalPoint
     * @param segments the number of segments to subdivide the line (the more segments, the smoother the interpolation)
     * @return a list of Points in canvas space, or an empty list if the conversion fails
     */
    public static List<Point> getInterpolatedCanvasPoints(Client client, LocalPoint start, LocalPoint end, int segments) {
        int plane = client.getPlane();
        int startHeight = Perspective.getTileHeight(client, start, plane);
        int endHeight = Perspective.getTileHeight(client, end, plane);

        List<Point> points = new ArrayList<>();

        // For each subdivision, interpolate x, y, and height.
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            int interpX = (int) (start.getX() + t * (end.getX() - start.getX()));
            int interpY = (int) (start.getY() + t * (end.getY() - start.getY()));
            int interpHeight = (int) (startHeight + t * (endHeight - startHeight));

            // Convert the interpolated local coordinates to a canvas point.
            Point canvasPoint = Perspective.localToCanvas(client, interpX, interpY, interpHeight);
            if (canvasPoint != null) {
                points.add(canvasPoint);
            }
        }

        return points;
    }
    public static void drawLinesOnWorld3(Graphics2D graphics, Client client, List<PreciseWorldPoint> linePoints,
                                        Color color)
    {
        for (int i = 0; i < linePoints.size() - 1; i++)
        {

            LocalPoint startLp = linePoints.get(i).getLocalPoint(client);
            LocalPoint endLp = linePoints.get(i+1).getLocalPoint(client);
            if (startLp == null && endLp == null)
            {
                continue;
            }

            int MAX_LP = 13056;


            Line2D.Double newLine = getWorldLines(client, startLp, endLp);
            if (newLine != null)
            {

                float[] dashPattern = {10f, 5f};
                BasicStroke dashedStroke = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dashPattern, 0f);
                // Compute the arrow at the start of the line using our new helper.
                // Adjust d and h as appropriate (they are in local units; e.g. 6 and 12).
                Polygon arrow = getArrowPolygonAtMidpoint(client, startLp, endLp, 64, 32);

                //Polygon Arrow = drawArrowLine((int)newLine.getX1(), (int)newLine.getY1(), (int)newLine.getX2(), (int)newLine.getY2(), 6,12);
                //Stroke arrowStroke = new ArrowStroke(new BasicStroke(2f), 10, Math.toRadians(20));
                OverlayUtil.renderPolygon(graphics, newLine, ColorUtil.colorWithAlpha(color, color.getAlpha() / 2), color, dashedStroke );
                OverlayUtil.renderPolygon(graphics, arrow, ColorUtil.colorWithAlpha(color, color.getAlpha() / 2), color, dashedStroke );
            }
        }
    }
    /**
     * Computes an arrow polygon to be drawn at the midpoint of a line segment.
     * The arrow tip is placed at the midpoint, and its base is offset from the tip.
     *
     * @param client  the Client instance.
     * @param startLp the LocalPoint at the start of the segment.
     * @param endLp   the LocalPoint at the end of the segment.
     * @param d       the distance (in local units) from the tip toward the base along the line.
     * @param h       the perpendicular offset (in local units) from the base center to each side.
     * @return        a Polygon (in canvas coordinates) representing the arrow, or an empty polygon if conversion fails.
     */
    private static Polygon getArrowPolygonAtMidpoint(Client client, LocalPoint startLp, LocalPoint endLp, int d, int h) {
        final int plane = client.getPlane();

        // Compute the direction vector from startLp to endLp in local coordinates.
        int dx = endLp.getX() - startLp.getX();
        int dy = endLp.getY() - startLp.getY();
        double D = Math.sqrt(dx * dx + dy * dy);
        if (D == 0) {
            return new Polygon();
        }
        double sin = dy / D;
        double cos = dx / D;

        // Compute the midpoint of the segment in local coordinates.
        double midX = (startLp.getX() + endLp.getX()) / 2.0;
        double midY = (startLp.getY() + endLp.getY()) / 2.0;

        // The arrow tip is at the midpoint.
        double tipX = midX;
        double tipY = midY;

        // Compute the base center: shift backwards along the line by d units.
        double baseCenterX = tipX - d * cos;
        double baseCenterY = tipY - d * sin;

        // Compute the two base corners by offsetting perpendicular to the line by h.
        double baseLeftX = baseCenterX + h * sin;
        double baseLeftY = baseCenterY - h * cos;
        double baseRightX = baseCenterX - h * sin;
        double baseRightY = baseCenterY + h * cos;

        // Compute tile heights at the start and end, and interpolate to get the mid-height.
        int startHeight = Perspective.getTileHeight(client, startLp, plane);
        int endHeight = Perspective.getTileHeight(client, endLp, plane);
        int midHeight = (startHeight + endHeight) / 2;

        // Convert local coordinates to canvas coordinates using the interpolated midHeight.
        Point canvasTip = Perspective.localToCanvas(client, (int)Math.round(tipX), (int)Math.round(tipY), midHeight);
        Point canvasLeft = Perspective.localToCanvas(client, (int)Math.round(baseLeftX), (int)Math.round(baseLeftY), midHeight);
        Point canvasRight = Perspective.localToCanvas(client, (int)Math.round(baseRightX), (int)Math.round(baseRightY), midHeight);

        if (canvasTip == null || canvasLeft == null || canvasRight == null) {
            return new Polygon();
        }

        int[] xpoints = { canvasTip.getX(), canvasLeft.getX(), canvasRight.getX() };
        int[] ypoints = { canvasTip.getY(), canvasLeft.getY(), canvasRight.getY() };

        return new Polygon(xpoints, ypoints, 3);
    }
    /**
     * Computes an arrow polygon to be drawn at the start of a line.
     * The arrow will be based on the direction from startLp to nextLp.
     *
     * @param client   the Client instance
     * @param startLp  the LocalPoint at the start of the segment (where the arrow will be placed)
     * @param nextLp   the next LocalPoint along the segment (used to determine the direction)
     * @param d        the distance (in local units) from the start where the arrow base is (i.e. arrow "width offset")
     * @param h        the arrow "height" (in local units) offset; positive and negative values determine the two arrow head points
     * @return a Polygon (in canvas coordinates) representing the arrow, or an empty polygon if conversion fails
     */
    private static Polygon getArrowPolygonAtStart(Client client, LocalPoint startLp, LocalPoint nextLp, int d, int h)
    {
        // Get tile height from the starting point.
        int tileHeight = Perspective.getTileHeight(client, startLp, client.getPlane());

        // Compute the direction vector (dx, dy) from startLp to nextLp in local space.
        int dx = nextLp.getX() - startLp.getX();
        int dy = nextLp.getY() - startLp.getY();
        double D = Math.sqrt(dx * dx + dy * dy);
        if (D == 0)
        {
            // Avoid division by zero.
            return new Polygon();
        }

        double sin = dy / D;
        double cos = dx / D;

        // We want to place the arrow at the start of the line.
        // Instead of placing it at the end, we use startLp as the base.
        // Compute two points that will form the arrow head.
        // The idea is similar to the common arrow computation:
        //   - We shift along the line by d units (local units) from the start.
        //   - Then we offset perpendicular by ±h to get the two arrow head points.
        double xm = d;  // distance from the start along the line for the arrow base
        double ym = h;  // vertical offset for one side
        double yn = -h; // vertical offset for the other side

        // Compute the two arrow head points in local coordinate space.
        double arrowLocalX1 = startLp.getX() + xm * cos - ym * sin;
        double arrowLocalY1 = startLp.getY() + xm * sin + ym * cos;
        double arrowLocalX2 = startLp.getX() + xm * cos - yn * sin;
        double arrowLocalY2 = startLp.getY() + xm * sin + yn * cos;

        // Convert the start point and the computed arrow points to canvas coordinates.
        // We use the tileHeight from the starting point.
        Point canvasStart = Perspective.localToCanvas(client, startLp.getX(), startLp.getY(), tileHeight);
        Point canvasArrow1 = Perspective.localToCanvas(client, (int) Math.round(arrowLocalX1), (int) Math.round(arrowLocalY1), tileHeight);
        Point canvasArrow2 = Perspective.localToCanvas(client, (int) Math.round(arrowLocalX2), (int) Math.round(arrowLocalY2), tileHeight);

        if (canvasStart == null || canvasArrow1 == null || canvasArrow2 == null)
        {
            return new Polygon();
        }

        // Create a polygon for the arrow.
        // Here we choose the start point as the tip of the arrow and the two computed points as the base.
        int[] xpoints = { canvasStart.getX(), canvasArrow1.getX(), canvasArrow2.getX() };
        int[] ypoints = { canvasStart.getY(), canvasArrow1.getY(), canvasArrow2.getY() };
        return new Polygon(xpoints, ypoints, 3);
    }

    /**
     * Draw an arrow line between two points.
     * @param x1 x-position of first point.
     * @param y1 y-position of first point.
     * @param x2 x-position of second point.
     * @param y2 y-position of second point.
     * @param d  the width of the arrow.
     * @param h  the height of the arrow.
     */
    private static Polygon drawArrowLine(int x1, int y1, int x2, int y2, int d, int h) {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        return new Polygon(xpoints, ypoints, 3);
    }

    public static Polygon getLineArrow(Graphics2D g2d, int x1, int y1, int x2, int y2, int arrowSize)
    {
        // Draw the main line

        // Compute the angle of the line
        double angle = Math.atan2(y2 - y1, x2 - x1);

        // Define the arrow head angle (for example, 30° on either side)
        double arrowAngle = Math.toRadians(30);

        // Compute the coordinates of the two arrow head points
        int xArrow1 = (int) (x2 - arrowSize * Math.cos(angle - arrowAngle));
        int yArrow1 = (int) (y2 - arrowSize * Math.sin(angle - arrowAngle));
        int xArrow2 = (int) (x2 - arrowSize * Math.cos(angle + arrowAngle));
        int yArrow2 = (int) (y2 - arrowSize * Math.sin(angle + arrowAngle));

        // Create a polygon for the arrow head and fill it
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(x2, y2);
        arrowHead.addPoint(xArrow1, yArrow1);
        arrowHead.addPoint(xArrow2, yArrow2);
        return arrowHead;
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


    private void renderDoors(Graphics2D graphics)
    {
        Room currentRoom = plugin.getMaze().getRoom(client.getLocalPlayer().getWorldLocation());
        Solution solution = plugin.getSolution();

        if(solution == null) return;

        Set<Door> doorsToHighlight = config.highlightDoors() == BarrowsPathConfig.HighlightDoors.CURRENT_ROOM ? currentRoom.getDoors() : plugin.getDoors();

        if(config.drawPath()) {
            BarrowsPathOverlay.drawLinesOnWorld3(graphics, client, solution.getPath(), config.pathColor());
        }

        int alpha = 255;

        for (int i = 0, n = Math.min(3, solution.getDoors().size()); i < n; i++) {
            solution.getDoors().get(i).drawOutline(graphics, ColorUtil.colorWithAlpha(config.correctDoorColor(), alpha), client);
            alpha /= 3;
        }

        for (Door door : doorsToHighlight)
        {
            if(solution.getDoors().contains(door)) {

            }else if (door.isOpen()) {
                if (config.highlightOpenDoors()) {
                    door.drawOutline(graphics, config.openDoorColor(), client);
                }
            } else {
                if (config.highlightClosedDoors()) {
                    door.drawOutline(graphics, config.closedDoorColor(), client);
                }
            }
        }
      //  if(config.drawPath()) {
       //     drawLinesOnWorld3(graphics, client, s.getPath(), config.pathColor());
       // }

        //for (Door door : s.getDoors()) {
        //    if (!config.showCurrentRoom() || door.connects(currentRoom)) {
        //        door.drawOutline(graphics, config.correctDoorColor(), client);
        //    }
       // }



//            ObjectComposition objectComp = client.getObjectDefinition(door.getId());
//            ObjectComposition impostor = objectComp.getImpostorIds() != null ? objectComp.getImpostor() : null;
//            if (impostor != null)
//            {
//                final Shape polygon;
//                final boolean isUnlockedDoor = impostor.getActions()[0] != null;
//                final Color color = isUnlockedDoor ? Color.green : Color.red;
//                polygon = door.getConvexHull();
//                if (polygon != null)
//                {
//
//                    //OverlayUtil.renderTextLocation(graphics, client.getLocalPlayer().get(), client.getLocalPlayer().getLocalLocation(), Color.white);
//                    OverlayUtil.renderPolygon(graphics, polygon, color);
//                    //OverlayUtil.renderTextLocation(graphics, door.getCanvasLocation() , String.valueOf(client.getSelectedSceneTile().getWorldLocation()), Color.white);
//                    OverlayUtil.renderTextLocation(graphics, door.getCanvasLocation() , String.valueOf(door.getId()) + " : " +  String.valueOf(door.getWorldLocation().hashCode()), Color.white);
//                }
//            }
//        }


    }
}