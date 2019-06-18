package art.lookingup.patterns;

import art.lookingup.RaveModel3D;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

public class Wiring extends RPattern {

  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", 61.0, 0.0, 61.0)
          .setDescription("Controls the frames per second.");
  public final CompoundParameter tailKnob =
      new CompoundParameter("Tail", 85.0, 0.0, 1051.0)
          .setDescription("Length of tail");

  protected int pos = 0;
  protected int tailLength = 3;
  protected double currentFrame = 0.0;
  protected int previousFrame = -1;
  protected double deltaDrawMs = 0.0;

  public Wiring(LX lx) {
    super(lx);
    addParameter(fpsKnob);
    addParameter(tailKnob);
    needsMirror = true;
  }

  public void render(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs / 1000.0) * fps;
    // We don't call draw() every frame so track the accumulated deltaMs for them.
    deltaDrawMs += deltaMs;
    if ((int) currentFrame > previousFrame) {
      // Time for new frame.
      renderWire(deltaDrawMs);
      previousFrame = (int) currentFrame;
      deltaDrawMs = 0.0;
    }
    // Don't let current frame increment forever.  Otherwise float will
    // begin to lose precision and things get wonky.
    if (currentFrame > 10000.0) {
      currentFrame = 0.0;
      previousFrame = -1;
    }
  }

  public void renderWire(double deltaMs) {
    int pIndex = RaveModel3D.frontWiringOrder.get(pos);
    for (int i = 0; i < colors.length; i++) {
      colors[i] = Colors.BLACK;
    }
    //System.out.println("pIndex=" + pIndex);
    colors[pIndex] = Colors.WHITE;
    int tailLength = (int)tailKnob.getValue();
    int visibleTailLength = (pos > tailLength)? tailLength : pos;
    if (visibleTailLength > 0) {
      //System.out.println("Visible tail length: " + visibleTailLength);
      float greyStep = 100f / (visibleTailLength + 1f);
      for (int i = 0; i < visibleTailLength; i++) {
        int tailPIndex = RaveModel3D.frontWiringOrder.get(pos - visibleTailLength + i);
        colors[tailPIndex] = LXColor.gray( greyStep * i);
      }
    }

    pos++;
    if (pos >= RaveModel3D.frontWiringOrder.size()) {
      pos = 0;
    }
  }
}
