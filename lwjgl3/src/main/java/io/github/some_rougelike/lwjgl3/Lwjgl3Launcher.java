package io.github.some_rougelike.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import io.github.some_rougelike.Main;
import io.github.some_rougelike.ui.WindowModeController;
import org.lwjgl.glfw.GLFW;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        Main mainApp = new Main();
        mainApp.setWindowModeController(new WindowModeController() {
            @Override
            public void setBorderless(boolean borderless) {
                Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
                Lwjgl3Window window = graphics.getWindow();
                Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
                long windowHandle = window.getWindowHandle();

                if (borderless) {
                    // Switch to borderless windowed mode by removing decorations and matching screen size
                    GLFW.glfwSetWindowAttrib(windowHandle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
                    Gdx.graphics.setWindowedMode(mode.width, mode.height);
                    window.setPosition(0, 0);
                } else {
                    // Switch back to standard windowed mode with decorations restored
                    GLFW.glfwSetWindowAttrib(windowHandle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
                    int width = 1280;
                    int height = 720;
                    Gdx.graphics.setWindowedMode(width, height);
                    // Center the window on the primary monitor
                    window.setPosition((mode.width - width) / 2, (mode.height - height) / 2);
                }
            }
        });

        return new Lwjgl3Application(mainApp, getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("softech");
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        // Start the application in borderless windowed mode by default using the monitor's display mode dimensions
        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        configuration.setWindowedMode(displayMode.width, displayMode.height);
        configuration.setDecorated(false); // Make it borderless at startup

        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        //// This could improve compatibility with Windows machines with buggy OpenGL drivers, Macs
        //// with Apple Silicon that have to emulate compatibility with OpenGL anyway, and more.
        //// This uses the dependency `com.badlogicgames.gdx:gdx-lwjgl3-angle` to function.
        //// You would need to add this line to lwjgl3/build.gradle , below the dependency on `gdx-backend-lwjgl3`:
        ////    implementation "com.badlogicgames.gdx:gdx-lwjgl3-angle:$gdxVersion"
        //// You can choose to add the following line and the mentioned dependency if you want; they
        //// are not intended for games that use GL30 (which is compatibility with OpenGL ES 3.0).
        //// Know that it might not work well in some cases.
        //configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        configuration.setTitle("Rougelike");

        return configuration;
    }
}