package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
import java.io.File;
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
public class Import extends AbstractPoolRunner {
    private ElasticStore store;
    private ImportOptions options;
    
    @Override
    public void run(String[] args) throws IOException, Exception {        
        options = new ImportOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
            if (options.getIndex() == null) {
                throw new CmdLineException("Index must be specified.");
            }
            if (options.getType() == null) {
                throw new CmdLineException("Type must be specified.");
            }
            if (options.getInfilename() == null) {
                throw new CmdLineException("Input file must be specified.");
            }
        } catch(CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar elasticstore.jar import [options...]");
            parser.printUsage(System.err);
            return;
        }        

        System.out.println("hostname=" + options.getHostname() + " inFilename=" + options.getInfilename() + " clustername=" + options.getClustername() + 
                " index=" + options.getIndex() + " type=" + options.getType() + " threads=" + options.getThreads()+ " blockSize=" + options.getBlockSize());
        File inFile = new File(options.getInfilename());
        if (! inFile.exists()) {
            System.out.println("File does not exist, exiting.");
            return;
        }
        
        // Connect to ElasticSearch
        store = ElasticStoreUtil.createElasticStore(
                options.getHostname(), options.getClustername(), options.getIndex(), options.getType(), options.isNode());
        FileBlocker blocker = new FileBlocker(options.getInfilename(), options.getBlockSize(), options.getLimit(), options.getSample());
        
        super.run(blocker, options.getThreads());
    }
    
    @Override
    protected Callable getNextCallable(List<String> nextBlock) throws Exception {
        return new JsonImporter(store, options.getIndex(), options.getType(), nextBlock, options.getIdField());
    }
}