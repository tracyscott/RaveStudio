package art.lookingup.pacman;

import java.util.BitSet;

import static processing.core.PConstants.P2D;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

public class PacmanGame {

    public static final boolean RANDOM_WALK = false;
    public static final boolean SMART_WALK = true;

    public static final int BLOCK_PIXELS = PacmanBoard.BLOCK_PIXELS;
    public static final int HALF_BLOCK = BLOCK_PIXELS / 2;
    public static final int QUARTER_BLOCK = BLOCK_PIXELS / 4;

    public final static float HALF_WIDTH = PacmanBoard.BOARD_WIDTH / 2f;
    public final static float HALF_HEIGHT = PacmanBoard.BOARD_HEIGHT / 2f;

    public static final int NUM_FRAMES = PacmanSprite.NUM_FRAMES;

    // How long to wait for start. READY!
    public static final int READY_MILLIS = 3000;
    public static final int STANDSTILL_MILLIS = 4000;

    // Most imporant setting
    public static final float PAC_PERIOD = 8f * BLOCK_PIXELS;

    public static final float FASTEST_PAC_PERIOD = PAC_PERIOD / 2;
    public static final float MOUTH_PERIOD = 3 * PacmanSprite.NUM_FRAMES;
    public static final float ENERGIZER_PERIOD = 250;
    public static final float POWERUP_MAX_DURATION = 3000;
    public static final float POWERUP_MIN_DURATION = 1000;
    public static final float RANDOM_DECISION_PERIOD = 10;

    public static final int READY_FONT_SIZE = (int)(PacmanBoard.BLOCK_PIXELS * 1.33);

    PGraphics buffer;
    PApplet app;
    PacmanBoard board;
    PacmanSprite pac;
    GhostSprite ghosts[];
    BitSet dotsTaken;
    Heading lastInput;
    Heading turning;
    int turnsteps;
    PFont font;
    int dotsTotal;
    int decisions;

    double fastUntil;
    float fastPeriod;
    float normalPeriod;

    double startMillis;
    double elapsedMillis;
    double lastElapsed;
    double accumElapsed;
    public int pacTicks;

    public PacmanGame(PApplet app, PacmanBoard board, PacmanSprite pac) {
	this.app = app;
	this.board = board;
	this.pac = pac;
	this.dotsTaken = new BitSet(PacmanBoard.MAZE_HEIGHT * PacmanBoard.MAZE_WIDTH);
	this.buffer = app.createGraphics(PacmanBoard.MAZE_WIDTH * PacmanBoard.BLOCK_PIXELS,
					 PacmanBoard.MAZE_HEIGHT * PacmanBoard.BLOCK_PIXELS);
	this.font = app.createFont("Zig", READY_FONT_SIZE, false);
	this.ghosts = new GhostSprite[4];
	this.ghosts[0] = new GhostSprite.Blinky(board);
	this.ghosts[1] = new GhostSprite.Inky(board); 
	this.ghosts[2] = new GhostSprite.Pinky(board);
	this.ghosts[3] = new GhostSprite.Clyde(board);
        this.normalPeriod = PAC_PERIOD * (float)(0.93 + 0.07 * board.rand.nextFloat());
    }

    public boolean finished() {
        return dotsTotal == board.completeDots;
    }

    public void render(double millis, Heading input) {
	this.buffer.beginDraw();
	this.buffer.background(0);
	this.buffer.image(board.pg, 0, 0);
	advanceGame(millis, input);
	drawDots(this.buffer);
	drawReady(this.buffer);
	drawPac(this.buffer);
	for (GhostSprite ghost : this.ghosts) {
	    String []sprite = GhostSprite.GHOST_A;
	    if (ghost.ticks > 0) {
		int gi = (int)(ghost.ticks * ghost.period() / GhostSprite.FEATHER_PERIOD);
		sprite = (gi % 2) == 0 ? GhostSprite.GHOST_A : GhostSprite.GHOST_B;
	    }
	    ghost.drawGhost(this.buffer, sprite);
	}
	this.buffer.endDraw();
    }

    public PImage get() {
	this.buffer.loadPixels();
	return this.buffer;
    }

    public int pacX() {
	return board.pacpos.blockX * BLOCK_PIXELS + board.pacpos.subX;
    }

    public int pacY() {
	return board.pacpos.blockY * BLOCK_PIXELS + board.pacpos.subY;
    }

    public int redX() {
	return ghosts[0].pos.blockX * BLOCK_PIXELS + ghosts[0].pos.subX;
    }

    public int redY() {
	return ghosts[0].pos.blockY * BLOCK_PIXELS + ghosts[0].pos.subY;
    }
    
    void setDot(PacmanBoard.Block b) {
        if (b.btype == PacmanBoard.BlockType.POWERUP && !this.dotsTaken.get(b.index)) {
            fastUntil = elapsedMillis +
                POWERUP_MIN_DURATION +
                (POWERUP_MAX_DURATION - POWERUP_MIN_DURATION) * board.rand.nextFloat();
            fastPeriod = PAC_PERIOD - (float)(PAC_PERIOD - FASTEST_PAC_PERIOD) * board.rand.nextFloat();
        }
        switch (b.btype) {
        case DOT:
        case POWERUP:
        case SPECIALDOT:
            if (!this.dotsTaken.get(b.index)) {
                this.dotsTotal++;
                this.dotsTaken.set(b.index);
            }
            break;
        }       
    }

    Heading bestMove() {
	PacmanBoard.Block current = board.pacpos.block();
	Heading o = board.pacpos.heading.opposite();

        if (current.walkTo.size() == 2) {
            // Continue
	    for (Heading h : current.walkTo) {
		if (h != o) {
		    return h;
		}
	    }
        }

        decisions++;
        if (decisions % RANDOM_DECISION_PERIOD == 0) {
            return board.pacpos.randomWalkDir();
        }

	Heading nearestGhost = null;
	int dsq = 0;

	for (Heading h : current.walkTo) {
	    PacmanBoard.Block test = current.neighbor(h);

            if (h != o && test.btype == PacmanBoard.BlockType.SLOWGHOST) {
                return h;
            }

	    int dX = test.xpos - ghosts[0].pos.blockX;
	    int dY = test.ypos - ghosts[0].pos.blockY;

	    int testD = dX * dX + dY * dY;
	    if (nearestGhost == null || testD < dsq) {
		dsq = testD;
		nearestGhost = h;
	    }
	}

        Heading nearestDot = null;
        dsq = 0;
        
	for (Heading h : current.walkTo) {
            if (h == nearestGhost) {
                continue;
            }
	    PacmanBoard.Block test = current.neighbor(h);

            for (int j = 0; j < PacmanBoard.MAZE_HEIGHT; j++) {
                for (int i = 0; i < PacmanBoard.MAZE_WIDTH; i++) {
                    PacmanBoard.Block d = board.blocks[j][i];

                    switch (d.btype) {
                    case SPECIALDOT:
                    case DOT:
                        break;
                    default:
                        continue;
                    }

                    if (this.dotsTaken.get(d.index)) {
                        continue;
                    }
                
                    int dX = test.xpos - d.xpos;
                    int dY = test.ypos - d.ypos;

                    int testD = dX * dX + dY * dY;
                    
                    if (nearestDot == null || testD < dsq) {
                        dsq = testD;
                        nearestDot = h;
                    }
                }
            }
        }

        return nearestDot;
    }

    void pacAdvance(Heading input) {
	PacmanBoard.Block b = board.pacpos.block();
	Heading heading = board.pacpos.heading;

	if (input != null) {
	    lastInput = input;
	}

	boolean inTurn = this.turning != null;

	boolean entering = 
	    (!inTurn) &&
	    ((heading == Heading.EAST && board.pacpos.subX == 0) ||
	     (heading == Heading.WEST && board.pacpos.subX == BLOCK_PIXELS - 1) ||
	     (heading == Heading.SOUTH && board.pacpos.subY == 0) ||
	     (heading == Heading.NORTH && board.pacpos.subY == BLOCK_PIXELS - 1));

        if (SMART_WALK && entering && lastInput == null) {
            lastInput = bestMove();
        } else if (RANDOM_WALK && entering && lastInput == null) {
	    lastInput = board.pacpos.randomWalkDir();
	}

	boolean startTurn =
	    entering &&
	    (lastInput != null) &&
	    (lastInput != heading) &&
	    (lastInput.opposite() != heading) &&
	    b.walkTo.contains(lastInput);

	boolean reversal =
	    (!inTurn) && 
	    (lastInput != null) &&
	    (lastInput == heading.opposite()) &&
	    b.walkTo.contains(lastInput);

	boolean lateTurn =
	    (!inTurn) &&
	    (board.pacpos.subX == HALF_BLOCK) &&
	    (board.pacpos.subY == HALF_BLOCK) &&
	    (lastInput != null) &&
	    b.walkTo.contains(lastInput);

	if (startTurn) {
	    inTurn = true;
	    this.turning = lastInput;
	    this.lastInput = null;
	} else if (lateTurn || reversal) {
	    board.pacpos.heading = lastInput;
	    this.lastInput = null;
	}	    

	if (!inTurn) {
	    board.pacpos.advance();
	    if (board.pacpos.subX == HALF_BLOCK && board.pacpos.subY == HALF_BLOCK) {
		setDot(b);
	    }
	    return;
	}

	board.pacpos.advance();
	this.turnsteps++;
	if (turnsteps == QUARTER_BLOCK) {
	    setDot(b);
	}
	if (board.pacpos.subX == HALF_BLOCK && board.pacpos.subY == HALF_BLOCK) {
	    board.pacpos.heading = turning;
	    switch (turning) {
	    case NORTH:
		board.pacpos.subY = BLOCK_PIXELS - 1;
		board.pacpos.blockY--;
		break;
	    case SOUTH:
		board.pacpos.subY = 0;
		board.pacpos.blockY++;
		break;
	    case WEST:
		board.pacpos.subX = BLOCK_PIXELS - 1;
		board.pacpos.blockX--;
		break;
	    case EAST:
		board.pacpos.subX = 0;
		board.pacpos.blockX++;
		break;
	    }
	    turning = null;
	    turnsteps = 0;
	}
    }

    void drawReady(PGraphics pg) {
	if (pacTicks > 0) {
	    return;
	}
    
	if (font != null) {
	    pg.textFont(font);
	}
	pg.fill(255,255,0);
	pg.textSize(READY_FONT_SIZE);
	pg.text("READY!",
		11 * PacmanBoard.BLOCK_PIXELS,
		18 * PacmanBoard.BLOCK_PIXELS);
    }

    void setElapsed(double millis) {
	if (startMillis > STANDSTILL_MILLIS) {
	    elapsedMillis = millis - startMillis;
	}
    }

    void advanceGame(double millis, Heading input) {
	if (startMillis == 0) {
	    startMillis = millis + STANDSTILL_MILLIS;
	    return;
	}

	setElapsed(millis);

	for (int ticks = pacTicksNeeded(); pacTicks < ticks; pacTicks++) {
	    pacAdvance(input);
	}

	for (GhostSprite ghost : ghosts) {
	    for (int ticks = ghost.ticksNeeded(elapsedMillis);
		 ghost.ticks < ticks; ghost.ticks++) {
		ghost.advance();
	    }
	}	    
    }

    float pacPeriod() {
        if (fastUntil > 0 && elapsedMillis < fastUntil) {
            return fastPeriod;
        }
        return normalPeriod;
    }

    int pacTicksNeeded() {
        // @@@ copy-pasted from ghost behavior
	if (this.lastElapsed <= 0) {
	    this.lastElapsed = elapsedMillis;
	    return 0;
	}
	double delta = this.elapsedMillis - this.lastElapsed;
	float p = pacPeriod();
	float r = BLOCK_PIXELS / p;

	this.lastElapsed = this.elapsedMillis;
	this.accumElapsed += delta;

	int ticknow = (int)(r * this.accumElapsed - 0.5);

	this.accumElapsed -= ticknow / r;

	return pacTicks + ticknow;
    }

    void drawPac(PGraphics pg) {
	pg.pushMatrix();

	Heading heading = board.pacpos.heading;
	float angle = heading.theta();

	board.pacpos.translate(pg);

	// Fast turns
	if (turning != null) {
	    float rads = (float) (Math.PI / 2) * (float) turnsteps / HALF_BLOCK;

	    switch (turning) {
	    case NORTH:
		if (heading == Heading.WEST) {
		    pg.translate(+turnsteps, 0);
		    pg.translate(0, -HALF_BLOCK);
		    pg.translate(HALF_BLOCK * (float) -Math.sin(rads), HALF_BLOCK * (float) Math.cos(rads));
		    angle += rads;
		} else {
		    pg.translate(-turnsteps, 0);
		    pg.translate(0, -HALF_BLOCK);
		    pg.translate(HALF_BLOCK * (float) Math.sin(rads), HALF_BLOCK * (float) Math.cos(rads));
		    angle -= rads;
		}
		break;
	    case SOUTH:
		if (heading == Heading.WEST) {
		    pg.translate(+turnsteps, 0);
		    pg.translate(0, HALF_BLOCK);
		    pg.translate(HALF_BLOCK * (float) -Math.sin(rads), -HALF_BLOCK * (float) Math.cos(rads));
		    angle -= rads;
		} else {
		    pg.translate(-turnsteps, 0);
		    pg.translate(0, HALF_BLOCK);
		    pg.translate(HALF_BLOCK * (float) Math.sin(rads), -HALF_BLOCK * (float) Math.cos(rads));
		    angle += rads;
		}
		break;
	    case EAST:
		if (heading == Heading.NORTH) {
		    pg.translate(0, +turnsteps);
		    pg.translate(HALF_BLOCK, 0);
		    pg.translate(HALF_BLOCK * (float) -Math.cos(rads), -HALF_BLOCK * (float) Math.sin(rads));
		    angle += rads;
		} else {
		    pg.translate(0, -turnsteps);
		    pg.translate(HALF_BLOCK, 0);
		    pg.translate(HALF_BLOCK * (float) -Math.cos(rads), +HALF_BLOCK * (float) Math.sin(rads));
		    angle -= rads;
		}
		break;
	    case WEST:
		if (heading == Heading.NORTH) {
		    pg.translate(0, +turnsteps);
		    pg.translate(-HALF_BLOCK, 0);
		    pg.translate(HALF_BLOCK * (float) Math.cos(rads), -HALF_BLOCK * (float) Math.sin(rads));
		    angle -= rads;
		} else {
		    pg.translate(0, -turnsteps);
		    pg.translate(-HALF_BLOCK, 0);
		    pg.translate(HALF_BLOCK * (float) Math.cos(rads), +HALF_BLOCK * (float) Math.sin(rads));
		    angle += rads;
		}
		break;
	    }
	}

	// Rotate at the center point.
	pg.translate(BLOCK_PIXELS, BLOCK_PIXELS);
	pg.rotate(angle);
	pg.translate(-BLOCK_PIXELS, -BLOCK_PIXELS);

 	pg.image(pacFrame(), 0, 0);

	pg.popMatrix();
    }

    PImage pacFrame() {
	if (pacTicks <= 0) {
	    return pac.frames[0];
	}

	int fi = (int)(((float)pacTicks * PAC_PERIOD / MOUTH_PERIOD) % (2f * NUM_FRAMES));
	
	if (fi >= NUM_FRAMES) {
	    fi = 2 * NUM_FRAMES - 1 - fi;
	}
	return pac.frames[fi];
    }

    public boolean collision() {
        return board.pacpos.distance(ghosts[0].pos) < 1.5 * BLOCK_PIXELS;
    }
    
    void drawDots(PGraphics pg) {
	for (int j = 0; j < board.MAZE_HEIGHT; j++) {
	    int y = j * BLOCK_PIXELS + HALF_BLOCK;

	    for (int i = 0; i < board.MAZE_WIDTH; i++) {
		int x = i * BLOCK_PIXELS + HALF_BLOCK;
		PacmanBoard.Block b = board.blocks[j][i];

		if (dotsTaken.get(b.index)) {
		    continue;
		}

		switch (b.btype) {
		case DOT:
		case SPECIALDOT:
		    pg.noStroke();
		    pg.fill(200, 150, 0);
		    pg.ellipse(x, y, BLOCK_PIXELS/4, BLOCK_PIXELS/4);
		    break;
		case POWERUP:
		    int ei = (int)(elapsedMillis / ENERGIZER_PERIOD);
		    if ((ei % 2) == 0) {
			pg.noStroke();
			pg.fill(200, 150, 0);
			pg.ellipse(x, y, BLOCK_PIXELS - 2, BLOCK_PIXELS - 2);
		    }
		    break;
		}
	    }
	}
    }
};
