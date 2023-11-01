package skjsjhb.rhytick.opfw.je.finder;

import skjsjhb.rhytick.opfw.je.dce.DCEModule;
import skjsjhb.rhytick.opfw.je.dce.Expose;

import java.io.File;
import java.io.IOException;

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
     * intepreting in the JS environment.
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
    public byte[] readFileContent(String vpt) {
        try {
            return Finder.readFileBytes(vpt);
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
