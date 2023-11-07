package skjsjhb.rhytick.opfw.je.tests;

import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skjsjhb.rhytick.opfw.je.dce.Expose;
import skjsjhb.rhytick.opfw.je.dce.ScriptEnv;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dynamic Code Evaluation Module Tests")
public class DCETests {
    static AccessTester accessTester = new AccessTester();

    static IntCache buffer = new IntCache();

    static ScriptEnv se;

    @BeforeAll
    static void init() {
        se = new ScriptEnv();
        se.initVMAPI();

        // Report value
        se.setGlobal("report", buffer, false);
        se.setGlobal("access", accessTester, false);
    }

    @BeforeEach
    void resetTesters() {
        accessTester.accessed = false;
        buffer.set(0);
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
        assertTrue(accessTester.accessed);
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
    @DisplayName("Block Access to Unexposed Interfaces")
    void testUnexposedAccess() {
        assertThrows(PolyglotException.class, () -> {
            se.eval("access.disallow();");
        });
        assertFalse(accessTester.accessed);
    }

    public static class AccessTester {
        boolean accessed = false;

        @Expose
        public void allow() {
            accessed = true;
        }

        public void disallow() {
            accessed = false;
        }
    }

    public static class IntCache {
        int content;

        @Expose
        public void set(int c) {
            content = c;
        }
    }
}
