package art.lookingup.patterns;

import static processing.core.PConstants.PI;
import static processing.core.PConstants.P3D;

import art.lookingup.RaveStudio;
import art.lookingup.colors.Colors;
import art.lookingup.model.space.Space3D;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import heronarts.lx.parameter.DiscreteParameter;
import org.joml.Vector3f;
import processing.core.PImage;
import processing.core.PGraphics;
import processing.core.PVector;

@LXCategory(LXCategory.FORM)
public class CubeLineup extends RPattern {

  public final int MAX_SIZE = 150;
  public final int MAX_CUBES = 200;
  public final float MAX_SPEED = 100;
  public final float SPEED_RATE = 4;

  public final float ROLL_RATE = 4;
  public final float MSHZ = 1.f / 10000.f;

  public final Vector3f DEFAULT_EYE = new Vector3f(0, Space3D.MIN_Y + 6, 60);

  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", Math.sqrt(MAX_SPEED), -MAX_SPEED, MAX_SPEED)
          .setDescription("Speed");
  public final CompoundParameter rollKnob =
      new CompoundParameter("Roll", -0.15, -1, 1).setDescription("Roll");
  public final CompoundParameter countKnob =
      new CompoundParameter("Count", 25, 10, MAX_CUBES).setDescription("Count");
  public final DiscreteParameter paletteKnob =
      new DiscreteParameter("Palette", 0, 0, Colors.ALL_PALETTES.length).setDescription("Palette");
  public final BooleanParameter randomPaletteKnob =
      new BooleanParameter("RandomPlt", true);

  public int[] palette;

  final int width = 46;
  final int height = 46;

  PGraphics pg;

  public CubeLineup(LX lx) {
    super(lx);

    this.pg = RaveStudio.pApplet.createGraphics(width, height, P3D);

    palette = Colors.RAINBOW_PALETTE;
    addParameter(speedKnob);
    addParameter(countKnob);
    addParameter(rollKnob);
    addParameter(paletteKnob);
    addParameter(randomPaletteKnob);

    space = new Space3D(DEFAULT_EYE);
    boxes = new Box[MAX_CUBES];
    Random rnd = new Random();

    eye = new PVector(space.eye.x, space.eye.y, space.eye.z);
    center = new PVector(space.center.x, space.center.y, space.center.z);

    int trials = 0;
    for (int i = 0; i < boxes.length; i++) {
      Box b;
      do {
        b = new Box(rnd);
        trials++;
      } while (!space.testBox(
          -b.radius(), -b.radius(), -b.radius(), b.radius(), b.radius(), b.radius()));

      boxes[i] = b;
    }

    System.err.printf(
        "Found boxes by %.1f%% rejection sampling\n", 100. * (float) boxes.length / (float) trials);
  }

  Box boxes[];
  double elapsed;
  double relapsed;
  PImage texture;
  Space3D space;
  PVector eye;
  PVector center;

  public class Box {
    PVector R;
    int W;

    float radius() {
      return (float) W / 2;
    }

    float partW() {
      return W / (float) palette.length;
    }

    Box(Random rnd) {
      W = (int) (rnd.nextFloat() * MAX_SIZE);
      R = PVector.random3D();
    }

    void drawPart(float zoff, int C, int part) {
      pg.beginShape();

      pg.fill(C);

      float xmin = -radius() + (float) part * partW();
      float xmax = xmin + partW();

      pg.vertex(xmin, -radius(), zoff);
      pg.vertex(xmax, -radius(), zoff);
      pg.vertex(xmax, +radius(), zoff);
      pg.vertex(xmin, +radius(), zoff);
      pg.endShape();
    }

    void drawRect(float zoff) {
      for (int i = 0; i < palette.length; i++) {
        drawPart(zoff, palette[i], i);
      }
    }

    void drawSides() {
      pg.pushMatrix();

      drawRect(radius());
      drawRect(-radius());

      pg.popMatrix();
    }

    void draw3Sides() {
      drawSides();

      pg.pushMatrix();
      pg.rotateX(PI / 2);
      drawSides();
      pg.popMatrix();

      pg.pushMatrix();
      pg.rotateY(PI / 2);
      drawSides();
      pg.popMatrix();
    }

    void draw() {
      pg.pushMatrix();

      pg.rotate((float) (elapsed / 10000), R.x, R.y, R.z);

      draw3Sides();

      pg.popMatrix();
    }
  };

  public void onActive() {
    super.onActive();
    if (randomPaletteKnob.getValueb()) {
      int paletteNumber = ThreadLocalRandom.current().nextInt(0, Colors.ALL_PALETTES.length);
      palette = Colors.ALL_PALETTES[paletteNumber];
    } else {
      palette = Colors.ALL_PALETTES[paletteKnob.getValuei()];
    }
  }

  public void render(double deltaMs) {
    double speed = 0;
    double knob = Math.abs(speedKnob.getValue());
    double direction = knob < 0 ? -1. : 1.;

    if (knob > 10) {
      speed = Math.log10(knob);
    } else {
      speed = knob / 10;
    }
    speed *= direction * SPEED_RATE;
    elapsed += deltaMs * speed;

    double rollspeed = rollKnob.getValue();
    relapsed += deltaMs * rollspeed;

    pg.beginDraw();
    pg.noStroke();
    pg.background(0);

    float theta = ((float) relapsed) * ROLL_RATE * MSHZ;

    pg.camera(
        eye.x,
        eye.y,
        eye.z,
        center.x,
        center.y,
        center.z,
        (float) Math.sin(theta),
        (float) Math.cos(theta),
        0);

    for (int i = 0; i < (int) countKnob.getValue(); i++) {
      if (i >= boxes.length) {
        break;
      }
      boxes[i].draw();
    }
    pg.endDraw();
    pg.loadPixels();

    RenderImageUtil.imageToPointsPixelPerfect(lx.getModel(), pg.get(), colors, 0, 0);
  }
}
