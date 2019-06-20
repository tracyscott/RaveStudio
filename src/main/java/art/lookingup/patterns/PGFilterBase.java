package art.lookingup.patterns;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import processing.core.PConstants;
import processing.core.PImage;
import processing.opengl.PShader;

/**
 * Base class that adds the ability to either Blur with a radius
 * or perform a multipass vertical blur.
 */
public class PGFilterBase extends PGPixelPerfect {

  protected PShader dirBlur;
  protected int blurRadius = 0;

  protected boolean doDirBlur = false;
  protected int dirBlurPasses = 10;

  public PGFilterBase(LX lx) {
    super(lx, "");
  }

  public void draw(double drawDeltaMs) {
      // Run a filter on pg
    if (blurRadius >= 1) {
      pg.filter(PConstants.BLUR, blurRadius);
    }
    if (doDirBlur && dirBlurPasses > 0) {
      for (int i = 0; i < dirBlurPasses; i++) {
       dirBlur();
      }
    }
  }

  public void dirBlur() {
    float[][] blur = { { 0, 0, 0 }, { 1.0f/3.0f, 1.0f/3.0f, 1.0f/3.0f }, { 0, 0, 0 } };
    float [][] gFilter = blur;
    int matrixsize = gFilter.length;
    PImage img = pg;
    img.loadPixels();
    for(int x = 0; x < pg.width; x++)
      for(int y = 0; y < pg.height; y++) { // for(int y = 0; y < img.width; y++) {
        int c = convolution(x, y, blur, matrixsize, img);
        int loc = x + y * pg.width;
        img.pixels[loc] = c;
      }
    img.updatePixels();
  }

  int convolution(int x, int y, float[][] matrix, int matrixsize, PImage img) {
    float rtotal = 0.0f;
    float gtotal = 0.0f;
    float btotal = 0.0f;
    int offset = matrixsize / 2;
    for(int i = 0; i < matrixsize; i++)
      for(int j = 0; j < matrixsize; j++) {
        int xloc = x + i - offset;
        int yloc = y + j - offset;
        int loc = xloc + yloc * pg.width;
        int newloc = constrain(loc, 0, img.pixels.length - 1);
        if (loc != newloc) continue;
        else loc = newloc;
        rtotal +=  (LXColor.red(img.pixels[loc])&0xFF) * matrix[i][j];
        gtotal += (LXColor.green(img.pixels[loc])&0xFF) * matrix[i][j];
        btotal +=  (LXColor.blue(img.pixels[loc])&0xFF) * matrix[i][j];
      }
    rtotal = constrain(rtotal, 0, 255);
    gtotal = constrain(gtotal, 0, 255);
    btotal = constrain(btotal, 0, 255);
    return LXColor.rgb((int)(rtotal), (int)(gtotal), (int)(btotal));
  }

  float constrain(float value, float low, float high) {
    if (value < low) return low;
    if (value > high) return high;
    return value;
  }

  int constrain(int value, int  low, int high) {
    if (value < low) return low;
    if (value > high) return high;
    return value;
  }
}
