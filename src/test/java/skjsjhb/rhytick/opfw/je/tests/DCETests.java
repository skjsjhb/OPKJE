package skjsjhb.rhytick.opfw.je.tests;

import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.*;
import skjsjhb.rhytick.opfw.je.dce.*;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dynamic Code Evaluation")
@Timeout(5)
public class DCETests {
    static AccessTester accessTester = new AccessTester();

    static IntCache buffer = new IntCache();

    static ScriptEnv se;

    @BeforeAll
    static void init() {
        Modular.autoRegister();

        se = new ScriptEnv();
        se.initVMAPI();
        se.setGlobal("report", buffer, false);
        se.setGlobal("access", accessTester, false);
        se.setGlobal("array", GetArray.class, true);
        se.setGlobal("statik", AccessTester.class, true);
        Modular.addModule("tt", ThreadTester.class, true);
    }

    @BeforeEach
    void resetTesters() {
        AccessTester.accessed = false;
        buffer.set(0);
    }

    @Test
    @DisplayName("Array Type Transform")
    void testArrayTransform() {
        se.eval("const b = array.getArray(); report.set(VM.require('util').toArray(b)[3]);");
        assertEquals(buffer.content, GetArray.getArray()[3]);
    }

    @Test
    @DisplayName("Minimum Script Evaluation")
    void testEval() {
        se.eval("report.set(42);");
        assertEquals(42, buffer.content);
    }

    @Test
    @DisplayName("Accept Access to Exposed Interfaces")
    void testExposedAccess() {
        se.eval("access.allow();");
        assertTrue(AccessTester.accessed);
    }

    @Test
    @DisplayName("Block Access to Unexposed Interfaces")
    void testNoUnexposedAccess() {
        assertThrows(PolyglotException.class, () -> se.eval("access.disallow();"));
        assertFalse(AccessTester.accessed);
    }

    @Test
    @DisplayName("Planned Script Execution")
    void testPushScript() {
        se.pushScript("report.set(1);");
        se.pushScript("report.set(2);");
        se.getLoop().makeCurrent(Thread.currentThread());
        se.getLoop().runOnce();
        assertEquals(buffer.content, 1);
        se.getLoop().runOnce();
        assertEquals(buffer.content, 2);
    }

    @Test
    @DisplayName("Access to Static Methods")
    void testStaticAccess() {
        se.eval("statik.accessStatic();");
        assertTrue(AccessTester.accessed);
    }

    @Test
    @DisplayName("String Encoding and Decoding")
    void testStringCoding() {
        String str = "hello, world";
        se.setGlobal("s", str, false);
        se.eval("const u = VM.require('util');if(s !== u.decodeString(u.encodeString(s))) throw 'Failed!';");
    }


    @Test
    @DisplayName("Minimum Call to VMAPI")
    void testVMAPI() {
        se.eval("VM.requestLoop(()=>{report.set(1);})");
        se.getLoop().makeCurrent(Thread.currentThread());
        se.getLoop().runOnce();
        assertEquals(buffer.content, 1);
    }

    @RepeatedTest(value = 8, name = Values.REPEAT_TEST_TITLE)
    @DisplayName("Worker Thread Spawning")
    @SuppressWarnings("StatementWithEmptyBody")
    void testWorkerSpawn() throws InterruptedException {
        se.eval("report.set(VM.require('workers').createWorker(`VM.require('tt').set();VM.stop();`));");
        Emulation wk = Objects.requireNonNull(WorkerFactory.getWorker(buffer.content));
        while (!wk.getEnv().getLoop().isRunning()) {
        } // Wait until the worker is ready, this won't take long
        Objects.requireNonNull(wk.getThread()).join();
        se.eval("VM.require('tt').check();");
    }

    @RepeatedTest(value = 8, name = Values.REPEAT_TEST_TITLE)
    @DisplayName("External Worker Stop Request")
    @SuppressWarnings("StatementWithEmptyBody")
    void testWorkerStopExternal() throws InterruptedException {
        se.eval("report.set(VM.require('workers').createWorker(``));");
        Emulation wk = Objects.requireNonNull(WorkerFactory.getWorker(buffer.content));
        while (!wk.getEnv().getLoop().isRunning()) {
        }
        wk.getEnv().getLoop().requestStop();
        Objects.requireNonNull(wk.getThread()).join();
        assertFalse(wk.getEnv().getLoop().isRunning());
    }

    void testWorkerStopInternal() {
        se.eval("report.set(VM.require('workers').createWorker(``));");
    }

    public static class AccessTester {
        static boolean accessed = false;

        @Expose
        @SuppressWarnings("unused")
        public static void accessStatic() {
            accessed = true;
        }

        @Expose
        @SuppressWarnings("unused")
        public void allow() {
            accessed = true;
        }

        @SuppressWarnings("unused")
        public void disallow() {
            accessed = false;
        }
    }

    public static class GetArray {
        @Expose
        public static int[] getArray() {
            return new int[]{1, 3, 5, 2, 4, 6};
        }
    }

    public static class IntCache {
        int content;

        @Expose
        public void set(int c) {
            content = c;
        }
    }

    public static class ThreadTester {
        static long threadId = -1;

        @Expose
        @SuppressWarnings("unused")
        public synchronized static void check() {
            assertNotEquals(threadId, -1);
            assertNotEquals(threadId, Thread.currentThread().threadId());
        }

        @Expose
        @SuppressWarnings("unused")
        public synchronized static void set() {
            threadId = Thread.currentThread().threadId();
        }
    }
}
