package art.lookingup.patterns;

import art.lookingup.RaveModel3D;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

public class Rave extends LXPattern {
  public CompoundParameter flashTimeKnob = new CompoundParameter("fstime", 2000f, 0f, 5000f);
  public CompoundParameter flashHzKnob = new CompoundParameter("fshz", 3f, 0.01f, 20f);
  public BooleanParameter whiteKnob = new BooleanParameter("white", false);

  int curFlashCh = CH_R;
  double currentFlashTime = 0.0;
  static public final int CH_R = 0;
  static public final int CH_A = 1;
  static public final int CH_V = 2;
  static public final int CH_E = 3;
  static public final int CH_ALL = 4;
  public boolean chIsOn = true;
  public double curFlash = 0.0;

  public Rave(LX lx) {
    super(lx);
    addParameter(flashTimeKnob);
    addParameter(flashHzKnob);
    addParameter(whiteKnob);
  }

  public void run(double deltaMs) {
    if (curFlash > 1000.0f/flashHzKnob.getValue()) {
      curFlash = 0.0;
      chIsOn = !chIsOn;
    }
    curFlash += deltaMs;
    if (currentFlashTime > flashTimeKnob.getValue()) {
      curFlashCh++;
      if (curFlashCh > CH_ALL)
        curFlashCh = CH_R;
      currentFlashTime = 0.0;
      chIsOn = true;
      curFlash = 0.0;
    }
    for (LXPoint p : model.points) {
      if (RaveModel3D.pointIsR(p) && chIsOn && (curFlashCh == CH_R || curFlashCh == CH_ALL)) {
        colors[p.index] = (whiteKnob.getValueb())?Colors.WHITE:Colors.RED;
      } else if (RaveModel3D.pointIsA(p) && chIsOn && (curFlashCh == CH_A || curFlashCh == CH_ALL) ) {
        colors[p.index] = (whiteKnob.getValueb())?Colors.WHITE:Colors.GREEN;
      } else if (RaveModel3D.pointIsV(p) && chIsOn && (curFlashCh == CH_V || curFlashCh == CH_ALL)) {
        colors[p.index] = (whiteKnob.getValueb())?Colors.WHITE:Colors.BLUE;
      } else if (RaveModel3D.pointIsE(p) && chIsOn && (curFlashCh == CH_E || curFlashCh == CH_ALL)) {
        colors[p.index] = (whiteKnob.getValueb())?Colors.WHITE:Colors.ORANGE;
      } else {
        colors[p.index] = Colors.BLACK;
      }
    }
    currentFlashTime += deltaMs;
  }
}
