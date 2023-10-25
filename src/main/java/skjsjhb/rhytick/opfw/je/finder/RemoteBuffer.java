package skjsjhb.rhytick.opfw.je.finder;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.Buffer;

/**
 * A class representing a buffer in the OPFW library. Guest script can operate it without knowing
 * its existance or content.
 * <br/>
 * Suppose we have a video player class, as well as a video file. Now the guest script wants to play
 * the video file. Is the guest script required to read the content of the file, convert it to a language
 * specific buffer, and then call the player method? This will bring heavy throughout impact and more importantly,
 * this is not necessary.
 * <br/>
 * By using {@link RemoteBuffer}, the library manages all the buffer for the guest script. When the guest
 * <b>REFERENCES</b> but not <b>READS</b> the buffer, only an identifier is returned. The buffer might be
 * filled instantly or on demand, with itself managed by the library, rather than the guest.
 */
public class RemoteBuffer {
    /**
     * Underlying native {@link Buffer}. Might be {@code null} if the buffer is not initialized.
     */
    @Nullable
    protected Buffer buffer = null;
    /**
     * Corresponding {@link java.io.File} object. Might be {@code null} if the
     * buffer does not have linked file.
     */
    @Nullable
    protected File file = null;
}
