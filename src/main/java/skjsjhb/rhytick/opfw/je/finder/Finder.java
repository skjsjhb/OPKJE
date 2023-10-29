package skjsjhb.rhytick.opfw.je.finder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Storage path provider.
 */
public class Finder {
    /**
     * OPFW base path
     */
    protected static String root = "";

    /**
     * Checks if the requested path is a subdirectory of the configured root. If the check fails, an exception is
     * thrown.
     * <br/>
     * Malicious code might utilize relative path functionality to write file to arbitrary place
     * on the client system. This function prevents this (partially) by analyzing the paths and
     * restricting the target path statically. However, this does not prevent symbolic links or other
     * special mechanisms inside the root from being accessed.
     *
     * @param target Relative path for checking.
     */
    public static void checkPathBounds(String target) {
        checkRoot();
        boolean pass = Paths.get(root, target).normalize().toAbsolutePath().startsWith(Paths.get(root));
        if (!pass) {
            throw new IllegalArgumentException("vfs path out of bounds: " + target);
        }
    }

    /**
     * Check if the root path is configured.
     */
    protected static void checkRoot() {
        if (root.isEmpty()) {
            throw new IllegalStateException("root path not configured");
        }
    }

    /**
     * Configure the root path.
     */
    public static void configure() throws IOException {
        System.out.println("Configuring Finder paths.");
        try {
            configureRootPath();
        } catch (IOException e) {
            throw new IOException("could not configure root path", e);
        }
    }

    /**
     * Configure a usable base path.
     *
     * @throws IOException If directory operations failed.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    protected static void configureRootPath() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) {
            root = System.getProperty("user.home") + "/Library/Application Support/Rhytick/OPFW";
        } else if (os.contains("win")) {
            if ((root = System.getenv("LOCALAPPDATA")) != null) {
            } else if ((root = System.getenv("APPDATA")) != null) {
            } else {
                root = System.getProperty("user.home");
            }
            root += "\\Rhytick\\OPFW";
        } else {
            root = System.getProperty("user.home") + "/.rhytick/OPFW";
        }
        root = Paths.get(root).normalize().toAbsolutePath().toString();
        System.out.println("Root path set to: " + root);
        Files.createDirectories(Paths.get(root));
        System.out.println("Successfully configured root path.");
    }

    /**
     * Create specified directory on demand.
     *
     * @param path Relative path. Its <b>parent</b> will be checked and created.
     * @throws IOException If the directory could not be created.
     */
    public static void ensureDir(String path) throws IOException {
        checkRoot();
        checkPathBounds(path);
        Path target = Paths.get(resolve(path)).getParent();
        if (Files.exists(target) && Files.isDirectory(target)) {
            return;
        }
        Files.createDirectories(target);
    }

    /**
     * Overload of {@link #readFileBytes(String, boolean)} without validation.
     */
    public static byte[] readFileBytes(String pt) throws IOException {
        return readFileBytes(pt, false);
    }

    /**
     * Read all bytes of a file. Optinally verify its integrity.
     *
     * @param pt Relative path of the file.
     * @return The content of the file.
     * @throws IOException If the file cannot be read, or the integrity check failed. The error
     *                     message will indicate the cause.
     * @apiNote The file must be encoded using UTF-8.
     */
    public static byte[] readFileBytes(String pt, boolean validate) throws IOException {
        String apt = resolve(pt);
        checkPathBounds(pt);
        byte[] content = Files.readAllBytes(Paths.get(apt));
        if (validate) {
            String sig = apt + ".sig";
            boolean verified;
            try {
                verified = verifyIntegrity(content, Files.readString(Paths.get(sig)));
            } catch (IOException e) {
                throw new IOException("could not read integrity file", e);
            }
            if (!verified) {
                throw new IOException("integrity check failed");
            }
        }
        return content;
    }

    /**
     * Resolve relative path based on the root path.
     *
     * @param rel Relative path.
     * @return Resolved absolute path.
     * @apiNote The returned path is not checked by any means. Allowing read from / write to arbitrary locations
     * brings unacceptable risks to the client system. Use {@link #checkPathBounds(String)} to limit the access.
     */
    public static String resolve(String rel) {
        checkRoot();
        return Paths.get(root, rel).normalize().toString();
    }

    /**
     * Validate the integrity of the spcified file against provided hash.
     *
     * @return {@code true} If the hash matches.
     */
    public static boolean verifyIntegrity(byte[] source, String hash) {
        // Use SHA-256 to hash the source
        try {
            byte[] hashSource = MessageDigest.getInstance("SHA-256").digest(source);
            return Arrays.equals(hashSource, hash.getBytes());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Could not verify file using SHA-256, algorithm is missing.");
            return false;
        }
    }
}
