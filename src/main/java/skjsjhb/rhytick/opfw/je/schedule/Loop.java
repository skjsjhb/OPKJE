package skjsjhb.rhytick.opfw.je.schedule;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Loops process micro non-blocking tasks synchronizingly.
 * <br/>
 * One loop takes one dedicated thread to run itself (the home thread). It can only be started and stopped on
 * this thread.
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
    protected boolean running = false;

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
     *
     * @apiNote Even if this method returns {@code false}, it does not mean that the loop
     * has stopped, nor indicating the future status of the loop.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Main loop executor.
     * <br/>
     * This method continously poll event from the queue and execute them. If the queue is empty, it
     * will wait for the next task to come.
     */
    protected void loop() {
        while (running) {
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
        if (isRunning()) {
            throw new IllegalStateException("loop is stopping");
        }
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
     * This method must be called on the home thread.
     */
    public void requestStop() {
        checkThread();
        running = false;
    }

    /**
     * Start the loop. This method will block the thread until the loop ends.
     */
    public void start() {
        homeThreadId = Thread.currentThread().threadId();
        running = true;
        loop();
    }
}
