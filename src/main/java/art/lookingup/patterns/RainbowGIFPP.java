package art.lookingup.patterns;

import art.lookingup.RaveModel3D;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;

/**
 * Pixel perfect animated GIFs.  Uses base class with directory data/gifpp, default file of life2.gif, and
 * no antialias toggle.
 */
@LXCategory(LXCategory.FORM)
public class RainbowGIFPP extends RainbowGIFBase {
  public RainbowGIFPP(LX lx) {
    super(lx, RaveModel3D.POINTS_WIDE, RaveModel3D.POINTS_HIGH,
        "gifpp/",
        "life2",
        false);
  }

  protected void renderToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(lx.getModel(), images[(int)currentFrame], colors);
  }
}
