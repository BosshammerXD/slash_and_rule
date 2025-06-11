package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager.LevelData;

// implement generation on initialization by pushing the generation to the todo Stack
public class DungeonRoom implements Runnable {
    public static int numRooms = 0;
    public boolean isMain;
    // start = 0; filler = 1; leaf = 2; boss = 3
    public byte type;
    public int difficulty;
    public DungeonRoom[] neighbors = new DungeonRoom[4]; // 0: left, 1: right, 2: top, 3: bottom
    public String path;
    private boolean stuck = false;
    private boolean visited = false;
    private String[][][] representation;
    private Runnable runFunc;

    public DungeonRoom(LevelData level, int depth, int maxDifficulty, BitSet roomStructure, Random random,
            int branchDepthCap, float branchmul) {
        int[] xy = getStartXY(depth, branchDepthCap);
        int x = xy[0];
        int y = xy[1];
        this.isMain = true;
        this.type = 0;
        this.path = level.startRoom;
        this.difficulty = 0;
        numRooms++;

        this.representation = new String[(depth + branchDepthCap) * 2 - 1][3][(depth + branchDepthCap) * 2 + 1];
        for (int i = 0; i < representation.length; i++) {
            for (int j = 0; j < representation[i].length; j++) {
                Arrays.fill(representation[i][j], "   ");
            }
        }

        int arrayLen = 2 * depth;
        flagRoom(roomStructure, x, y, arrayLen);
        flagRoom(roomStructure, x, y + 1, arrayLen);
        this.runFunc = () -> neighbors[1] = new DungeonRoom(level, depth, maxDifficulty, roomStructure, random, x,
                y + 1, true, arrayLen,
                this, 3, branchDepthCap, branchmul);
    }

    private DungeonRoom(LevelData level, int depth, int maxDifficulty, BitSet roomStructure, Random random, int x,
            int y, boolean isMain, int arrayLen, DungeonRoom origin, int origin_dir, int branchDepthCap,
            float branchmul) {
        this.isMain = isMain;
        numRooms++;

        this.type = getType(isMain, depth, random, level);
        this.neighbors[origin_dir] = origin; // Set the neighbor in the direction of the origin
        this.difficulty = calcDifficulty(depth, maxDifficulty);
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
        // Math.min(depth, 4)
        // numNeighbours
        int numNewRooms = random.nextInt((isMain) ? numNeighbours + 1 : Math.min(depth + 1, 4)) + ((isMain) ? 1 : 0);
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

        int count = 0;
        do {
            stuck = false;
            count = genMain(emptyNeighbours, level, depth, maxDifficulty, roomStructure, random, x, y, arrayLen,
                    count, branchDepthCap, branchmul, numNewRooms);
        } while (count < numNeighbours && stuck);
        if (stuck && isMain) {
            this.isMain = false;
            origin.mainIsStuck();
            return;
        }

        if (isMain) {
            depth = (int) Math.max(0, depth - branchDepthCap * branchmul) + branchDepthCap;
            maxDifficulty = this.difficulty + 1 + depth;
        } else {
            depth -= 1;
        }

        for (int i = count; i < numNewRooms; i++) {
            int neighbourDir = emptyNeighbours[i];
            int[] coords = dirToXY(neighbourDir, x, y);
            this.neighbors[neighbourDir] = new DungeonRoom(level, depth, maxDifficulty, roomStructure, random,
                    coords[0],
                    coords[1], false,
                    arrayLen, this, getOriginDir(neighbourDir), branchDepthCap, branchmul);
        }
    }

    private int genMain(int[] Neighbours, LevelData level, int depth, int maxDifficulty, BitSet roomStructure,
            Random random,
            int x, int y, int arrayLen, int count, int branchDepthCap, float branchmul, int numNewRooms) {
        if (!isMain) {
            return count; // Only generate main rooms
        }
        int neighbourDir = Neighbours[count];
        int[] coords = dirToXY(neighbourDir, x, y);
        if (count >= numNewRooms && isFlagged(coords[0], coords[1], arrayLen, roomStructure)) {
            stuck = true; // If the room is already flagged, mark as stuck
            return count; // If the room is already flagged, skip it
        }
        if (count >= numNewRooms) {
            flagRoom(roomStructure, coords[0], coords[1], arrayLen);
        }
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
            // move down (75%), with the main path, so that it is more likely for the
            // endroom to be down
            float a = (0.625f * mylen - 0.375f) / mylen;
            if (random.nextFloat() < a) {
                int temp = array[indexOf1];
                array[indexOf1] = array[0];
                array[0] = temp;
            }
        } else if (array[0] == 3) {
            // here we decrease the cahnce of the main path goining up if it would go up and
            // can't go down
            if (random.nextFloat() < 0.25f) {
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
        byte val = (byte) ((random.nextInt(depth * 2) == 0) ? 2 : 1);
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

        if (!isFlagged(x - 1, y, arrayLen, roomStructure)) { // Left
            emptyNeighbours[count++] = 0;
        }
        if (!isFlagged(x, y + 1, arrayLen, roomStructure)) { // Bottom
            emptyNeighbours[count++] = 1;
        }
        if (!isFlagged(x + 1, y, arrayLen, roomStructure)) { // Right
            emptyNeighbours[count++] = 2;
        }
        if (!isFlagged(x, y - 1, arrayLen, roomStructure)) { // Top
            emptyNeighbours[count++] = 3;
        }

        // Resize the array to the actual number of empty neighbours
        int[] result = new int[count];
        System.arraycopy(emptyNeighbours, 0, result, 0, count);
        return result;
    }

    private static int calcDifficulty(int depth, int maxDifficulty) {
        return maxDifficulty - depth;
    }

    private static void flagRoom(BitSet roomStructure, int x, int y, int arrayLen) {
        int index = y * arrayLen + x;
        roomStructure.set(index);
    }

    private static boolean isFlagged(int x, int y, int arrayLen, BitSet roomStructure) {
        int index = y * arrayLen + x;
        return roomStructure.get(index);
    }

    private int getOriginDir(int outDir) {
        return (outDir + 2) % 4; // Returns the opposite direction
    }

    private int[] dirToXY(int dir, int x, int y) {
        switch (dir) {
            case 0: // Left
                return new int[] { x - 1, y };
            case 1: // Bottom
                return new int[] { x, y + 1 };
            case 2: // Right
                return new int[] { x + 1, y };
            case 3: // Top
                return new int[] { x, y - 1 };
            default:
                throw new IllegalArgumentException("Invalid direction: " + dir);
        }
    }

    public void print() {
        if (this.type != 0) {
            return; // Only print the start room
        }
        int x = (representation[0][0].length - 1) / 2; // Center x-coordinate
        int y = (representation.length - 5) / 2; // Center y-coordinate

        this.getString(representation, x, y);
        String[] dummy = new String[representation[0][0].length];
        Arrays.fill(dummy, "   ");
        for (String[][] row : representation) {
            if (!Arrays.equals(row[0], dummy)) {
                System.out.println(String.join("", row[0]));
                System.out.println(String.join("", row[1]));
                System.out.println(String.join("", row[2]));
            }

        }
    }

    private void getString(String[][][] rooms, int x, int y) {
        if (this.visited) {
            return; // If already visited, do not process again
        }
        this.visited = true;
        rooms[y][0][x] = "\u250C" + ((neighbors[3] == null) ? "\u2500" : "d") + "\u2510";
        rooms[y][1][x] = ((neighbors[0] == null) ? "\u2502" : "d") + this.type
                + ((neighbors[2] == null) ? "\u2502" : "d");
        rooms[y][2][x] = "\u2514" + ((neighbors[1] == null) ? "\u2500" : "d") + "\u2518";
        int index = 0;
        for (DungeonRoom neighbor : neighbors) {
            if (neighbor != null) {
                int[] coords = dirToXY(index, x, y);
                neighbor.getString(rooms, coords[0], coords[1]);
            }
            index++;
        }
        this.visited = false; // Reset visited status for future calls
    }

    private static int[] getStartXY(int depth, int branchDepthCap) {
        int x = depth + branchDepthCap;
        int y = depth + branchDepthCap - 3;
        return new int[] { x, y };
    }

    public void run() {
        if (this.runFunc != null) {
            this.runFunc.run();
        } else {
            System.out.println("No run function defined for this room.");
        }
    }
}
