package skjsjhb.rhytick.opfw.je.finder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Storage path provider.
 */
public class Finder {
    /**
     * OPFW base path
     */
    protected static String root = "";

    /**
     * Checks if the requested path is a subdirectory of the configured root.
     * <br/>
     * Malicious code might utilize relative path functionality to write file to arbitrary place
     * on the client system. This function prevents this (partially) by analyzing the paths and
     * restricting the target path statically. However, this does not prevent symbolic links or other
     * special mechanisms inside the root from being accessed.
     *
     * @param target Relative path for checking.
     * @return {@code true} if this path can be accessed safely.
     */
    public static boolean checkPathBounds(String target) {
        checkRoot();
        boolean pass = Paths.get(root, target).normalize().toAbsolutePath().startsWith(Paths.get(root));
        if (!pass) {
            System.out.println("Warning: Invalid access to virtual path " + target);
        }
        return pass;
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
    public static void configure() {
        try {
            configureRootPath();
        } catch (IOException e) {
            System.err.println("Could not configure root path for Finder: " + e);
            System.err.println("This is swallowed for now, but derived exceptions will likely to occur.");
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
     */
    public static void ensureDir(String path) {
        checkRoot();
        checkPathBounds(path);
        Path target = Paths.get(resolve(path)).getParent();
        if (Files.exists(target) && Files.isDirectory(target)) {
            return;
        }
        // If the target directory exists but is not a directory, this operation will fail silently (the exception
        // is swallowed for now).
        try {
            Files.createDirectories(target);
        } catch (IOException e) {
            System.err.printf("Failed to create directory (%s). This may trigger subsequent exceptions later.\n", path);
        }
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


}
