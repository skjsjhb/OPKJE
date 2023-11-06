package skjsjhb.rhytick.opfw.je.dce;

import skjsjhb.rhytick.opfw.je.finder.Finder;
import skjsjhb.rhytick.opfw.je.launcher.Cfg;

import java.io.IOException;

/**
 * Util class for script code loading.
 */
public final class Codeload {
    /**
     * Reads a script source from file.
     * <br/>
     * Scripts of specific name can be found at <pre>/opt/(name).js</pre>. The script file is read, and verified
     * together with <pre>(name).js.sig</pre>. The signature might be optional for other files, but for scripts,
     * they are necessary, unless flag <pre>emulation.no_script_verify</pre> is set, which is for dev purpose only.
     *
     * @param name Script virtual path.
     * @return The source code of the script.
     * @throws IOException If I/O errors occurred.
     */
    public static String readScriptSource(String name) throws IOException {
        boolean noVerify = Cfg.getBoolean("emulation.no_script_verify", false);
        if (noVerify) {
            System.out.println("Script integrity check has been disabled. The usage of this flag should be limited within" +
                    " development only.");
        }
        byte[] buf = Finder.readFileBytes(name, !noVerify);
        if (buf.length == 0) {
            throw new IOException("empty script source");
        }
        return new String(buf);
    }
}
