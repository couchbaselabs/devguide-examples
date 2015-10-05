from __future__ import print_function

from couchbase.bucket import Bucket
from couchbase.bucket import LOCKMODE_WAIT
from threading import Thread
from couchbase.exceptions import KeyExistsError


cb = Bucket('couchbase://10.0.0.31/default', lockmode=LOCKMODE_WAIT)

cb.upsert('a_list', [])


print('Will attempt concurrent document mutations without CAS')


def add_item_to_list(client, new_item):
    l = client.get('a_list').value
    l.append(new_item)
    client.replace('a_list', l)

threads = [Thread(target=add_item_to_list, args=(cb, "item_" + str(x)))
           for x in range(0, 10)]

[t.start() for t in threads]
[t.join() for t in threads]

cur_list = cb.get('a_list').value
print('Current list has {0} elements'.format(len(cur_list)))
if len(cur_list) != 10:
    print('Concurrent modifications removed some of our items!', cur_list)


# The same as above, but using CAS
def add_item_to_list_safe(client, new_item):
    while True:
        rv = client.get('a_list')
        l = rv.value
        l.append(new_item)

        try:
            cb.replace('a_list', l, cas=rv.cas)
            return
        except KeyExistsError:
            print("Cas mismatch for item", new_item)
            continue

# Reset the list again
cb.upsert('a_list', [])

print('Will attempt concurrent modifications using CAS')
threads = [Thread(target=add_item_to_list_safe, args=(cb, "item_" + str(x)))
           for x in range(0, 10)]

[t.start() for t in threads]
[t.join() for t in threads]
cur_list = cb.get('a_list').value
print('Current list has {0} elements'.format(len(cur_list)))
assert len(cur_list) == 10
