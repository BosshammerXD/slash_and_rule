package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.Arrays;
import java.util.BitSet;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager.DungeonGenerationData;

// implement generation on initialization by pushing the generation to the todo Stack
public class DungeonRoom implements Runnable {
    private static class Coord {
        public int x;
        public int y;

        public Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Coord move(int dir) {
            switch (dir) {
                case 0: // Left
                    return new Coord(x - 1, y);
                case 1: // Bottom
                    return new Coord(x, y + 1);
                case 2: // Right
                    return new Coord(x + 1, y);
                case 3: // Top
                    return new Coord(x, y - 1);
                default:
                    throw new IllegalArgumentException("Invalid direction: " + dir);
            }
        }

        public int[] getEmptyNeighbours(BitSet roomStructure, int arrayLen) {
            int[] emptyNeighbours = new int[4];
            int count = 0;

            for (int i = 0; i < 4; i++) {
                if (!isFlagged(i, roomStructure, arrayLen)) {
                    emptyNeighbours[count++] = i;
                }
            }

            // Resize the array to the actual number of empty neighbours
            return Arrays.copyOf(emptyNeighbours, count);
        }

        public boolean isFlagged(int dir, BitSet roomStructure, int arrayLen) {
            if (dir < 0 || dir > 3) {
                throw new IllegalArgumentException("Invalid direction: " + dir);
            }
            // f(x) = (~x & (1 | (x << 1) & 2)) -1 => f(0)=0, f(1)=1, f(2)=0, f(3)=-1
            int myY = y + (~dir & (1 | (dir << 1) & 2)) - 1; // binary magic to get the offset correct
            // f(x) = (x & (1 | (~x << 1) & 2)) -1 => f(0)=-1, f(1)=0, f(2)=1, f(3)=0
            int myX = x + (dir & (1 | (~dir << 1) & 2)) - 1;
            int index = myY * arrayLen + myX;
            return roomStructure.get(index);
        }
    }

    public static int numRooms = 0;
    public boolean isMain;
    // start = 0; filler = 1; leaf = 2; boss = 3
    public byte type;
    public int difficulty;
    public DungeonRoom[] neighbours = new DungeonRoom[4]; // 0: left, 1: right, 2: top, 3: bottom
    private boolean stuck = false;
    private boolean visited = false;
    private String[][][] representation;
    private Runnable runFunc;
    public String path = null;
    public boolean cleared = false;

    public DungeonRoom(DungeonGenerationData genData) {
        int depth = genData.depth;
        int branchcap = genData.branchcap;
        BitSet roomStructure = genData.getRoomStructure();

        Coord coord = getStartXY(depth, branchcap);
        this.isMain = true;
        this.type = 0;
        this.difficulty = 0;
        numRooms = 1;
        this.cleared = true; // Start room is considered cleared

        this.representation = new String[(depth + branchcap) * 2][3][genData.getArrayLength()];
        for (int i = 0; i < representation.length; i++) {
            for (int j = 0; j < representation[i].length; j++) {
                Arrays.fill(representation[i][j], "   ");
            }
        }

        flagRoom(roomStructure, coord, genData.getArrayLength());
        flagRoom(roomStructure, coord.move(1), genData.getArrayLength());
        this.runFunc = () -> makeNewMain(genData, depth, genData.maxDifficulty, coord.move(1), 1);
    }

    private DungeonRoom(DungeonGenerationData genData, int depth, int maxDifficulty, Coord coord, boolean isMain,
            DungeonRoom origin) {
        this.isMain = isMain;
        numRooms++;

        this.type = getType(isMain, depth);
        this.difficulty = calcDifficulty(depth, genData.maxDifficulty);

        // Leaf or boss rooms do not generate further neighbors
        if (this.type == 2 || this.type == 3) {
            return;
        }
        int ret = generateNeighbors(genData, depth, maxDifficulty, coord);
        if (ret == -1 && isMain) {
            this.isMain = false;
            origin.mainIsStuck();
        }
    }

    private int generateNeighbors(DungeonGenerationData genData, int depth, int maxDifficulty, Coord coord) {
        BitSet roomStructure = genData.getRoomStructure();
        int[] emptyNeighbours = coord.getEmptyNeighbours(roomStructure, genData.getArrayLength());
        if (emptyNeighbours.length == 0) {
            this.type = 2;
            return -1;
        }

        int numNeighbours = emptyNeighbours.length;
        shuffleArray(emptyNeighbours);

        int numNewRooms = getNumNewRooms(depth, numNeighbours);

        // Flag new rooms so no room can be generated in the same place
        for (int i = 0; i < numNewRooms; i++) {
            int roomDir = emptyNeighbours[i];
            flagRoom(roomStructure, coord.move(roomDir), genData.getArrayLength());
        }

        int count = 0;
        stuck = isMain;
        while (stuck) {
            // if count is greater than numNeighbours, we are stuck
            // (we have tried every path)
            if (count > numNeighbours) {
                return -1;
            }
            int neighbourDir = emptyNeighbours[count++];
            // if we need to look at more rooms than the already reserved ones
            // we need to checj if the room is still available
            // if it is available, we flag it, if not, we skip it
            if (count > numNewRooms) {
                if (coord.isFlagged(neighbourDir, roomStructure, genData.getArrayLength())) {
                    continue;
                }
                flagRoom(roomStructure, coord, neighbourDir);
            }
            // if the new main gets stuck it will set this mains stuck to true
            // if not we were able to generate a complete main path
            // (because the main path is generated before makeNewMain is done)
            stuck = false;
            makeNewMain(genData, depth - 1, maxDifficulty, coord.move(neighbourDir), neighbourDir);
        }

        // now generate the rest of the rooms
        if (isMain) {
            // if this is the main room ist branches have a different depth than normal
            depth = (int) (depth * genData.branchmul) + genData.branchcap;
            maxDifficulty = this.difficulty + 1 + depth;
        } else {
            depth -= 1;
        }

        for (int i = count; i < numNewRooms; i++) {
            int neighbourDir = emptyNeighbours[i];
            makeNewDungeonRoom(genData, depth, maxDifficulty, coord.move(neighbourDir), neighbourDir);
        }

        return 0;
    }

    private void shuffleArray(int[] array) {
        int indexOf1 = -1;
        int mylen = array.length;
        for (int i = mylen - 1; i > 0; i--) {
            int index = Globals.random.nextInt(i + 1);
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
            if (Globals.random.nextFloat() < a) {
                int temp = array[indexOf1];
                array[indexOf1] = array[0];
                array[0] = temp;
            }
        } else if (array[0] == 3) {
            // here we decrease the cahnce of the main path going up if it would go up and
            // can't go down
            if (Globals.random.nextFloat() < 0.25f) {
                int otherIndex = (mylen == 2) ? 1 : Globals.random.nextInt(mylen - 1) + 1; // Random index from 1 to
                                                                                           // len-1
                int temp = array[0];
                array[0] = array[otherIndex];
                array[otherIndex] = temp;
            }
        }
    }

    private void mainIsStuck() {
        this.stuck = true;
    }

    private void makeNewMain(DungeonGenerationData genData, int depth, int maxDiff, Coord coord, int dir) {
        DungeonRoom newRoom = new DungeonRoom(genData, depth, maxDiff, coord, true, this);
        System.out.println(newRoom);
        this.neighbours[dir] = newRoom;
        newRoom.neighbours[(dir + 2) % 4] = this;
    }

    private void makeNewDungeonRoom(DungeonGenerationData genData, int depth, int maxDifficulty, Coord coord, int dir) {
        // Create a new DungeonRoom with the given parameters
        DungeonRoom newRoom = new DungeonRoom(genData, depth, maxDifficulty, coord, false, this);
        this.neighbours[dir] = newRoom;
        newRoom.neighbours[(dir + 2) % 4] = this;
    }

    // region Getter
    private int getNumNewRooms(int depth, int numNeighbours) {
        int ret;
        ret = Globals.random.nextInt(1, numNeighbours + 1);
        if (ret == numNeighbours) {
            return ret;
        }
        if (isMain && ret < 2) {
            ret++;
        } else if (!isMain && depth == 1) {
            ret = (ret % 2) + 1;
        }
        return ret;
    }

    private byte getType(boolean isMain, int depth) {
        if (isMain && depth == 0) {
            return 3;
        } else if (isMain) {
            return 1;
        }
        if (depth == 0) {
            return 2; // Leaf room
        }
        // For non-main rooms, we randomly choose between 1 and 2 weighted by depth
        return (byte) ((Globals.random.nextInt(depth * 2) == 0) ? 2 : 1);
    }

    private static int calcDifficulty(int depth, int maxDifficulty) {
        return maxDifficulty - depth;
    }

    private static void flagRoom(BitSet roomStructure, Coord xy, int arrayLen) {
        int index = xy.y * arrayLen + xy.x;
        roomStructure.set(index);
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

    private static Coord getStartXY(int depth, int branchDepthCap) {
        int x = depth + branchDepthCap;
        int y = depth + branchDepthCap - 2;
        return new Coord(x, y);
    }
    // endregion

    // region: Print func
    public void print() {
        if (this.type != 0) {
            return; // Only print the start room
        }
        int x = (representation[0][0].length - 1) / 2; // Center x-coordinate
        int y = (representation.length - 1) / 2 - 1; // Center y-coordinate

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
        rooms[y][0][x] = "\u250C" + ((neighbours[3] == null) ? "\u2500" : "d") + "\u2510";
        rooms[y][1][x] = ((neighbours[0] == null) ? "\u2502" : "d") + this.type
                + ((neighbours[2] == null) ? "\u2502" : "d");
        rooms[y][2][x] = "\u2514" + ((neighbours[1] == null) ? "\u2500" : "d") + "\u2518";
        int index = 0;
        for (DungeonRoom neighbor : neighbours) {
            if (neighbor != null) {
                int[] coords = dirToXY(index, x, y);
                neighbor.getString(rooms, coords[0], coords[1]);
            }
            index++;
        }
        this.visited = false; // Reset visited status for future calls
    }
    // endregion

    public void run() {
        if (this.runFunc != null) {
            this.runFunc.run();
        } else {
            System.out.println("No run function defined for this room.");
        }
    }
}
