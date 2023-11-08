package skjsjhb.rhytick.opfw.je.tests;

import org.junit.jupiter.api.*;
import skjsjhb.rhytick.opfw.je.schedule.AlwaysTask;
import skjsjhb.rhytick.opfw.je.schedule.Loop;
import skjsjhb.rhytick.opfw.je.schedule.Scheduler;
import skjsjhb.rhytick.opfw.je.schedule.Task;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Loop and Scheduler")
@Timeout(5)
public class LoopTests {
    Loop loop;

    @BeforeEach
    void init() {
        loop = new Loop();
        loop.makeCurrent(Thread.currentThread());
    }

    @Test
    @DisplayName("Auto Renew Loop Task")
    void testAutoGenTask() {
        AtomicInteger a = new AtomicInteger(0);
        Task tk = new Task() {
            @Override
            public void execute() {
                if (a.incrementAndGet() != 5) {
                    Objects.requireNonNull(getLoop()).push(this);
                }
            }
        };
        loop.push(tk);
        while (loop.getQueueLength() > 0) {
            loop.runOnce();
        }
        assertEquals(a.get(), 5);
    }

    @Test
    @DisplayName("Minimum Loop Run")
    void testLoop() {
        AtomicInteger a = new AtomicInteger(0);
        loop.push(() -> a.set(5));
        loop.runOnce();
        assertEquals(a.get(), 5);
    }

    @Test
    @DisplayName("Block Starting from Another Thread")
    void testNoAsyncStart() throws InterruptedException {
        ExecutorService es = Executors.newSingleThreadExecutor();
        var r = es.submit(() -> loop.start());
        try {
            r.get();
        } catch (ExecutionException e) {
            assertInstanceOf(IllegalStateException.class, e.getCause());
        }
        es.close();
    }

    @RepeatedTest(value = 8, name = Values.REPEAT_TEST_TITLE)
    @DisplayName("Stop Infinite Loop")
    @SuppressWarnings("StatementWithEmptyBody")
    void testStopInfiniteLoop() throws InterruptedException {
        loop.push(new Runnable() {
            @Override
            public void run() {
                loop.push(this);
            }
        });
        var t = new Thread(() -> {
            loop.makeCurrent(Thread.currentThread());
            loop.start();
        });
        t.start();
        while (!loop.isRunning()) {
        }
        loop.stop();
        t.join();
    }

    @Test
    @DisplayName("Task Execution Order in Loop")
    void testTaskOrder() {
        AtomicInteger a = new AtomicInteger(0);
        int cmp = 0;
        AlwaysTask at = new AlwaysTask() {
            @Override
            public boolean always() {
                a.addAndGet(2);
                return true;
            }
        };
        AlwaysTask at2 = new AlwaysTask() {
            @Override
            public boolean always() {
                a.addAndGet(1);
                return true;
            }
        };
        loop.push(at);
        loop.push(at2);
        for (int i = 0; i < 10; i++) {
            loop.runOnce();
            cmp += 2;
            assertEquals(a.get(), cmp);
            loop.runOnce();
            cmp += 1;
            assertEquals(a.get(), cmp);
        }
    }

    @Test
    @DisplayName("Async Thread Calls")
    void testThread() throws InterruptedException {
        AtomicInteger a = new AtomicInteger(0);
        Scheduler.runOnNewThread(() -> {
            a.incrementAndGet();
            return a;
        }, AtomicInteger::incrementAndGet).join();
        assertEquals(a.get(), 2);
    }
}
