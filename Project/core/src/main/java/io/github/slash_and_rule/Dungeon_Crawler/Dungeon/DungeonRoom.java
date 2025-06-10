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
    private int x, y;
    private boolean visited = false;
    private String[][][] representation;

    public DungeonRoom(LevelData level, int depth, int maxDifficulty, BitSet roomStructure, Random random,
            int branchDepthCap, float branchmul) {
        int x = depth + branchDepthCap;
        int y = depth + branchDepthCap - 2;
        this.x = x;
        this.y = y;
        this.isMain = true;
        this.type = 0;
        this.difficulty = 0;

        this.representation = new String[(depth + branchDepthCap) * 2 - 1][3][(depth + branchDepthCap) * 2 + 1];
        for (int i = 0; i < representation.length; i++) {
            for (int j = 0; j < representation[i].length; j++) {
                Arrays.fill(representation[i][j], "   ");
            }
        }

        int arrayLen = 2 * depth;
        flagRoom(roomStructure, x, y, arrayLen);
        // printRoomStructure(roomStructure, arrayLen);
        flagRoom(roomStructure, x, y + 1, arrayLen);
        this.path = level.startRoom;
        this.difficulty = 0;
        neighbors[1] = new DungeonRoom(level, depth, maxDifficulty, roomStructure, random, x, y + 1, true, arrayLen,
                this, 3, branchDepthCap, branchmul);
    }

    private DungeonRoom(LevelData level, int depth, int maxDifficulty, BitSet roomStructure, Random random, int x,
            int y, boolean isMain, int arrayLen, DungeonRoom origin, int origin_dir, int branchDepthCap,
            float branchmul) {
        this.isMain = isMain;
        this.x = x;
        this.y = y;
        // printRoomStructure(roomStructure, arrayLen);
        this.type = getType(isMain, depth, random, level);
        this.neighbors[origin_dir] = origin; // Set the neighbor in the direction of the origin
        setDifficulty(depth, maxDifficulty);
        generateNeighbors(level, depth, maxDifficulty, roomStructure, random, x, y, isMain, arrayLen, origin,
                branchDepthCap, branchmul);
    }

    private void generateNeighbors(LevelData level, int depth, int maxDifficulty, BitSet roomStructure, Random random,
            int x, int y, boolean isMain, int arrayLen, DungeonRoom origin, int branchDepthCap, float branchmul) {
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

        int numNewRooms = random.nextInt(numNeighbours) + 1 + ((isMain) ? 1 : 0);
        if (numNewRooms > numNeighbours) {
            numNewRooms = numNeighbours; // Ensure we don't exceed the number of available empty neighbours
        } else if (numNewRooms == 0) {
            numNewRooms = 1; // Ensure at least one new room is created
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
                    count, branchDepthCap, branchmul);
        } while (count < numNeighbours && stuck);

        for (int i = count; i < numNewRooms; i++) {
            int neighbourDir = emptyNeighbours[i];
            int[] coords = dirToXY(neighbourDir, x, y);
            int tempDepth;
            if (isMain) {
                tempDepth = (int) Math.max(0, depth - branchDepthCap * branchmul) + branchDepthCap;
            } else {
                tempDepth = depth - 1;
            }
            this.neighbors[neighbourDir] = new DungeonRoom(level, tempDepth, maxDifficulty, roomStructure, random,
                    coords[0],
                    coords[1], false,
                    arrayLen, this, getOriginDir(neighbourDir), branchDepthCap, branchmul);
        }
    }

    private int genMain(int[] Neighbours, LevelData level, int depth, int maxDifficulty, BitSet roomStructure,
            Random random,
            int x, int y, int arrayLen, int count, int branchDepthCap, float branchmul) {
        if (!isMain) {
            return count; // Only generate main rooms
        }

        int neighbourDir = Neighbours[count];
        int[] coords = dirToXY(neighbourDir, x, y);
        this.neighbors[neighbourDir] = new DungeonRoom(level, depth - 1, maxDifficulty, roomStructure, random,
                coords[0],
                coords[1], true,
                arrayLen, this, getOriginDir(neighbourDir), branchDepthCap, branchmul);

        return count + 1;
    }

    private void shuffleArray(int[] array, Random random) {
        int indexOf1 = -1;
        int mylen = array.length;
        for (int i = mylen - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // Swap
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
            if (array[i] == 1) {
                indexOf1 = i; // Store the index of the first 1 found
            } else if (array[index] == 1) {
                indexOf1 = index; // Store the index of the first 1 found
            }
        }
        if (!this.isMain || mylen == 1) {
            return;
        }
        if (indexOf1 != -1) {
            // if this is the Main path andd we can move down we want a higher chance to
            // move down (50%), with the main path, so that it is more likely for the
            // endroom to be down
            float a = (0.5f * mylen - 0.5f) / mylen;
            if (random.nextFloat() < a) {
                int temp = array[indexOf1];
                array[indexOf1] = array[0];
                array[0] = temp;
            }
        } else if (array[0] == 3) {
            // here we decrease the cahnce of the main path goining up if it would go up and
            // can't go down
            if (random.nextFloat() < 0.5f) {
                int otherIndex = (mylen == 2) ? 1 : random.nextInt(mylen - 1) + 1; // Random index from 1 to len-1
                int temp = array[0];
                array[0] = array[otherIndex];
                array[otherIndex] = temp;
            }
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

    public void printRoomStructure(BitSet roomStructure, int arrayLen) {
        for (int y = 0; y < arrayLen + 1; y++) {
            for (int x = 0; x < arrayLen; x++) {
                System.out.print(roomStructure.get(y * arrayLen + x) ? "1 " : "0 ");
            }
            System.out.println();
        }
        System.out.println("-----");
    }

    public void print() {
        if (this.type != 0) {
            return; // Only print the start room
        }
        this.getString(representation);
        for (String[][] row : representation) {
            System.out.println(String.join("", row[0]));
            System.out.println(String.join("", row[1]));
            System.out.println(String.join("", row[2]));
        }
    }

    private void getString(String[][][] rooms) {
        if (this.visited) {
            return; // If already visited, do not process again
        }
        this.visited = true;
        rooms[y][0][x] = "\u250C" + ((neighbors[3] == null) ? "\u2500" : "d") + "\u2510";
        rooms[y][1][x] = ((neighbors[0] == null) ? "\u2502" : "d") + this.type
                + ((neighbors[2] == null) ? "\u2502" : "d");
        rooms[y][2][x] = "\u2514" + ((neighbors[1] == null) ? "\u2500" : "d") + "\u2518";
        for (DungeonRoom neighbor : neighbors) {
            if (neighbor != null) {
                neighbor.getString(rooms);
            }
        }
        this.visited = false; // Reset visited status for future calls
    }
}
