package art.lookingup;

import heronarts.lx.LX;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.LXDatagramOutput;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles output from our 'colors' buffer to our DMX lights.  Currently using E1.31.
 */
public class Output {
  private static final Logger logger = Logger.getLogger(Output.class.getName());

  public enum LightType {
    LALUCE(1),
    PRODPAR(2),
    PRODWASH(3),
    OPPSKPAR(4);

    private int value;
    private static Map map = new HashMap<Integer, LightType>();

    private LightType(int value) {
      this.value = value;
    }

    static {
      for (LightType lightType : LightType.values()) {
        map.put(lightType.value, lightType);
      }
    }

    public static LightType valueOf(int pageType) {
      return (LightType) map.get(pageType);
    }

    public int getValue() {
      return value;
    }
  }

  public static LXDatagramOutput datagramOutput = null;

  public static String artnetIpAddress = "127.0.0.1";
  public static int artnetPort = 6454;

  public static void configureArtnetOutput(LX lx) {
    // This only works if we have less than 170 lxpoints.
    int[] dmxChannelsForUniverse = new int[20];
    for (int i = 0; i < 20; i++) {
      dmxChannelsForUniverse[i] = i;
    }
    ArtNetDatagram artnetDatagram = new ArtNetDatagram(dmxChannelsForUniverse);

    try {
      artnetDatagram.setAddress(artnetIpAddress).setPort(artnetPort);
    } catch (UnknownHostException uhex) {
      logger.log(Level.SEVERE, "Configuring ArtNet: " + artnetIpAddress, uhex);
    }
    try {
      datagramOutput = new LXDatagramOutput(lx);
      datagramOutput.addDatagram(artnetDatagram);
    } catch (SocketException sex) {
      logger.log(Level.SEVERE, "Initializing LXDatagramOutput failed.", sex);
    }
    if (datagramOutput != null) {
      System.out.println("Output added");
      lx.engine.output.addChild(datagramOutput);
    } else {
      logger.log(Level.SEVERE, "Did not configure output, error during LXDatagramOutput init");
    }
  }

  public static void configureE131Output(LX lx, LightType lightType) {
  }
}
