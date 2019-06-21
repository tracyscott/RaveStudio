package art.lookingup.pacman;

import static processing.core.PConstants.P2D;

import java.awt.Color;

import processing.core.PApplet;

public class App extends PApplet {
    public static void main( String[] args ) {
        PApplet.main("art.lookingup.pacman.App");
    }

    PacmanBoard board;
    PacmanSprite sprite;
    PacmanGame game;
    Heading    input;

    public void settings() {
	size(PacmanBoard.BOARD_WIDTH, PacmanBoard.BOARD_HEIGHT, P2D);
    }

    public void setup() {
        board = new PacmanBoard(this);
        sprite = new PacmanSprite(this);
	game = new PacmanGame(this, board, sprite);
    }

    public void draw() {
	game.render(millis(), input);
	image(game.get(), 0, 0);
        if (game.finished()) {
            board.reset();
            game = new PacmanGame(this, board, sprite);
        }
    }

    public static final int UP_KEY    = 'i';
    public static final int DOWN_KEY  = 'm';
    public static final int LEFT_KEY  = 'j';
    public static final int RIGHT_KEY = 'k';

    public void keyPressed() {
	switch (this.key) {
	case UP_KEY:
	    input = Heading.NORTH;
	    break;
	case DOWN_KEY:
	    input = Heading.SOUTH;
	    break;
	case RIGHT_KEY:
	    input = Heading.EAST;
	    break;
	case LEFT_KEY:
	    input = Heading.WEST;
	    break;
	}
    }

    public void keyReleased() {
	input = null;
    }
}
