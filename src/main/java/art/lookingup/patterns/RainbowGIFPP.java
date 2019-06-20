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
    int xOffset = (int) xOff.getValue();
    int yOffset = (int) yOff.getValue();
    // Constrain the values to the right edge and bottom.
    if (xOffset >= images[(int)currentFrame].width - 46)
      xOffset = images[(int)currentFrame].width - 47;
    if (yOffset >= images[(int)currentFrame].height - 46)
      yOffset = images[(int)currentFrame].height - 47;
    RenderImageUtil.imageToPointsPixelPerfect(lx.getModel(), images[(int)currentFrame], colors,
        xOffset, yOffset);
  }
}
