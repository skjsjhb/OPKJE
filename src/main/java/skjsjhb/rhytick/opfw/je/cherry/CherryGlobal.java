package skjsjhb.rhytick.opfw.je.cherry;


import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import skjsjhb.rhytick.opfw.je.launcher.Cfg;
import skjsjhb.rhytick.opfw.je.schedule.ServiceTask;

/**
 * Global support for Cherry service including event polling.
 */
public class CherryGlobal extends ServiceTask {

    protected static boolean glfwInitialized = false;

    protected static int instanceCount = 0;

    protected static int irqMax, pscInterval;

    public static void addInstance() {
        instanceCount++;
    }

    public static void pollEvents() {
        if (instanceCount > 0) {
            GLFW.glfwPollEvents();
        }
    }

    public static void subInstance() {
        instanceCount--;
    }

    @Override
    public boolean always() {
        if (instanceCount > 0) {
            pollEvents();
        }
        return true;
    }

    @Override
    public void initial() {
        if (glfwInitialized) {
            return;
        }
        glfwInitialized = true;
        System.out.println("Initializing native GLFW library.");
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new CherryRuntimeException("failed to initialize GLFW");
        }
        irqMax = Cfg.getInt("cherry.irq_max", 2048);
        System.out.println("IRQ limit set to " + irqMax);
        pscInterval = Cfg.getInt("cherry.psc_interval", 30);
    }

    @Override
    public void stop() {
        if (!glfwInitialized) {
            return;
        }
        glfwInitialized = false;
        System.out.println("Terminating GLFW library.");
        GLFW.glfwTerminate();
    }
}
