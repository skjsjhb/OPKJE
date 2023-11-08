package skjsjhb.rhytick.opfw.je.dce;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guest script worker implementation.
 * <br/>
 * Workers have dedicated loop, with modules shared with the main entry.
 * i.e. A new {@link Emulation} instance will be created.
 */
@GuestModule(value = "workers", statik = true)
public final class WorkerFactory {
    private static final Map<Integer, Emulation> WORKERS_RECORD = new ConcurrentHashMap<>();

    /**
     * Create a worker.
     * <br/>
     * This method also does a self cleanup, check all recorded workers and remove unused records.
     * <br/>
     * This may be called from any thread.
     *
     * @param src Source code for the worker.
     * @return The ID of the newly created worker.
     */
    @Expose
    @SuppressWarnings("unused")
    public static int createWorker(String src) {
        // Cleanup - this won't take long
        for (var e : WORKERS_RECORD.entrySet()) {
            if (!e.getValue().getEnv().getLoop().isRunning()) {
                WORKERS_RECORD.remove(e.getKey());
            }
        }

        // Create new worker
        Emulation emul = new Emulation();
        int id = emul.getEnv().getID();
        emul.prepareRun();

        WORKERS_RECORD.put(id, emul);
        emul.startAsync(src);
        return id;
    }

    /**
     * Gets the worker instance.
     */
    @Nullable
    public static Emulation getWorker(int id) {
        return WORKERS_RECORD.get(id);
    }

    /**
     * Check and stop running workers.
     * <br/>
     * This method is only considered a fallback way to close workers which <b>should have been</b> closed. The
     * creator of the worker is fully responsible for closing the worker when it's not needed anymore, or the creator
     * itself is closing. Relying on this method is considered a bad practice.
     * <br/>
     * This should only be called from the main thread.
     */
    public static void stopAll() {
        int c = 0;
        for (Emulation e : WORKERS_RECORD.values()) {
            if (e.getEnv().getLoop().isRunning()) {
                e.getEnv().getLoop().stop(); // Stop immediately
                c++;
            }
        }
        if (c > 0) {
            System.out.printf("""
                    %d worker(s) have been forcefully closed. Workers should be closed by its caller rather than I.
                    If you're in development, check for any leaks and re-run.
                    """, c);
        }
        WORKERS_RECORD.clear();
    }

    /**
     * Stop a specified worker forcefully.
     * <br/>
     * Note that when a worker is running, it usually know when to stop (calling its {@link VMAPI#stop()}), either
     * by a timing control or a message. This method can be used to stop the worker, and no warnings will be displayed,
     * but is still considered a bad manner.
     *
     * @param id Worker ID.
     */
    @Expose
    @SuppressWarnings("unused")
    public static void stopWorker(int id) {
        if (WORKERS_RECORD.containsKey(id)) {
            WORKERS_RECORD.get(id).getEnv().getLoop().requestStop();
        }
    }
}
