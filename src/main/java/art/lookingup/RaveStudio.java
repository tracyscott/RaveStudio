package art.lookingup;

import art.lookingup.ui.*;
import com.google.common.reflect.ClassPath;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.studio.LXStudio;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import processing.core.PApplet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class RaveStudio extends PApplet {
	
	static {
    System.setProperty(
        "java.util.logging.SimpleFormatter.format",
        "%3$s: %1$tc [%4$s] %5$s%6$s%n");
  }

    /**
   * Set the main logging level here.
   *
   * @param level the new logging level
   */
  public static void setLogLevel(Level level) {
    // Change the logging level here
    Logger root = Logger.getLogger("");
    root.setLevel(level);
    for (Handler h : root.getHandlers()) {
      h.setLevel(level);
    }
  }


  /**
   * Adds logging to a file. The file name will be appended with a dash, date stamp, and
   * the extension ".log".
   *
   * @param prefix prefix of the log file name
   * @throws IOException if there was an error opening the file.
   */
  public static void addLogFileHandler(String prefix) throws IOException {
    String suffix = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    Logger root = Logger.getLogger("");
    Handler h = new FileHandler(prefix + "-" + suffix + ".log");
    h.setFormatter(new SimpleFormatter());
    root.addHandler(h);
  }

  private static final Logger logger = Logger.getLogger(RaveStudio.class.getName());

  public static void main(String[] args) {
    PApplet.main(RaveStudio.class.getName(), args);
  }

  private static final String LOG_FILENAME_PREFIX = "lookinguparts";

  // Reference to top-level LX instance
  private heronarts.lx.studio.LXStudio lx;

  public static PApplet pApplet;
  public static final int GLOBAL_FRAME_RATE = 40;

  public static RainbowOSC rainbowOSC;

  public static UIGammaSelector gammaControls;
  public static UIModeSelector modeSelector;
  public static UIAudioMonitorLevels audioMonitorLevels;
  public static UIPixliteConfig pixliteConfig;
  public static UIMidiControl uiMidiControl;
  public static com.giantrainbow.OSCSensor oscSensor;
  public static OSCSensorUI oscSensorUI;

  @Override
  public void settings() {
    size(1200, 678, P3D);
  }

  /**
   * Registers all patterns and effects that LX doesn't already have registered.
   * This check is important because LX just adds to a list.
   *
   * @param lx the LX environment
   */
  private void registerAll(LXStudio lx) {
    List<Class<? extends LXPattern>> patterns = lx.getRegisteredPatterns();
    List<Class<? extends LXEffect>> effects = lx.getRegisteredEffects();
    final String parentPackage = getClass().getPackage().getName();

    try {
      ClassPath classPath = ClassPath.from(getClass().getClassLoader());
      for (ClassPath.ClassInfo classInfo : classPath.getAllClasses()) {
        // Limit to this package and sub-packages
        if (!classInfo.getPackageName().startsWith(parentPackage)) {
          continue;
        }
        Class<?> c = classInfo.load();
        if (Modifier.isAbstract(c.getModifiers())) {
          continue;
        }
        if (LXPattern.class.isAssignableFrom(c)) {
          Class<? extends LXPattern> p = c.asSubclass(LXPattern.class);
          if (!patterns.contains(p)) {
            lx.registerPattern(p);
            logger.info("Added pattern: " + p);
          }
        } else if (LXEffect.class.isAssignableFrom(c)) {
          Class<? extends LXEffect> e = c.asSubclass(LXEffect.class);
          if (!effects.contains(e)) {
            lx.registerEffect(e);
            logger.info("Added effect: " + e);
          }
        }
      }
    } catch (IOException ex) {
      logger.log(Level.WARNING, "Error finding pattern and effect classes", ex);
    }
  }

  static String readFile(String path, Charset encoding)
      throws IOException
  {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  @Override
  public void setup() {
    // Processing setup, constructs the window and the LX instance
    pApplet = this;

    pApplet.sketchPath("./");

    try {
      addLogFileHandler(LOG_FILENAME_PREFIX);
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Error creating log file: " + LOG_FILENAME_PREFIX, ex);
    }

    List<LXPoint> points = new ArrayList<LXPoint>();
    String drawingSvg = "";
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse("rave-cnc.svg");
      // //@d^='m 0,0 c'
      //String xpathExpression = "//g[path[@d]]/@transform";
      String xpathExpression = "//g";
      XPathFactory xpf = XPathFactory.newInstance();
      XPath xpath = xpf.newXPath();
      XPathExpression expression = xpath.compile(xpathExpression);
      NodeList svgPaths = (NodeList)expression.evaluate(document, XPathConstants.NODESET);
      //logger.log(Level.INFO, "Num total nodes: " + svgPaths.getLength());
      boolean skippedFirstAlready = false;
      for (int i = 0; i < svgPaths.getLength(); i++) {
        Node node = svgPaths.item(i);
        NamedNodeMap gNodeMap = node.getAttributes();
        Node transformNode = gNodeMap.getNamedItem("transform");
        if (transformNode == null) continue;
        //logger.log(Level.INFO, "Transform=" + transformNode.getNodeValue());
        //logger.log(Level.INFO, "N Value = " + node.getNodeValue());
        //logger.log(Level.INFO, "Child Nodes: " + node.getChildNodes().getLength());
        Node path = node.getChildNodes().item(0);
        //logger.log(Level.INFO, "node value " + path.getNodeValue());
        if (path == null) continue;
        NamedNodeMap nodeMap = path.getAttributes();
        Node dNode = nodeMap.getNamedItem("d");
        String dText = "";
        if (dNode != null) {
          dText = dNode.getNodeValue();
          //logger.log(Level.INFO, "D text = " + dText);
        }

        int count = dText.length() - dText.replace(",", "").length();
        int littleCCount = dText.length() - dText.replace("c", "").length();
        int bigCCount = dText.length() - dText.replace("C", "").length();
        String nodeValue = transformNode.getNodeValue(); //svgPaths.item(i).getNodeValue();
        //logger.log(Level.INFO, "found: " + nodeValue);
        if (nodeValue.contains("matrix")) {
          continue;
        } else {
          nodeValue = nodeValue.replace("translate", "");
          nodeValue = nodeValue.replace("(", "");
          nodeValue = nodeValue.replace(")", "");
          String[] xy = nodeValue.split(",");
          if (count == 13 && (dText.contains("c") || dText.contains("C"))) {
            if (!skippedFirstAlready) {
              skippedFirstAlready = true;
              continue;
            }
            //logger.log(Level.INFO, "Adding: " + xy[0] + "," + xy[1] + " with count: " + count + " " + littleCCount + " " + bigCCount);
            LXPoint point = new LXPoint(Float.parseFloat(xy[0]) / 1000.0f, Float.parseFloat(xy[1]) / 1000.0f, 0.0);
            points.add(point);
          }
        }
      }
    } catch (IOException ioex) {
      logger.log(Level.SEVERE, "Unable to read svg layout file: ", ioex);
    } catch ( javax.xml.parsers.ParserConfigurationException pcex) {
      logger.log(Level.SEVERE, "ParserConfigurationException", pcex);
    } catch (org.xml.sax.SAXException sex) {
      logger.log(Level.SEVERE, "SAXException", sex);
    } catch ( javax.xml.xpath.XPathExpressionException xpex) {
      logger.log(Level.SEVERE, " XPathExpressionException: ", xpex);
    }

    logger.log(Level.INFO, "Num points:" + points.size());
    // Save points to a file in order.  Used by Processing sketch to interactively do the pixel mapping.
    try {
      PrintWriter lxpointsFile = new PrintWriter("lxpoints.csv");
      for (LXPoint p : points) {
        lxpointsFile.println(p.x + "," + p.y);
      }
      lxpointsFile.close();
    } catch (IOException ioex) {

    }
    LXModel model = RaveModel3D.createModel(points);
    LXStudio.Flags flags = new LXStudio.Flags();
    //flags.showFramerate = false;
    //flags.isP3LX = true;
    //flags.immutableModel = true;
    flags.useGLPointCloud = false;
    flags.startMultiThreaded = true;
    //flags.showFramerate = true;

    logger.info("Current renderer:" + sketchRenderer());
    logger.info("Current graphics:" + getGraphics());
    logger.info("Current graphics is GL:" + getGraphics().isGL());
    //logger.info("Multithreaded hint: " + MULTITHREADED);
    //logger.info("Multithreaded actually: " + (MULTITHREADED && !getGraphics().isGL()));
    lx = new LXStudio(this, flags, model);

    lx.ui.setResizable(true);

    // Put this here because it needs to be after file loads in order to find appropriate channels.
    modeSelector = (UIModeSelector) new UIModeSelector(lx.ui, lx, audioMonitorLevels).setExpanded(true).addToContainer(lx.ui.leftPane.global);
    modeSelector.standardMode.setActive(true);
    //frameRate(GLOBAL_FRAME_RATE);
  }


  public void initialize(final LXStudio lx, LXStudio.UI ui) {
    // Add custom components or output drivers here
    // Register settings
    // lx.engine.registerComponent("yomigaeSettings", new Settings(lx, ui));

    // Common components
    // registry = new Registry(this, lx);

    // Register any patterns and effects LX doesn't recognize
    registerAll(lx);
  }

  public void onUIReady(LXStudio lx, LXStudio.UI ui) {
    oscSensor = new com.giantrainbow.OSCSensor(lx);
    lx.engine.registerComponent("oscsensor", oscSensor);
    //modeSelector = (UIModeSelector) new UIModeSelector(lx.ui, lx, audioMonitorLevels).setExpanded(true).addToContainer(lx.ui.leftPane.global);
    //modeSelector = (UIModeSelector) new UIModeSelector(lx.ui, lx, audioMonitorLevels).setExpanded(true).addToContainer(lx.ui.leftPane.global);
    oscSensorUI = (OSCSensorUI) new OSCSensorUI(lx.ui, lx, oscSensor).setExpanded(false).addToContainer(lx.ui.leftPane.global);

    audioMonitorLevels = (UIAudioMonitorLevels) new UIAudioMonitorLevels(lx.ui).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    gammaControls = (UIGammaSelector) new UIGammaSelector(lx.ui).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    uiMidiControl = (UIMidiControl) new UIMidiControl(lx.ui, lx, modeSelector).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    pixliteConfig = (UIPixliteConfig) new UIPixliteConfig(lx.ui, lx).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    lx.engine.midi.addListener(uiMidiControl);
    if (enableOutput) {
      //Output.configureE131Output(lx, Output.LightType.OPPSKPAR);
      Output.configureArtNetOutput(lx);
    }
    if (disableOutputOnStart)
      lx.engine.output.enabled.setValue(false);

    rainbowOSC = new RainbowOSC(lx);



    // Disable preview for faster UI.
    //lx.ui.preview.setVisible(false);
  }

  public void draw() {
    // All is handled by LX Studio
  }

  // Configuration flags
  private final static boolean MULTITHREADED = false;  // Disabled for anything GL
                                                       // Enable at your own risk!
                                                       // Could cause VM crashes.
  private final static boolean RESIZABLE = true;

  // Helpful global constants
  final static float INCHES = 1.0f / 12.0f;
  final static float IN = INCHES;
  final static float FEET = 1.0f;
  final static float FT = FEET;
  final static float CM = IN / 2.54f;
  final static float MM = CM * .1f;
  final static float M = CM * 100;
  final static float METER = M;

  public static final boolean enableOutput = true;
  public static final boolean disableOutputOnStart = false;

  public static final int LEDS_PER_UNIVERSE = 170;
}