package com.barrowspath;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.util.*;

public class Maze {

    @Getter
    private final Set<Room> rooms;
    private final Room endingRoom;

    public Maze(Set<Room> rooms, Room endingRoom) {
        this.rooms = rooms;
        this.endingRoom = endingRoom;
    }

    // A helper class that stores a room and its cumulative cost.
    private static class RoomEntry {
        Room room;
        int priority;

        public RoomEntry(Room room, int priority) {
            this.room = room;
            this.priority = priority;
        }
    }

    public Solution solve(WorldPoint startLocation)
    {
        // Determine the starting room.
        Room startingRoom = getRoom(startLocation);
        if (startingRoom == null)
        {
            return new Solution(Collections.emptyList());
        }

        // Dijkstra's.
        Map<Room, Integer> costSoFar = new HashMap<>();
        Map<Room, Door> cameFromDoor = new HashMap<>();
        Map<Room, Room> cameFromRoom = new HashMap<>();
        PriorityQueue<RoomEntry> frontier = new PriorityQueue<>(Comparator.comparingInt(e -> e.priority));

        costSoFar.put(startingRoom, 0);
        frontier.add(new RoomEntry(startingRoom, 0));

        while (!frontier.isEmpty())
        {
            RoomEntry currentEntry = frontier.poll();
            Room current = currentEntry.room;

            // Stop if we have reached the ending room.
            if (current.equals(endingRoom))
            {
                break;
            }

            // Explore each open door in the current room.
            for (Door door : current.getDoors())
            {
                if (!door.isOpen())
                {
                    continue;
                }
                // Check each neighboring room via this door.
                for (Room neighbor : door.getOtherRooms(current))
                {
                    // If we're in the starting room, add the extra cost for reaching the door.
                    int extraCost = current.equals(startingRoom)
                            ? startLocation.distanceTo(door.getWorldLocation()) : 0;

                    // The cost to reach the neighbor is the cost so far plus the neighbor's length
                    int newCost = costSoFar.get(current) + neighbor.getLength() + extraCost;
                    if (!costSoFar.containsKey(neighbor) || newCost < costSoFar.get(neighbor))
                    {
                        costSoFar.put(neighbor, newCost);
                        cameFromDoor.put(neighbor, door);
                        cameFromRoom.put(neighbor, current);
                        frontier.add(new RoomEntry(neighbor, newCost));
                    }
                }
            }
        }

        // Ending room never reached
        if (!cameFromDoor.containsKey(endingRoom))
        {
            return new Solution(Collections.emptyList());
        }

        // Reconstruct the pathway
        LinkedList<Door> doorPath = new LinkedList<>();
        Room roomInPath = endingRoom;
        while (!roomInPath.equals(startingRoom))
        {
            Door door = cameFromDoor.get(roomInPath);
            doorPath.addFirst(door);
            roomInPath = cameFromRoom.get(roomInPath);
        }

        return new Solution(doorPath);
    }

    public Room getRoom(WorldPoint location)
    {
        return rooms.stream()
                .filter(room -> room.contains(location))
                .findFirst()
                .orElse(null);
    }
}
