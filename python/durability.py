#!/usr/bin/env python

from couchbase.bucket import Bucket

cb = Bucket('couchbase://10.0.0.31/default')

# In the Python SDK you can specify "maximum" persistence and
# replication by specifying -1 for either valie
cb.upsert('docid', {'some': 'value'}, persist_to=-1, replicate_to=-1)

# Store with persisting to master node
cb.upsert('docid', {'some': 'value'}, persist_to=1)

# Note, this will fail if there are no replicas online
cb.upsert('docid', {'some': 'value'}, persist_to=1, replicate_to=1)