package skjsjhb.rhytick.opfw.je.timer;

import org.lwjgl.glfw.GLFW;

public class Timer {
    /**
     * Nano time offset for {@link #getHighResTime()}.
     */
    protected long nanoTimeOffset;

    public Timer() {
        nanoTimeOffset = System.nanoTime();

    }

    /**
     * Gets the time since LWJGL is initialized.
     * <br/>
     * Note that GLFW must be initialized when calling.
     *
     * @return Current time in seconds.
     */
    public double getTime() {
        return GLFW.glfwGetTime();
    }

    /**
     * Gets the relative system time in ns since the timer is created.
     *
     * @return Current time in ns.
     */
    public long getHighResTime() {
        return System.nanoTime() - nanoTimeOffset;
    }
}
