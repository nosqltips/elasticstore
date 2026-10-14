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

## Import, export and transfer pipeline

The reader puts complete blocks into a bounded queue. With one worker (including
exports), the calling thread processes blocks while the reader fetches ahead. With
multiple workers, completed tasks receive new blocks in completion order. The queue
holds at most `max(2, workers)` blocks, plus the block the reader is building and the
blocks being processed.

Completion and failure signals do not wait for free queue space. The final partial
block is processed, and a reader or worker failure cancels outstanding work. The
runner closes its reader after stopping the executors; the CLI owns the HTTP clients
and export stream. Callers using cursors directly should close them with
try-with-resources to release their Elasticsearch point-in-time context.

Document cursors return typed `_source` values. Hit cursors include Elasticsearch
IDs and metadata. Normal JSON exports contain one UTF-8 document per line. Model
exports contain an `id` and a `json` string holding the original source. Concurrent
writers keep each document and its newline together. Bulk imports send JSON objects,
including blocks containing a single document; malformed JSON and rejected bulk
items fail the run instead of being reported as successful.

`-s N` keeps every Nth document (starting with document N); `-l N` limits the number
of selected documents. Elasticsearch paging uses one sequential `search_after`
reader. Parallel sliced reads and throughput measurements against a live cluster
remain separate work.

Run the pipeline regression tests with Maven:

```sh
mvn -Dtest=PipelineTest,PagingFailureTest,CursorPayloadTest test
```

These tests cover cancellation, failure cleanup, partial blocks, sampling, concurrent
JSON output, and real client serialization over a local HTTP fixture. They do not
require Elasticsearch; `CursorPayloadTest` needs permission to bind to loopback.
The older cluster-dependent tests need a separately configured Elasticsearch server.

For opt-in live validation, start an isolated Elasticsearch instance:

```sh
docker run -d --name elasticstore-validation --memory=2g \
  -p 127.0.0.1:19200:9200 \
  -e discovery.type=single-node -e xpack.security.enabled=false \
  -e ES_JAVA_OPTS="-Xms512m -Xmx512m" \
  docker.elastic.co/elasticsearch/elasticsearch:8.2.2
# Wait until curl http://127.0.0.1:19200/ responds, then run:
mvn -Dtest=PipelineTest,PagingFailureTest,CursorPayloadTest,LivePipelineTest \
  -Delasticstore.test.url=http://127.0.0.1:19200 test
docker rm -f elasticstore-validation
```

`LivePipelineTest` skips unless the URL property is supplied. It creates uniquely
named test indices and deletes them afterward. The test verifies all 1,021 documents
through import, export/reimport and transfer, including multiple search pages and a
one-document final block, and checks failure cleanup after a real mapping rejection.
It injects an HTTP client into the store for the isolated Docker test; the CLI's
existing HTTPS connection and credential setup are unchanged.
