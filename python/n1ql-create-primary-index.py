#!/usr/bin/env python

from couchbase.bucket import Bucket

cb = Bucket('couchbase://localhost/default')
manager = cb.bucket_manager()
manager.n1ql_index_create_primary(ignore_exists=True)