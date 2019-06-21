package art.lookingup.pacman;

import static processing.core.PConstants.P2D;
import static processing.core.PConstants.CLOSE;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class PacmanSprite {

    public static final int BLOCK_PIXELS = PacmanBoard.BLOCK_PIXELS;
    public static final int SPRITE_SIZE = 2 * BLOCK_PIXELS;
    public static final int NUM_FRAMES = BLOCK_PIXELS;

    // Pac is 13 pixels in the original, so 13/8 times larger than an
    // original 8x8 block.
    public static final float PAC_DIAMETER = BLOCK_PIXELS * 13f / 8f;
    // Pac's mouth hinges off center.
    public static final float MOUTH_OFFSET = BLOCK_PIXELS * 6.5f / 8f;

    // Eyes for Neva:
    public static final float EYE_OFFSET_X = BLOCK_PIXELS * 5.5f / 8f;
    public static final float EYE_OFFSET_Y = BLOCK_PIXELS * 4.5f / 8f;
    public static final float EYE_DIAMETER_X = BLOCK_PIXELS * 3.5f / 8f;
    public static final float EYE_DIAMETER_Y = BLOCK_PIXELS * 2.5f / 8f;

    // Half-angle of mouth opening at peak.
    public static final float MOUTH_HALFANGLE = (float) Math.PI / 2;

    PImage []frames;

    public PacmanSprite(PApplet app) {
	frames = new PImage[NUM_FRAMES];

	for (int i = 0; i < NUM_FRAMES; i++) {
	    float halftheta = MOUTH_HALFANGLE * (float)i / (float)NUM_FRAMES;
	    float rise = (float)BLOCK_PIXELS * (float)Math.sin(halftheta);

	    PGraphics pg = app.createGraphics(SPRITE_SIZE, SPRITE_SIZE);
	    pg.beginDraw();
	    pg.background(255, 255, 0);

	    // Eyes!
	    // pg.translate(EYE_OFFSET_X, EYE_OFFSET_Y);
	    // pg.fill(255);
	    // pg.noStroke();
	    // pg.arc(0, 0, EYE_DIAMETER_X, EYE_DIAMETER_Y, 0, (float) Math.PI * 2);
	    // pg.fill(0);
	    // pg.arc(0, 0, EYE_DIAMETER_X / 2, EYE_DIAMETER_X / 2, 0, (float) Math.PI * 2);
	    
	    pg.endDraw();

	    PGraphics mask = app.createGraphics(SPRITE_SIZE, SPRITE_SIZE);

	    mask.beginDraw();
	    // Default mask all.
	    mask.background(0);

	    // Unmask the circle.
	    mask.pushMatrix();
	    mask.translate(SPRITE_SIZE/2, SPRITE_SIZE/2);
	    mask.noStroke();
	    mask.fill(255);
	    mask.arc(0, 0, PAC_DIAMETER, PAC_DIAMETER, 0, (float) Math.PI * 2);
	    mask.popMatrix();
	    
	    // Mask the mouth.
	    mask.pushMatrix();
	    mask.fill(0);
	    mask.beginShape();
	    mask.vertex(MOUTH_OFFSET, BLOCK_PIXELS);
	    mask.vertex(SPRITE_SIZE, BLOCK_PIXELS + rise);
	    mask.vertex(SPRITE_SIZE, BLOCK_PIXELS - rise);
	    mask.endShape(CLOSE);
	    mask.popMatrix();

	    mask.endDraw();
	    
	    frames[i] = pg.get();
	    frames[i].mask(mask.get());
	}
    }
};
