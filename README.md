# ElasticStore

The main idea of ElasticStore is to create a new NoSQL database that is based on the ElasticSearch core. ElasticSearch is an incredible search engine with a very flexible search-oriented API.
While the ElasticSearch API is very easy to use, it is a search API and as such, is somewhat foreign to someone coming from a traditional data background.
The ElasticStore API presents a more familiar API while simplifying many of the search constructs to make them more accessible to the average developer.

## Why is ElasticSearch a great NoSQL engine?
* Document oriented - schema-less, automatic, or defined schema
* Very advanced search capabilities, no need to shunt data somewhere else for search
* Very advanced indexing features - based on lucene and associated analyzers, filters, etc
* Built-in scale - one or many nodes with auto-discovery(unicast, multicast), no changes to client or configuration
* Built in cloud support - AWS auto-discovery
* Durablility - shards and replicas defined per-index, shard placement policies
* Advanced query routing - hit single shard for very fast performance

## Why is ElasticStore a great NoSQL database?
* Built on top of all of that ElasticSearch/Lucene goodness!
* Simplified, document-oriented API
* Provides strong object typing or json serialization
* Provides many annotations for object mapping, object id and routing support
* Provides Collection and Iterator support for easy paging of results
* Easy bulk-loading of strongly typed of json objects
* SQLish QueryBuilder for advanced queries and those coming from a SQL background
* Lots of units tests, code examples and javadocs

## So... What does it look like?
```java
    ElasticStore elasticStore = new ElasticStore().asLocal().execute();
    Index<Person> index = ElasticStore.getIndex(Person.class, "index", "type");

    // Create
    Person p = new Person()
        .setId("1")
        .setName("Homer Simpson")
        .setUsername("hsimpson");
    index.write(p);
            
    // Read
    Person p2 = index.findById("1");

    // Update
    p.setName("Marge Simpson");
    p.setUsername("msimpson");
    index.write(p);
            
    // Delete
    index.remove(p);
```

```java
public class Person {
    @DocumentId
    private String id;
    private String name;
    private String username;

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public Person setId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public Person setName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty
    public String getUsername() {
        return username;
    }

    @JsonProperty
    public Person setUsername(String username) {
        this.username = username;
        return this;
    }
}
```

## Stay Tuned for more ...
