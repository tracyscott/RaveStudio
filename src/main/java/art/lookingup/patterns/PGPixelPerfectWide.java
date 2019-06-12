package art.lookingup.patterns;

import art.lookingup.RaveModel3D;
import heronarts.lx.LX;

abstract class PGPixelPerfectWide extends PGBase {
  public PGPixelPerfectWide(LX lx, String drawMode) {
    super(lx, RaveModel3D.POINTS_WIDE * 2,
        RaveModel3D.POINTS_HIGH,
        drawMode);
  }

  protected void imageToPoints() {
    RenderImageUtil.imageToPointsPixelPerfectWide(lx.getModel(), pg, colors);
  }

  // Implement PGGraphics drawing code here.  PGPixelPerfect handles beginDraw()/endDraw();
  protected abstract void draw(double deltaDrawMs);
}
