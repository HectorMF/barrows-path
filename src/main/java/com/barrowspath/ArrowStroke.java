package com.barrowspath;

import java.awt.*;
import java.awt.geom.*;

public class ArrowStroke implements Stroke
{
    private final Stroke baseStroke;
    private final int arrowHeadLength;
    private final double arrowHeadAngle;

    public ArrowStroke(Stroke baseStroke, int arrowHeadLength, double arrowHeadAngle)
    {
        this.baseStroke = baseStroke;
        this.arrowHeadLength = arrowHeadLength;
        this.arrowHeadAngle = arrowHeadAngle;
    }

    @Override
    public Shape createStrokedShape(Shape p)
    {
        // First, get the basic stroked shape.
        Shape baseShape = baseStroke.createStrokedShape(p);
        PathIterator it = baseShape.getPathIterator(null);
        GeneralPath pathWithArrows = new GeneralPath();

        double[] coords = new double[6];
        double lastX = 0, lastY = 0;
        double moveX = 0, moveY = 0;
        while (!it.isDone())
        {
            int type = it.currentSegment(coords);
            switch (type)
            {
                case PathIterator.SEG_MOVETO:
                    moveX = coords[0];
                    moveY = coords[1];
                    pathWithArrows.moveTo(moveX, moveY);
                    lastX = moveX;
                    lastY = moveY;
                    break;
                case PathIterator.SEG_LINETO:
                    double x = coords[0];
                    double y = coords[1];
                    pathWithArrows.lineTo(x, y);
                    // Draw arrow head on this segment:
                    addArrowHead(pathWithArrows, lastX, lastY, x, y);
                    lastX = x;
                    lastY = y;
                    break;
                case PathIterator.SEG_CLOSE:
                    pathWithArrows.closePath();
                    break;
            }
            it.next();
        }
        return pathWithArrows;
    }

    private void addArrowHead(GeneralPath path, double x1, double y1, double x2, double y2)
    {
        double theta = Math.atan2(y2 - y1, x2 - x1);
        double phi = arrowHeadAngle;

        double xArrow1 = x2 - arrowHeadLength * Math.cos(theta - phi);
        double yArrow1 = y2 - arrowHeadLength * Math.sin(theta - phi);
        double xArrow2 = x2 - arrowHeadLength * Math.cos(theta + phi);
        double yArrow2 = y2 - arrowHeadLength * Math.sin(theta + phi);

        // Instead of drawing immediately, add these as segments in the path.
        path.moveTo(x2, y2);
        path.lineTo(xArrow1, yArrow1);
        path.moveTo(x2, y2);
        path.lineTo(xArrow2, yArrow2);
    }
}