#!/usr/bin/env python
from pprint import pprint

from couchbase.bucket import Bucket
from couchbase.n1ql import N1QLQuery

cb = Bucket('couchbase://10.0.0.31/travel-sample')

query = N1QLQuery('SELECT airportname, city, country FROM `travel-sample` '
                  'WHERE type="airport" AND city="Reno"')
for row in cb.n1ql_query(query):
    pprint(row)