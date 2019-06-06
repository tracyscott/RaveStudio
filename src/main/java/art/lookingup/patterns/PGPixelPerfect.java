package art.lookingup.patterns;

import art.lookingup.RaveModel3D;
import heronarts.lx.LX;

/**
 * Abstract base class for pixel perfect Processing drawings.  Use this
 * class for 1-1 pixel mapping with the rainbow.  The drawing will be
 * a rectangle but in physical space it will be distorted by the bend of
 * the rainbow. Gets FPS knob from PGBase.
 */
abstract class PGPixelPerfect extends PGBase {
  public PGPixelPerfect(LX lx, String drawMode) {
    super(lx, RaveModel3D.POINTS_HIGH,
      RaveModel3D.POINTS_WIDE,
      drawMode);
  }

  protected void imageToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(lx.getModel(), pg, colors);
  }

  // Implement PGGraphics drawing code here.  PGPixelPerfect handles beginDraw()/endDraw();
  protected abstract void draw(double deltaDrawMs);
}
