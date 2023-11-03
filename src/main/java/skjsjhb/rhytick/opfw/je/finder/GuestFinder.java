package skjsjhb.rhytick.opfw.je.finder;

import skjsjhb.rhytick.opfw.je.dce.DCEModule;
import skjsjhb.rhytick.opfw.je.dce.Expose;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The finder for guest script. With extra limitation and checks.
 */
@DCEModule("finder")
public class GuestFinder {
    /**
     * Gets a {@link File} object by using virtual path.
     *
     * @param vpt File virtual path.
     * @return Opened file object.
     * @apiNote Although the return value is an real instance of {@link File}, it is not accessible from the guest
     * (since class {@code java.io.File} is not exposed to guest). The value can only be used as a reference when
     * intepreting in the guest environment.
     */
    @Expose
    @SuppressWarnings("unused")
    public File getFile(String vpt) {
        Finder.checkPathBounds(vpt);
        return new File(Finder.resolve(vpt));
    }

    /**
     * Reads the content of given file path.
     *
     * @param vpt File virtual path.
     * @return A byte array contains the content of the file. An empty array indicates that the file is empty, or
     * an I/O error occurred.
     */
    @Expose
    @SuppressWarnings("unused")
    public byte[] readFile(String vpt) {
        try {
            return Finder.readFileBytes(vpt);
        } catch (IOException e) {
            System.err.println("Could not read file " + vpt + ": " + e);
            return new byte[0];
        }
    }

    @Expose
    @SuppressWarnings("unused")
    public void writeFile(String vpt, String content) {
        writeFile(vpt, content.getBytes());
    }

    @Expose
    @SuppressWarnings("unused")
    public void writeFile(String vpt, byte[] buf) {
        try {
            Finder.checkPathBounds(vpt);
            Finder.ensureDir(vpt);
            Files.write(Paths.get(Finder.resolve(vpt)), buf);
        } catch (IOException e) {
            System.err.println("Could not write file " + vpt + ": " + e);
        }
    }
}
