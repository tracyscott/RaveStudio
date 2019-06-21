package art.lookingup.pacman;

import processing.core.PGraphics;

class Position {
    public static final int BLOCK_PIXELS = PacmanBoard.BLOCK_PIXELS;
    public static final int HALF_BLOCK = BLOCK_PIXELS / 2;

    public final static float HALF_WIDTH = PacmanBoard.BOARD_WIDTH / 2f;
    public final static float HALF_HEIGHT = PacmanBoard.BOARD_HEIGHT / 2f;

    Position(PacmanBoard board, Heading h, int x, int y, int subx, int suby) {
	this.board = board;
	this.heading = h;
	this.blockX = x;
	this.blockY = y;
	this.subX = subx;
	this.subY = suby;
    }

    PacmanBoard board;
    Heading heading;
    int blockX;
    int blockY;
    int subX;
    int subY;

    void translate(PGraphics pg) {
	pg.translate(blockX * BLOCK_PIXELS + subX - BLOCK_PIXELS,
		     blockY * BLOCK_PIXELS + subY - BLOCK_PIXELS);
    }
    
    void advance() {
	if (subX == HALF_BLOCK && subY == HALF_BLOCK) {
	    if (!block().walkTo.contains(heading)) {
		return;
	    }
	}	    
	switch (heading) {
	case EAST:
	    subX++;
	    if (subX == BLOCK_PIXELS) {
		blockX++;
		subX = 0;

		// Portal case
		if (blockX == board.MAZE_WIDTH) {
		    blockX = 0;
		}		    
	    }
	    break;
	case SOUTH:
	    subY++;
	    if (subY == BLOCK_PIXELS) {
		blockY++;
		subY = 0;
	    }
	    break;
	case WEST:
	    if (subX == 0) {
		subX = BLOCK_PIXELS;
		blockX--;

		// Portal case
		if (blockX < 0) {
		    blockX = board.MAZE_WIDTH - 1;
		}		    
	    }
	    subX--;
	    break;
	case NORTH:
	    if (subY == 0) {
		subY = BLOCK_PIXELS;
		blockY--;
	    }
	    subY--;
	    break;
	}
    }

    Heading randomWalkDir() {
	PacmanBoard.Block b = block();
	Heading o = heading.opposite();
	assert(b.walkTo.contains(o));

	if (b.walkTo.size() == 2) {
	    // Find the one exit.
	    for (Heading h : b.walkTo) {
		if (h != o) {
		    return h;
		}
	    }
	}

	int n = board.rand.nextInt(b.walkTo.size() - 1);

	for (Heading h : b.walkTo) {
	    if (h == o) {
		continue;
	    }

	    if (n == 0) {
		return h;
	    }
	    n--;
	}
	return null;
    }

    boolean isGhostHouse() {
	return board.blocks[blockY][blockX].btype == PacmanBoard.BlockType.GHOST;
    }

    PacmanBoard.Block block() {
	return board.blocks[blockY][blockX];
    }

    float distance(Position o) {
        float dX = (blockX * BLOCK_PIXELS + subX) - (o.blockX * BLOCK_PIXELS + o.subX);
        float dY = (blockY * BLOCK_PIXELS + subY) - (o.blockY * BLOCK_PIXELS + o.subY);
        return (float)Math.sqrt(dX * dX + dY * dY);
    }
};
