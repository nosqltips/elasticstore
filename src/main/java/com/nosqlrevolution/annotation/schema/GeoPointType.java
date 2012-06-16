package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;
import com.nosqlrevolution.enums.Schema.GEOHASH;
import com.nosqlrevolution.enums.Schema.LATLON;

/**
 * The geo_point mapping will index a single field with the format of lat,lon. The lat_lon option can be set to also index the .lat and .lon as numeric fields, and geohash can be set to true to also index .geohash value.
 * A good practice is to enable indexing lat_lon as well, since both the geo distance and bounding box filters can either be executed using in memory checks, or using the indexed lat lon values, and it really depends on the data set which one performs better. Note though, that indexed lat lon only make sense when there is a single geo point value for the field, and not multi values.
 * 
 * The above mapping defines a geo_point, which accepts different formats. The following formats are supported:
 * 
 * <B>Lat Lon as Properties</B>
 * {
 *     "pin" : {
 *         "location" : {
 *             "lat" : 41.12,
 *             "lon" : -71.34
 *         }
 *     }
 * }
 * 
 * <B>Lat Lon as String</B>
 * Format in lat,lon.
 * 
 * {
 *     "pin" : {
 *         "location" : "41.12,-71.34"
 *     }
 * }
 * 
 * <B>Geohash</B>
 * 
 * {
 *     "pin" : {
 *         "location" : "drm3btev3e86"
 *     }
 * }
 * 
 * <B>Lat Lon as Array</B>
 * 
 * Format in [lon, lat], note, the order of lon/lat here in order to conform with GeoJSON.
 * 
 * {
 *     "pin" : {
 *         "location" : [-71.34, 41.12]
 *     }
 * }
 * 
 * 
 * @param <B>lat_lon</B>            Set to true to also index the .lat and .lon as fields. Defaults to <B>false</B>.
 * @param <B>geohash</B>            Set to true to also index the .geohash as a field. Defaults to <B>false</B>.
 * @param <B>geohash_precision</B>  Sets the geohash precision, defaults to <B>12</B>.    
 * 
 * @author cbrown
 */
// TODO: add some field/object references to docs.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface GeoPointType {
    /**
     * Set to true to also index the .lat and .lon as fields. Defaults to <B>false</B>.
     */
    LATLON lat_lon() default LATLON.DEFAULT;

    /**
     * Set to true to also index the .geohash as a field. Defaults to <B>false</B>.
     */
    GEOHASH geohash() default GEOHASH.DEFAULT;

    /**
     * Sets the geohash precision, defaults to <B>12</B>.
     */
    int geohash_precision() default 12;
}
