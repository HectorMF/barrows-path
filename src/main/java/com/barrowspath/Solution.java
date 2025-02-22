package com.barrowspath;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Solution {
    @Getter
    private final List<Door> doors;
    @Getter
    private List<PreciseWorldPoint> path;

    private boolean valid;

    public Solution(List<Door> doors) {
        this(doors, true);
    }

    public Solution(List<Door> doors, boolean valid)
    {
        this.doors = doors;
        this.path = stitchDoorPaths(doors);
        this.valid = valid;
    }

    public boolean isInvalid(){
        return !this.valid;
    }

    public void invalidate()
    {
        this.valid = false;
    }

    private List<PreciseWorldPoint> stitchDoorPaths(List<Door> doorPathList) {
        List<PreciseWorldPoint> aggregatedPath = new ArrayList<>();

        for (Door door : doorPathList) {
            List<PreciseWorldPoint> doorPath = door.getPath();
            if (doorPath.isEmpty()) {
                continue;
            }

            // If no points have been aggregated yet, simply add the entire doorPath.
            if (aggregatedPath.isEmpty()) {
                aggregatedPath.addAll(doorPath);
                continue;
            }

            // Get the connection point from the aggregated path.
            PreciseWorldPoint connection = aggregatedPath.get(aggregatedPath.size() - 1);

            // Case 1: The doorPath is in proper order.
            if (doorPath.get(0).equals(connection)) {
                // Append doorPath skipping the duplicate connection point.
                aggregatedPath.addAll(doorPath.subList(1, doorPath.size()));
            }
            // Case 2: The doorPath is reversed.
            else if (doorPath.get(doorPath.size() - 1).equals(connection)) {
                List<PreciseWorldPoint> reversedDoorPath = new ArrayList<>(doorPath);
                Collections.reverse(reversedDoorPath);
                // Now, the first point of reversedDoorPath equals the connection.
                aggregatedPath.addAll(reversedDoorPath.subList(1, reversedDoorPath.size()));
            }
            // Case 3: The connection doesn't match—try flipping the aggregated path.
            else {
                List<PreciseWorldPoint> reversedAggregated = new ArrayList<>(aggregatedPath);
                Collections.reverse(reversedAggregated);
                PreciseWorldPoint newConnection = reversedAggregated.get(reversedAggregated.size() - 1);
                if (doorPath.get(0).equals(newConnection)) {
                    // Replace aggregatedPath with its reversed version.
                    aggregatedPath = reversedAggregated;
                    aggregatedPath.addAll(doorPath.subList(1, doorPath.size()));
                } else if (doorPath.get(doorPath.size() - 1).equals(newConnection)) {
                    List<PreciseWorldPoint> reversedDoorPath = new ArrayList<>(doorPath);
                    Collections.reverse(reversedDoorPath);
                    aggregatedPath = reversedAggregated;
                    aggregatedPath.addAll(reversedDoorPath.subList(1, reversedDoorPath.size()));
                } else {
                    // If no connection is found, log a warning and simply append the entire doorPath.
                    System.out.println("Warning: Connection point not found between aggregated path and door: " + door);
                    aggregatedPath.addAll(doorPath);
                }
            }
        }

        return aggregatedPath;
    }


    public static final Solution EMPTY = new Solution(Collections.emptyList(), false);
}
