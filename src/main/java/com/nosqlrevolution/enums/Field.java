package com.nosqlrevolution.enums;

/**
 *
 * @author cbrown
 */
public enum Field {
    TYPE("type"),
    INDEX_NAME("index_name"),
    STORE("store"),
    INDEX("index"),
    TERM_VECTOR("term_vector"),
    BOOST("boost"),
    NULL_VALUE("null_value"),
    OMIT_NORMS("omit_norms"),
    OMIT_TERMS("omit_term_freq_and_positions"),
    ANALYZER("analyzer"),
    INDEX_ANALYZER("index_analyzer"),
    SEARCH_ANALYZER("search_analyzer"),
    INCLUDE_IN_ALL("include_in_all"),
    FORMAT("format"),
    PRECISION_STEP("precision_step"),
    LAT_LON("lat_lon"),
    GEOHASH("geohash"),
    GEOHASH_PRECISION("geohash_precision"),
    DYNAMIC("dynamic"),
    ENABLED("enabled"),
    PATH("path");

    private String name;
    Field(String name) {
        this.name = name;
    }
    public String getName() { return name; }
}
