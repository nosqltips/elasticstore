package com.nosqlrevolution;

import com.nosqlrevolution.apps.Export;
import com.nosqlrevolution.apps.Import;
import java.io.IOException;

/**
 *
 * @author cbrown
 */
public class App {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, Exception {
        if (args == null || args.length == 0) {
            System.out.println("No parameters, exiting.");
            System.out.println("export");
            System.out.println("import");
            return;
        }
        
        if (args[0].toLowerCase().equals("export")) {
            Export.run(args);
        } else if (args[0].toLowerCase().equals("import")) {
            Import.run(args);
        } else {
            System.out.println("1st parameter must be either export or import");
        }
    }
}
