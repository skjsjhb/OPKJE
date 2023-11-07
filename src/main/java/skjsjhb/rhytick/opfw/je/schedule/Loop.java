package skjsjhb.rhytick.opfw.je.schedule;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Loops process micro non-blocking tasks synchronizingly.
 * <br/>
 * One loop takes one dedicated thread to run itself (the home thread). It can only be started and stopped on
 * this thread.
 * <br/>
 * Once a loop has stopped, it cannot be re-used (i.e. start again). Resource leak might happen if doing so.
 */
public class Loop {
    /**
     * Tasks buffer.
     */
    protected final Queue<Task> tasks = new ConcurrentLinkedQueue<>();

    /**
     * The thread this loop belongs.
     */
    protected long homeThreadId;

    /**
     * Running flag.
     */
    protected RunningFlag running = new RunningFlag();

    /**
     * Check if this method is called on the same thread where the loop is created.
     */
    protected void checkThread() {
        if (homeThreadId != Thread.currentThread().threadId()) {
            throw new IllegalStateException("not calling from the home thread");
        }
    }

    /**
     * Check if the loop is running.
     * <br/>
     * This method may be called from any thread.
     */
    public synchronized boolean isRunning() {
        return running.isRunning();
    }

    /**
     * Main loop executor.
     * <br/>
     * This method continously poll event from the queue and execute them. If the queue is empty, it
     * will wait for the next task to come.
     */
    protected void loop() {
        while (running.isRunning()) {
            Task t = tasks.poll();
            if (t != null) {
                t.execute();
            }
        }
    }

    /**
     * Append a task to the end of the task queue.
     * <br/>
     * This method can be called from any thread.
     *
     * @param a Task to add.
     */
    public void push(Task a) {
        tasks.add(a);
        a.setLoop(this);
    }

    /**
     * {@link Runnable} overload of {@link #push(Task)}.
     */
    public void push(Runnable a) {
        push(Task.from(a));
    }

    /**
     * Requests the loop to stop.
     * <br/>
     * The {@link #running} flag will be set to {@code false} immediately. No more tasks are allowed to
     * be added after this method. Existing tasks can still finish execution.
     * <br/>
     * This method can be called from any thread.
     */
    public void requestStop() {
        running.setRunning(false);
    }

    /**
     * Start the loop. This method will block the thread until the loop ends.
     */
    public void start() {
        if (isRunning()) {
            throw new IllegalStateException("loop is already running");
        }
        homeThreadId = Thread.currentThread().threadId();
        running.setRunning(true);
        loop();
    }

    /**
     * Stop the loop and ignore any subsequent tasks.
     * <br/>
     * This can be called from any thread.
     */
    public void stop() {
        if (!isRunning()) {
            return; // Fails silently
        }
        requestStop();
        tasks.clear();
    }

    /**
     * Wrapper object for running flag.
     */
    protected static final class RunningFlag {
        private boolean running = false;

        public synchronized boolean isRunning() {
            return running;
        }

        public synchronized void setRunning(boolean ar) {
            running = ar;
        }
    }
}
