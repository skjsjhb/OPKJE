package skjsjhb.rhytick.opfw.je.finder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Storage path provider.
 */
public class Finder {
    /**
     * OPFW base path
     */
    protected String root;

    /**
     * Configure a usable base path.
     *
     * @throws IOException If directory operations failed.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    protected void configureRootPath() throws IOException {
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
     * Create file manager with root set automatically.
     */
    public Finder() {
        try {
            configureRootPath();
        } catch (IOException e) {
            System.err.println("Could not configure root path for Finder: " + e);
            System.err.println("This is swallowed for now, but derived exceptions will likely to occur.");
        }
    }

    /**
     * Create file manager with root set to the specified value.
     *
     * @param aRoot An existing path to be used as the root directory.
     */
    public Finder(String aRoot) {
        System.out.println("Using external root path: " + aRoot);
        this.root = Paths.get(aRoot).normalize().toAbsolutePath().toString();
    }

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
    public boolean checkPathBounds(String target) {
        boolean pass = Paths.get(root, target).normalize().toAbsolutePath().startsWith(Paths.get(root));
        if (!pass) {
            System.out.println("Warning: Invalid access to virtual path " + target);
        }
        return pass;
    }

    /**
     * Resolve relative path based on the root path.
     *
     * @param rel Relative path.
     * @return Resolved absolute path.
     * @apiNote The returned path is not checked by any means. Allowing read from / write to arbitrary locations
     * brings unacceptable risks to the client system. Use {@link #checkPathBounds(String)} to limit the access.
     */
    public String resolve(String rel) {
        return Paths.get(root, rel).normalize().toString();
    }

    /**
     * Access the target file using relative path.
     * <br/>
     * The path specified is resolved, normalized and checked. Then the file is opened and returned to the caller.
     *
     * @param rel Relative path.
     * @return Corresponding {@link File}. Note that the file might not exist or ready for use.
     * @throws IllegalAccessException If the target path is invalid.
     */
    public File getFile(String rel) throws IllegalAccessException {
        if (!checkPathBounds(rel)) {
            System.err.println("Malicious access to invalid path blocked. Target: " + rel);
            throw new IllegalAccessException("Virtual target path is not allowed: " + rel);
        }
        return new File(resolve(rel));
    }
}
