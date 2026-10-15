package com.nosqlrevolution.util;

import com.nosqlrevolution.enums.Wildcard;
import java.util.List;

/**
 *
 * @author cbrown
 */
public class ParseUtil {
    
    /**
     * Clean some of the bad stuff out of our term.
     * 
     * TODO: There may be a better/faster way to do this with regex
     * 
     * @param term
     * @return 
     */
    public static String cleanTerm(String term) {
        if (term == null) { return term; }
        
        return term
                .trim()
                .toLowerCase(java.util.Locale.ROOT)
                .replace(",", "")
                .replace("\\\"", "")
                .replace("'", "")
                .replace("(", "")
                .replace(")", "");
    }

    /**
     * Check to see if this term has a wildcard character in it- * or ?
     * @param term
     * @return 
     */
    public static Wildcard isWildcard(String term) {
        java.util.Objects.requireNonNull(term, "Search term");
        int star = term.indexOf('*');
        if (star < 0 && term.indexOf('?') < 0) return Wildcard.NONE;
        if (term.indexOf('\\') < 0 && term.indexOf('?') < 0 && star == term.length() - 1) return Wildcard.PREFIX;
        return Wildcard.FULL;
    }


    public static String combineArrayValues(List values, String combinator) {
        if (values == null) {
            return null;
        }
        
        if (values.size() == 1) {
            return values.get(0).toString();
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(values.get(0).toString());
        for (int i=1; i<values.size(); i++) {
            sb.append(combinator).append(values.get(i).toString());
        }
        return sb.toString();
    }    
}
