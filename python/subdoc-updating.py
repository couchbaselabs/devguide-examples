#!/usr/bin/env python
from __future__ import print_function

from couchbase.bucket import Bucket
import couchbase.exceptions as E
import couchbase.subdocument as SD

cb = Bucket('couchbase://localhost/default')

cb.upsert('docid', {
    'name': 'Mark',
    'email': 'm@n.com',
    'array': [1, 2, 3, 4]
})

cb.mutate_in('docid',
             # Add 42 as a new element to 'array'
             SD.array_append('array', '42'),
             # Increment the numeric value of the first element by 99
             SD.counter('array[0]', 99),
             # Add a new 'description' field
             SD.upsert('description', 'just a dev'))

print('Document is now:', cb.get('docid').value)

try:
    cb.mutate_in('docid', SD.upsert('deep.nested.path', 'some-value'))
except E.SubdocPathNotFoundError as e:
    print('Caught exception', e)

# Use `create`
cb.mutate_in('docid', SD.upsert(
    'deep.nested.path', 'some-value', create_parents=True))
print('Getting value back:', cb.retrieve_in('docid', 'deep.nested.path')[0])