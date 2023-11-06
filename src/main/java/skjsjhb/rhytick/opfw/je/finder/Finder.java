package skjsjhb.rhytick.opfw.je.finder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Storage path provider.
 */
public final class Finder {
    private static final String SIG_ALGO = "SHA-256";

    private static final String SIG_EXT = ".sig";

    /**
     * OPFW base path
     */
    private static String root = "";

    /**
     * Internal method for encoding byte to hex.
     */
    private static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

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
    private static void checkRoot() {
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
    private static void configureRootPath() throws IOException {
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
     * Internal method for encoding byte to hex.
     */
    private static String encodeHexString(byte[] byteArray) {
        StringBuilder hexStringBuffer = new StringBuilder();
        for (byte b : byteArray) {
            hexStringBuffer.append(byteToHex(b));
        }
        return hexStringBuffer.toString();
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
     * <br/>
     * The specified path is checked before the file is read.
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
            String sig = apt + SIG_EXT;
            boolean verified;
            try {
                verified = verifyIntegrity(content, Files.readString(Paths.get(sig)).trim());
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
        try {
            byte[] hashSource = MessageDigest.getInstance(SIG_ALGO).digest(source);
            return encodeHexString(hashSource).equalsIgnoreCase(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.printf("Could not verify file using %s, algorithm is missing.\n", SIG_ALGO);
            return false;
        }
    }
}
