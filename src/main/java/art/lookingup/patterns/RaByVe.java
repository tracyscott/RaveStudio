package art.lookingup.patterns;

import art.lookingup.RaveModel3D;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import static java.lang.Math.ceil;

public class RaByVe extends LXPattern {
  public CompoundParameter chTimeKnob = new CompoundParameter("chtime", 2000f, 0f, 5000f);
  public DiscreteParameter paletteKnob = new DiscreteParameter("palette", 1, 1, Colors.ALL_PALETTES.length + 1);
  public BooleanParameter whiteKnob = new BooleanParameter("white", false);

  int curCh = CH_RA;
  double curChTime = 0.0;
  static public final int CH_RA = 0;
  static public final int CH_VE = 1;
  public boolean chIsOn = true;
  public int curColor = Colors.BLACK;

  public RaByVe(LX lx) {
    super(lx);
    addParameter(chTimeKnob);
    addParameter(paletteKnob);
    addParameter(whiteKnob);
    curColor = chooseRandomColor();
  }

  protected int chooseRandomColor() {
    int whichPalette = paletteKnob.getValuei();
    int[] palette = Colors.ALL_PALETTES[whichPalette - 1];
    int index = (int) ceil(Math.random() * (palette.length)) - 1;
    if (index < 0) index = 0;
    int color = palette[index];
    return color;
  }

  public void run(double deltaMs) {
    if (curChTime > chTimeKnob.getValue()) {
      curCh++;
      if (curCh > CH_VE)
        curCh = CH_RA;
      curChTime = 0.0;
      chIsOn = true;
      curColor = chooseRandomColor();
    }
    for (LXPoint p : model.points) {
      if (RaveModel3D.pointIsR(p) && chIsOn && curCh == CH_RA) {
        colors[p.index] = (whiteKnob.getValueb())? Colors.WHITE:curColor;
      } else if (RaveModel3D.pointIsA(p) && chIsOn && curCh == CH_RA ) {
        colors[p.index] = (whiteKnob.getValueb())?Colors.WHITE:curColor;
      } else if (RaveModel3D.pointIsV(p) && chIsOn && curCh == CH_VE) {
        colors[p.index] = (whiteKnob.getValueb())?Colors.WHITE:curColor;
      } else if (RaveModel3D.pointIsE(p) && chIsOn && curCh == CH_VE) {
        colors[p.index] = (whiteKnob.getValueb())?Colors.WHITE:curColor;
      } else {
        colors[p.index] = Colors.BLACK;
      }
    }
    curChTime += deltaMs;
  }
}
