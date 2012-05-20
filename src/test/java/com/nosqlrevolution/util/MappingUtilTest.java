package com.nosqlrevolution.util;

import com.nosqlrevolution.model.Person;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class MappingUtilTest {
    private MappingUtil<Person> instance = new MappingUtil<Person>();
    private Person person = new Person().setId("1234").setName("Homer Simpson").setUsername("hsimpson");
    private String json = "{\"id\":\"1234\",\"name\":\"Homer Simpson\",\"username\":\"hsimpson\"}";

    /**
     * Test of get method, of class MappingUtil.
     */
    @Test
    public void testGet() {
        Person result = instance.get(new Person(), json);
        assertNotNull(result);
        assertEquals(person.getId(), result.getId());
    }

    /**
     * Test of asString method, of class MappingUtil.
     */
    @Test
    public void testAsString() {
        String result = instance.asString(person);
        System.out.println("person=" + result);
        assertNotNull(result);
        assertEquals(json, result);
    }

    /**
     * Test of asClass method, of class MappingUtil.
     */
    @Test
    public void testAsClass() {
        Person result = (Person)instance.asClass(json, Person.class);
        System.out.println("result");
        assertEquals(person.getId(), result.getId());
    }
}
