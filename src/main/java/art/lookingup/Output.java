package art.lookingup;

import art.lookingup.ui.UIPixliteConfig;
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

  public static LXDatagramOutput datagramOutput = null;

  public static void configureArtNetOutput(LX lx) {
    // This only works if we have less than 170 lxpoints.
    int[] dmxChannelsForUniverse = new int[20];
    for (int i = 0; i < 20; i++) {
      dmxChannelsForUniverse[i] = i;
    }
    ArtNetDatagram artnetDatagram = new ArtNetDatagram(dmxChannelsForUniverse);

    String artNetIpAddress = RaveStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_IP).getString();
    int artNetIpPort = Integer.parseInt(RaveStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PORT).getString());
    try {
      logger.log(Level.INFO, "Using ArtNet: " + artNetIpAddress + ":" + artNetIpPort);
      artnetDatagram.setAddress(artNetIpAddress).setPort(artNetIpPort);
    } catch (UnknownHostException uhex) {
      logger.log(Level.SEVERE, "Configuring ArtNet: " + artNetIpAddress + ":" + artNetIpPort, uhex);
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

  public static void configureE131Output(LX lx) {
  }
}
