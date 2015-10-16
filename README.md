# Developer Guide Examples

These examples are intended to be linked to from within the developer guide.
They are currently cross-referenced by language.

Each example should be fully self-contained and executable. For languages which have a significant setup, the example may be split into multiple files, but should still be executable. Error handling is optional but should be be at least hinted at.

### Basic Connection
This example should show how to connect to a remote Couchbase cluster and bucket

[C](c/connecting.c) |
[Python](python/connecting.py) |
[Java](java/src/main/java/com/couchbase/devguide/ConnectionBase.java)|
.NET |
[Go](go/connecting.go) |
[node.js](nodejs/connecting.js)

### Updating/Storing
This example should show how to store an item into a cluster

[C](c/updating.c) |
[Python](python/updating.py) |
[Java](java/src/main/java/com/couchbase/devguide/Updating.java) |
.NET |
[Go](go/updating.go) |
[node.js](nodejs/updating.js)

### Retrieving
This example should show how to get items out of the cluster

[C](c/retrieving.cc) |
[Python](python/retrieving.py) |
[Java](java/src/main/java/com/couchbase/devguide/Retrieving.java) |
.NET |
[Go](go/retrieving.go) |
[node.js](nodejs/retrieving.js)

### Bulk Storing
This example should show how to store items into a cluster using the Bulk API

[C](c/bulk-store.cc) |
[Python](python/bulk-operations.py) |
[Java](java/src/main/java/com/couchbase/devguide/BulkInsert.java) |
.NET |
[Go](go/bulk-insert.go) |
[node.js]

### Bulk Retrieving
This example should show how to get items out of the cluster using the Bulk API

[C](c/bulk-get.cc) |
[Python](python/bulk-operations.py) |
[Java](java/src/main/java/com/couchbase/devguide/BulkGet.java) |
.NET |
[Go](go/bulk-get.go) |
[node.js]

### Counter
This example should show how to initialize and update a counter

[C](c/counter.cc) |
[Python](python/counter.py) |
[Java](java/src/main/java/com/couchbase/devguide/Counter.java) |
.NET |
[Go](go/counter.go) |
[node.js](nodejs/counter.js)

### Expiry
This example should show how to initialize a document with an Expiry or "ttl" - time to live

[C](c/expiration.cc) |
[Python](python/expiration.py) |
[Java](java/src/main/java/com/couchbase/devguide/Expiration.java) |
.NET |
[Go](go/expiration.go) |
[node.js](nodejs/expiration.js)

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
[node.js](nodejs/query-criteria.js)


### Query with placeholders
This example should demonstrate how to use placeholders, and also the advantages they afford, perhaps by abstracting a given query away as a function, and passing a function parameter down as a query parameter

[C](c/query-placeholders.cc) |
[Python](python/query-placeholders.py) |
[Java](java/src/main/java/com/couchbase/devguide/QueryPlaceholders.java) |
.NET |
[Go](go/query-placeholders.go) |
[node.js](nodejs/query-placeholders.js)

### Query - Ensuring all documents are the latest (scan consistency)
This example should show how the `scan_consistency` parameter may be enabled for a specific query.

[C](c/query-consistency.cc) |
[Python](python/query-consistency.py) |
[Java](java/src/main/java/com/couchbase/devguide/QueryConsistency.java) |
.NET |
Go |
node.js

### CAS Handling - Using CAS for concurrent mutations
This example will demonstrate concurrent mutations with and without using the
CAS value. Without using the CAS value, some modifications may end up getting
lost, whereas using the CAS within a proper retry mechanism will ensure that
all mutations remain in tact

[C](c/cas.cc) |
[Python](python/cas.py) |
[Java](java/src/main/java/com/couchbase/devguide/Cas.java) |
.NET |
Go |
node.js

### Durability
Shows storing an item with durability requirements, attempting to persist/replicate
to the maximum number of nodes available.

Some SDKs provide APIs to determine how many nodes are in the cluster, while some SDKs allow dynamically persisting to the total number of nodes available, while others only allow fixed numbers. More details may be found in the examples.

[C](c/durability.cc) |
[Python](python/durability.py) |
[Java](java/src/main/java/com/couchbase/devguide/Durability.java) |
.NET |
Go |
node.js
