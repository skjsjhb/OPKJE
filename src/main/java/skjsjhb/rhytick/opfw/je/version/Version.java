package skjsjhb.rhytick.opfw.je.version;

import skjsjhb.rhytick.opfw.je.cfg.Cfg;
import skjsjhb.rhytick.opfw.je.dce.Expose;

/**
 * Meta class declaring the version of the framework.
 */
public final class Version {
    /**
     * Gets the API version.
     *
     * @return The latest OPFW API version this implementation supports.
     * @apiNote APIs are not guaranteed to
     */
    public int getAPIVersion() {
        return Cfg.getInt("version.api_ver");
    }

    /**
     * Get the product info string.
     *
     * @return Generated product info.
     */
    @Expose
    public String getProdString() {
        return Cfg.getValue("version.api_name") + " Version " +
                Cfg.getValue("version.api_ver") + " (" +
                Cfg.getValue("version.impl_name") + " " +
                Cfg.getValue("version.impl_ver") + ")";
    }


}
