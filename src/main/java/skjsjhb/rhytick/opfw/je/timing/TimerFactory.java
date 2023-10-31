package skjsjhb.rhytick.opfw.je.timing;

import skjsjhb.rhytick.opfw.je.dce.DCEModule;
import skjsjhb.rhytick.opfw.je.dce.Expose;

@DCEModule("timer")
public final class TimerFactory {
    /**
     * Generate a new timer instance.
     */
    @Expose
    public Timer newTimer() {
        return new Timer();
    }
}
