#!/usr/bin/env python
from __future__ import print_function

from couchbase.bucket import Bucket

cb = Bucket('couchbase://10.0.0.31/default')

# Remove document so we have predictable behavior in this example
# the 'quiet' argument instructs the client not to raise an exception if the
# document doesn't exist, but fail silently (the status can still be retrieved
# from the returned result object)
cb.remove('docid', quiet=True)

# Without the 'initial' parameter, this command would fail if the item doesn't exist
rv = cb.counter('docid', delta=20, initial=100)
print('Delta=20, Initial=100. Current value is:', rv.value)

rv = cb.counter('docid', delta=1)
print('Delta=1. Current value is:', rv.value)

rv = cb.counter('docid', delta=-50)
print('Delta=-50. Current value is:', rv.value)