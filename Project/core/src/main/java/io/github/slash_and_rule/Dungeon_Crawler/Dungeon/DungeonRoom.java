package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager.LevelData;

public class DungeonRoom {
    public boolean isMain;
    // start = 0; filler = 1; leaf = 2; boss = 3
    public byte type;
    public int difficulty;
    public DungeonRoom[] neighbors = new DungeonRoom[4]; // 0: left, 1: right, 2: top, 3: bottom
    public String path;
    private boolean stuck = false;

    public DungeonRoom(LevelData level, int depth, int maxDifficulty, BitSet roomStructure, Random random) {
        int x = depth - 2;
        int y = depth;
        this.isMain = true;
        this.type = 0;
        this.difficulty = 0;
        int arrayLen = 2 * depth;
        flagRoom(roomStructure, x, y, arrayLen);
        printRoomStructure(roomStructure, arrayLen);
        flagRoom(roomStructure, x + 1, y, arrayLen);
        this.path = level.startRoom;
        this.difficulty = 0;
        neighbors[2] = new DungeonRoom(level, depth, maxDifficulty, roomStructure, random, x + 1, y, true, arrayLen,
                this, getOriginDir(2));
    }

    private DungeonRoom(LevelData level, int depth, int maxDifficulty, BitSet roomStructure, Random random, int x,
            int y, boolean isMain, int arrayLen, DungeonRoom origin, int origin_dir) {
        this.isMain = isMain;
        printRoomStructure(roomStructure, arrayLen);
        this.type = getType(isMain, depth, random, level);
        this.neighbors[origin_dir] = origin; // Set the neighbor in the direction of the origin
        setDifficulty(depth, maxDifficulty);
        generateNeighbors(level, depth, maxDifficulty, roomStructure, random, x, y, isMain, arrayLen, origin);
    }

    private void generateNeighbors(LevelData level, int depth, int maxDifficulty, BitSet roomStructure, Random random,
            int x, int y, boolean isMain, int arrayLen, DungeonRoom origin) {
        if (this.type == 2 || this.type == 3) {
            return; // Leaf or boss rooms do not generate further neighbors
        }
        int[] emptyNeighbours = getEmptyNeighbours(roomStructure, x, y, arrayLen);
        if (emptyNeighbours.length == 0) {
            this.type = 2;
            if (isMain) {
                this.isMain = false;
                origin.mainIsStuck();
            }
            return;
        }
        int numNeighbours = emptyNeighbours.length;
        shuffleArray(emptyNeighbours, random);

        int numNewRooms = random.nextInt(numNeighbours) + ((isMain) ? 1 : 0);
        if (numNewRooms > numNeighbours) {
            numNewRooms = numNeighbours; // Ensure we don't exceed the number of available empty neighbours
        }

        for (int i = 0; i < numNewRooms; i++) {
            int roomDir = emptyNeighbours[i];
            int[] coords = dirToXY(roomDir, x, y);
            flagRoom(roomStructure, coords[0], coords[1], arrayLen);
        }
        // int[] temp = new int[numNewRooms];
        // System.arraycopy(emptyNeighbours, 0, temp, 0, temp.length);
        // System.out.println("Room: (" + x + "," + y + ") " + Arrays.toString(temp));

        int count = 0;
        do {
            count = genMain(emptyNeighbours, level, depth, maxDifficulty, roomStructure, random, x, y, arrayLen,
                    count);
        } while (count < numNeighbours && stuck);

        for (int i = count; i < numNewRooms; i++) {
            int neighbourDir = emptyNeighbours[i];
            int[] coords = dirToXY(neighbourDir, x, y);
            new DungeonRoom(level, depth - 1, maxDifficulty, roomStructure, random, coords[0], coords[1], false,
                    arrayLen, this, getOriginDir(neighbourDir));
        }
    }

    private int genMain(int[] Neighbours, LevelData level, int depth, int maxDifficulty, BitSet roomStructure,
            Random random,
            int x, int y, int arrayLen, int count) {
        if (!isMain) {
            return count; // Only generate main rooms
        }
        int neighbourDir = Neighbours[count];
        int[] coords = dirToXY(neighbourDir, x, y);
        new DungeonRoom(level, depth - 1, maxDifficulty, roomStructure, random, coords[0], coords[1], true,
                arrayLen, this, getOriginDir(neighbourDir));

        return count + 1;
    }

    private static void shuffleArray(int[] array, Random random) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // Swap
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    private void mainIsStuck() {
        this.stuck = true;
    }

    private byte getType(boolean isMain, int depth, Random random, LevelData level) {
        if (isMain && depth == 0) {
            this.path = level.endRoom;
            return 3;
        } else if (isMain) {
            this.path = level.fillerRooms[random.nextInt(level.fillerRooms.length)];
            return 1;
        }
        if (depth == 0) {
            this.path = level.leafRooms[random.nextInt(level.leafRooms.length)];
            return 2; // Leaf room
        }
        byte val = (byte) ((random.nextInt(depth) == 0) ? 2 : 1);
        if (val == 2) {
            this.path = level.leafRooms[random.nextInt(level.leafRooms.length)];
        } else {
            this.path = level.fillerRooms[random.nextInt(level.fillerRooms.length)];
        }
        return val; // Randomly choose between 1 and 2 for non-main rooms
    }

    private int[] getEmptyNeighbours(BitSet roomStructure, int x, int y, int arrayLen) {
        int[] emptyNeighbours = new int[4];
        int count = 0;

        if (!roomStructure.get(y * arrayLen + (x - 1))) { // Left
            emptyNeighbours[count++] = 0;
        }
        if (!roomStructure.get((y + 1) * arrayLen + x)) { // Right
            emptyNeighbours[count++] = 1;
        }
        if (!roomStructure.get(y * arrayLen + (x + 1))) { // Top
            emptyNeighbours[count++] = 2;
        }
        if (!roomStructure.get((y - 1) * arrayLen + x)) { // Bottom
            emptyNeighbours[count++] = 3;
        }

        // Resize the array to the actual number of empty neighbours
        int[] result = new int[count];
        System.arraycopy(emptyNeighbours, 0, result, 0, count);
        return result;
    }

    private void setDifficulty(int depth, int maxDifficulty) {
        this.difficulty = maxDifficulty - depth;
    }

    private void flagRoom(BitSet roomStructure, int x, int y, int arrayLen) {
        int index = y * arrayLen + x;
        roomStructure.set(index);
    }

    private int getOriginDir(int outDir) {
        return (outDir + 2) % 4; // Returns the opposite direction
    }

    private int[] dirToXY(int dir, int x, int y) {
        switch (dir) {
            case 0: // Left
                return new int[] { x - 1, y };
            case 1: // Right
                return new int[] { x, y + 1 };
            case 2: // Top
                return new int[] { x + 1, y };
            case 3: // Bottom
                return new int[] { x, y - 1 };
            default:
                throw new IllegalArgumentException("Invalid direction: " + dir);
        }
    }

    private void printRoomStructure(BitSet roomStructure, int arrayLen) {
        for (int y = 0; y < arrayLen + 1; y++) {
            for (int x = 0; x < arrayLen; x++) {
                System.out.print(roomStructure.get(y * arrayLen + x) ? "1 " : "0 ");
            }
            System.out.println();
        }
        System.out.println("-----");
    }
}
