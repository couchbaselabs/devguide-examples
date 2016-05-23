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

# Do it the simple way:
rv = cb.retrieve_in('docid', 'name', 'array[1]')
print('Name is: {0}, array[1] is: {1}'.format(rv[0], rv[1]))

# If all results are successful:
name, array_2ndelem = rv
print('Name is: {0}, Array[1] is: {1}'.format(name, array_2ndelem))

# Perform mixed-mode operations
rv = cb.lookup_in('docid',
                  SD.get('name'), SD.get('array[1]'), SD.exists('non-exist'))
print('Name is', rv[0])
print('Array[1] is', rv[1])
print('non-exist exists?', rv.exists(2))

# See what happens when we try to reference a failed path:
try:
    rv[2]
except E.SubdocPathNotFoundError:
    print('Using subscript access raises exception for missing item')


# If we try to get a non-existent document, it will fail as normal
try:
    cb.retrieve_in('non-exist', 'pth1', 'pth2', 'pth3')
except E.NotFoundError:
    print('Document itself not found!')