#!/usr/bin/env python
from __future__ import print_function
from pprint import pprint

from couchbase.bucket import Bucket
from couchbase.n1ql import N1QLQuery

cb = Bucket('couchbase://10.0.0.31/travel-sample')


def query_city(bkt, city):
    query = N1QLQuery('SELECT airportname FROM `travel-sample` '
                      'WHERE city=$1 AND type="airport"', city)

    # Uncomment the following line to make the query optimized for
    # repeated invocations.
    # The query string is compiled, and the the compiled form is
    # stored in the client (as a dictionary value to the query string
    # itself).
    #
    # q.adhoc = False
    return bkt.n1ql_query(query)


print('Airports in Reno:')
for row in query_city(cb, 'Reno'):
    pprint(row)

print('Airports in Dallas')
for row in query_city(cb, 'Dallas'):
    pprint(row)

print('Airports in Los Angeles')
for row in query_city(cb, 'Los Angeles'):
    pprint(row)
