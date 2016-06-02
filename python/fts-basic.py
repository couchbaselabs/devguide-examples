#!/usr/bin/env python
from __future__ import print_function
from pprint import pprint

from couchbase.bucket import Bucket
import couchbase.fulltext as FT

cb = Bucket()
results = cb.search(
        'travel-search',
        FT.MatchQuery('part', fuzziness=0, field='content'),
        limit=3,
        facets={'countries': FT.TermFacet('country', limit=3)})

for row in results:
    pprint(row)

print('Facet results:')