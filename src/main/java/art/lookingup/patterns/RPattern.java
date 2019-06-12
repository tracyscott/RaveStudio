package art.lookingup.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;

abstract public class RPattern extends LXPattern {
  // Default behavior is to mirror the points on each side.  Set this to false in
  // order to render to a full 92x46 buffer representing both sides.  This allows
  // front to back effects.
  protected boolean needsMirror = true;

  public RPattern(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
    render(deltaMs);
    if (needsMirror) {
      // Copy front colors to back colors.
      for (int i = 0; i < 1050; i++) {
        colors[i + 1050] = colors[i];
      }
    }
  }

  /**
   * Rave patterns have access to both sides of the installation.  Each side is a subset of a 46x46 grid.
   * The first half of the LXPoints are on the front side.  The second half of the points are on the backside.
   * Most existing patterns from Rainbow will default to isMirrored = true which means that we will copy
   * the colors from the front side to the backside./
   * @param deltaMs
   */
  public abstract void render(double deltaMs);
}
