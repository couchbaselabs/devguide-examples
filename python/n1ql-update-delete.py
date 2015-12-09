#!/usr/bin/env python
from couchbase.bucket import Bucket
from couchbase.n1ql import N1QLQuery

cb = Bucket('couchbase://localhost/default')

# Create a set of 'product' documents
docs = {
    'bundt_cake_pan1': {
        'type': 'product',
        'name': 'Bundt Cake Pan',
        'price': 7.35,
        'categories': ['kitchen', 'home']
    },
    'led_4d_900in_tv1': {
        'type': 'product',
        'name': '4D 900" LED TV',
        'price': 1250000.99,
        'categories': ['technology', 'electronics']
    },
    'light_bulb_40w_led': {
        'type': 'product',
        'name': '40-Watt LED Bulb',
        'price': 4.40,
        'categories': ['electronics', 'home']
    },
    'red_puff_parka3': {
        'type': 'product',
        'name': 'Parka (red)',
        'price': 54.99,
        'categories': ['clothing']
    },
    'utilikilt1_speaker': {
        'type': 'product',
        'name': 'Utilikilt with Bluetooth Speaker in Rear',
        'price': 124.95,
        'categories': ['clothing', 'electronics', 'technology']
    }
}

# Delete any prior products so we start with a clean dataset
meta = cb.n1ql_query(
    N1QLQuery('DELETE from default WHERE type=$1', 'product')
).execute().meta
print 'Deleted {0} items!'.format(meta['metrics'].get('mutationCount', 0))

# Everything's 25% off!
cb.upsert_multi(docs)
query = N1QLQuery(
    'UPDATE default '
    'SET sale_price=ROUND(price-(price * 0.25), 2) '
    'WHERE type=$1'
    'RETURNING name, price, sale_price',
    'product'
)
for row in cb.n1ql_query(query):
    print '{0} WAS: {1:2}. NOW ONLY {2:2}'.format(
        row['name'], row['price'], row['sale_price'])

# Show how we can update a single document by its ID
query = N1QLQuery(
    'UPDATE default USE KEYS $keys SET description=$desc',
    keys=['utilikilt1_speaker'],
    desc='Use this handy utilikilt as a fashion accessory or in the garage, and lets '
)
cb.n1ql_query(query).execute()
# Show the new value
print cb.get('utilikilt1_speaker').value['description']