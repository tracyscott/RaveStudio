package art.lookingup.pacman;

import java.util.EnumSet;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PGraphics;

import static processing.core.PConstants.CLOSE;

public class PacmanBoard {
    public static final String []BOARD = {
	"                            ", // 0
	" dddddddddddd  dddddddddddd ", // 1
	" d    d     d  d     d    d ", // 2
	" P    d     d  d     d    P ", // 3
	" d    d     d  d     d    d ", // 4
	" dddddddddddddddddddddddddd ", // 5
	" d    d  d        d  d    d ",
	" d    d  d        d  d    d ",
	" dddddd  dddd  dddd  dddddd ",
	"      d     s  s     d      ",
	"xxxxx d     E  E     d xxxxx", // ...
	"xxxxx d  ssssssssss  d xxxxx", // 11 GHOST START!
	"xxxxx d  sggggggggs  d xxxxx", // 12
	"      d  sggggggggs  d      ",
	"WWWWWWdsssggggggggsssdWWWWWW", // 14 GHOSTS!
	"      d  sggggggggs  d      ",
	"xxxxx d  sggggggggs  d xxxxx", // 16
	"xxxxx d  ssssssssss  d xxxxx", // 17 READY!
	"xxxxx d  s        s  d xxxxx",
	"      d  s        s  d      ",
	" dddddddddddd  dddddddddddd ",
	" d    d     d  d     d    d ",
	" d    d     D  D     d    d ",
	" Pdd  dddddddSSddddddd  ddP ", // 23 START!
	"   d  d  d        d  d  d   ",
	"   d  d  d        d  d  d   ",
	" dddddd  dddd  dddd  dddddd ",
	" d          d  d          d ",
	" d          d  d          d ",
	" dddddddddddddddddddddddddd ",
	"                            ", // 30
    };  // 31

    public final static int MAZE_WIDTH = BOARD[0].length();
    public final static int MAZE_HEIGHT = BOARD.length;
    public final static int BLOCK_PIXELS = 32;
    public final static int OG_PIXELS = 8;

    // Note: 2 is an exageraton for RAVE SIGN
    public final static float STROKE = 2 * (float) BLOCK_PIXELS / (float) OG_PIXELS;

    public final static int BOARD_HEIGHT = MAZE_HEIGHT * BLOCK_PIXELS;
    public final static int BOARD_WIDTH = MAZE_WIDTH * BLOCK_PIXELS;

    public final static int GHOSTHOUSE_X0 = 10;
    public final static int GHOSTHOUSE_X1 = 17;
    public final static int GHOSTHOUSE_Y0 = 12;
    public final static int GHOSTHOUSE_Y1 = 16;
    public final static int GHOSTHOUSE_LEX = 12;
    public final static int GHOSTHOUSE_REX = 15;

    public final static float EXTERIOR_OFFSET = (float)7./(float)8.;
    public final static float INTERIOR_OFFSET = (float)4./(float)8.;

    public final static float MAX_DISTANCE = (float) Math.sqrt(BLOCK_PIXELS * (MAZE_WIDTH - 2) * BLOCK_PIXELS * (MAZE_WIDTH - 2) +
							       BLOCK_PIXELS * (MAZE_HEIGHT - 2) * BLOCK_PIXELS * (MAZE_HEIGHT - 2));

    enum BlockType {
	DOT,          // d
	SPECIALDOT,   // D (Ghosts may not turn north)
	SPECIALSPACE, // E (Ghosts may not turn north)
	EMPTY,        // s
	SLOWGHOST,    // W
	POWERUP,      // P
	START,        // S
	BORDER,       // <space>
	XBORDER,      // x
	GHOST;        // g

	boolean isSpecial() {
	    return this == SPECIALDOT || this == SPECIALSPACE;
	}
    };

    enum Border {
	EXTERIOR,
	INTERIOR;
    };

    class Block {
	BlockType btype;

	int index;
	int xpos;
	int ypos;

	boolean drawn;

	EnumSet<Heading> walkTo;
	
	Block neighbor(Heading dir) {
	    switch (dir) {
	    case NORTH:
		return north();
	    case EAST:
		return east();
	    case SOUTH:
		return south();
	    case WEST:
		return west();
	    }
	    return null;
	}

	Block north() {
	    if (ypos == 0) {
		return null;
	    }
	    return blocks[ypos-1][xpos];
	}

	Block south() {
	    if (ypos == MAZE_HEIGHT-1) {
		return null;
	    }
	    return blocks[ypos+1][xpos];
	}

	Block east() {
	    if (xpos == MAZE_WIDTH-1) {
		return null;
	    }
	    return blocks[ypos][xpos+1];
	}

	Block west() {
	    if (xpos == 0) {
		return null;
	    }
	    return blocks[ypos][xpos-1];
	}

	boolean canWalk() {
	    switch (this.btype) {
	    case POWERUP:
	    case START:
	    case DOT:
	    case SPECIALDOT:
	    case SPECIALSPACE:
	    case EMPTY:
	    case SLOWGHOST:
		return true;
	    }
	    return false;
	}

	public String toString() {
	    return "<" + xpos + "," + ypos + ">";
	}
    };

    public PGraphics pg;
    Block [][]blocks;
    Block leftPortal;
    Block rightPortal;
    Random rand;
    Position pacpos;
    int completeDots;

    public PacmanBoard(PApplet app) {
	this.pg = app.createGraphics(MAZE_WIDTH * BLOCK_PIXELS, MAZE_HEIGHT * BLOCK_PIXELS);
	this.blocks = new Block[MAZE_HEIGHT][MAZE_WIDTH];
	this.rand = new Random();

	for (int j = 0; j < MAZE_HEIGHT; j++) {
	    for (int i = 0; i < MAZE_WIDTH; i++) {
		Block b = new Block();
		BlockType btype = BlockType.BORDER;

		b.index = j * MAZE_WIDTH + i;
		b.xpos = i;
		b.ypos = j;

		switch (BOARD[j].charAt(i)) {
		case 'E': b.btype = BlockType.SPECIALSPACE; break;
		case 'D': b.btype = BlockType.SPECIALDOT; completeDots++; break;
		case 'd': b.btype = BlockType.DOT; completeDots++; break;
		case ' ': b.btype = BlockType.BORDER; break;
		case 'x': b.btype = BlockType.XBORDER; break;
		case 'g': b.btype = BlockType.GHOST; break;
		case 's': b.btype = BlockType.EMPTY; break;
		case 'W': b.btype = BlockType.SLOWGHOST; break;
		case 'S': b.btype = BlockType.START; break;
		case 'P': b.btype = BlockType.POWERUP; completeDots++; break;
		}

		blocks[j][i] = b;
	    }
	}

	this.leftPortal = blocks[14][0];
	this.rightPortal = blocks[14][MAZE_WIDTH-1];

	for (int j = 0; j < MAZE_HEIGHT; j++) {
	    for (int i = 0; i < MAZE_WIDTH; i++) {
		Block b = blocks[j][i];
		if (!b.canWalk()) {
		    continue;
		}

		b.walkTo = EnumSet.noneOf(Heading.class);

		for (Heading h : Heading.values()) {
		    Block n = b.neighbor(h);
		    if (n != null && n.canWalk()) {
			b.walkTo.add(h);
		    }
		}
	    }
	}

	this.leftPortal.walkTo.add(Heading.WEST);
	this.rightPortal.walkTo.add(Heading.EAST);

        this.reset();
	
	render();
    }

    Heading leftOrRight() {
        if (rand.nextBoolean() == true) {
            return Heading.WEST;
        }
        return Heading.EAST;
    }

    public void reset() {
	this.pacpos = new Position(this, leftOrRight(), 14, 23, 0, BLOCK_PIXELS / 2);
    }

    void render() {
	pg.beginDraw();
	pg.background(0);

	pg.pushMatrix();

	// Translate by half a pixel so that 1-pixel lines land on
	// whole-pixels.  N.B.: For BLOCK_PIXELS=8 (original pacman)
	// we should use a mid-point circle algorithm, not the arc()
	// primitive, to get whole-pixel; this changes the 4-pixel
	// square dots into 4-pixel crosses, for example. Shrug.
	pg.translate(0.5f, 0.5f);

	drawBorders();

	drawGhostHouse();

	pg.popMatrix();
	pg.endDraw();
    }

    void drawBorders() {
	resetDrawn();
	drawLeftBorderType(leftPortal, Border.EXTERIOR, Heading.EAST);
	drawLeftBorderType(rightPortal, Border.EXTERIOR, Heading.WEST);

	resetDrawn();
	drawLeftBorderType(leftPortal, Border.INTERIOR, Heading.EAST);
	drawLeftBorderType(rightPortal, Border.INTERIOR, Heading.WEST);

	// Interior searches for un-drawn edges.
	drawInterior();
    }

    // Note the assymetry, for drawing the border.  Left turns are
    // handled one ahead of the turn, right turns are handled when
    // the wall is reached.  This routine is used to draw the
    // border space to the left of the position.
    Turn nextLeftTurn(Block position, Heading heading, Border border) {
	Heading leftDir = heading.left();
	Heading rightDir = heading.right();

	Block ahead = position.neighbor(heading);

	if (ahead == null) {
	    return Turn.STRAIGHT;
	}

	if (!ahead.canWalk()) {
	    if (border == Border.INTERIOR) {
		return Turn.RIGHT;
	    } else {
		// See if we can walk through this obstacle, for
		// the exterior border.
		Block a2 = ahead.neighbor(heading);
		if (a2 != null && a2.btype != BlockType.XBORDER) {
		    Block a3 = a2.neighbor(heading);
		    if (a3 != null && a3.btype != BlockType.XBORDER) {
			return Turn.STRAIGHT;
		    } else {
			return Turn.RIGHT;
		    }
		} else {
		    return Turn.RIGHT;
		}
		    
	    }
	}
	    
	Block ln = ahead.neighbor(leftDir);
	if (ln != null && ln.canWalk()) {
	    return Turn.LEFT;
	}

	return Turn.STRAIGHT;
    }

    void drawLeftBorderType(Block start, Border border, Heading heading) {
	Block block = start;

	while (block != null) {
	    Turn turn = nextLeftTurn(block, heading, border);

	    Heading leftDir = heading.left();
	    Block leftOf = block.neighbor(leftDir);

	    if (leftOf.drawn) {
		return;
	    }

	    if (turn == Turn.STRAIGHT) {
		drawStraightBorder(leftOf, leftDir, border);
		block = block.neighbor(heading);
		continue;
	    }

	    if (turn == Turn.LEFT) {
		drawLeftTurnBorder(leftOf, leftDir, border);
		block = block.neighbor(heading);
		block = block.neighbor(leftDir);
		block = block.neighbor(leftDir);
		heading = leftDir;
		continue;
	    }

	    drawStraightBorder(leftOf, leftDir, border);
	    drawStraightBorder(block.neighbor(heading), heading, border);

	    drawRightTurnBorder(leftOf.neighbor(heading), leftDir, border);
    
	    heading = heading.right();
	    block = block.neighbor(heading);
	    continue;
	}	
    }

    void drawStraightBorder(Block block, Heading perpendicular, Border border) {
	int blockX = block.xpos * BLOCK_PIXELS;
	int blockY = block.ypos * BLOCK_PIXELS;

	double offset;
	if (border == Border.EXTERIOR) {
	    offset = EXTERIOR_OFFSET;
	} else {
	    offset = INTERIOR_OFFSET;
	}

	block.drawn = true;
	
	int lineX0 = 0, lineY0 = 0, lineX1 = 0, lineY1 = 0;

	switch (perpendicular) {
	case NORTH:
	    lineY0 = blockY + (int)((double)BLOCK_PIXELS * (1-offset)) ;
	    lineY1 = lineY0;
	    lineX0 = blockX;
	    lineX1 = blockX + BLOCK_PIXELS;
	    break;

	case SOUTH:
	    lineY0 = blockY + (int)((double)BLOCK_PIXELS * (offset));
	    lineY1 = lineY0;
	    lineX0 = blockX;
	    lineX1 = blockX + BLOCK_PIXELS;
	    break;

	case EAST:
	    lineX0 = blockX + (int)((double)BLOCK_PIXELS * (offset));
	    lineX1 = lineX0;
	    lineY0 = blockY;
	    lineY1 = blockY + BLOCK_PIXELS;
	    break;

	case WEST:
	    lineX0 = blockX + (int)((double)BLOCK_PIXELS * (1-offset)) ;
	    lineX1 = lineX0;
	    lineY0 = blockY;
	    lineY1 = blockY + BLOCK_PIXELS;
	    break;
	    
	}

	pg.strokeWeight(STROKE);
	pg.stroke(0,0,255);

	pg.line(lineX0, lineY0, lineX1, lineY1);
    }

    void drawLeftTurnBorder(Block block, Heading perpendicular, Border border) {
	int blockX = block.xpos * BLOCK_PIXELS;
	int blockY = block.ypos * BLOCK_PIXELS;

	float offset;
	if (border == Border.EXTERIOR) {
	    offset = EXTERIOR_OFFSET;
	} else {
	    offset = INTERIOR_OFFSET;
	}

	block.drawn = true;

	float centerX0 = 0, centerY0 = 0, radius = 0, rot = 0;

	switch (perpendicular) {
	case NORTH:
	    centerY0 = (float)blockY;
	    centerX0 = (float)blockX;
	    radius = (float)(BLOCK_PIXELS * (1-offset)) * 2;
	    break;

	case SOUTH:
	    centerY0 = (float)blockY + BLOCK_PIXELS;
	    centerX0 = (float)blockX + BLOCK_PIXELS;
	    radius = (float)(BLOCK_PIXELS * (1-offset)) * 2;
	    rot = (float) (Math.PI);
	    break;

	case EAST:
	    centerX0 = (float)blockX + BLOCK_PIXELS;
	    centerY0 = (float)blockY;
	    radius = (float)(BLOCK_PIXELS * (1-offset)) * 2;
	    rot = (float)(Math.PI / 2.);
	    break;

	case WEST:
	    centerX0 = (float)blockX;
	    centerY0 = (float)(blockY + BLOCK_PIXELS);
	    radius = (float)(BLOCK_PIXELS * (1-offset)) * 2;
	    rot = (float)(3. * Math.PI / 2.);
	    break;
	    
	}

	pg.noFill();

	pg.strokeWeight(STROKE);
	pg.stroke(0,0,255);

	pg.pushMatrix();
	pg.translate(centerX0, centerY0);
	pg.rotate(rot);
	pg.arc(0, 0, radius, radius, 0, (float)(Math.PI/2.));
	pg.popMatrix();
    }

    void drawRightTurnBorder(Block block, Heading perpendicular, Border border) {
	int blockX = block.xpos * BLOCK_PIXELS;
	int blockY = block.ypos * BLOCK_PIXELS;

	float offset;
	if (border == Border.EXTERIOR) {
	    offset = EXTERIOR_OFFSET;
	} else {
	    offset = INTERIOR_OFFSET;
	}

	block.drawn = true;

	float centerX0 = 0, centerY0 = 0, radius = 0, rot = 0;

	switch (perpendicular) {
	case NORTH:
	    centerY0 = (float)(blockY + BLOCK_PIXELS);
	    centerX0 = (float)blockX;
	    radius = (float)(BLOCK_PIXELS * (offset)) * 2;
	    rot = (float)(3. * Math.PI / 2.);
	    break;

	case SOUTH:
	    centerY0 = (float)blockY;
	    centerX0 = (float)(blockX + BLOCK_PIXELS);
	    radius = (float)(BLOCK_PIXELS * (offset)) * 2;
	    rot = (float) (Math.PI / 2);
	    break;

	case EAST:
	    centerX0 = (float)blockX;
	    centerY0 = (float)blockY;
	    radius = (float)(BLOCK_PIXELS * (offset)) * 2;
	    break;

	case WEST:
	    centerX0 = (float)(blockX + BLOCK_PIXELS);
	    centerY0 = (float)(blockY + BLOCK_PIXELS);
	    radius = (float)(BLOCK_PIXELS * (offset)) * 2;
	    rot = (float)Math.PI;
	    break;
	    
	}

	pg.noFill();

	pg.strokeWeight(STROKE);
	pg.stroke(0,0,255);

	pg.pushMatrix();
	pg.translate(centerX0, centerY0);
	pg.rotate(rot);
	pg.arc(0, 0, radius, radius, 0, (float)(Math.PI/2.));
	pg.popMatrix();
    }
    
    void resetDrawn() {
	for (int j = 0; j < MAZE_HEIGHT; j++) {
	    for (int i = 0; i < MAZE_WIDTH; i++) {
		Block b = blocks[j][i];
		b.drawn = false;
	    }
	}
    }

    Block atIndex(int index) {
        return blocks[index / MAZE_WIDTH][index % MAZE_WIDTH];
    }

    void drawInterior() {
	for (int j = 0; j < MAZE_HEIGHT; j++) {
	    for (int i = 0; i < MAZE_WIDTH; i++) {
		Block b = blocks[j][i];

		if (b.btype != BlockType.BORDER) {
		    continue;
		}

		if (b.drawn) {
		    continue;
		}

		drawInteriorWall(b);
	    }
	}
    }

    void drawInteriorWall(Block start) {
	Block pos = null;
	Heading dir = null;
	// This is super cheesey.
	for (Heading h : Heading.values()) {
	    Block n = start.neighbor(h);
	    if (n == null) {
		// Edge of pg.
		return;
	    }

	    if (n.btype == BlockType.XBORDER) {
		// Exterior/Interior border (but not edge).
		return;
	    }

	    if (n.canWalk()) {
		pos = n;
		dir = h.left();
	    }
	}

	if (pos == null || pos.drawn) {
	    return;
	}

	Turn turn = nextLeftTurn(pos, dir, Border.INTERIOR);

	if (turn == Turn.LEFT) {
	    pos = pos.neighbor(dir).neighbor(dir.left());
	    dir = dir.left();
	} else if (turn == Turn.STRAIGHT) {
	    pos = pos.neighbor(dir);
	} else {
	    // This is possible for certain maps, but not the original map.
	    throw new RuntimeException("Not reached");
	}

	drawLeftBorderType(pos, Border.INTERIOR, dir);
    }


    float blpix(int x, float offset) {
	float bp = (float) BLOCK_PIXELS;
	return (float)(x * BLOCK_PIXELS) + offset * bp;
    }

    void drawGhostHouse() {
	float mio = 1-INTERIOR_OFFSET;
	float pio = INTERIOR_OFFSET;
	float meo = 1-EXTERIOR_OFFSET;
	float peo = EXTERIOR_OFFSET;

	pg.noFill();
	pg.beginShape();
	pg.strokeWeight(STROKE);
	pg.stroke(0, 0, 255);
	pg.vertex(blpix(GHOSTHOUSE_X0, mio), blpix(GHOSTHOUSE_Y0, mio));
	pg.vertex(blpix(GHOSTHOUSE_X0, mio), blpix(GHOSTHOUSE_Y1, pio));
	pg.vertex(blpix(GHOSTHOUSE_X1, pio), blpix(GHOSTHOUSE_Y1, pio));
	pg.vertex(blpix(GHOSTHOUSE_X1, pio), blpix(GHOSTHOUSE_Y0, mio));
	pg.vertex(blpix(GHOSTHOUSE_REX, meo), blpix(GHOSTHOUSE_Y0, mio));
	pg.vertex(blpix(GHOSTHOUSE_REX, meo), blpix(GHOSTHOUSE_Y0, peo));
	pg.vertex(blpix(GHOSTHOUSE_X1, meo), blpix(GHOSTHOUSE_Y0, peo));
	pg.vertex(blpix(GHOSTHOUSE_X1, meo), blpix(GHOSTHOUSE_Y1, meo));
	pg.vertex(blpix(GHOSTHOUSE_X0, peo), blpix(GHOSTHOUSE_Y1, meo));
	pg.vertex(blpix(GHOSTHOUSE_X0, peo), blpix(GHOSTHOUSE_Y0, peo));
	pg.vertex(blpix(GHOSTHOUSE_LEX, peo), blpix(GHOSTHOUSE_Y0, peo));
	pg.vertex(blpix(GHOSTHOUSE_LEX, peo), blpix(GHOSTHOUSE_Y0, mio));
	pg.endShape(CLOSE);	

	pg.fill(0xff, 0xc0, 0xcb);
	pg.noStroke();
	pg.beginShape();
	pg.vertex(blpix(GHOSTHOUSE_REX, meo), blpix(GHOSTHOUSE_Y0, mio+meo));
	pg.vertex(blpix(GHOSTHOUSE_REX, meo), blpix(GHOSTHOUSE_Y0, peo-meo));
	pg.vertex(blpix(GHOSTHOUSE_LEX, peo), blpix(GHOSTHOUSE_Y0, peo-meo));
	pg.vertex(blpix(GHOSTHOUSE_LEX, peo), blpix(GHOSTHOUSE_Y0, mio+meo));
	pg.endShape(CLOSE);
    }
}
