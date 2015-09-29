from __future__ import print_function
from time import sleep

from couchbase.bucket import Bucket
from couchbase.exceptions import NotFoundError

cb = Bucket('couchbase://10.0.0.31/default')

print('Storing with an expiration of 2 seconds')
cb.upsert('docid', {'some': 'value'}, ttl=2)

print('Getting item back immediately')
print(cb.get('docid').value)

print('Sleeping for 4 seconds...')
sleep(4)
print('Getting key again...')
try:
    cb.get('docid')
except NotFoundError:
    print('Get failed because item has expired')

print('Storing item again (without expiry)')
cb.upsert('docid', {'some': 'value'})

print('Using get-and-touch to retrieve key and modify expiry')
rv = cb.get('docid', ttl=2)
print('Value is:', rv.value)

print('Sleeping for 4 seconds again')
sleep(4)
print('Getting key again (should fail)')
try:
    cb.get('docid')
except NotFoundError:
    print('Failed (not found)')

print('Storing key again...')
cb.upsert('docid', {'some': 'value'})
print('Using touch (without get). Setting expiry for 1 second')
cb.touch('docid', ttl=1)

print('Sleeping for 4 seconds...')
sleep(4)
print('Will try to get item again...')
try:
    cb.get('docid')
except NotFoundError:
    print('Get failed because key has expired')