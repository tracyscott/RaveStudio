# Rave

---
Based on LX Studio.  Lighting software for Rave Sign.

---
**BY DOWNLOADING OR USING THE LX STUDIO SOFTWARE OR ANY PART THEREOF,
YOU AGREE TO THE TERMS AND CONDITIONS OF THE
[LX STUDIO SOFTWARE LICENSE AND DISTRIBUTION AGREEMENT](http://lx.studio/license).**

Please note that LX Studio is not open-source software. The license grants
permission to use this software freely in non-commercial applications.
Commercial use is subject to a total annual revenue limit of $25K on any and
all projects associated with the software. If this licensing is obstructive to
your needs or you are unclear as to whether your desired use case is compliant,
contact me to discuss proprietary licensing: mark@heronarts.com
---


[LX Studio](http://lx.studio/) is a digital lighting workstation, bringing concepts from digital audio workstations and modular synthesis into the realm of LED lighting control. Generative patterns, interactive inputs, and flexible parameter-driven modulation — a rich environment for lighting composition and performance.

## Getting Started

To set up RaveStudio, perform the following steps:

1. [Download and install JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
2. [Download and install Maven](https://maven.apache.org).
3. Clone this [project](https://github.com/tracyscott/RaveStudio.git).
4. From the cloned directory, build the project using `mvn package`.
5. Change directories to `projects/` and execute
   `java -jar ../target/ravestudio-1.0.0-jar-with-dependencies.jar`.
5. RaveStudio should launch with a side-scrolling white dot on a black grid
   1. Click "Folder" icon in top-center and select a pattern
   3. RaveStudio should now show the desired pattern.

Helpful links for Linux environments:

1. JDK 8: https://www.javahelps.com/2015/03/install-oracle-jdk-in-ubuntu.html
2. Maven: https://www.javahelps.com/2017/10/install-apache-maven-on-linux.html
3. Clone this project.
4. IntelliJ: https://www.jetbrains.com/idea/download/#section=linux
   1. Using IntelliJ import Maven project from the cloned directory
   2. In the IntelliJ project, add run configuration using Maven template and enter 'compile exec:java' in command line field
5. Common issues 
   1. Window size error: set "lx.ui.setResizable(RESIZABLE);" to false 

## Documentation

[LX Studio User Guide](https://github.com/tracyscott/RainbowStudio/blob/master/LXStudioUserGuide.md)

There is some minimal documentation on the
[LX Studio Wiki](https://github.com/heronarts/LXStudio/wiki).

Consult the [LX Studio API reference &rarr;](http://lx.studio/api/)

More and better documentation is coming soon!

## Troubleshooting

If you encounter an issue like the following on Linux:

```
com.jogamp.opengl.GLException: Caught ThreadDeath: null on thread main-FPSAWTAnimator#00-Timer0
    at com.jogamp.opengl.GLException.newGLException(GLException.java:76)
```

Then we recommend wiping your `.lxproject` file, selecting a `.lxp` file,
and rebooting RaveStudio twice. It was causing us issues, potentially due
to odd-sized windows.

## LX Studio Contact and Collaboration

Building a big cool project? I'm probably interested in hearing about it!
Want to solicit some help, request new framework features, or just ask a
random question? Open an issue on the project or drop me a line:
mark@heronarts.com

---

HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR OTHERWISE, AND
SPECIFICALLY DISCLAIMS ANY WARRANTY OF MERCHANTABILITY, NON-INFRINGEMENT, OR
FITNESS FOR A PARTICULAR PURPOSE, WITH RESPECT TO THE SOFTWARE.
