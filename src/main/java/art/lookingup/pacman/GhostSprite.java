package art.lookingup.pacman;

import processing.core.PGraphics;

abstract class GhostSprite {

    public static final int BLUE = 0xff0000ff;
    public static final int WHITE = 0xffffffff;
    public static final int RED = 0xffff0000;

    public static final int BLOCK_PIXELS = PacmanBoard.BLOCK_PIXELS;
    public static final int HALF_BLOCK = BLOCK_PIXELS / 2;

    public final static float HALF_WIDTH = PacmanBoard.BOARD_WIDTH / 2f;
    public final static float HALF_HEIGHT = PacmanBoard.BOARD_HEIGHT / 2f;

    public static final float GHOST_PERIOD = PacmanGame.PAC_PERIOD / 1.05f;
    public static final float FEATHER_PERIOD = GHOST_PERIOD * 10;

    abstract int color();

    Position pos;
    Heading pending;

    double lastElapsed;
    double accumElapsed;

    int ticks;

    GhostSprite(Position pos) {
	this.pos = pos;
	this.pending = pos.heading;
    }

    static class Blinky extends GhostSprite {
    	Blinky(PacmanBoard board) {
    	    super(new Position(board, board.leftOrRight(), 14, 11, 0, HALF_BLOCK));
    	}

    	int color() {
    	    return RED;
    	}
    };

    static class Inky extends GhostSprite {
    	Inky(PacmanBoard board) {
    	    super(new Position(board, Heading.NORTH, 12, 14, 0, HALF_BLOCK));
    	}

    	int color() {
    	    return 0xff00ffff;
    	}
    };

    static class Pinky extends GhostSprite {
    	Pinky(PacmanBoard board) {
    	    super(new Position(board, Heading.SOUTH, 14, 14, 0, HALF_BLOCK));
    	}

    	int color() {
    	    return 0xffffB8ff;
    	}
    };

    static class Clyde extends GhostSprite {
    	Clyde(PacmanBoard board) {
    	    super(new Position(board, Heading.NORTH, 16, 14, 0, HALF_BLOCK));
    	}

    	int color() {
	    return 0xffffb852;
	}
    };

    float period() {
	if (pos.block().btype == PacmanBoard.BlockType.SLOWGHOST) {
	    return GHOST_PERIOD * 2;
	}
	return GHOST_PERIOD;
    }

    Heading target() {
	PacmanBoard.Block current = pos.block();
	PacmanBoard.Block next = current.neighbor(pos.heading);

	if (!current.walkTo.contains(pos.heading)) {
	    throw new RuntimeException("Pending heading was invalid");
	}

	Heading o = pos.heading.opposite();
	assert(next.walkTo.contains(o));

	if (next == null) {
	    // Portal case.
	    return pos.heading;
	}

	if (next.walkTo.size() == 2) {
	    // Find the exit.
	    for (Heading h : next.walkTo) {
		if (h != o) {
		    return h;
		}
	    }
	}

	Heading best = null;
	int dsq = 0;

	for (Heading h : next.walkTo) {
	    if (h == o) {
		continue;
	    }
	    if (h == Heading.NORTH && next.neighbor(h).btype.isSpecial()) {
		continue;
	    }

	    // NOTE! This is Blinky's targeting mode, no scattering
	    // behavior, energizers have no effect.
	    PacmanBoard.Block test = next.neighbor(h);

	    int dX = test.xpos - pos.board.pacpos.blockX;
	    int dY = test.ypos - pos.board.pacpos.blockY;

	    int testD = dX * dX + dY * dY;
	    if (best == null || testD < dsq) {
		dsq = testD;
		best = h;
	    }
	}

	return best;
    }

    void ghostHouseAdvance() {
	if (pos.heading == Heading.NORTH) {
	    pos.subY--;
	} else {
	    pos.subY++;
	}

	if (pos.subY == 0) {
	    pos.heading = Heading.SOUTH;
	} else if (pos.subY == BLOCK_PIXELS-1) {
	    pos.heading = Heading.NORTH;
	}
    }

    int ticksNeeded(double elapsed) {
	if (this.lastElapsed <= 0) {
	    this.lastElapsed = elapsed;
	    return 0;
	}
	double delta = elapsed - this.lastElapsed;
	float p = period();
	float r = BLOCK_PIXELS / p;

	this.lastElapsed = elapsed;
	this.accumElapsed += delta;

	int ticknow = (int)(r * this.accumElapsed - 0.5);

	this.accumElapsed -= ticknow / r;

	return this.ticks + ticknow;
    }
	
    void advance() {
	if (pos.isGhostHouse()) {
	    ghostHouseAdvance();
	    return;
	}

	if (pos.subX == HALF_BLOCK && pos.subY == HALF_BLOCK) {
	    pos.heading = pending;

	    pending = target();

	    pos.advance();
	} else {
	    pos.advance();
	}
    }

    void drawGhost(PGraphics pg, String []pixels) {
	int eyeX = 0, eyeY = 0;
	int pupX = 0, pupY = 0;
	int offX = 0;
	int offY = 0; 

	switch (pending) {
	case NORTH:
	    eyeX = 3;
	    eyeY = 2;
	    pupX = 1;
	    pupY = 0;
	    break;
	case SOUTH:
	    eyeX = 3;
	    eyeY = 6;
	    pupX = 1;
	    pupY = 3;
	    break;
	case EAST:
	    eyeX = 4;
	    eyeY = 4;
	    pupX = 2;
	    pupY = 2;
	    break;
	case WEST:
	    eyeX = 2;
	    eyeY = 4;
	    pupX = 0;
	    pupY = 2;
	    break;
	}

	pg.pushMatrix();
	pos.translate(pg);

	pg.fill(color());
	float onesixteenth = 2 * BLOCK_PIXELS / 16;
	for (int j = 0; j < 16; j++) {
	    for (int i = 0; i < 16; i++) {
		if (pixels[j].charAt(i) == 'c') {
		    pg.rect((offX + i) * onesixteenth, (offY + j) * onesixteenth, onesixteenth, onesixteenth);
		}
	    }
	}
	final int eyeOffset = 6;
	for (int eye = 0; eye < 2; eye++) {
	    pg.fill(WHITE);
	    for (int j = 0; j < 5; j++) {
		for (int i = 0; i < 4; i++) {
		    if (EYE_WHITE[j].charAt(i) == 'c') {
			pg.rect((offX + eyeX + i + eyeOffset * eye) * onesixteenth,
				(offY + eyeY + j) * onesixteenth, onesixteenth, onesixteenth);
		    }
		}
	    }
	    pg.fill(BLUE);
	    for (int j = 0; j < 2; j++) {
		for (int i = 0; i < 2; i++) {
		    pg.rect((offX + eyeX + pupX + i + eyeOffset * eye) * onesixteenth,
			    (offY + eyeY + pupY + j) * onesixteenth, onesixteenth, onesixteenth);
		}
	    }
	}	
	pg.popMatrix();
    }

    public static final String []GHOST_A = {
	"bbbbbbbbbbbbbbbb",
	"bbbbbbccccbbbbbb",
	"bbbbccccccccbbbb",
	"bbbccccccccccbbb",
	"bbccccccccccccbb",
	"bbccccccccccccbb",
	"bbccccccccccccbb",
	"bccccccccccccccb",
	"bccccccccccccccb",
	"bccccccccccccccb",
	"bccccccccccccccb",
	"bccccccccccccccb",
	"bccccccccccccccb",
	"bccbcccbbcccbccb",
	"bcbbbccbbccbbbcb",
	"bbbbbbbbbbbbbbbb",
    };

    public static final String []GHOST_B = {
	"bbbbbbbbbbbbbbbb",
	"bbbbbbccccbbbbbb",
	"bbbbccccccccbbbb",
	"bbbccccccccccbbb",
	"bbccccccccccccbb",
	"bbccccccccccccbb",
	"bbccccccccccccbb",
	"bccccccccccccccb",
	"bccccccccccccccb",
	"bccccccccccccccb",
	"bccccccccccccccb",
	"bccccccccccccccb",
	"bccccccccccccccb",
	"bccccbccccbccccb",
	"bbccbbbccbbbccbb",
	"bbbbbbbbbbbbbbbb",
    };

    public static final String []EYE_WHITE = {
	"bccb",
	"cccc",
	"cccc",
	"cccc",
	"bccb",
    };
};
