package skjsjhb.rhytick.opfw.je.launcher;

import skjsjhb.rhytick.opfw.je.cfg.Cfg;
import skjsjhb.rhytick.opfw.je.dce.Emulation;
import skjsjhb.rhytick.opfw.je.finder.Finder;
import skjsjhb.rhytick.opfw.je.finder.KV;

import javax.swing.*;
import java.io.IOException;

/**
 * Main entry class for the OPFW (OPKJE) support.
 * This launcher starts all services, call the main script entry and maintain all necessary envs.
 */
public final class Launcher {
    public static void main(String[] args) {
        // Set UI LAF for message displaying
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException e) {
            System.out.println("Could not initialize UI look and feel: " + e);
        }
        prepareLaunch();
        Emulation emul = new Emulation();
        try {
            emul.start();
        } catch (Emulation.EmulationVMException e) {
            Reporter.reportError(e, "VM is required for code evaluation.\nThe application cannot continue.");
            System.exit(1);
        }
        prepareExit();
    }

    private static void prepareExit() {
        System.out.println("Stopping!");
        KV.save();
    }

    private static void prepareLaunch() {
        try {
            Cfg.loadBundledCfg();
        } catch (IOException e) {
            Reporter.reportError(e, "Built-in configuration won't be applied. Therefore, the application will" +
                    " likely to malfunction.");
        }
        try {
            Finder.configure();
        } catch (IOException e) {
            Reporter.reportError(e, "OPKJE cannot save or load data. The application cannot continue.");
            System.exit(1);
        }
        Cfg.loadUserCfg(); // Make sure user cfg overrides the built-in one.
        KV.load();
    }
}
