package skjsjhb.rhytick.opfw.je.cherry;

import skjsjhb.rhytick.opfw.je.cfg.Cfg;
import skjsjhb.rhytick.opfw.je.schedule.AlwaysTask;
import skjsjhb.rhytick.opfw.je.timing.Throttle;

/**
 * Rhytick's Cherry graphics interface. Each instance owns a dedicated window and render context.
 */
public class Cherry extends AlwaysTask {
    /**
     * Host window instance.
     */
    protected Window window;

    /**
     * Flag field indicating whether Cherry is running.
     */
    protected boolean running;

    /**
     * FPS limit.
     */
    protected Throttle fpsThrottle = new Throttle();

    /**
     * Constructs the Cherry instance, create a corresponding window, and load cfg.
     */
    public Cherry() {
        CherryGlobal.addInstance();
        // Create window and mark running
        window = new Window();
        running = true;
        int fps = Cfg.getInt("cherry.fps_max", 330);
        System.out.println("FPS set to " + fps);
        fpsThrottle.setFrequency(fps);
    }

    /**
     * Check if this Cherry instance is running.
     *
     * @return Running status.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Stop the current cherry instance and stop the library.
     */
    public void stop() {
        CherryGlobal.subInstance();
        System.out.println("Stopping Cherry!");
        window.close();
    }

    @Override
    public boolean run() {
        if (running) {
            window.flush();
            if (window.shouldClose()) {
                running = false;
                stop();
            }
        }
        return running;
    }
}
