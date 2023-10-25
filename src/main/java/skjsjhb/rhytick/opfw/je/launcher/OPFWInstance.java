package skjsjhb.rhytick.opfw.je.launcher;

import skjsjhb.rhytick.opfw.je.dce.JavaScriptEnv;

/**
 * Main OPFW reference instance holding services and systems.
 * <br/>
 * OPFW instance does not launch itself, rather, the main entry {@link OPFWLauncher} will be responsible
 * for the bootstrap progress.
 */
public class OPFWInstance {
    public JavaScriptEnv sysScriptVM = new JavaScriptEnv(true);

}
