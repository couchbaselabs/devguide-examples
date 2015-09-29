#!/usr/bin/env python

from couchbase.bucket import Bucket

cb = Bucket('couchbase://10.0.0.31/default')
print cb.server_nodes