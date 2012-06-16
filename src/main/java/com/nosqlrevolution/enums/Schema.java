package com.nosqlrevolution.enums;

/**
 *
 * @author cbrown
 */
public class Schema {
    public enum STORE { YES, NO, DEFAULT }
    public enum INDEX { ANALYZED, NOT_ANALYZED, NO, DEFAULT }    
    public enum INCLUDE_IN_ALL { TRUE, FALSE, DEFAULT }
    public enum OMIT_NORMS { TRUE, FALSE, DEFAULT }
    public enum OMIT_TERM_FREQ { TRUE, FALSE, DEFAULT }
    public enum VECTOR { NO, YES, WITH_OFFSETS, WITH_POSITIONS, WITH_POSITIONS_OFFSETS, DEFAULT }
    public enum DYNAMIC { TRUE, FALSE, STRICT }
    public enum ENABLED { TRUE, FALSE, DEFAULT }
    public enum LATLON { TRUE, FALSE, DEFAULT }
    public enum GEOHASH { TRUE, FALSE, DEFAULT }
    public enum NUMERIC_TYPE { FLOAT, DOUBLE, INTEGER, LONG, SHORT, BYTE }
}
