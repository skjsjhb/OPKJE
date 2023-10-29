package skjsjhb.rhytick.opfw.je.launcher;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * An error reporter which handles submitted errors / exceptions and optionally ask user for further action.
 */
public final class Reporter {

    /**
     * Report the error as a UI dialog.
     *
     * @param what    The error object.
     * @param explain A brief message to explain the consequences of this error.
     */
    public static void reportError(Throwable what, String explain) {
        var option = JOptionPane.showConfirmDialog(null,
                String.format("""
                        Error detected:

                        %s
                                                
                        Possible consequences:
                                                
                        %s

                        This error is displayed, as its severity is worth reporting.
                        Consider reporting it to Team Rhytick for future fixes.
                        We apologize for the incovenience.
                                                
                        Continue and view detailed stacktrace? (Click 'Yes')
                        """, what.toString(), explain),
                "OPKJE Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            StringWriter stack = new StringWriter();
            what.printStackTrace(new PrintWriter(stack));
            String stackFmt = stack.toString().replaceAll("\t", "    ");
            JOptionPane.showMessageDialog(null, String.format("""
                    The stacktrace of the last error:
                                        
                    %s
                                        
                    Note that the stacktrace is usually not sufficient for resolving the issue.
                    Please refer to output logs for further information.
                    """, stackFmt), "OPKJE Error Stacktrace", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * The async overload of {@link #reportError(Throwable, String)}
     */
    public static void reportErrorAsync(Throwable what, String explain) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                reportErrorAsync(what, explain);
                return null;
            }
        }.execute();
    }
}
