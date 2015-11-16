#!/usr/bin/env python
from __future__ import print_function

from couchbase.bucket import Bucket

cb = Bucket('couchbase://10.0.0.31/default')

# First insert the documents we care about
cb.upsert_multi({
    'foo': {'foo': 'value'},
    'bar': {'bar': 'value'},
    'baz': {'baz': 'value'}
})

# Get them back again
rvs = cb.get_multi(['foo', 'bar', 'baz'])
for key, info in rvs.items():
    print('Value for {0}: {1}'.format(key, info.value))

# See other error handling examples showing how to handle errors
# in multi operations
