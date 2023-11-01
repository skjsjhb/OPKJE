package skjsjhb.rhytick.opfw.je.cherry;

import skjsjhb.rhytick.opfw.je.schedule.ServiceTask;

/**
 * Rhytick's Cherry graphics interface. Each instance owns a dedicated window and render context.
 */
public class Cherry extends ServiceTask {
    /**
     * Flag field indicating whether Cherry is running.
     */
    protected boolean running;

    /**
     * Host window instance.
     */
    protected Window window;

    /**
     * Constructs the Cherry instance, create a corresponding window, and load cfg.
     */
    public Cherry() {
        CherryGlobal.addInstance();
        // Create window and mark running
        window = new Window();
        running = true;
    }

    @Override
    public boolean always() {
        if (running) {
            if (window.shouldGenNewFrame()) {
                // TODO main draw methods
                window.flush();
            }
            if (window.shouldClose()) {
                running = false;
                stop();
            }
        }
        return running;
    }

    @Override
    public void initial() {

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
    @Override
    public void stop() {
        CherryGlobal.subInstance();
        System.out.println("Stopping Cherry!");
        window.close();
    }
}
