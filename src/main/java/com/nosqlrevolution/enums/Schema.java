package com.nosqlrevolution.enums;

/**
 *
 * @author cbrown
 */
public class Schema {
    /**
     * Default is NO
     */
    public enum STORE { YES, NO, DEFAULT }
    /**
     * Default is ANALYZED
     */
    public enum INDEX { ANALYZED, NOT_ANALYZED, NO, DEFAULT }    
    /**
     * Default is TRUE
     */
    public enum INCLUDE_IN_ALL { TRUE, FALSE, DEFAULT }
    /**
     * Default is FALSE
     */
    public enum OMIT_NORMS { TRUE, FALSE, DEFAULT }
    /**
     * Default is FALSE
     */
    public enum OMIT_TERM_FREQ { TRUE, FALSE, DEFAULT }
    /**
     * Default is NO
     */
    public enum TERM_VECTOR { NO, YES, WITH_OFFSETS, WITH_POSITIONS, WITH_POSITIONS_OFFSETS, DEFAULT }
    /**
     * Default is TRUE
     */
    public enum DYNAMIC { TRUE, FALSE, STRICT, DEFAULT }
    /**
     * Default is TRUE
     */
    public enum ENABLED { TRUE, FALSE, DEFAULT }
    /**
     * Default is FALSE
     */
    public enum LATLON { TRUE, FALSE, DEFAULT }
    /**
     * Default is FALSE
     */
    public enum GEOHASH { TRUE, FALSE, DEFAULT }
    /**
     * Default is INTEGER
     */
    public enum NUMERIC_TYPE { FLOAT, DOUBLE, INTEGER, LONG, SHORT, BYTE }
}
