package com.nosqlrevolution.query;

import org.junit.Test;
import static com.nosqlrevolution.query.Condition.*;
import static com.nosqlrevolution.query.Query.*;

/**
 *
 * @author cbrown
 */
public class QueryTest {

    @Test
    // Just testing to see how the structure looks.
    public void testSomething() {
        select()
            .where(
                field("something").equal("one"), 
                and(
                    field("something").gt("some"),
                    field("something").lt("another")
                )
            );
    }
}
