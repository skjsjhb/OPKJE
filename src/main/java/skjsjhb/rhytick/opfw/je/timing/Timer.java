package skjsjhb.rhytick.opfw.je.timing;

import skjsjhb.rhytick.opfw.je.dce.Expose;
import skjsjhb.rhytick.opfw.je.dce.GuestModule;

@GuestModule(value = "timer", statik = true)
public class Timer {
    /**
     * Nano time offset for {@link #getHighResTime()}.
     */
    protected long nanoTimeOffset;

    public Timer() {
        nanoTimeOffset = System.nanoTime();
    }

    /**
     * Factory method.
     */
    @Expose
    public static Timer newTimer() {
        return new Timer();
    }

    /**
     * Gets the approximate system time.
     *
     * @return Current time in seconds.
     */
    @Expose
    public double getApproxTime() {
        return System.currentTimeMillis() / 1e3;
    }

    /**
     * Gets the relative system time in ns since the timer is created.
     *
     * @return Current time in ns.
     */
    @Expose
    public long getHighResTime() {
        return System.nanoTime() - nanoTimeOffset;
    }

}
