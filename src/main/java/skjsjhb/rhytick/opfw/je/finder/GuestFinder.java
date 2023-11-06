package skjsjhb.rhytick.opfw.je.finder;

import skjsjhb.rhytick.opfw.je.dce.Expose;
import skjsjhb.rhytick.opfw.je.dce.GuestModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The finder for guest script. With extra limitation and checks.
 */
@GuestModule(value = "finder", statik = true)
@SuppressWarnings("unused")
public class GuestFinder {
    /**
     * Reads the content of given file path.
     *
     * @param vpt File virtual path.
     * @return A byte array contains the content of the file. An empty array indicates that the file is empty, or
     * an I/O error occurred.
     */
    @Expose
    @SuppressWarnings("unused")
    public static byte[] readFile(String vpt) {
        try {
            return Finder.readFileBytes(vpt);
        } catch (IOException e) {
            System.err.println("Could not read file " + vpt + ": " + e);
            return new byte[0];
        }
    }

    @Expose
    @SuppressWarnings("unused")
    public static void writeFile(String vpt, String content) {
        writeFile(vpt, content.getBytes());
    }

    @Expose
    @SuppressWarnings("unused")
    public static void writeFile(String vpt, byte[] buf) {
        try {
            Finder.checkPathBounds(vpt);
            Finder.ensureDir(vpt);
            Files.write(Paths.get(Finder.resolve(vpt)), buf);
        } catch (IOException e) {
            System.err.println("Could not write file " + vpt + ": " + e);
        }
    }
}
