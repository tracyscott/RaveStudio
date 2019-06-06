package art.lookingup;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.TEST)
public class RGBManual extends LXPattern {
  public final CompoundParameter redKnob = new CompoundParameter("R", 0.5f, 0.0f, 1.0f).setDescription("R");
  public final CompoundParameter greenKnob = new CompoundParameter("G", 0.5f, 0.0f, 1.0f).setDescription("G");
  public final CompoundParameter blueKnob = new CompoundParameter("B", 0.0f, 0.0f, 1.0f).setDescription("B");

  public RGBManual(LX lx) {
    super(lx);
    addParameter(redKnob);
    addParameter(greenKnob);
    addParameter(blueKnob);
  }

  /**
   *  Allow manual specification of R, G, B.  Useful for testing lights for color correction purposes.
   * @param deltaMs
   */
  public void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = LXColor.rgb((int)(redKnob.getValue() * 255f), (int)(greenKnob.getValue() * 255f),
          (int)(blueKnob.getValue() * 255f));
    }
  }
}
