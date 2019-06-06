package art.lookingup;

public class AnimUtils {
  /**
   * Evaluate a triangle wave.  Linear oscillation between 0 and 1.
   */
  static public float triWave(float t, float p)
  {
    return 2.0f * (float)Math.abs(t / p - Math.floor(t / p + 0.5f));
  }

  /**
   * Step wave with attack slope.
   * Returns value from 0.0f to 1.0f
   */
  static public float stepWave(float stepPos, float slope, float x, boolean forward)
  {
    float value;
    if (forward) {
      if (x < stepPos)
        value = 1.0f;
      else {
        value = -slope * (x - stepPos) + 1.0f;
        if (value < 0f) value = 0f;
      }
    } else {
      if (x > stepPos)
        value = 1.0f;
      else {
        value = slope * (x - stepPos) + 1.0f;
        if (value < 0f) value = 0f;
      }

    }
    return value;
  }

  /**
   * Normalized triangle wave function.  Given position of triangle peak and the
   * slope, return value of function at evalAtX.  If less than 0, clip to zero.
   */
  static public float triangleWave(float peakX, float slope, float evalAtX)
  {
    // If we are to the right of the triangle, the slope is negative
    if (evalAtX > peakX) slope = -slope;
    float y = slope * (evalAtX - peakX) + 1.0f;
    if (y < 0f) y = 0f;
    return y;
  }

  /**
   * Returns the light intensity given the X coordinate of the triangle peak, the slope of the
   * triangle, and the X coordinate of the light.  If the value is less than the minimum intensity
   * this will return the minimum intensity.
   *
   * Point Slope line form: (y - y1) = m(x - x1)
   * => y = m(x - x1) + y1;
   *
   */
  /*
  static public float getTriangleIntensity(float peakX, float peakSlope, float lightX, float minIntensity, float maxIntensity)
  {
    float x1 = peakX;
    float m = peakSlope;
    float y1 = maxIntensity;

    // If we are to the right of the triangle, the slope is negative
    if (lightX > peakX) m = -m;

    float y = m * (lightX - peakX) + y1;
    if (y < minIntensity) y = minIntensity;
    return y;
  }
  */
}
