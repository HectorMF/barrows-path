package com.barrowspath;

import com.google.common.collect.ImmutableSet;
import net.runelite.api.Client;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;
import java.util.*;
import java.util.List;

import static net.runelite.api.NullObjectID.*;
import static net.runelite.api.NullObjectID.NULL_20715;

public class Maze {

    private final Set<Room> rooms;
    private final Room endingRoom;

    public Maze(Set<Room> rooms, Room endingRoom) {
        this.rooms = rooms;
        this.endingRoom = endingRoom;
    }

    public Solution solve(WorldPoint startLocation)
    {
        List<Door> doorways = solveDoors(startLocation);
        List<PreciseWorldPoint> pathway;// = stitchDoorPaths(doorways, client);
        return new Solution(doorways);
    }

    private List<Door> solveDoors(WorldPoint startLocation)
    {
        Room startingRoom = getRoom(startLocation);

        if (startingRoom == null) {
            return Collections.emptyList();
        }

        // Dijkstra's algorithm initialization.
        Map<Room, Integer> costSoFar = new HashMap<>();
        Map<Room, Door> cameFromDoor = new HashMap<>();
        Map<Room, Room> cameFromRoom = new HashMap<>();
        PriorityQueue<RoomEntry> frontier = new PriorityQueue<>(Comparator.comparingInt(e -> e.priority));

        costSoFar.put(startingRoom, 0);
        frontier.add(new RoomEntry(startingRoom, 0));

        while (!frontier.isEmpty()) {
            RoomEntry currentEntry = frontier.poll();
            Room current = currentEntry.room;

            // Stop if we have reached the ending room.
            if (current.equals(endingRoom)) {
                break;
            }

            // Explore each open door in the current room.
            for (Door door : current.getDoors()) {
                if(!door.isOpen()){
                    continue;
                }
                // For every room accessible via this door...
                for (Room neighbor : door.getOtherRooms(current)) {
                    // If we're in the starting room, add the extra cost for reaching the door.
                    int extraCost = 0;
                    if (current.equals(startingRoom))
                    {
                        // Compute the cost from the starting world point to the door's world point.
                        // (Assuming WorldPoint has a distanceTo method.)
                        extraCost = startLocation.distanceTo(door.getWorldLocation());
                    }

                    // The cost to reach neighbor is the cost so far plus the neighbor's length.
                    int newCost = costSoFar.get(current) + neighbor.getLength() + extraCost;
                    if (!costSoFar.containsKey(neighbor) || newCost < costSoFar.get(neighbor)) {
                        costSoFar.put(neighbor, newCost);
                        cameFromDoor.put(neighbor, door);
                        cameFromRoom.put(neighbor, current);
                        frontier.add(new RoomEntry(neighbor, newCost));
                    }
                }
            }
        }

        // If the ending room was never reached, return an empty set.
        if (!cameFromDoor.containsKey(endingRoom)) {
            return Collections.emptyList();
        }

        // Reconstruct the door path by backtracking from the ending room.
        LinkedList<Door> doorPath = new LinkedList<>();
        Room roomInPath = endingRoom;
        while (!roomInPath.equals(startingRoom)) {
            Door door = cameFromDoor.get(roomInPath);
            doorPath.addFirst(door);
            roomInPath = cameFromRoom.get(roomInPath);
        }


        return doorPath;
    }

//    private List<LocalPoint> stitchDoorPaths(Set<Door> doorPathList, Client client) {
//        List<LocalPoint> aggregatedPath = new ArrayList<>();
//
//        for (Door door : doorPathList) {
//            List<LocalPoint> doorPath = door.getPath(client);
//            if (doorPath.isEmpty()) {
//                continue;
//            }
//
//            // If no points have been aggregated yet, simply add the entire doorPath.
//            if (aggregatedPath.isEmpty()) {
//                aggregatedPath.addAll(doorPath);
//                continue;
//            }
//
//            // Get the connection point from the aggregated path.
//            LocalPoint connection = aggregatedPath.get(aggregatedPath.size() - 1);
//
//            // Case 1: The doorPath is in proper order.
//            if (doorPath.get(0).equals(connection)) {
//                // Append doorPath skipping the duplicate connection point.
//                aggregatedPath.addAll(doorPath.subList(1, doorPath.size()));
//            }
//            // Case 2: The doorPath is reversed.
//            else if (doorPath.get(doorPath.size() - 1).equals(connection)) {
//                List<LocalPoint> reversedDoorPath = new ArrayList<>(doorPath);
//                Collections.reverse(reversedDoorPath);
//                // Now, the first point of reversedDoorPath equals the connection.
//                aggregatedPath.addAll(reversedDoorPath.subList(1, reversedDoorPath.size()));
//            }
//            // Case 3: The connection doesn't match—try flipping the aggregated path.
//            else {
//                List<LocalPoint> reversedAggregated = new ArrayList<>(aggregatedPath);
//                Collections.reverse(reversedAggregated);
//                LocalPoint newConnection = reversedAggregated.get(reversedAggregated.size() - 1);
//                if (doorPath.get(0).equals(newConnection)) {
//                    // Replace aggregatedPath with its reversed version.
//                    aggregatedPath = reversedAggregated;
//                    aggregatedPath.addAll(doorPath.subList(1, doorPath.size()));
//                } else if (doorPath.get(doorPath.size() - 1).equals(newConnection)) {
//                    List<LocalPoint> reversedDoorPath = new ArrayList<>(doorPath);
//                    Collections.reverse(reversedDoorPath);
//                    aggregatedPath = reversedAggregated;
//                    aggregatedPath.addAll(reversedDoorPath.subList(1, reversedDoorPath.size()));
//                } else {
//                    // If no connection is found, log a warning and simply append the entire doorPath.
//                    System.out.println("Warning: Connection point not found between aggregated path and door: " + door);
//                    aggregatedPath.addAll(doorPath);
//                }
//            }
//        }
//
//        return aggregatedPath;
//    }

    // A helper class for Dijkstra's algorithm that stores a room and its cumulative cost.
    private static class RoomEntry {
        Room room;
        int priority;

        public RoomEntry(Room room, int priority) {
            this.room = room;
            this.priority = priority;
        }
    }

    public Room getRoom(WorldPoint location)
    {
        return rooms.stream()
                .filter(room -> room.contains(location))
                .findFirst()
                .orElse(null);
    }

    public void drawZones(Graphics2D graphics, Client client)
    {
        for (Room room : rooms) {
            OverlayUtil.renderPolygon(graphics, QuestPerspective.getZonePoly(client, room.getZone()), room.getColor());
        }
    }
}
