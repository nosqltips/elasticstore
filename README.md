# ElasticStore

## Requirements and compatibility

- JDK 11 or newer to build and run; compilation uses `--release 11`.
- Maven 3.6.3 or newer.
- Elasticsearch 8.19 or newer. The tested server and Java client are 8.19.23.
  Server 8.18 and older, including the previous 8.2.2 baseline, are rejected during
  startup with a version message. Elasticsearch 9.x is not yet validated.
- CI runs the default and full Elasticsearch-backed suites on Java 11, 17 and 21.

### Earlier Java client dependencies

The default dependency remains `elasticsearch-java:8.19.23`. Applications can use
older 8.x Java client dependencies; the compatibility matrix covers 8.2.2, 8.14.3
and 8.15.5 against the same Elasticsearch 8.19.23 server. CI runs the full suite and
packaged CLI with each older dependency while keeping production classes compiled
against 8.19.23, to catch runtime linkage errors. Versions outside that matrix,
including 8.0 and 8.1, are not validated.

To build a CLI with an older client:

```sh
mvn clean verify -Delasticsearch.version=8.2.2
```

Library consumers should explicitly select their `co.elastic.clients:elasticsearch-java`
dependency in Maven so dependency resolution uses the intended version. Keep its
matching transitive REST client and JSON-P provider dependencies together. This
supports older **Java clients**; it does not lower the **server** minimum. The
8.19 client can require response fields absent from earlier servers. Elastic's
[compatibility policy](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/8.19/introduction.html)
guarantees older clients against equal or newer server minor versions.

Jackson 2 components are aligned through BOM 2.22.3. The alternative Jackson 3
mapper dependencies are excluded to preserve the Java 11 runtime baseline; this
library explicitly uses Jackson 2. Unused Guava, OpenCSV and SLF4J implementation
libraries have been removed. args4j is updated to 2.37, and build plugin versions
are pinned in the POM instead of depending on the local Maven installation.


ElasticStore is a Java library and CLI for Elasticsearch. It provides typed and JSON
CRUD operations, annotation-based mappings, a query DSL, paged cursors, and bulk
import, export and transfer. Connections use explicit HTTP(S) endpoints.

## Library example

```java
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.annotation.DocumentId;

// Model class (public fields are serialized by Jackson).
public class Person {
    @DocumentId public String id;
    public String name;
}
```

```java
try (ElasticStore store = new ElasticStore()
        .withEndpoints("http://localhost:9200").execute()) {
    Index<Person> people = store.getIndex(Person.class, "people");
    Person person = new Person();
    person.id = "1";
    person.name = "Homer Simpson";
    people.write(person);
    Person loaded = people.findById("1");
    person.name = "Marge Simpson";
    people.write(person);
    people.remove(person);
}
```

Use `createIndex(Person.class, "people")` when creating an index with generated
annotation mappings. Configure authentication as described below before `execute()`.

## Build and run the CLI

```sh
mvn verify
java -jar target/elasticstore-4.0-cli.jar --help
java -jar target/elasticstore-4.0-cli.jar export --help
```

The build produces `elasticstore-4.0.jar` for library consumers and
`elasticstore-4.0-cli.jar` with runtime dependencies for command-line use. `verify`
runs subprocess tests against the packaged CLI; the Docker verifier also exercises
import, export and transfer against Elasticsearch. Invalid arguments and file or
operation failures exit with a nonzero status. Existing export files are never
overwritten.

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
exports contain an `id`, a `json` string holding the original source, and an optional
`routing` value. Concurrent
writers keep each document and its newline together. Bulk imports send JSON objects,
including blocks containing a single document; malformed JSON and rejected bulk
items fail the run instead of being reported as successful.

Use `export -m` and `import -m` for a metadata-preserving round trip. Run the packaged CLI:

```sh
java -jar target/elasticstore-4.0-cli.jar export -h http://localhost:9200 -i source -f documents.json.gz -m -g
java -jar target/elasticstore-4.0-cli.jar import -h http://localhost:9200 -i restored -f documents.json.gz -m
java -jar target/elasticstore-4.0-cli.jar transfer -sh http://localhost:9200 -dh http://localhost:9200 -si source -di copied
```

Transfers now preserve Elasticsearch IDs and routing automatically, including IDs
absent from `_source` or different from a source `id` field. Model imports use the
envelope ID and routing and restore only the inner JSON as `_source`. Old envelopes
without routing still work. Normal imports treat envelope-shaped JSON as ordinary
documents unless `-m` is supplied. `import -m` rejects `-id`, and transfer rejects
`-id`; use raw export/import with `-id` when intentionally changing identity.

Re-running a model import or transfer updates the same IDs. Existing destination
documents with the same ID/routing are overwritten. Create destination mappings
and settings separately when needed; these operations copy documents, not index
configuration, source versions, or sequence numbers. A malformed record fails its
whole work block before submission, but earlier blocks and successful items in a
rejected bulk request may already have been written.

`-s N` keeps every Nth document (starting with document N); `-l N` limits the number
of selected documents. Elasticsearch paging uses one sequential `search_after`
reader. Parallel sliced reads remain separate work; local throughput measurements are
recorded below.

### Performance measurements and reproduction

The write path reuses a strictly validated Jackson document tree for identity and
serialization. Raw imports parse each document once with the default Jackson
provider, instead of validating, extracting identity, removing metadata and parsing
again for JSON-P. Model imports parse the envelope and inner source once each.
Non-Jackson JSON-P clients retain a serialization/parsing fallback. Whole-block
validation, top-level `_id` removal, nested IDs and decimal precision are preserved.

Measurements on October 15, 2026 used an Apple M4 Pro, Temurin 11.0.26, Docker
29.3.1 and Elasticsearch/client 8.19.23. The disposable server used one shard,
zero replicas, a 512 MiB heap and a 2 GiB container limit. Production baseline:
`fa21f0d`, using the same benchmark harness. Documents are about 890 UTF-8 bytes.

The isolated preparation benchmark calls the real importer and bulk builder, but
replaces HTTP/server work with a sink that serializes each document source and
returned an empty successful bulk response for these historical measurements.
The current harness returns a cached result for every requested document to satisfy
response-integrity checks; the historical figures below have not been remeasured
with that change. The benchmark excludes NDJSON action serialization,
network and Elasticsearch work. With three warmups and five measured samples of
100,000 documents, median throughput rose from **263,061 to 710,312 docs/sec
(2.70×)**. Current-thread allocation fell from **13,946 to 5,780 bytes/document
(59% less)**. These are preparation measurements, not end-to-end import rates.

The full file-to-bulk benchmark used 10,000 documents, one warmup and three
measured samples per configuration. Repeated imports update the same IDs; refresh
and count validation run outside timing. Timing includes file counting and reader
setup. Reads use a sequential paged cursor and discard output after serialization.
Median docs/sec:

| Operation | Block/page size | Workers | Baseline | Optimized |
| --- | ---: | ---: | ---: | ---: |
| Import | 250 | 1 | 18,300 | 19,636 |
| Import | 250 | 4 | 49,312 | 50,023 |
| Import | 1,000 | 1 | 26,918 | 29,048 |
| Import | 1,000 | 4 | 58,942 | 54,409 |
| Read | 250 | 1 | 60,358 | 54,331 |
| Read | 1,000 | 1 | 91,324 | 87,935 |

These small, separate-container runs include JIT, filesystem cache and server
variation. Four-worker imports did not consistently improve. The reader code was
unchanged; its variation is another reason not to infer a general throughput gain
from these samples. CPU savings are clear in the isolated workload, while actual
throughput still depends on server, document and request sizes. This workload does
not justify adding reader slicing yet. Raw samples are in
[benchmarks/](benchmarks/); Flight Recorder files stay under ignored `target/`.

Opt-in benchmarks are skipped during normal verification. To measure preparation
without a server (three warmups, five samples):

```sh
mvn test '-Dtest=PipelineBenchmarkTest#measurePreparationAndSerializationWithoutNetwork' \
  -Delasticstore.benchmark=true -Delasticstore.benchmark.documents=100000
```

To measure the live reader/bulk matrix and preparation, using the Docker verifier:

```sh
./scripts/verify-docker.sh -Dtest=PipelineBenchmarkTest \
  -Delasticstore.benchmark=true -DskipITs
```

CSV files go to `target/benchmarks/`. Override sample/document counts with
`elasticstore.benchmark.samples` and `elasticstore.benchmark.documents`; override
output files with `elasticstore.benchmark.output` and
`elasticstore.benchmark.cpu.output`. For a Java Flight Recorder profile, first
create `target/benchmarks/`, then add
`'-DargLine=-XX:StartFlightRecording=filename=target/benchmarks/pipeline.jfr,settings=profile,dumponexit=true'`.
The baseline recording identified repeated parsing in `JsonImporter`, `JsonUtil`
and `QueryService`; the optimized recording contains one strict document parse on
the Jackson import path. JFR startup output can produce a Surefire stream warning;
the recording and test results are still available.

Run the pipeline regression tests with Maven:

```sh
mvn -Dtest=PipelineTest,PagingFailureTest,CursorPayloadTest test
```

These tests cover cancellation, failure cleanup, partial blocks, sampling, concurrent
JSON output, and real client serialization over a local HTTP fixture. They do not
require Elasticsearch; `CursorPayloadTest` needs permission to bind to loopback.
Live CRUD and integration tests use a separately configured Elasticsearch server.
They skip unless `elasticstore.test.url` is supplied, create uniquely named indices,
and delete only those indices afterward.

For repeatable live validation, run:

```sh
./scripts/verify-docker.sh
```

This requires Docker, curl, and Maven 3.6.3+ on `PATH`. It starts Elasticsearch 8.19.23 on
an automatically allocated loopback port, waits for cluster readiness, runs the
entire Maven test suite, and removes its container on success or failure. The
Elasticsearch log is retained at `target/elasticsearch-integration.log` and Maven
reports at `target/surefire-reports/` and `target/failsafe-reports/`.

Docker limits the test container to 2 GiB and the JVM heap is explicitly set to
512 MiB. The old 8.2.2 JVM cgroup workaround is no longer needed.

Use `MVN=/path/to/mvn` if Maven is elsewhere. `ES_IMAGE` selects another image and
`ES_STARTUP_TIMEOUT` changes the 120-second startup deadline. Extra arguments are
passed to Maven, for example `./scripts/verify-docker.sh -o` for an offline Maven
run with cached dependencies. Use separate checkouts for concurrent verification
because Maven reports share the same target directory.

For manual debugging, start an isolated Elasticsearch instance:

```sh
docker run -d --name elasticstore-validation --memory=2g \
  -p 127.0.0.1:19200:9200 \
  -e discovery.type=single-node -e xpack.security.enabled=false \
  -e ES_JAVA_OPTS="-Xms512m -Xmx512m" \
  docker.elastic.co/elasticsearch/elasticsearch:8.19.23
# Wait until curl http://127.0.0.1:19200/ responds, then run:
mvn test -Delasticstore.test.url=http://127.0.0.1:19200
docker rm -f elasticstore-validation
```

`LivePipelineTest` skips unless the URL property is supplied. It creates uniquely
named test indices and deletes them afterward. The test verifies all 1,021 documents
through import, export/reimport and transfer, including multiple search pages and a
one-document final block, and checks failure cleanup after a real mapping rejection.
`LiveCrudTest` also exercises the public connection API and CLI against that URL.

GitHub Actions runs on pushes and pull requests, with Java 11, 17 and 21 jobs for
the default server-free suite and the complete Docker-backed suite. The latter
uses the same `verify-docker.sh` command. Both jobs retain test reports for seven
days; the Docker job also retains the Elasticsearch log. The workflow is in
`.github/workflows/verify.yml` and can also be started manually from Actions.

## Connections and document operations

Supply explicit REST endpoints (port 9200), using HTTP for an unsecured local
cluster or HTTPS for a secured deployment:

```java
try (ElasticStore store = new ElasticStore()
        .withEndpoints("https://localhost:9200")
        .withUsername("elastic").withPassword(password)
        .withCaCertificate(java.nio.file.Paths.get("http_ca.crt"))
        .withConnectTimeout(java.time.Duration.ofSeconds(20))
        .withSocketTimeout(java.time.Duration.ofMinutes(5))
        .execute()) {
    // Access indices through store.getIndex(...).
}
```

Authentication is optional. Use `withApiKey(encodedApiKey)` instead of basic
authentication when needed. TLS certificate and hostname verification remain
enabled; a CA file adds trust for a private certificate. Startup checks the server
version and cluster health, so secured credentials need permission to read cluster health as well as
the relevant index permissions. A store closes clients it creates. An injected
`withClient(client)` is borrowed; `withClient(client, true)` transfers ownership.

CLI import/export accept `-ak` for an encoded API key and `-ca` for a CA file;
transfer uses `-sak`/`-dak` and `-sca`/`-dca`. Host arguments accept explicit URLs,
and basic authentication works without a cloud cluster ID. Bare hosts default to
HTTP port 9200; legacy `-e` selects HTTPS. Credentials are not printed. Embedded
nodes, multicast discovery, and old cloud-ID/region setup now fail with migration
instructions; use a running cluster's REST endpoint.

Single and bulk writes send JSON objects and preserve annotated document IDs.
Generated UUIDs are assigned before serialization and remain stable on updates.
Missing gets return `null`; multi-get preserves input order with `null` slots for
missing documents. Delete-by-ID is idempotent, and query deletion rejects skip or
limit rather than silently ignoring them. Write and read failures propagate as
exceptions; bulk writes can partially succeed before reporting an error.

Writes and delete-by-ID return an `OperationStatus` with ordered `getItems()`
results and success/failure counts. Items expose the index, document ID, result,
HTTP status, version, sequence number, primary term, and error type/reason when
available. Bulk item failures throw `OperationException`; `getStatus()` on that
exception includes both successful and failed items. Inspect those results before
retrying a partially successful request. Transport errors and rejected single
requests retain their original exception behavior and have no completed item list,
except HTTP 409 write conflicts, which expose an `OperationException` item.

### Routing and conditional writes

`DocumentOptions` is immutable and applies to one call, so constraints do not leak
into later operations:

```java
DocumentOptions routed = DocumentOptions.DEFAULT.routing("tenant-42");
OperationStatus.Item saved = index.write(routed, document).getItems().get(0);
DocumentOptions unchanged = routed.ifUnchanged(
    saved.getSequenceNumber(), saved.getPrimaryTerm());
index.write(unchanged, document);
// Reusing unchanged after a successful modification raises a conflict.
Document current = index.findByIdWithRouting(saved.getId(), "tenant-42");
```

Conditional writes and deletes require exactly one document/ID. Pass options to
`removeById(options, id)` for conditional or routed deletion. Routed multi-get is
available as `findAllByIdWithRouting(Document.class, routing, ids...)`. Use the same
routing key for the document throughout its lifetime. Searches still visit all
shards; routing here is for document operations.

Typed writes read routing and external versions from `@Routing` and `@Version`
fields/getters (including inherited members), or conventional `routing`/`_routing`
and `version`/`_version` fields. External versions must be positive and strictly
increase. They are supplied by the application and are not incremented by the
library. Explicit `routing(...)` and `externalVersion(...)` options override model
values. JSON writes use explicit options for routing/version constraints.
`externalVersion(...)` and `ifUnchanged(...)` are mutually exclusive, including
when a model supplies an external version. Typed document deletion reads its
routing metadata; version constraints on deletion must be passed explicitly.

### Reflected metadata contracts

Document IDs, routing and external versions can come from annotated fields or
zero-argument getters, including inherited members. Supported declared types are
`String`, `int`, `long`, `Integer`, `Long`, and the existing `Object` compatibility
form (converted with `toString()`). A genuine `null` remains absent metadata.
Unsupported declared types, blank conversions, getters with parameters or no
return value, inaccessible members and getter/conversion exceptions now fail
explicitly instead of becoming missing metadata. Errors identify the declaring
class/member and retain the original cause. Fatal getter errors propagate.

`@UUIDProvider` requires a mutable instance `String` field. Non-String, static or
final targets are rejected even if populated; older releases silently skipped
invalid targets. Provider target types/modifiers are checked before generation.
A null valid field receives a UUID before serialization, while an existing value
is kept. Deletes read identity without generating a UUID. Replace invalid legacy
provider annotations or use an ordinary explicit ID field.

Explicit routing and external-version options skip reflected metadata lookup for
the values they override. Ordinary JSON serialization can still invoke getters
that are exposed as JSON properties. Deleting a typed document reads identity and
routing, not write-only
version metadata. A metadata validation failure in any document prevents the
whole single/bulk request from being sent. Buffered writes retain their documents
on failure. Earlier completed calls are not rolled back; UUIDs may already have
been assigned to valid objects before a later document fails, and retries reuse
those IDs.

### Supported index APIs

`getIndex(Document.class, name)` returns an `Index<Document>`;
`getIndex(String.class, name)` reads and writes JSON strings; and
`getIndex(JsonData.class, name)` reads and writes `JsonData`. Single reads, cursors,
and multi-get now consistently return that selected type. Code that relied on
string-index cursors returning `JsonData` should request `JsonData.class` instead.

| Operation | Supported behavior |
| --- | --- |
| `write(...)`, `write(list, options)` | Single/bulk replacement writes with ordered results; typed annotations or explicit request constraints |
| `addBulk(...)`, `flushBulk()` | Buffered writes; automatic flush at the configured size; explicit flush for the final partial block |
| `find(...)`, `findById(...)` | One source object, or `null` if no match/document; optional conversion class |
| `findAll(...)`, `findAllScroll(...)` | Closeable PIT cursors with projection, sorting, skip, limit, and page size |
| `findAllScrollHit(...)` | Full hit cursor with Elasticsearch identity and routing metadata |
| `findAllById(...)` | Ordered results with null slots for missing IDs; optional conversion class |
| `findByIdWithRouting(...)`, `findAllByIdWithRouting(...)` | Routed reads with the same missing-document semantics |
| `remove(...)`, `removeById(...)` | Delete by source identity, explicit IDs, or query; query deletion rejects pagination |
| `exists()`, `refresh()`, `count(...)` | Index existence, search visibility refresh, and matching count (independent of pagination) |

Cursors stream once and must be closed; collection mutations and membership
operations are unsupported. Index handles and their bulk buffers are intended for
one caller at a time. A failed bulk flush keeps its buffer; inspect partial results
before retrying. JSON identity is taken only from a top-level ID field; child IDs
never supply the parent document's identity. Null/missing IDs allow server-generated
IDs, while invalid or blank IDs are rejected.

`createIndex(Document.class, name)` creates an index with a generated mapping.
`applyMapping(String)` and `applyMapping(Class<?>)` update an existing index.
The annotation-only `getIndex(Document.class)` overload requires `@Index`;
`@IndexType` is deprecated and no longer required.

The default Maven test suite runs without Elasticsearch. It includes unit tests,
legacy helper compatibility checks, and local HTTP/HTTPS fixtures. A JDK with
`keytool` and permission to bind to loopback are needed for the connection tests:

```sh
mvn test
# Enable all live tests as well, using the Docker instance described above:
mvn test -Delasticstore.test.url=http://127.0.0.1:19200
```

The former embedded-node tests now cover the current REST client lifecycle and
index factories, or run as explicit live CRUD tests. They do not connect implicitly
to localhost or reuse fixed shared indices. If a live URL is supplied but the
server cannot be reached, the live tests fail rather than skip.

## Typeless mappings

`SchemaService.generateSchema(Document.class)` returns a mapping body beginning
with `properties`, without a mapping-type wrapper. The deprecated two-argument
overload ignores its old type-name argument and returns the same typeless body.
Generation uses Jackson's serializable properties, including inherited fields,
public getters, `@JsonProperty` names and `@JsonIgnore` exclusions. Schema annotations
belong on the underlying fields.

```java
@com.nosqlrevolution.annotation.index.Index("articles")
public static class Article {
    @DocumentId public String id;
    @StringType @MultiFieldType public String title;
    @StringType(type = STRING_TYPE.KEYWORD) public String category;
}

Index<Article> articles = store.createIndex(Article.class, "articles");
articles.applyMapping("{\"properties\":{\"summary\":{\"type\":\"text\"}}}");
```

Schema annotations are in `com.nosqlrevolution.annotation.schema`; `STRING_TYPE`
is in `com.nosqlrevolution.enums.Schema`. Index creation is explicit and fails if
the index already exists. Mapping updates require an existing index, accept additive
changes, and report server rejections such as incompatible field-type changes.
Raw mapping bodies must be typeless; omit both type-name and `mappings` wrappers.
Unknown options unsupported by the pinned ES8 client fail instead of being dropped.
For settings, aliases, custom analyzers or advanced configuration, use
`store.getClient().indices()` directly.

| Java type or annotation | Generated mapping |
| --- | --- |
| String/char, `@StringType` | `text`; `@DocumentId` strings default to `keyword` |
| `@StringType(type=KEYWORD)` or legacy `index=NOT_ANALYZED` | `keyword` |
| `@MultiFieldType` on text | Adds `fields.keyword` (configurable name and `ignore_above`) |
| Primitive/wrapper numbers, boolean | Corresponding ES numeric or boolean type |
| Enum, UUID | `keyword` |
| Date/Calendar or `@DateType` | `date`; legacy `dateOptionalTime` becomes `strict_date_optional_time\|\|epoch_millis` |
| Object bean, array, typed collection | Object properties, or the collection's element mapping |
| `@NestedType` | `nested` with object-element properties |
| Map or `@ObjectType` | `object`; `dynamic` and `enabled` settings supported |
| `byte[]`/`@BinaryType`, `@IPType`, `@GeoPointType` | `binary`, `ip`, `geo_point` |
| `@IgnoreType` | Retains source while disabling indexing; objects use `enabled:false` |

Unannotated serializable properties are inferred by default. Class-level
`@IgnoreType` disables indexing of unannotated properties; field `@DefaultType` or an
explicit type annotation opts a property back in. `@RootObject` controls root
`dynamic`/`enabled`. Raw collections and recursive object graphs fail clearly;
use typed collections or disable parsing of recursive properties explicitly.
BigDecimal/BigInteger need an explicit numeric annotation, with application-chosen
range/precision. Java-time serialization is not configured automatically.

Legacy settings with useful equivalents are translated: `store`/`index` become
booleans, `omit_norms` becomes `norms`, and `index_analyzer` aliases `analyzer`.
Non-default removed settings (`index_name`, mapping-time `boost`, `precision_step`,
`include_in_all`, object `path`, and old geo hash/lat-lon options) fail with migration
messages. Attachment mappings require an ingest attachment pipeline. Existing
`com.nosqlrevolution.schema.*TypeUtil` fragment helpers are deprecated pre-ES8 APIs;
use `SchemaService` for modern mappings.

## Fluent queries

```java
import static com.nosqlrevolution.query.Condition.*;
import static com.nosqlrevolution.query.ElasticStoreQuery.select;

ElasticStoreQuery query = select("id", "name", "age")
    .where(
        field("active").equal(true),
        field("age").gte(18),
        or(field("country").equal("US"), field("country").equal("CA"))
    .sort("-age", "id")
    .skip(10)
    .limit(100)
    .pageSize(25);

try (Cursor<Person> people = index.findAll(query, Person.class)) {
    for (Person person : people) {
        // Process each matching document.
    }
}
```

`where(a, b)` and `and(a, b)` require both conditions. `or(a, b)` requires at
least one, and `not(a, b)` negates their conjunction: `NOT (a AND b)`. Conditions
are immutable and can be reused. Query configuration remains fluent and mutable;
compilation takes a snapshot rather than caching a query that could become stale.

Equality and membership use exact terms, preserving strings, booleans and primitive
numeric wrapper types. Use keyword fields for exact text comparisons; the DSL does
not automatically append `.keyword`. `gt`, `gte`, `lt` and `lte` use range queries;
dates can be supplied as date strings. `exists()` tests field presence. Exclusions
follow Elasticsearch semantics, so `notEqual`/`notIn` also match missing fields;
combine them with `exists()` when missing fields should be excluded.

`match()` uses analyzed text, and `matchPhrase()` requires an analyzed phrase.
The older `mustContain` requires all supplied text queries, with all words in each;
`shouldContain` requires any supplied text query; `mustNotContain` excludes any
matching supplied text query. Fluent `where()` conditions run in filter context.
For custom scoring, use `setQuery()` with a native Elasticsearch `Query`; calling
`where()` afterward replaces that raw query, and vice versa. `toQuery()` exposes
the compiled Elasticsearch query without changing the builder.

`field(Field.ID)` queries Elasticsearch metadata IDs. `Field.ALL` is rejected
because Elasticsearch 8 has no `_all` field. Empty groups, empty membership lists,
null values and invalid pagination settings are rejected rather than becoming an
unfiltered search. A selection without conditions intentionally matches all.

Sorting accepts multiple field names; a leading `-` means descending and `+` (or
no prefix) means ascending. `sort(field, SortOrder)` appends an explicit sort.
PIT searches add `_shard_doc` as a tie-breaker. `skip()` discards matching documents
across cursor pages, `limit()` caps the total returned documents (zero means
unlimited), and `pageSize()` controls fetch size. Deep skips still require reading
and discarding earlier pages. `count(query)` returns the full matching count,
independent of pagination. Non-cursor searches use `from`/`size` and remain subject
to Elasticsearch's result-window limit.

Library-built searches set `allow_partial_search_results=false`. Single-document
searches and cursors also reject responses reporting `timed_out`, early termination
or shard failures, even when HTTP succeeds. Counts reject shard failures rather
than returning an incomplete count. A failed cursor closes its latest PIT and
keeps throwing the original failure on later iteration calls; it does not silently
become exhausted or retry the page. Cleanup errors are attached as suppressed
exceptions. Cursor pages retain the original request timeout.

PIT creation uses Elasticsearch's strict default. When the Java client exposes PIT
shard statistics, those are checked before any search; a failed PIT is closed.
Older Java clients discard that metadata, so this initial check is unavailable,
but strict server-side PIT creation and per-page response checks still apply.
These checks cover reported search/shard failures; they do not override a remote
cluster's configured availability/skip policy.

An export or transfer that encounters an incomplete page fails the run. Earlier
blocks may already have been written; there is no rollback. A failed export can
leave a partial output file that must be inspected or removed before retrying.

For a native request, `QueryService.executeSearch(request, Person.class)` returns
`SearchResponse<Person>` with typed sources, hit IDs, totals and response metadata.
Use `JsonData.class` for raw JSON. It applies the same incomplete-response checks
and propagates transport, server and deserialization errors. If the request uses a
PIT, the caller owns it and must keep the latest response PIT ID. An
`IncompleteSearchException` retains the rejected response in `getResponse()`, so
callers can inspect diagnostics and close the latest PIT even after a partial
response. This low-level helper does not manage PIT cleanup.
Use a cursor for automatic failure cleanup. The legacy `executeBuilder(request)`
signature is deprecated: its source type is `Hit`, not the document model, and
failures now propagate instead of returning `null`.

`blockSize()`/`setSize()` alias page size, and `setFrom()` aliases skip.
`ESQuery` and `ESQueryImpl` remain deprecated facades for existing fluent calls;
subclasses that accessed their old protected state must migrate to the accessors.
Completed conditions now use immutable snapshots instead of an enclosing mutable
field builder.

DSL tests without a server: `mvn -Dtest=QueryTest test`. With the Docker instance
from the earlier instructions, include `LiveQueryTest` alongside `LivePipelineTest`
and pass `-Delasticstore.test.url=http://127.0.0.1:19200`. Live tests verify expected
document IDs for boolean, range, membership, ID and text queries, plus paging and
projection through JSON and typed cursors.

### Failure reporting

Bulk writes require one response item per requested document and check both item
errors and the response error flag. Multi-get also checks response count and ID
order, preserving `null` only for genuine missing documents. Single and bulk
writes, deletes and refresh reject reported shard failures. A failed write may
already have completed on the primary shard: inspect `OperationException` item
results before deciding which operations to retry. Buffered documents remain in
memory when flushing fails; a retry resends the retained batch.

File imports require valid UTF-8, including compressed inputs. Malformed coordinate
strings now fail rather than leaving a default or previous location unchanged.
Legacy wildcard queries retain embedded and escaped wildcard patterns instead of
incorrectly reducing them to prefix searches.

Native cursor requests retain their search body and query parameters across pages,
including post-filters and runtime fields. Unsupported query parameters fail
explicitly rather than changing the search. Cursor source-conversion failures
close the PIT, and unsuccessful PIT cleanup is reported to the caller.
