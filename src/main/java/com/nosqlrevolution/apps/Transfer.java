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
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java -jar elasticstore.jar transfer [options...]");
            parser.printUsage(System.err);
            return;
        }        

        System.out.println("source hostname=" + options.getSourceHostname() + " source clustername=" + options.getSourceClustername() + 
                " source index=" + options.getSourceIndex() + 
                " threads=" + options.getThreads()+ " blockSize=" + options.getBlockSize());
        System.out.println("destination hostname=" + options.getDestHostname() + " destination clustername=" + options.getDestClustername() +  
                " destination index=" + options.getDestIndex());
        System.out.println("source clusterId=" + options.getSourceClusterId() + " source region=" + options.getSourceRegion()+ " source username=" + options.getSourceUsername()+ " source password=" + options.getSourcePassword());
        System.out.println("destination clusterId=" + options.getDestClusterId() + " destination region=" + options.getDestRegion()+ " destination username=" + options.getDestUsername()+ " destination password=" + options.getDestPassword());

        // Connect to ElasticSearch
        destStore = ElasticStoreUtil.createElasticStore(
                options.getDestHostname(), options.getDestClustername(), options.getDestIndex(), options.isDestNode(), options.isDestElastic());
        try (ElasticStore destination = destStore) {
            if (options.getDestClusterId() != null) {
                destStore.asElastic().withClusterId(options.getDestClusterId()).withRegion(options.getDestRegion()).withUsername(options.getDestUsername()).withPassword(options.getDestPassword());
            }
            destStore.execute();

            ElasticStore sourceStore = ElasticStoreUtil.createElasticStore(
                    options.getSourceHostname(), options.getSourceClustername(), options.getSourceIndex(), options.isSourceNode(), options.isSourceElastic());
            try (ElasticStore source = sourceStore) {
                if (options.getSourceClusterId() != null) {
                    sourceStore.asElastic().withClusterId(options.getSourceClusterId()).withRegion(options.getSourceRegion()).withUsername(options.getSourceUsername()).withPassword(options.getSourcePassword());
                }
                sourceStore.execute();

                JsonBlocker blocker = new JsonBlocker(sourceStore, null, options.getSourceIndex(), options.getBlockSize(), options.getLimit(), options.getSample());

                super.run(blocker, options.getThreads());
            }
        }
    }
    
    @Override
    protected Callable<CountsAndBytes> getNextCallable(List<String> nextBlock) throws Exception {
        return new JsonImporter(destStore, options.getDestIndex(), nextBlock, options.getIdField());
    }
}