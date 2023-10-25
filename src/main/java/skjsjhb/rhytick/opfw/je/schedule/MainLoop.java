package skjsjhb.rhytick.opfw.je.schedule;

import java.util.List;
import java.util.Vector;
import java.util.function.Supplier;

/**
 * Main loop of the OPFW system. Processing all tasks and update status.
 * <br/>
 * One loop takes one dedicated thread to run itself.
 */
public class MainLoop {
    protected boolean running = false;
    protected final List<Runnable> mainLoopTasksOnce = new Vector<>();
    protected final List<Supplier<Boolean>> mainLoopTasksUntil = new Vector<>();
    protected final List<Runnable> mainLoopTasksForever = new Vector<>();


    /**
     * Schedule a {@link Runnable} task to be run once on the main loop (immediately or later). The task
     * will be executed on the next tick if created after the pre-process of the tick, or on the current
     * tick if not.
     * <br/>
     * The tasks will be called in their order in registration.
     *
     * @param a {@link Runnable} object to be run on the main thread.
     * @apiNote All tasks are called synchronized during the main loop one-by-one. If a task takes
     * too long to execute, the main loop will be blocked and the application will freeze. Avoid running
     * CPU-heavy or long-blocking tasks using this method.
     */
    public void runOnMainLoopOnce(Runnable a) {
        mainLoopTasksOnce.add(a);
    }

    /**
     * Add a {@link Supplier} task to be run in the main loop until certain conditions are met.
     * <br/>
     * The provided {@link Supplier} will be executed on each tick in the main loop, then its return
     * value will be examined. If {@code true} is returned, the task will be removed and no longer runs.
     * Otherwise the task will be executed on the next tick and the same progress will happen. Suitable
     * for temporal tasks which can't be done in one tick, or monitor tasks to wait for certain signal.
     *
     * @param a {@link Supplier} object to be run on the main thread which returns a {@link Boolean} indicating
     *          whether the task has finished.
     * @apiNote All tasks are called synchronized during the main loop one-by-one. If a task takes
     * too long to execute, the main loop will be blocked and the application will freeze. Avoid running
     * CPU-heavy or long-blocking tasks using this method.
     */
    public void runOnMainLoopUntil(Supplier<Boolean> a) {
        mainLoopTasksUntil.add(a);
    }

    /**
     * Add a {@link Runnable} task to be run on the main loop forever.
     * <br/>
     * This is designed to be called by most long-term services (e.g. graphics rendering) which does
     * not stop until the main loop itself ends. Once a task is added, it will keep running on each
     * tick.
     * <br/>
     * Tasks with forever timescale always runs first among all the tasks scheduled.
     *
     * @param a {@link Runnable} object to be run on the main loop forever.
     * @apiNote All tasks are called synchronized during the main loop one-by-one. If a task takes
     * too long to execute, the main loop will be blocked and the application will freeze. Avoid running
     * CPU-heavy or long-blocking tasks using this method.
     */
    public void runOnMainLoopForever(Runnable a) {
        mainLoopTasksForever.add(a);
    }


    /**
     * Start the loop on <b>the current thread</b>. This method will block this thread until the
     * loop is stopped.
     */
    public void start() {
        running = true;
        loopLauncher();
    }

    /**
     * Stop the loop. This method must be called on the thread where {@link #start()} is called.
     */
    public void requestStop() {
        running = false;
    }

    /**
     * Main loop executor.
     */
    protected void loop() {
        List<Runnable> currentLoop = new Vector<>();
        currentLoop.addAll(mainLoopTasksForever);
        currentLoop.addAll(mainLoopTasksOnce);
        mainLoopTasksOnce.clear();
        currentLoop.addAll(mainLoopTasksUntil.stream().map((s) -> (Runnable) () -> {
            if (s.get()) {
                mainLoopTasksUntil.remove(s);
            }
        }).toList());
        currentLoop.forEach(Runnable::run);
    }

    /**
     * Internal method which runs a while loop forever until the {@code running} flag is set.
     */
    protected void loopLauncher() {
        while (running) {
            loop();
        }
    }
}
