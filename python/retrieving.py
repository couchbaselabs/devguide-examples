#!/usr/bin/env python
from __future__ import print_function

from couchbase.bucket import Bucket
from couchbase.exceptions import NotFoundError

cb = Bucket('couchbase://10.0.0.31/default')

print('Getting non-existent key. Should fail..')
try:
    cb.get('non-exist-document')
except NotFoundError:
    print('Got exception for missing document!')
print('...')

print('Upserting...')
cb.upsert('new_document', {'foo': 'bar'})
print('Getting...')
print(cb.get('new_document').value)
