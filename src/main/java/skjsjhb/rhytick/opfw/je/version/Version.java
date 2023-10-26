package skjsjhb.rhytick.opfw.je.version;

import skjsjhb.rhytick.opfw.je.dce.Expose;

/**
 * Meta class declaring the version of the framework.
 */
public final class Version {
    /**
     * API spec name.
     */
    public static final String API_NAME = "OPFW Series";
    /**
     * API spec version in number.
     */
    public static final int API_VER = 1;
    /**
     * Implementation project name.
     */
    public static final String IMPL_NAME = "OPKJE";
    /**
     * Implementation version in string.
     */
    public static final String IMPL_VER = "1.0 \"Cruiser\"";

    /**
     * Get the product info string.
     *
     * @return Generated product info.
     */
    @Expose
    public String getProdString() {
        return API_NAME + " Version " + API_VER + " (" + IMPL_NAME + " " + IMPL_VER + ")";
    }


}
