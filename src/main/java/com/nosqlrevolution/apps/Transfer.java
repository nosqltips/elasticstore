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
            if (options.getIdField() != null) {
                throw new CmdLineException("Transfer preserves Elasticsearch IDs; use raw export/import -id to remap IDs.");
            }
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

        // Connect to ElasticSearch
        destStore = ElasticStoreUtil.createElasticStore(
                options.getDestHostname(), options.getDestClustername(), options.getDestIndex(), options.isDestNode(), options.isDestElastic());
        try (ElasticStore destination = destStore) {
            ElasticStoreUtil.configureAuthentication(destStore, options.getDestUsername(), options.getDestPassword(),
                    options.getDestApiKey(), options.getDestCaCertificate(), options.getDestClusterId(), options.getDestRegion());
            destStore.execute();

            ElasticStore sourceStore = ElasticStoreUtil.createElasticStore(
                    options.getSourceHostname(), options.getSourceClustername(), options.getSourceIndex(), options.isSourceNode(), options.isSourceElastic());
            try (ElasticStore source = sourceStore) {
                ElasticStoreUtil.configureAuthentication(sourceStore, options.getSourceUsername(), options.getSourcePassword(),
                        options.getSourceApiKey(), options.getSourceCaCertificate(), options.getSourceClusterId(), options.getSourceRegion());
                sourceStore.execute();

                ModelBlocker blocker = new ModelBlocker(sourceStore, null, options.getSourceIndex(), options.getBlockSize(), options.getLimit(), options.getSample());

                super.run(blocker, options.getThreads());
            }
        }
    }
    
    @Override
    protected Callable<CountsAndBytes> getNextCallable(List<String> nextBlock) throws Exception {
        return new JsonImporter(destStore, options.getDestIndex(), nextBlock, null, true);
    }
}