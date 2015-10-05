#!/usr/bin/env python
from __future__ import print_function
import random
import os

from couchbase.bucket import Bucket
from couchbase.n1ql import N1QLQuery, CONSISTENCY_REQUEST

# Ensure there is a primary index on the default bucket!
RANDOM_NUMBER = random.randint(0, 10000000)

cb = Bucket('couchbase://10.0.0.31/default')
cb.upsert('user:{}'.format(RANDOM_NUMBER), {
    'name': ['Brass', 'Doorknob'],
    'email': ['brass.doorknob@juno.com'],
    'random': RANDOM_NUMBER
})

query = N1QLQuery(
    'SELECT name, email, random, META(default).id FROM default WHERE $1 IN name', 'Brass')
# If this line is removed, the latest 'random' field might not be present
query.consistency = CONSISTENCY_REQUEST

print('Expecting random:', RANDOM_NUMBER)

for row in cb.n1ql_query(query):
    print('Name: {0}, Email: {1}, Random: {2}'.format(row['name'], row['email'], row['random']))
    if row['random'] == RANDOM_NUMBER:
        print('!!! Found our newly inserted document !!!')
    if os.environ.get('REMOVE_DOORKNOBS'):
        cb.remove(row['id'])
