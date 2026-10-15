package com.nosqlrevolution;

import com.nosqlrevolution.apps.*;
import java.util.Arrays;
import org.kohsuke.args4j.CmdLineParser;

/** Command-line entry point for document import, export and transfer. */
public class App {
    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0 || "--help".equals(args[0])) {
            System.out.println("Usage: java -jar elasticstore-4.0-cli.jar <import|export|transfer> [options]");
            System.out.println("Use <command> --help to list its options.");
            return;
        }
        String command = args[0];
        if (args.length == 2 && "--help".equals(args[1])) {
            Object options;
            switch (command) {
                case "export": options = new ExportOptions(); break;
                case "import": options = new ImportOptions(); break;
                case "transfer": options = new TransferOptions(); break;
                default: throw new IllegalArgumentException("Unknown command: " + command);
            }
            new CmdLineParser(options).printUsage(System.out);
            return;
        }
        String[] options = Arrays.copyOfRange(args, 1, args.length);
        switch (command) {
            case "export": new Export().run(options); break;
            case "import": new Import().run(options); break;
            case "transfer": new Transfer().run(options); break;
            default: throw new IllegalArgumentException("Unknown command: " + command);
        }
    }
}
