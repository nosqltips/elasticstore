package com.nosqlrevolution;

import com.nosqlrevolution.apps.Export;
import com.nosqlrevolution.apps.Import;
import com.nosqlrevolution.apps.Transfer;
import java.io.IOException;
import java.util.Arrays;

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
            System.out.println("transfer");
            return;
        }
        
        String command = args[0];
        String[] options = Arrays.copyOfRange(args, 1, args.length);
        
        if (command.equals("export")) {
            new Export().run(options);
        } else if (command.equals("import")) {
            new Import().run(options);
        } else if (command.equals("transfer")) {
            new Transfer().run(options);
        } else {
            System.out.println("1st parameter must be either export or import");
        }
    }
}
