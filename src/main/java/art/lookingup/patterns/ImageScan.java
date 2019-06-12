package art.lookingup.patterns;

import art.lookingup.RaveModel3D;
import heronarts.lx.LX;

public class ImageScan extends RainbowImageBase {
  protected int xOffset = 0;
  protected int yOffset = 0;

  protected boolean movingVertically = false;
  protected boolean movingForwards = true;
  protected int verticalMovement = 0;

  public ImageScan(LX lx) {
    super(lx, RaveModel3D.POINTS_WIDE, RaveModel3D.POINTS_HIGH,
        "imgpp/",
        "oregon.jpg",
        false,
        true);
    needsMirror = false;
  }

  protected void renderToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(lx.getModel(), image, colors, xOffset, yOffset);
    if (!movingVertically) {
      if (movingForwards) xOffset++;
      else xOffset--;
      if (xOffset >= tileImage.width - imageWidth) {
        movingForwards = false;
        movingVertically = true;
      } else if (xOffset < 0) {
        movingForwards = true;
        movingVertically = true;
      }
    } else {
      yOffset++;
      verticalMovement++;
      if (verticalMovement > imageHeight) {
        verticalMovement = 0;
        movingVertically = false;
      }
      if (yOffset + imageHeight >= tileImage.height) {
        yOffset = 0;
        xOffset = 0;
      }
    }
  }
}
