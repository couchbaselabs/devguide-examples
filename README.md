# Developer Guide Examples

These examples are intended to be linked to from within the developer guide.
They are currently cross-referenced by language.

Each example should be fully self-contained and executable. For languages which have a significant setup, the example may be split into multiple files, but should still be executable. Error handling is optional but should be be at least hinted at.

### Basic Connection
This example should show how to connect to a remote Couchbase cluster and bucket

[C](c/connecting.c) |
[Python](python/connecting.py) |
Java |
.NET |
Go |
node.js

### Updating/Storing
This example should show how to store an item into a cluster

[C](c/updating.c) |
[Python](python/updating.py) |
Java |
.NET |
Go |
node.js

### Retrieving
This example should show how to get items out of the cluster

[C](c/retrieving.cc) |
[Python](python/retrieving.py) |
Java |
.NET |
Go |
node.js

### Counter
This example should show how to initialize and update a counter

[C](c/counter.cc) |
[Python](python/counter.py) |
Java |
.NET |
Go |
node.js

### Query with criteria
This example should show how to perform a simple query against the travel-sample bucket. The query is something like:

```
query = N1QLQuery('SELECT airportname, city, country FROM `travel-sample` '
                  'WHERE type="airport" AND city="Reno"')
```

[C](c/query-criteria.cc) |
[Python](python/query-criteria.py) |
Java |
.NET |
Go |
node.js


### Query with placeholders
This example should demonstrate how to use placeholders, and also the advantages they afford, perhaps by abstracting a given query away as a function, and passing a function parameter down as a query parameter

[C](c/query-placeholders.cc) |
[Python](python/query-placeholders.py) |
Java |
.NET |
Go |
node.js