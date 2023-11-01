package skjsjhb.rhytick.opfw.je.cherry;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;
import skjsjhb.rhytick.opfw.je.cfg.Cfg;
import skjsjhb.rhytick.opfw.je.timing.Throttle;

/**
 * Abstract window object.
 */
public class Window {
    /**
     * FPS throttler.
     */
    protected Throttle fpsThrottle = new Throttle();

    /**
     * Internal GLFW window instance.
     */
    protected long gWindow = MemoryUtil.NULL;

    /**
     * Internal {@link GLCapabilities} reference.
     */
    protected GLCapabilities glCapabilities = null;

    /**
     * Construct a window and initialize OpenGL context.
     */
    public Window() {
        init();
    }

    /**
     * Close the window, destroy the context, and free its resources.
     */
    public void close() {
        GLFW.glfwDestroyWindow(gWindow);
    }

    /**
     * Flush a new frame.
     *
     * @apiNote A new frame will be pushed for rendering. However, the events related to this
     * window are not polled.
     */
    public void flush() {
        GLFW.glfwSwapBuffers(gWindow);
    }

    /**
     * Internal method for initializing.
     */
    protected void init() {
        // Configure video mode
        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vmode = GLFW.glfwGetVideoMode(monitor);
        if (vmode == null) {
            throw new CherryRuntimeException("could not get default video mode");
        }

        int ww = vmode.width();
        int wh = vmode.height();
        System.out.printf("Video mode: %dx%d@%d\n", ww, wh, vmode.refreshRate());

        // Request an OpenGL 3.3 Core profile
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        // Create window
        boolean fullscreen = Cfg.getBoolean("cherry.fullscreen");
        String title = Cfg.getValue("cherry.window_title", "OPKJE");
        if (fullscreen) {
            System.out.println("Entering fullscreen mode.");
            gWindow = GLFW.glfwCreateWindow(ww, wh, title, monitor, MemoryUtil.NULL);
        } else {
            System.out.println("Entering windowed mode.");
            double scale = Cfg.getDouble("cherry.window_scale", 0.8);
            gWindow = GLFW.glfwCreateWindow((int) (scale * ww), (int) (scale * wh), title,
                    MemoryUtil.NULL, MemoryUtil.NULL);
        }
        if (gWindow == MemoryUtil.NULL) {
            throw new CherryRuntimeException("failed to create window");
        }

        GLFW.glfwMakeContextCurrent(gWindow);

        // Configure vsync & fps
        boolean vsync = Cfg.getBoolean("cherry.vsync");
        setVsync(vsync);

        int fps = vsync ? vmode.refreshRate() : Cfg.getInt("cherry.fps_max", 330);
        setFPSLimit(fps);

        // Initialize OpenGL methods
        glCapabilities = GL.createCapabilities();
        String vendor = GL33C.glGetString(GL33C.GL_VENDOR);
        String version = GL33C.glGetString(GL33C.GL_VERSION);
        String glslVersion = GL33C.glGetString(GL33C.GL_SHADING_LANGUAGE_VERSION);
        System.out.printf("OpenGL %s (%s) GLSL %s\n", version, vendor, glslVersion);
    }

    /**
     * Sets the FPS limit.
     *
     * @param fps FPS limit.
     * @apiNote Render requests to {@link #flush()} are not throttled by this option. Also, the generation of
     * frames are not done by {@link Window}, use {@link #shouldGenNewFrame()} to generate frames on demand.
     */
    public void setFPSLimit(int fps) {
        System.out.println("FPS limit set to " + fps);
        fpsThrottle.setFrequency(fps);
    }

    /**
     * Enable or disable vsync.
     *
     * @param e {@code true} to enable vsync.
     * @apiNote GLFW can 'suggest' the system to disable / enable vsync. However, the final decision is made
     * by drivers / system.
     */
    public void setVsync(boolean e) {
        if (e) {
            System.out.println("Enabling VSYNC.");
        } else {
            System.out.println("Disabling VSYNC.");
        }
        GLFW.glfwSwapInterval(e ? 1 : 0);
    }

    /**
     * Check if the close flag has been set.
     *
     * @return Whether the window should be closed.
     */
    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(gWindow);
    }

    /**
     * Check if a new frame is needed.
     *
     * @return {@code true} if it's suitable to generate a new frame and {@link #flush()} the window.
     */
    public boolean shouldGenNewFrame() {
        return fpsThrottle.shouldRun();
    }
}
