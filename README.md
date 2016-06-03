# Developer Guide Examples

These examples are intended to be linked to from within the developer guide.
They are currently cross-referenced by language.

Each example should be fully self-contained and executable. For languages which have a significant setup, the example may be split into multiple files, but should still be executable. Error handling is optional but should be be at least hinted at.

### How to use these examples

The examples are versioned by the features introduced in a given server
version (and if required, by an SDK-specific sub-series within them).

The `server-4.5` branch should be used for documentation content related
to Couchbase Server 4.5, and so on.

<!-- toc -->

- [Connecting to Couchbase](#connecting-to-couchbase)
  * [Basic Connection](#basic-connection)
  * [SSL Connection](#ssl-connection)
- [Basic Document/KV Operations](#basic-documentkv-operations)
  * [Updating/Storing](#updatingstoring)
  * [Retrieving](#retrieving)
- [Advanced Document/KV Operations](#advanced-documentkv-operations)
  * [Bulk Storing](#bulk-storing)
  * [Bulk Retrieving](#bulk-retrieving)
  * [Counter](#counter)
  * [Expiry](#expiry)
  * [CAS Handling - Using CAS for concurrent mutations](#cas-handling---using-cas-for-concurrent-mutations)
  * [Durability](#durability)
- [Sub-Document Operations](#sub-document-operations)
  * [Subdoc - Retrieving](#subdoc---retrieving)
  * [Subdoc - Updating/Storing](#subdoc---updatingstoring)
- [N1QL Queries](#n1ql-queries)
  * [Query with criteria](#query-with-criteria)
  * [Query with placeholders](#query-with-placeholders)
  * [Query - Ensuring all documents are the latest (scan consistency)](#query---ensuring-all-documents-are-the-latest-scan-consistency)
  * [Query - Better reuse of queries with adhoc(false) and Prepared Statements](#query---better-reuse-of-queries-with-adhocfalse-and-prepared-statements)
  * [Query - UPDATE and DELETE](#query---update-and-delete)

<!-- tocstop -->

## Connecting to Couchbase
These examples show how to establish a connection from an SDK to
a Couchbase cluster.

### Basic Connection
This example should show how to connect to a remote Couchbase cluster and bucket

[C](c/connecting.c) |
[Python](python/connecting.py) |
[Java](java/src/main/java/com/couchbase/devguide/ConnectionBase.java)|
[.NET](dotnet/ConnectionBase.cs) |
[Go](go/connecting.go) |
[node.js](nodejs/connecting.js) |
[PHP](php/connecting.php)

### SSL Connection
This example shows how to connect using the server's SSL certificate which has
already been downloaded locally to the SDK host. The C-based examples will
refer to this path when connecting, while the other SDKs will refer to a
specific store and possibly have the example divided into installing and using
the certificate.

[C](c/connecting-ssl.c) |
[Python](python/connecting-ssl.py) |
[Java](java/src/main/java/com/couchbase/devguide/ConnectingSsl.java) |
.NET |
Go |
node.js |
[PHP](connecting-ssl.php) (currently crashes!)

## Basic Document/KV Operations

These examples demonstrate the most basic functionality of working with
documents.

### Updating/Storing
These examples show how to store an item into a cluster

[C](c/updating.c) |
[Python](python/updating.py) |
[Java](java/src/main/java/com/couchbase/devguide/Updating.java) |
[.NET](dotnet/Update.cs) |
[Go](go/updating.go) |
[node.js](nodejs/updating.js) |
[PHP](php/updating.php)

### Retrieving
This example should show how to get items out of the cluster

[C](c/retrieving.cc) |
[Python](python/retrieving.py) |
[Java](java/src/main/java/com/couchbase/devguide/Retrieving.java) |
[.NET](dotnet/Retrieve.cs) |
[Go](go/retrieving.go) |
[node.js](nodejs/retrieving.js) |
[PHP](php/retrieving.php)

## Advanced Document/KV Operations
These examples show how to perform more detailed operations on documents

### Bulk Storing
This example should show how to store items into a cluster using the Bulk API

[C](c/bulk-store.cc) |
[Python](python/bulk-operations.py) |
[Java](java/src/main/java/com/couchbase/devguide/BulkInsert.java) |
[.NET](dotnet/BulkInsert.cs) |
[Go](go/bulk-insert.go) |
node.js (N/A) |
[PHP](php/bulk-operations.php)

### Bulk Retrieving
This example should show how to get items out of the cluster using the Bulk API

[C](c/bulk-get.cc) |
[Python](python/bulk-operations.py) |
[Java](java/src/main/java/com/couchbase/devguide/BulkGet.java) |
[.NET](dotnet/BulkGet.cs) |
[Go](go/bulk-get.go) |
node.js (N/A) |
[PHP](php/bulk-operations.php)

### Counter
This example should show how to initialize and update a counter

[C](c/counter.cc) |
[Python](python/counter.py) |
[Java](java/src/main/java/com/couchbase/devguide/Counter.java) |
[.NET](dotnet/Counter.cs) |
[Go](go/counter.go) |
[node.js](nodejs/counter.js) |
[PHP](php/counter.php)

### Expiry
This example should show how to initialize a document with an Expiry or "ttl" - time to live

[C](c/expiration.cc) |
[Python](python/expiration.py) |
[Java](java/src/main/java/com/couchbase/devguide/Expiration.java) |
[.NET](dotnet/Expiration.cs) |
[Go](go/expiration.go) |
[node.js](nodejs/expiration.js) |
[PHP](php/expiration.php)


### CAS Handling - Using CAS for concurrent mutations
This example will demonstrate concurrent mutations with and without using the
CAS value. Without using the CAS value, some modifications may end up getting
lost, whereas using the CAS within a proper retry mechanism will ensure that
all mutations remain in tact

[C](c/cas.cc) |
[Python](python/cas.py) |
[Java](java/src/main/java/com/couchbase/devguide/Cas.java) |
[.NET](dotnet/Cas.cs) |
Go |
node.js |
[PHP](php/cas.php)

### Durability
Shows storing an item with durability requirements, attempting to persist/replicate
to the maximum number of nodes available.

Some SDKs provide APIs to determine how many nodes are in the cluster, while some SDKs allow dynamically persisting to the total number of nodes available, while others only allow fixed numbers. More details may be found in the examples.

[C](c/durability.cc) |
[Python](python/durability.py) |
[Java](java/src/main/java/com/couchbase/devguide/Durability.java) |
[.NET](dotnet/Durability.cs) |
Go |
node.js |
PHP

## Sub-Document Operations

Sub-Document operations (new in Couchbase 4.5) allows efficient addressing of sections within documents
(sub-documents). Sub-Document is often abbreviated as _subdoc_.

### Subdoc - Retrieving
Retrieve a few fields from a document; also demonstrate error handling if some fields are missing

[C](c/subdoc-retrieving.cc) |
[Python](python/subdoc-retrieving.py) |
Java |
.NET |
Go |
node.js |
PHP

### Subdoc - Updating/Storing
Modify a few fields within a document. Show error handling and the behavior of the `create` option

[C](c/subdoc-updating.cc) |
[Python](python/subdoc-updating.py) |
Java |
.NET |
Go |
node.js |
PHP

## N1QL Queries

These examples show how to query using N1QL

### Query with criteria
This example should show how to perform a simple query against the travel-sample bucket. The query is something like:

```
query = N1QLQuery('SELECT airportname, city, country FROM `travel-sample` '
                  'WHERE type="airport" AND city="Reno"')
```

[C](c/query-criteria.cc) |
[Python](python/query-criteria.py) |
[Java](java/src/main/java/com/couchbase/devguide/QueryCriteria.java) |
.NET |
[Go](go/query-criteria.go) |
[node.js](nodejs/query-criteria.js) |
[PHP](php/query-criteria.php)


### Query with placeholders
This example should demonstrate how to use placeholders, and also the advantages they afford, perhaps by abstracting a given query away as a function, and passing a function parameter down as a query parameter.

This example should also demonstrate in a *commented* section of code how to
optimize this query.

[C](c/query-placeholders.cc) |
[Python](python/query-placeholders.py) |
[Java](java/src/main/java/com/couchbase/devguide/QueryPlaceholders.java) |
.NET |
[Go](go/query-placeholders.go) |
[node.js](nodejs/query-placeholders.js) |
[PHP](php/query-placeholders.php)

### Query - Ensuring all documents are the latest (scan consistency)
This example should show how the `scan_consistency` parameter may be enabled for a specific query.

[C](c/query-consistency.cc) |
[Python](python/query-consistency.py) |
[Java](java/src/main/java/com/couchbase/devguide/QueryConsistency.java) |
.NET |
Go |
node.js |
[PHP](php/query-consistency.php)

### Query - Better reuse of queries with adhoc(false) and Prepared Statements
This example should demonstrate best practice when a statement is to be reused heavily. Setting the `adhoc` N1QL query tuning to `false` will use Prepared Statements in the background, which is useful in such a case. Note how this works with placeholders (but of course simple statements work too).

C |
Python |
[Java](java/src/main/java/com/couchbase/devguide/QueryPrepared.java) |
.NET |
Go |
node.js

### Query - UPDATE and DELETE

Show how these statements can be used to modify existing documents using
secondary document attributes. Also show how a single document can be
modified via the `USE KEYS` clause.

C |
[Python](python/n1ql-update-delete.py)
| .NET |
Go |
node.js

## Full-Text (CBFT) Queries

### Basic full-text example

This should show basic functionality of the FTS feature. Search for 'hoppy'
in `beer-sample`

[C](c/fts-basic.cc) |
[Python](python/fts-basic.py) |
.NET |
Go |
node.js |
Java |
PHP

## Cluster Management

### Create/Remove buckets

This should show a basic example of creating and removing a bucket. It might
also be good to show how to wait until a newly created bucket becomes ready
as well.

[C](c/create-remove-bucket.cc) |
[Python](python/create-remove-bucket.py) |
.NET |
Go |
node.js |
Java |
PHP

### Flushing a Bucket

This should show an example of how to flush a bucket.

[C](c/flush.cc) |
[Python](python/flush.py) |
.NET |
Go |
node.js |
Java |
PHP

### Creating N1QL Primary Index

This should show how to create the primary N1QL index on a bucket, ignoring
whether it exists or not

[C](c/n1ql-create-primary-index.cc) |
[Python](python/n1ql-create-primary-index.py) |
.NET |
Go |
node.js |
Java |
PHP
