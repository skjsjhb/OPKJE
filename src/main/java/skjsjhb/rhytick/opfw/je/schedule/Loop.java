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
     * Running flag.
     */
    protected boolean running = false;

    /**
     * The thread this loop belongs.
     */
    protected long homeThreadId;


    /**
     * Check if this method is called on the same thread where the loop is created.
     */
    protected void checkThread() {
        if (homeThreadId != Thread.currentThread().threadId()) {
            throw new IllegalStateException("not calling from the home thread");
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
     * Start the loop. This method will block the thread until the loop ends.
     */
    public void start() {
        homeThreadId = Thread.currentThread().threadId();
        running = true;
        loop();
    }

    /**
     * Stop the loop.
     * <br/>
     * This method must be called on the home thread.
     */
    public void requestStop() {
        checkThread();
        running = false;
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
}
