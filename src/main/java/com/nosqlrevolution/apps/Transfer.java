package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
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
public class Transfer extends AbstractPoolRunner {
    private ElasticStore destStore;
    private TransferOptions options;
    
    @Override
    public void run(String[] args) throws IOException, Exception {        
        options = new TransferOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
            if (options.getSourceIndex() == null) {
                throw new CmdLineException("Source index must be specified.");
            }
            if (options.getDestIndex()== null) {
                throw new CmdLineException("Destination index must be specified.");
            }
            if (options.getDestType()== null) {
                throw new CmdLineException("Destination type must be specified.");
            }
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java -jar elasticstore.jar transfer [options...]");
            parser.printUsage(System.err);
            return;
        }        

        System.out.println("source hostname=" + options.getSourceHostname() + " source clustername=" + options.getSourceClustername() + 
                " source index=" + options.getSourceIndex() + " source type=" + options.getSourceType() + 
                " threads=" + options.getThreads()+ " blockSize=" + options.getBlockSize());
        System.out.println("destination hostname=" + options.getDestHostname() + " destination clustername=" + options.getDestClustername() +  
                " destination index=" + options.getDestIndex() + " destination type=" + options.getDestType());

        // Connect to ElasticSearch
        destStore = ElasticStoreUtil.createElasticStore(
                options.getDestHostname(), options.getDestClustername(), options.getDestIndex(), options.getDestType(), options.isDestNode());
        ElasticStore sourceStore = ElasticStoreUtil.createElasticStore(
                options.getSourceHostname(), options.getSourceClustername(), options.getSourceIndex(), options.getSourceType(), options.isSourceNode());
        JsonBlocker blocker = new JsonBlocker(sourceStore, options.getSourceIndex(), options.getSourceType(), options.getBlockSize(), options.getLimit(), options.getSample());

        super.run(blocker, options.getThreads());
    }
    
    @Override
    protected Callable getNextCallable(List<String> nextBlock) throws Exception {
        return new JsonImporter(destStore, options.getDestIndex(), options.getDestType(), nextBlock, options.getIdField());
    }
}