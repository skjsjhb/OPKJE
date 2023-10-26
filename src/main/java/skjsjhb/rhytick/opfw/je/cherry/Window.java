package skjsjhb.rhytick.opfw.je.cherry;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;
import skjsjhb.rhytick.opfw.je.cfg.Cfg;

/**
 * Abstract window object.
 */
public class Window {
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
     * Internal method for initializing.
     */
    protected void init() {
        // Configure video mode
        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (vmode == null) {
            throw new RuntimeException("could not get default video mode");
        }

        int ww, wh;
        ww = vmode.width();
        wh = vmode.height();
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
            throw new RuntimeException("failed to create window");
        }

        // Configure VSYNC
        GLFW.glfwMakeContextCurrent(gWindow);
        if (Cfg.getBoolean("cherry.vsync")) {
            System.out.println("Enabled VSYNC.");
            GLFW.glfwSwapInterval(1);
        } else {
            System.out.println("Disabled VSYNC.");
            GLFW.glfwSwapInterval(0);
        }

        // Initialize OpenGL methods
        glCapabilities = GL.createCapabilities();
        String vendor = GL33C.glGetString(GL33C.GL_VENDOR);
        String version = GL33C.glGetString(GL33C.GL_VERSION);
        String glslVersion = GL33C.glGetString(GL33C.GL_SHADING_LANGUAGE_VERSION);
        System.out.printf("OpenGL %s (%s) GLSL %s\n", version, vendor, glslVersion);
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
     * Close the window, destroy the context, and free its resources.
     */
    public void close() {
        GLFW.glfwDestroyWindow(gWindow);
    }

    /**
     * Check if the close flag has been set.
     *
     * @return Whether the window should be closed.
     */
    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(gWindow);
    }
}
