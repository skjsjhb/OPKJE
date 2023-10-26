package skjsjhb.rhytick.opfw.je.timing;

/**
 * A throttled action which runs with limited speed.
 */
public class Throttle {
    protected double nspt = 0;

    protected long lastTimestamp = 0;
    protected Timer timer = new Timer();

    /**
     * Set the target frequency.
     *
     * @param f Expected frequency.
     * @apiNote This sets the internal interval to a proper value which makes the specified event
     * happens <b>no more than</b> the expected frequency.
     */
    public void setFrequency(int f) {
        nspt = 1e9 / f;
    }

    /**
     * Check if it's already time.
     */
    public boolean shouldRun() {
        var delay = timer.getHighResTime() - lastTimestamp;
        if (delay >= nspt) {
            lastTimestamp += delay;
            return true;
        }
        return false;
    }
}
