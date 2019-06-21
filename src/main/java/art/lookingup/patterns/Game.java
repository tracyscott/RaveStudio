package art.lookingup.patterns;

import static processing.core.PConstants.CLAMP;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import art.lookingup.RaveStudio;
import art.lookingup.pacman.PacmanBoard;
import art.lookingup.pacman.PacmanGame;
import art.lookingup.pacman.PacmanSprite;
import processing.core.PImage;
import processing.core.PGraphics;

/**
 */
@LXCategory(LXCategory.FORM)
public class Game extends RPattern {
    // Speed determines the overall speed of the entire pattern.
    public final CompoundParameter speedKnob =
        new CompoundParameter("Speed", 1.5, 0, 10).setDescription("Speed");

    public static final int BLOCK_PIXELS = PacmanBoard.BLOCK_PIXELS;
    public static final int BOARD_HEIGHT = PacmanBoard.BOARD_HEIGHT;
    public static final int BOARD_WIDTH = PacmanBoard.BOARD_WIDTH;

    public final static float HALF_WIDTH = BOARD_WIDTH / 2f;
    public final static float HALF_HEIGHT = BOARD_HEIGHT / 2f;

    public final static float FULL_HEIGHT = BOARD_HEIGHT;
    public final static float FULL_WIDTH = BOARD_WIDTH;

    // 10 minute game max.
    public final static float MAX_GAME_MILLIS = 600000;

    float telapsed;

    PGraphics pg;
    
    PacmanBoard board;
    PacmanGame game;
    PacmanSprite pac;
    PImage gboard;
    // PImage ctexture;

    float rainbowLX;
    float rainbowLY;
    float rainbowRX;
    float rainbowRY;

    final int width = 46;
    final int height = 46;

    float rainbowYOffset;
    float rainbowLROffset;

    public Game(LX lx) {
        super(lx);

	this.pg = RaveStudio.pApplet.createGraphics(width, height);

        this.telapsed = 0;

	this.board = new PacmanBoard(RaveStudio.pApplet);
	this.pac = new PacmanSprite(RaveStudio.pApplet);
	this.game = new PacmanGame(RaveStudio.pApplet, this.board, this.pac);
	
	//this.ctexture = RaveStudio.pApplet.loadImage("images/xyz-square-lookup.png");
        //this.ctexture.loadPixels();

        addParameter(speedKnob);
    }

    public void preDraw(double deltaMs) {
	double speed = speedKnob.getValue();
	telapsed += (float) (deltaMs * speed);

        if (game.finished() || this.telapsed > (float)(speed * MAX_GAME_MILLIS)) {
            this.board.reset();
            this.telapsed = (float)(deltaMs * speed);
            game = new PacmanGame(RaveStudio.pApplet, this.board, this.pac);
        }
        game.render(telapsed, null);
        gboard = game.get();
    }

    public void render(double deltaMs) {
	preDraw(deltaMs);

	float y;
	float gx2 = gboard.width / 2;

	// if (game.pacY() < gx2) {
	//     y = 0;
	// } else if (game.pacY() > gboard.height - gx2) {
	//     y = gboard.height - gboard.width;
	// } else {
	//     y = game.pacY() - gx2;
	// }

	y = Math.min(0, -game.pacY() + gx2);

	y = Math.max(y, gboard.width - gboard.height);

	y = y * width / gboard.width;

	// System.err.println("Y " + y + " PY " + game.pacY());

	pg.beginDraw();
	pg.image(gboard, 0, y, width, height * gboard.height / gboard.width);
	pg.endDraw();

	RenderImageUtil.imageToPointsPixelPerfect(lx.getModel(), pg.get(), colors, 0, 0);
    }

    // // Pacman stays on screen w/ this draw()
    // public void draw1(double deltaMs) {
    //     pg.background(0);

    //     float pacX = game.pacX();
    //     float pacY = game.pacY();

    //     float redX = game.redX();
    //     float redY = game.redY();

    //     float dX = pacX - pacX;
    //     float dY = pacY - pacY;
    //     float dAB = (float)Math.sqrt(dX * dX + dY * dY);
        
    //     // setControlPoints(pacX, pacY, redX, redY, dAB);
        
    //     float xratio = (float) width / (float) gboard.width;

    //     pg.translate(0, -rainbowYOffset);
    //     pg.scale(xratio, xratio);
    //     pg.translate(0, -pacY);

    //     // the READY! text is 6 blocks above the start position, pan the camera down.
    //     if (telapsed < PacmanGame.STANDSTILL_MILLIS) {
    //         if (telapsed < PacmanGame.READY_MILLIS) {
    //             pg.translate(0, PacmanBoard.BLOCK_PIXELS * 6f);
    //         } else {
    //             float ratio = (telapsed - PacmanGame.READY_MILLIS) /
    //                 (PacmanGame.STANDSTILL_MILLIS - PacmanGame.READY_MILLIS);
    //             pg.translate(0, (1 - ratio) * PacmanBoard.BLOCK_PIXELS * 6f);
    //         }
    //     }

    //     pg.image(gboard, 0, 0);
    // }
}
