package com.nosqlrevolution.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.cursor.Cursor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Used to download data from an ElasticSearch cluster
 * 
 * @author cbrown
 */
public class Export extends PoolRunner {
    private final static String newLine = System.lineSeparator();
    private final static ObjectMapper mapper = new ObjectMapper();
    private final int THREADS = 1;
    private final int BLOCK_SIZE = 1000000;
    private BufferedWriter writer;
    
    @Override
    public void run(String[] args) throws IOException, Exception {        
        ExportOptions options = new ExportOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
            if (options.getIndex() == null) {
                throw new CmdLineException("Index must be specified.");
            }
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java -jar elasticstore.jar export [options...]");
            parser.printUsage(System.err);
            return;
        }        
        
        System.out.println("hostname=" + options.getHostname() + " outfilename=" + options.getOutfilename() + " clustername=" + options.getClustername() + 
                " index=" + options.getIndex() + " type=" + options.getType());
        File outFile = new File(options.getOutfilename());
        if (outFile.exists()) {
            System.out.println("File exists, exiting.");
            return;
        }
        
        // Connect to ElasticSearch
        ElasticStore store = new ElasticStore().asTransport().withClusterName(options.getClustername()).withUniCast(options.getHostname()).execute();
        Index data = store.getIndex(ExportModel.class, options.getIndex(), options.getType());
        Cursor<ExportModel> cursor = data.findAll();
        ElasticsearchBlocker blocker = new ElasticsearchBlocker(store.getIndex(String.class, options.getIndex(), options.getType()), BLOCK_SIZE);

        outFile.createNewFile();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(outFile);
            writer = new BufferedWriter(fileWriter);
            super.run(blocker, THREADS);
        } catch (IOException ie) {
            
        } finally {
            writer.flush();
            writer.close();
            fileWriter.close();
        }
    }

    @Override
    protected Callable getNextCallable(List<String> nextBlock) throws Exception {
        return new JsonExporter(writer, nextBlock);
    }
}