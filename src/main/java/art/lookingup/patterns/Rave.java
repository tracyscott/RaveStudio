package art.lookingup.patterns;

import art.lookingup.RaveModel3D;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;

public class Rave extends LXPattern {
  public Rave(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    for (LXPoint p : model.points) {
      if (RaveModel3D.pointIsR(p)) {
        colors[p.index] = Colors.RED;
      } else if (RaveModel3D.pointIsA(p)) {
        colors[p.index] = Colors.GREEN;
      } else if (RaveModel3D.pointIsV(p)) {
        colors[p.index] = Colors.BLUE;
      } else if (RaveModel3D.pointIsE(p)) {
        colors[p.index] = Colors.ORANGE;
      } else {
        colors[p.index] = Colors.YELLOW;
      }
    }
  }
}
