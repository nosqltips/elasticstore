package com.nosqlrevolution.util;

import com.nosqlrevolution.enums.Wildcard;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParseUtilTest {
    @Test public void prefixOptimizationOnlyAppliesToASingleUnescapedTrailingStar() {
        assertEquals(Wildcard.NONE, ParseUtil.isWildcard("plain"));
        assertEquals(Wildcard.PREFIX, ParseUtil.isWildcard("ab*"));
        for (String pattern : new String[] {"ab*cd*", "a?b*", "*x*", "ab\\*"}) {
            assertEquals(pattern, Wildcard.FULL, ParseUtil.isWildcard(pattern));
        }
    }
    @Test public void cleaningTermsDoesNotDependOnDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("index", ParseUtil.cleanTerm("INDEX"));
        } finally { Locale.setDefault(previous); }
    }
}
