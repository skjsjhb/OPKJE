package skjsjhb.rhytick.opfw.je.launcher;

import skjsjhb.rhytick.opfw.je.dce.Codeload;
import skjsjhb.rhytick.opfw.je.dce.Emulation;
import skjsjhb.rhytick.opfw.je.dce.Modular;
import skjsjhb.rhytick.opfw.je.dce.WorkerFactory;
import skjsjhb.rhytick.opfw.je.finder.Finder;
import skjsjhb.rhytick.opfw.je.finder.KV;

import javax.swing.*;
import java.io.IOException;

/**
 * Main entry class for the OPFW (OPKJE) support.
 * This launcher starts all services, call the main script entry and maintain all necessary envs.
 */
public final class Launcher {
    public static void main(String[] args) throws Exception {
        // Set UI LAF for message displaying
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException e) {
            System.out.println("Could not initialize UI look and feel: " + e);
        }
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Reporter.reportError(e, "The application cannot continue due to this failure.");
            System.exit(1);
        });
        System.out.println("Preparing for launch...");
        prepareLaunch();
        System.out.println("This is OPKJE, JVM edition of the OPK compatibility layer.");
        System.out.println("OPKJE is part of the OPFW Series.");
        Emulation me = new Emulation();
        me.prepareRun();
        String mainEntry = Cfg.getValue("emulation.entry", "/opt/main.js");
        me.start(Codeload.readScriptSource(mainEntry));
        prepareExit();
    }

    private static void prepareExit() {
        System.out.println("Stopping!");
        WorkerFactory.stopAll();
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
        Modular.autoRegister();
    }
}
