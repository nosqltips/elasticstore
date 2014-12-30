package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
        ElasticStore store = ElasticStoreUtil.createElasticStore(
                options.getHostname(), options.getClustername(), options.getIndex(), options.getType(), options.isNode());
        AbstractBlocker blocker;
        if (! options.isModelMode()) {
            blocker = new JsonBlocker(store, options.getIndex(), options.getType(), options.getBlockSize(), options.getLimit(), options.getSample());
        } else {
            blocker = new ModelBlocker(store, options.getIndex(), options.getType(), options.getBlockSize(), options.getLimit(), options.getSample());
        }

        outFile.createNewFile();
        FileOutputStream fStream = null;
        GZIPOutputStream gStream = null;
        try {
            fStream = new FileOutputStream(outFile);
            if (options.isGzip()) {
                gStream = new GZIPOutputStream(fStream);
                oStream = new BufferedOutputStream(gStream);
            } else {
                oStream = new BufferedOutputStream(fStream);
            }
            super.run(blocker, THREADS);
        } catch (IOException ie) {
            ie.printStackTrace();
        } finally {
            if (oStream != null) {
                oStream.flush();
                oStream.close();
            }
            if (gStream != null) {
                gStream.close();
            }
            if (fStream != null) {
                fStream.close();
            }
        }
    }

    @Override
    protected Callable getNextCallable(List<String> nextBlock) throws Exception {
        return new JsonExporter(oStream, nextBlock);
    }
}