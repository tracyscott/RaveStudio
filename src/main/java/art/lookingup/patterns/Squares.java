package art.lookingup.patterns;

import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import java.util.concurrent.ThreadLocalRandom;
import static java.lang.Math.ceil;
import static processing.core.PConstants.HSB;

public class Squares extends PGFilterBase {

  public DiscreteParameter paletteKnob = new DiscreteParameter("palette", 0, 0, Colors.ALL_PALETTES.length + 1);
  public CompoundParameter bgAlpha = new CompoundParameter("bgalpha", 0.25, 0.0, 1.0);
  // Max triangle size.  Allow only small triangles for example.
  public CompoundParameter sizeKnob = new CompoundParameter("size", 15.0, 3.0, 45.0);
  public CompoundParameter maxOffScreen = new CompoundParameter("off", 0.0, 0.0, 30.0);

  public CompoundParameter fillAlpha = new CompoundParameter("falpha", 0.75, 0.0, 1.0);
  public CompoundParameter saturation = new CompoundParameter("sat", 0.5, 0.0, 1.0);
  public CompoundParameter bright = new CompoundParameter("bright", 1.0, 0.0, 1.0);
  public CompoundParameter blurRadiusKnob = new CompoundParameter("blurR", 0.0, 0.0, 10.0);
  public CompoundParameter blurPassKnob = new CompoundParameter("blurP", 0.0, 0.0, 40.0);
  public final BooleanParameter randomPaletteKnob =
      new BooleanParameter("RandomPlt", true);
  public int[] palette;
  public int randomPalette = 0;

  public Squares(LX lx) {
    super(lx);
    addParameter(paletteKnob);
    addParameter(bgAlpha);
    addParameter(sizeKnob);
    addParameter(maxOffScreen);
    addParameter(fillAlpha);
    addParameter(saturation);
    addParameter(blurRadiusKnob);
    addParameter(blurPassKnob);
    addParameter(randomPaletteKnob);
    doDirBlur = true;
  }

  /*
  A new random polygon with background fades.  Random 3 or 4 points?  Maybe just 3 points
  * since that will always easily render regardless of the point ordering.  Polygon should
  * be rendered with transparency on a 25% (configurable) transparent background for fading away.
  * What to do for coloring?  Be able to specify saturation while randomizing hue.  Or possibly
  * selecting a random number from 0 to sizeof(palette) and then use that color if palette box is
  * checked?  Or if palette dropdown has a selected palette so can be both rainbow and redbull.


   */
  public void draw(double drawDeltaMs) {
    pg.colorMode(HSB, 1.0f);
    pg.background(0.0f, 0.0f, 0.0f, bgAlpha.getValuef());

    for (int i = 0; i < 16; i++) {
      drawSquare(i);
    }
    blurRadius = (int) blurRadiusKnob.getValue();
    dirBlurPasses = (int) blurPassKnob.getValue();
    super.draw(drawDeltaMs);
  }
  @Override
  public void onActive() {
    if (randomPaletteKnob.getValueb()) {
      int paletteNumber = ThreadLocalRandom.current().nextInt(0, Colors.ALL_PALETTES.length);
      palette = Colors.ALL_PALETTES[paletteNumber];
      randomPalette = paletteNumber;
    } else {
      palette = Colors.ALL_PALETTES[paletteKnob.getValuei()];
    }
  }


  public void drawSquare(int squareNum) {
    float centerX =  squareNum%4 * (pg.width / 4);
    float centerY =  squareNum/4 * (pg.height / 4);

    /*
    float pt1X = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt1Y = (float)Math.random() * pg.height;
    float pt2X = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt2Y = (float)Math.random() * pg.height;
    float pt3X = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt3Y = (float)Math.random() * pg.height;
    */

    int whichPalette = paletteKnob.getValuei();

    if (randomPaletteKnob.getValueb())
      whichPalette = randomPalette;

    float h = 0.0f;
    float s = 0.0f;
    float b = 0.0f;

    if (whichPalette == 0) {
      h = (float) Math.random();
      s = saturation.getValuef();
      b = bright.getValuef();
    } else {
      int[] palette = Colors.ALL_PALETTES[whichPalette - 1];
      int index = (int) ceil(Math.random() * (palette.length)) - 1;
      if (index < 0) index = 0;
      int color = palette[index];
      float[] hsb = {0.0f, 0.0f, 0.0f};
      Colors.RGBtoHSB(color, hsb);
      h = hsb[0];
      s = hsb[1];
      b = hsb[2];
    }

    pg.noStroke();
    pg.fill(h, s, b, fillAlpha.getValuef());

    pg.rect(centerX, centerY, (int)sizeKnob.getValue(), (int)sizeKnob.getValue());
  }
}
