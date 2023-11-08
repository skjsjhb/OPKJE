package skjsjhb.rhytick.opfw.je.schedule;

import javax.annotation.Nullable;

public abstract class Task {
    protected Loop parentLoop;

    /**
     * Converts a {@link Runnable} to {@link Task}.
     *
     * @param a Runnable object.
     */
    static Task from(Runnable a) {
        return new Task() {
            @Override
            public void execute() {
                a.run();
            }
        };
    }

    /**
     * Task executor.
     */
    public abstract void execute();

    /**
     * Gets the parent loop of this task.
     * <br/>
     * This method should only be called by the task executor.
     *
     * @return Parent loop.
     */
    @Nullable
    protected Loop getLoop() {
        return parentLoop;
    }

    /**
     * Sets the parent loop of this task.
     * <br/>
     * This method should only be called by {@link Loop}.
     */
    void setLoop(@Nullable Loop l) {
        parentLoop = l;
    }


}
