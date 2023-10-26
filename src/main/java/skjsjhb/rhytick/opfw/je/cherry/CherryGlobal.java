package skjsjhb.rhytick.opfw.je.cherry;


import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import skjsjhb.rhytick.opfw.je.cfg.Cfg;
import skjsjhb.rhytick.opfw.je.schedule.AlwaysTask;

/**
 * Global support for Cherry service including event polling.
 */
public class CherryGlobal extends AlwaysTask {

    protected static boolean glfwInitialized = false;
    protected static int irqMax, pscInterval;

    protected static int instanceCount = 0;

    public static void addInstance() {
        instanceCount++;
    }

    public static void subInstance() {
        instanceCount--;
    }

    public static void init() {
        if (glfwInitialized) {
            return;
        }
        glfwInitialized = true;
        System.out.println("Initializing native GLFW library.");
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("failed to initialize GLFW");
        }
        irqMax = Cfg.getInt("cherry.irq_max", 2048);
        System.out.println("IRQ set to " + irqMax);
        pscInterval = Cfg.getInt("cherry.psc_interval", 30);
    }

    public static void pollEvents() {
        if (instanceCount > 0) {
            GLFW.glfwPollEvents();
        }
    }

    public static void deInit() {
        if (!glfwInitialized) {
            return;
        }
        glfwInitialized = false;
        System.out.println("Terminating GLFW library.");
        GLFW.glfwTerminate();
    }

    @Override
    public boolean run() {
        if (instanceCount > 0) {
            pollEvents();
        }
        return true;
    }
}
