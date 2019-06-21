package art.lookingup.pacman;

public enum Heading {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    Heading turn(Turn turn) {
	switch (turn) {
	case LEFT:
	    return left();
	case RIGHT:
	    return right();
	}
	return this;
    }

    Heading left() {
	switch (this) {
	case NORTH: return WEST;
	case EAST:  return NORTH;
	case SOUTH: return EAST;
	case WEST:  return SOUTH;
	}
	return null;
    }

    Heading right() {
	switch (this) {
	case NORTH: return EAST;
	case EAST:  return SOUTH;
	case SOUTH: return WEST;
	case WEST:  return NORTH;
	}
	return null;
    }

    Heading opposite() {
	switch (this) {
	case NORTH: return SOUTH;
	case EAST:  return WEST;
	case SOUTH: return NORTH;
	case WEST:  return EAST;
	}
	return null;
    }

    float theta() {
	switch (this) {
	case NORTH: return (float)Math.PI * 3 / 2;
	case EAST:  return 0;
	case SOUTH: return (float)Math.PI / 2;
	case WEST:  return (float)Math.PI;
	}
	return 0;
    }
};

