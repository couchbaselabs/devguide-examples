#!/usr/bin/env python
import time

from couchbase.bucket import Bucket
from couchbase.n1ql import N1QLQuery, MutationState

TIMESTAMP = str(time.time())

cb = Bucket('couchbase://localhost/default?fetch_mutation_tokens=true')
rv = cb.upsert('ndoc', {'timestamp': TIMESTAMP})

ms = MutationState()
ms.add_results(rv)

query = N1QLQuery('SELECT * from default WHERE timestamp=$1', TIMESTAMP)
query.consistent_with(ms)
print query.encoded

for row in cb.n1ql_query(query):
    print row
