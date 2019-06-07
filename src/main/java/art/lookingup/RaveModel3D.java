package art.lookingup;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;

// TODO(tracy): This doesn't really need to extend LXModel anymore.  We can just construct it directly
// with wrapper.  RaveModel3D should not extend LXModel and should encompass the Tori gate dimensions,
// etc.  It should have a method generateLXModel() that generates our points.  Also, there could be
// multiple versions of generateLXModel() that generate multiple light placement strategies but that
// ship has sailed.
public class RaveModel3D extends LXModel {

  public final static int SIZE = 20;

  public static double minX = Float.MAX_VALUE;
  public static double minY = Float.MAX_VALUE;
  public static double maxX = Float.MIN_VALUE;
  public static double maxY = Float.MIN_VALUE;
  public static double computedWidth = 1f;
  public static double computedHeight= 1f;
  public static double rowIncrementLength;
  public static double colIncrementLength;

  public RaveModel3D(List<LXPoint> points) {
    super(points);
    // Compute some stats on our points.
    for (LXPoint p : points) {
      if (p.x < minX) minX = p.x;
      if (p.y < minY) minY = p.y;
      if (p.x > maxX) maxX = p.x;
      if (p.y > maxY) maxY = p.y;
    }
    computedWidth = maxX - minX;
    computedHeight = maxY - minY;
    colIncrementLength = computedWidth / (POINTS_WIDE - 1);
    rowIncrementLength = computedHeight  / (POINTS_HIGH - 1);
  }

  public static int[] pointToImageCoordinates(LXPoint p) {
    int[] coordinates = {0, 0};
    double offsetX = p.x - minX;
    double offsetY = p.y - minY;
    int columnNumber = (int)Math.round(offsetX / colIncrementLength);
    int rowNumber = (int)Math.round(offsetY  / rowIncrementLength);
    coordinates[0] = columnNumber;
    // Transpose for Processing Image coordinates, otherwise images are upside down.
    coordinates[1] = (POINTS_HIGH-1)-rowNumber;
    //System.out.println ("x,y " + coordinates[0] + "," + coordinates[1]);
    return coordinates;
  }

  /* @return true If point is contained in R, i.e. top-left quadrant. */
  static public boolean pointIsR(LXPoint p) {
    int[] coords = pointToImageCoordinates(p);
    if (coords[0] <= POINTS_WIDE/2 + 1 &&
        coords[1] < POINTS_HIGH/2)
      return true;
    return false;
  }

  /* @return true If point is contained in A, i.e. top-right quadrant */
  static public boolean pointIsA(LXPoint p) {
    int[] coords = pointToImageCoordinates(p);
    if (coords[0] > POINTS_WIDE/2 + 1 &&
        coords[1] < POINTS_HIGH/2)
      return true;
    return false;
  }

  /* @return true If point is contained in V, i.e. bottom-left quadrant */
  static public boolean pointIsV(LXPoint p) {
    int[] coords = pointToImageCoordinates(p);
    if (coords[0] <= POINTS_WIDE/2 &&
        coords[1] >= POINTS_HIGH/2)
      return true;
    return false;
  }

  /* @return true If pointt is contained in E, i.e. bottom-right quadrant */
  static public boolean pointIsE(LXPoint p) {
    int[] coords = pointToImageCoordinates(p);
    if (coords[0] > POINTS_WIDE/2 &&
        coords[1] >= POINTS_HIGH/2)
      return true;

    return false;
  }



  public static final int POINTS_WIDE = 46;
  public static final int POINTS_HIGH = 46;
}
