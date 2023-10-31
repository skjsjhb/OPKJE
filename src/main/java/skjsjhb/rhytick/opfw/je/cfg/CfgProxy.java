package skjsjhb.rhytick.opfw.je.cfg;

import skjsjhb.rhytick.opfw.je.dce.DCEModule;
import skjsjhb.rhytick.opfw.je.dce.Expose;

/**
 * Proxy to cfg static interfaces.
 */
@DCEModule("cfg")
public final class CfgProxy {
    @Expose
    public boolean getBoolean(String k, boolean dv) {
        return Cfg.getBoolean(k, dv);
    }

    @Expose
    public boolean getBoolean(String k) {
        return Cfg.getBoolean(k);
    }

    @Expose
    public double getDouble(String k, double dv) {
        return Cfg.getDouble(k, dv);
    }

    @Expose
    public double getDouble(String k) {
        return Cfg.getDouble(k);
    }

    @Expose
    public int getInt(String k, int dv) {
        return Cfg.getInt(k, dv);
    }

    @Expose
    public int getInt(String k) {
        return Cfg.getInt(k);
    }

    @Expose
    public String getValue(String k, String dv) {
        return Cfg.getValue(k, dv);
    }

    @Expose
    public String getValue(String k) {
        return Cfg.getValue(k);
    }
}
