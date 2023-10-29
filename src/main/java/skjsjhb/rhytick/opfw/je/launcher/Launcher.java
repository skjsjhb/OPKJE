package skjsjhb.rhytick.opfw.je.launcher;

import skjsjhb.rhytick.opfw.je.cfg.Cfg;

/**
 * Main entry class for the OPFW (OPKJE) support.
 * This launcher starts all services, call the main script entry and maintain all necessary envs.
 */
public final class Launcher {
    public static void main(String[] args) {
        Cfg.loadBundledCfg();
    }
}
