package art.lookingup;

public class ColorTemp {
  static public void convertKToRGB(float k, int[] rgb)
  {
    int red, green, blue;

    if (k < 1000)
      k = 1000;
    else if (k > 40000)
      k = 40000;
    float tmp = k / 100.0f;

    if (tmp <= 66.0f) {
      red = 255;
    } else {
      float tmpRed = 329.698727446f * (float)Math.pow(tmp - 60.0f, -0.1332047592f);
      if (tmpRed < 0) {
        red = 0;
      } else if (tmpRed > 255) {
        red = 255;
      } else {
        red = (int)tmpRed;
      }
    }

    float tmpGreen;
    if (tmp <= 66.0f) {
      tmpGreen = 99.4708025861f * (float)Math.log(tmp) - 161.1195681661f;
    } else {
      tmpGreen = 288.1221695283f *(float)Math.pow(tmp - 60.0f, -0.0755148492f);
    }
    if (tmpGreen < 0.0f) {
      green = 0;
    } else if (tmpGreen > 255.0f) {
      green = 255;
    } else {
      green = (int)tmpGreen;
    }

    if (tmp > 66.0f) {
      blue = 255;
    } else if (tmp < 19.0f) {
      blue = 0;
    } else {
      float tmpBlue = 138.5177312231f * (float)Math.log(tmp - 10.0f) - 305.0447927307f;
      if (tmpBlue < 0.0f) {
        blue = 0;
      } else if (tmpBlue > 255.0f) {
        blue = 255;
      } else {
        blue = (int)tmpBlue;
      }
    }
    rgb[0] = red;
    rgb[1] = green;
    rgb[2] = blue;
  }
}
