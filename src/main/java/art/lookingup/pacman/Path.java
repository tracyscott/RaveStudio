package art.lookingup.pacman;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

class Path {
    PacmanBoard board;
    BitSet unsettled;
    BitSet settled;
    int []predecessors;
    int []distances;

    Path(PacmanBoard board) {
        int size = PacmanBoard.MAZE_WIDTH * PacmanBoard.MAZE_HEIGHT;
        this.board = board;
        this.unsettled = new BitSet(size);
        this.settled = new BitSet(size);
        this.predecessors = new int[size];
        this.distances = new int[size];
    }

    int distance(PacmanBoard.Block tgt) {
        return distances[tgt.index];
    }
    
    void locate(PacmanBoard.Block tgt, ArrayList<PacmanBoard.Block> path) {
        PacmanBoard.Block step = tgt;
        path.add(step);
        while (predecessors[step.index] != -1) {
            step = board.atIndex(predecessors[step.index]);
            path.add(step);
        }
        Collections.reverse(path);
    }

    void findShortest(PacmanBoard.Block src, PacmanBoard.Block exclude) {
        unsettled.clear();
        settled.clear();
        Arrays.fill(predecessors, -1);
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[src.index] = 0;
        unsettled.set(src.index);

        while (true) {
            PacmanBoard.Block minimum = null;
            for (int i = unsettled.nextSetBit(0); i >= 0; i = unsettled.nextSetBit(i+1)) {
                PacmanBoard.Block block = board.atIndex(i);
                if (minimum == null) {
                    minimum = block;
                } else {
                    if (distances[block.index] < distances[minimum.index]) {
                        minimum = block;
                    }
                }
            }
            // System.err.println("Minimum is " + minimum);
            if (minimum == null) {
                // Unsettled is empty, done.
                return;
            }
            settled.set(minimum.index);
            unsettled.clear(minimum.index);

            for (Heading heading : minimum.walkTo) {
                PacmanBoard.Block neighbor = minimum.neighbor(heading);

                if (neighbor == exclude) {
                    continue;
                }

                // if (minimum.xpos == 0 && heading == Heading.WEST) {
                //     neighbor = board.blocks[minimum.ypos][PacmanBoard.MAZE_WIDTH-1];
                // } else if (minimum.xpos == PacmanBoard.MAZE_WIDTH-1 && heading == Heading.EAST) {
                //     neighbor = board.blocks[minimum.ypos][0];
                // }
                
                // System.err.println(" @ " + neighbor + " going " + heading);
                if (settled.get(neighbor.index)) {
                    // System.err.println("Neighbor " + heading + " is settled");
                    continue;
                }
                if (distances[neighbor.index] > distances[minimum.index] + 1) {
                    // System.err.println("Neighbor " + heading + " is new best path " + neighbor);
                    distances[neighbor.index] = distances[minimum.index] + 1;
                    predecessors[neighbor.index] = minimum.index;
                    unsettled.set(neighbor.index);
                }
            }
        }
    }
};
