#!/usr/bin/env python
from __future__ import print_function

from couchbase.bucket import Bucket
import couchbase.exceptions as E

cb = Bucket('couchbase://10.0.0.31/default')

# This always works!
print('Upserting')
cb.upsert('docid', {'property': 'value'})
print('Getting item back. Value is:',
      cb.get('docid').value)
print('...')

print('Will try to insert the document. Should fail because the item already exists..')
try:
    cb.insert('docid', {'property': 'value'})
except E.KeyExistsError:
    print('Insert failed because item already exists!')
print('...')

print('Replacing the document. This should work because the item already exists')
cb.replace('docid', {'property': 'new_value'})
print('Getting document again. Should contain the new contents:',
      cb.get('docid').value)
print('...')

print('Removing document.')
# Remove the item, then try to replace it!
cb.remove('docid')
print('Replacing document again. Should fail because document no longer exists')
try:
    cb.replace('docid', {'property': 'another value'})
except E.NotFoundError:
    print('Get failed since item does not exist')
