package skjsjhb.rhytick.opfw.je.cherry;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import skjsjhb.rhytick.opfw.je.cfg.Cfg;

/**
 * Rhytick's Cherry graphics interface.
 */
public class Cherry {
    /**
     * Flag to record whether the native library is already initialized.
     */
    protected static boolean nativeInitialized = false;

    /**
     * GLFW window reference.
     */
    protected long window;

    /**
     * Static method for initializing the underlying graphics library.
     *
     * @apiNote This method must only be called once (though this method already prevents redundant calls) and must
     * be called from the main thread only.
     */
    public static void initNative() {
        if (nativeInitialized) {
            return;
        }
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW!");
        }
    }

    /**
     * Initialize Cherry module.
     * <br/>
     * Cherry will create the default window (invisible by default), configure the OpenGL context, and get all
     * other preparation stuff done. After this call, all Cherry APIs will become usable.
     */
    public void init() {
        // Configure video mode
        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (vmode == null) {
            throw new RuntimeException("Could not get default video mode!");
        }
        int ww, wh;
        ww = vmode.width();
        wh = vmode.height();
        System.out.println("Cherry video mode: " + ww + "x" + wh + "@" + vmode.refreshRate());

        boolean fullscreen = Cfg.getBoolean("cherry.fullscreen");
        String title = Cfg.getValue("cherry.window_title", "OPKJE");
        if (fullscreen) {
            System.out.println("Entering fullscreen mode.");
            window = GLFW.glfwCreateWindow(ww, wh, title, monitor, MemoryUtil.NULL);
        } else {
            System.out.println("Entering windowed mode.");
            double scale = Cfg.getDouble("cherry.window_scale", 0.8);
            window = GLFW.glfwCreateWindow((int) (scale * ww), (int) (scale * wh), title,
                    MemoryUtil.NULL, MemoryUtil.NULL);
        }
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create display context!");
        }
        GLFW.glfwMakeContextCurrent(window);
        if (Cfg.getBoolean("cherry.vsync")) {
            GLFW.glfwSwapInterval(1);
        } else {
            GLFW.glfwSwapInterval(0);
        }
    }

    /**
     * Flush the frame.
     *
     * @apiNote This does not poll events of this window.
     */
    public void flush() {
        if (window != MemoryUtil.NULL) {
            GLFW.glfwSwapBuffers(window);
        }
    }

    /**
     * Poll all events related to the window.
     */
    public void updateEvents() {
        if (window != MemoryUtil.NULL) {
            GLFW.glfwPollEvents();
        }
    }
}
