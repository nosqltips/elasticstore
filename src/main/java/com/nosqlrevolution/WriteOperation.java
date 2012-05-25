package com.nosqlrevolution;

import org.elasticsearch.action.WriteConsistencyLevel;

/**
 * This class provides additional details that can be added to writer operations.
 * 
 * @author cbrown
 */
public class WriteOperation {
    private boolean refresh = false;
    private WriteConsistencyLevel consistencyLevel = WriteConsistencyLevel.DEFAULT;

    /**
     * Causes the indexed to be refreshed so that documents will be immediately visible in search results.
     * 
     * @return 
     */
    public boolean getRefresh() {
        return refresh;
    }

    /**
     * Causes the indexed to be refreshed so that documents will be immediately visible in search results.
     * 
     * @param refresh 
     */
    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    /**
     * Sets the consistency level for this write operation.
     *   ALL - All replicas need to commit the write before operation succeeds
     *   DEFAULT - Same as Quorum
     *   ONE - Only a single replica need to commit the write before operation succeeds
     *   QUORUM - A majority of replicas need to commit the write before the operation succeeds
     * 
     * @return 
     */
    public WriteConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Sets the consistency level for this write operation.
     *   ALL - All replicas need to commit the write before operation succeeds
     *   DEFAULT - Same as Quorum
     *   ONE - Only a single replica need to commit the write before operation succeeds
     *   QUORUM - A majority of replicas need to commit the write before the operation succeeds
     * 
     * @param consistencyLevel 
     */
    public void setConsistencyLevel(WriteConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }
    
}
