#!/usr/bin/env python

from couchbase.bucket import Bucket

cb = Bucket('couchbase://localhost/default')
cb.flush()