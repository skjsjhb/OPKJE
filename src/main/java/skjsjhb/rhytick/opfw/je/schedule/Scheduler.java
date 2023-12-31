package skjsjhb.rhytick.opfw.je.schedule;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Schedule, queue and run tasks specified by the main function.
 */
public final class Scheduler {
    /**
     * Run specified {@link Runnable} task on a seperated thread, without blocking the main loop
     * or other threads, or expecting any results.
     * <br/>
     * The thread will be destroyed once the {@link Runnable} returns. No value will be returned.
     *
     * @param a {@link Runnable} object to be run on the thread.
     * @return The created thread.
     */
    public static Thread runOnNewThread(Runnable a) {
        var th = new Thread(a);
        th.start();
        return th;
    }

    /**
     * Run specified {@link Supplier} task on a seperated thread and get its return value using
     * a {@link Consumer} callback.
     * <br/>
     * The thread will be destroyed once the callback returns. The callback will be called right
     * after the {@link Supplier} returns on the same thread where the {@link Supplier} runs on.
     *
     * @param a  {@link Supplier} object to be run on the thread.
     * @param cb Callback accepting the return value.
     * @return The created thread.
     */
    public static <T> Thread runOnNewThread(Supplier<T> a, Consumer<T> cb) {
        var th = new Thread(() -> cb.accept(a.get()));
        th.start();
        return th;
    }
}
