package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Used to download data from an ElasticSearch cluster
 * 
 * @author cbrown
 */
public class Export extends AbstractPoolRunner {
    private final int THREADS = 1;
    private BufferedOutputStream oStream;
    
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
            System.err.println("java -jar elasticstore-4.0-cli.jar export [options...]");
            parser.printUsage(System.err);
            throw e;
        }        
        
        File outFile = new File(options.getOutfilename());
        if (outFile.exists()) {
            throw new IOException("Output file already exists: " + outFile);
        }
        
        // Connect to ElasticSearch
        ElasticStore store = ElasticStoreUtil.createElasticStore(
                options.getHostname(), options.getClustername(), options.getIndex(), options.isNode(), options.isElastic());
        try (ElasticStore sourceStore = store) {
            ElasticStoreUtil.configureAuthentication(store, options.getShieldUsername(), options.getShieldPassword(),
                    options.getApiKey(), options.getCaCertificate(), options.getElasticClusterId(), options.getElasticRegion());
            store.execute();

            AbstractBlocker blocker;
            if (! options.isModelMode()) {
                blocker = new JsonBlocker(store, null, options.getIndex(), options.getBlockSize(), options.getLimit(), options.getSample());
            } else {
                blocker = new ModelBlocker(store, null, options.getIndex(), options.getBlockSize(), options.getLimit(), options.getSample());
            }

            try (AbstractBlocker source = blocker;
                    OutputStream fileStream = Files.newOutputStream(outFile.toPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                java.io.OutputStream output = options.isGzip()
                        ? new GZIPOutputStream(fileStream) : fileStream;
                try (BufferedOutputStream buffered = new BufferedOutputStream(output)) {
                    oStream = buffered;
                    super.run(blocker, THREADS);
                }
            }
        }
    }

    @Override
    protected Callable<CountsAndBytes> getNextCallable(List<String> nextBlock) throws Exception {
        return new JsonExporter(oStream, nextBlock);
    }
}