package art.lookingup.patterns;

import art.lookingup.RaveModel3D;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class RainbowImagePP extends RainbowImageBase {
  public RainbowImagePP(LX lx) {
    super(lx, RaveModel3D.POINTS_WIDE, RaveModel3D.POINTS_HIGH,
        "imgpp/",
        "oregon.jpg",
        false);
  }

  protected void renderToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(lx.getModel(), image, colors);
  }
}
