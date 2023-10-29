package skjsjhb.rhytick.opfw.je.schedule;


/**
 * A task implementation which executes initial method, runs always method until it returns false, and
 * then executes stop method.
 */
public abstract class ServiceTask extends AlwaysTask {

    protected ServiceStatus status = ServiceStatus.INITIAL;

    /**
     * Main executor returning a boolean value indicating whether the service is done.
     * <br/>
     * This is an explicit override.
     *
     * @return {@code false} if {@link #stop()} should be executed.
     */
    @Override
    public abstract boolean always();

    @Override
    public void execute() {
        if (getLoop() == null) {
            return;
        }
        if (status == ServiceStatus.INITIAL) {
            if (getLoop().isRunning()) {
                initial();
                status = ServiceStatus.ALWAYS;
            }
        } else if (status == ServiceStatus.ALWAYS) {
            if (always() && getLoop().isRunning()) {
                getLoop().push(this);
            } else {
                // Stop on demand or when loop stops
                status = ServiceStatus.STOP;
                stop();
            }
        }
    }

    /**
     * Initial method to be executed once.
     */
    public abstract void initial();

    /**
     * Stop method.
     * <br/>
     * The stop method will be called when {@link #always()} returns {@code false}, or automatically
     * when the loop stops.
     */
    public abstract void stop();

    protected enum ServiceStatus {
        INITIAL,
        ALWAYS,
        STOP,
    }

}
