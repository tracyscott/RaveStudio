package art.lookingup;

import art.lookingup.ui.UIPixliteConfig;
import heronarts.lx.LX;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.LXDatagramOutput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles output from our 'colors' buffer to our DMX lights.  Currently using E1.31.
 */
public class Output {
  private static final Logger logger = Logger.getLogger(Output.class.getName());

  public static LXDatagramOutput datagramOutput = null;
  public static int MAX_OUTPUTS = 16;

  public static ArrayList<Integer>[] outputs = new ArrayList[MAX_OUTPUTS];

  /**
   * Loads a wiring.txt file that is written by PixelMapping Processing sketch.
   *
   * @param filename
   * @return
   */
  static protected boolean loadWiring(String filename) {
    for (int i = 0; i < MAX_OUTPUTS; i++) {
      outputs[i] = new ArrayList<Integer>();
    }
    BufferedReader reader;
    int currentOutputNum = 0;
    List<Integer> currentOutputIndices = null;

    try {
      reader = new BufferedReader(new FileReader(filename));
      String line = reader.readLine();
      while (line != null) {
        // logger.log(Level.INFO, "Reading wiring: " + line);
        if (line.startsWith(":")) {
          currentOutputNum = Integer.parseInt(line.replace(":", ""));
          currentOutputIndices = outputs[currentOutputNum];
        } else {
          int pointIndex = Integer.parseInt(line);
          currentOutputIndices.add(pointIndex);
        }
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public static void configureArtNetOutput(LX lx) {
    loadWiring("wiring.txt");
    // This only works if we have less than 170 lxpoints.
    String artNetIpAddress = RaveStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_IP).getString();
    int artNetIpPort = Integer.parseInt(RaveStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PORT).getString());
    logger.log(Level.INFO, "Using ArtNet: " + artNetIpAddress + ":" + artNetIpPort);

    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();

    int outputNumber = 1;
    int universeNumber = 1;
    for (List<Integer> indices : outputs) {
      // Add point indices in chunks of 170.  After 170 build datagram and then increment the universeNumber.
      // Continuing adding points and building datagrams every 170 points.  After all points for an output
      // have been added to datagrams, start on a new output and reset counters.

      int chunkNumber = 0;
      boolean endOfOutput = false;
      int pointNum = 0;
      while (pointNum + chunkNumber * 170 < indices.size()) {
        // Compute the dataLength.  For a string of 200 leds, we should have dataLengths of
        // 170 and then 30.  So for the second pass, chunkNumber=1.  Overrun is 2*170 - 200 = 340 - 200 = 140
        // We subtract 170-overrun = 30, which is the remainder number of the leds on the last chunk.
        // 350 leds = chunkNumber = 2, 510 - 350 = 160.  170-160=10.
        int overrun = ((chunkNumber+1) * 170) - indices.size();
        int dataLength = (overrun < 0)?170:170 - overrun;
        int[] thisUniverseIndices = new int[dataLength];
        // For each chunk of 170 points, add them to a datagram.
        for (pointNum = 0; pointNum < 170 && (pointNum + chunkNumber * 170 < indices.size());
             pointNum++) {
          thisUniverseIndices[pointNum] = indices.get(pointNum + chunkNumber * 170);
        }
        logger.log(Level.INFO, "Adding datagram: output=" + outputNumber + " universe=" + universeNumber + " points=" + pointNum);
        ArtNetDatagram artNetDatagram = new ArtNetDatagram(thisUniverseIndices, universeNumber);
        try {
          artNetDatagram.setAddress(artNetIpAddress).setPort(artNetIpPort);
        } catch (UnknownHostException uhex) {
          logger.log(Level.SEVERE, "Configuring ArtNet: " + artNetIpAddress + ":" + artNetIpPort, uhex);
        }
        datagrams.add(artNetDatagram);
        // We have either added 170 points and maybe less if it is the last few points for a given output.  Each
        // time we build a datagram for a chunk, we need to increment the universeNumber, reset the pointNum to zero,
        // and increment our chunkNumber
        ++universeNumber;
        pointNum = 0;
        chunkNumber++;
      }
      outputNumber++;
    }

    try {
      datagramOutput = new LXDatagramOutput(lx);
      for (ArtNetDatagram datagram : datagrams) {
        datagramOutput.addDatagram(datagram);
      }
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
