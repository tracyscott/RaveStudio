package art.lookingup.patterns;

import art.lookingup.PathUtils;
import art.lookingup.RaveStudio;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.*;
import heronarts.p3lx.ui.CustomDeviceUI;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UISwitch;
import heronarts.p3lx.ui.component.UITextBox;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import processing.core.PConstants;
import processing.core.PImage;

/**
 * Display an animated GIF on the rainbow.  This base class is used by both
 * the larger texture with antialias sampling and the pixel perfect renderings
 * with direct 1:1 pixel mappings (with bend distortion in physical space).
 */
abstract class RainbowGIFBase extends RPattern implements CustomDeviceUI {
  private static final Logger logger = Logger.getLogger(RainbowGIFBase.class.getName());

  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", 1.0, 60.0)
          .setDescription("Controls the frames per second.");
  public final BooleanParameter antialiasKnob =
      new BooleanParameter("antialias", true);
  public final CompoundParameter xOff =
      new CompoundParameter("xOff", 0.0, 0.0, 800.0)
          .setDescription("x Offset into src image");
  public final CompoundParameter yOff =
      new CompoundParameter("yOff", 0.0, 0.0, 800.0)
          .setDescription("y Offset into src image");
  public final CompoundParameter scaleAmt =
      new CompoundParameter("scaleAmt", 1.0, 0.01, 2.0)
          .setDescription("Scale src image amount");
  public final BooleanParameter scaleSrc =
      new BooleanParameter("scaleSrc", true)
          .setDescription("Whether to scale source image");
  public final BooleanParameter fitSrc =
      new BooleanParameter("fitSrc", true)
          .setDescription("Fit src image to output");
  public final StringParameter gifKnob =
      new StringParameter("gif", "")
          .setDescription("Animated gif");

  protected List<FileItem> fileItems = new ArrayList<FileItem>();
  protected UIItemList.ScrollList fileItemList;
  protected List<String> gifFiles;
  protected static final int CONTROLS_MIN_WIDTH = 160;

  protected PImage[] images;
  protected double currentFrame = 0.0;
  protected int imageWidth = 0;
  protected int imageHeight = 0;
  protected String filesDir;  // Must end in a '/'
  boolean includeAntialias;

  public RainbowGIFBase(LX lx, int imageWidth, int imageHeight, String dir,
                        String defaultFile, boolean includeAntialias) {
    super(lx);
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    this.includeAntialias = includeAntialias;

    if (!dir.endsWith("/")) {
      dir = dir + "/";
    }
    filesDir = dir;
    reloadFileList();

    addParameter(fpsKnob);
    addParameter(scaleAmt);
    addParameter(scaleSrc);
    addParameter(fitSrc);
    addParameter(xOff);
    addParameter(yOff);

    if (includeAntialias) addParameter(antialiasKnob);
    addParameter(gifKnob);
    gifKnob.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        StringParameter gKnob = (StringParameter) parameter;
        loadGif(gKnob.getString());
      }
    });
    fpsKnob.setValue(10);
  }

  /**
   * Calls {@link PathUtils#loadSprite(processing.core.PApplet, String)} but also keeps track of
   * the current position. This prepends {@link #filesDir} and appends ".gif".
   *
   * @param gifname the sprite's name, not including parent paths or the ".gif" suffix
   */
  private void loadGif(String gifname) {
    logger.info("Loading gif: " + gifname);
    PImage[] newImages = PathUtils.loadSprite(RaveStudio.pApplet, filesDir + gifname + ".gif");
    if (scaleSrc.getValueb()) {
      for (PImage image : newImages) {
        if (fitSrc.getValueb())
          image.resize(imageWidth, imageHeight);
        else {
          image.resize((int)((float)image.width * scaleAmt.getValue()),
              (int)((float)image.height * scaleAmt.getValue()));
        }
      }
    }
    // minimize race condition when reloading.
    images = newImages;
  }

  public void render(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    if (images == null) return;
    if (currentFrame >= images.length) {
      currentFrame -= images.length;
    }
    try {
      renderToPoints();
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      // Sometimes caused by race condition when reloading, just skip a frame.
    }
  }

  protected abstract void renderToPoints();

  protected void reloadFileList() {
    gifFiles = PathUtils.findDataFiles(filesDir, ".gif");
    fileItems.clear();
    for (String filename : gifFiles) {
      // Use a name that's suitable for the knob
      int index = filename.lastIndexOf('/');
      if (index >= 0) {
        filename = filename.substring(index + 1);
      }
      index = filename.lastIndexOf('.');
      if (index >= 0) {
        filename = filename.substring(0, index);
      }
      fileItems.add(new FileItem(filename));
    }
    if (fileItemList != null) fileItemList.setItems(fileItems);
  }

  //
  // Custom UI to allow for the selection of the shader file
  //
  @Override
    public void buildDeviceUI(UI ui, final UI2dContainer device) {
    device.setContentWidth(CONTROLS_MIN_WIDTH);
    device.setLayout(UI2dContainer.Layout.VERTICAL);
    device.setPadding(3, 3, 3, 3);

    UI2dContainer knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(1, 1, 1, 1);
    new UIKnob(fpsKnob).addToContainer(knobsContainer);
    new UIKnob(xOff).addToContainer(knobsContainer);
    new UIKnob(yOff).addToContainer(knobsContainer);
    if (includeAntialias) {
      UISwitch antialiasButton = new UISwitch(0, 0);
      antialiasButton.setParameter(antialiasKnob);
      antialiasButton.setMomentary(false);
      antialiasButton.addToContainer(knobsContainer);
    }
    new UIButton(CONTROLS_MIN_WIDTH, 10, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          reloadFileList();
        }
      }
    }
    .setLabel("rescn").setMomentary(true).addToContainer(knobsContainer);
    knobsContainer.addToContainer(device);

    knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);

    new UIKnob(scaleAmt).addToContainer(knobsContainer);

    UISwitch scaleButton = new UISwitch(0, 0);
    scaleButton.setParameter(scaleSrc);
    scaleButton.setMomentary(false);
    scaleButton.addToContainer(knobsContainer);
    UISwitch fitButton = new UISwitch(0, 0);
    fitButton.setParameter(fitSrc);
    fitButton.setMomentary(false);
    fitButton.addToContainer(knobsContainer);
    knobsContainer.addToContainer(device);


    UI2dContainer filenameEntry = new UI2dContainer(0, 0, device.getWidth(), 30);
    filenameEntry.setLayout(UI2dContainer.Layout.HORIZONTAL);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 50);
    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
      .setParameter(gifKnob)
      .setTextAlignment(PConstants.LEFT)
      .addToContainer(filenameEntry);

    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
      @Override
        public void onToggle(boolean on) {
        if (on) {
          loadGif(gifKnob.getString());
        }
      }
    }
    .setLabel("\u21BA").setMomentary(true).addToContainer(filenameEntry);
    filenameEntry.addToContainer(device);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    fileItemList.setShowCheckboxes(false);
    fileItemList.setItems(fileItems);
    fileItemList.addToContainer(device);
  }

  public class FileItem extends FileItemBase {
    FileItem(String filename) {
      super(filename);
    }
    public void onActivate() {
      gifKnob.setValue(filename);
      loadGif(filename);
    }
  }
}
