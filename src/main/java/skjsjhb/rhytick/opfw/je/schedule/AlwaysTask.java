package skjsjhb.rhytick.opfw.je.schedule;

/**
 * A task implementation which optionally repeat itself after its execution.
 */
public abstract class AlwaysTask extends Task {
    @Override
    void execute() {
        if (run() && getLoop() != null && getLoop().running) {
            getLoop().push(this);
        }
    }

    /**
     * User defined method to be called.
     * <br/>
     * {@link AlwaysTask} pushes itself to the end of the task queue as long as this method returns
     * positive answer.
     * <br/>
     * It's highly recommended that no exception is thrown during the execution.
     *
     * @return {@code true} if the task needs to be repeated next time.
     */
    public abstract boolean run();
}
