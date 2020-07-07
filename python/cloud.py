from couchbase.cluster import Cluster
from couchbase.cluster import PasswordAuthenticator
from couchbase.n1ql import N1QLQuery

######## Update this to your cluster
endpoint = 'cb.e207a530-a469-492f-89d4-a5392e265c10.dp.cloud.couchbase.com'
username = 'user'
password = 'password'
bucket_name = 'couchbasecloudbucket'
#### User Input ends here.

# Initialize the Connection
cluster = Cluster('couchbases://' + endpoint + '?ssl=no_verify')  # Update the cluster endpoint
authenticator = PasswordAuthenticator(username, password) 
cluster.authenticate(authenticator)
cb = cluster.open_bucket(bucket_name)

# Create a N1QL Primary Index (but ignore if it exists)
cb.bucket_manager().n1ql_index_create_primary(ignore_exists=True)

# Store a Document
cb.upsert('u:king_arthur', {'name': 'Arthur', 'email': 'kingarthur@couchbase.com', 'interests': ['Holy Grail', 'African Swallows']})

# Load the Document and print it
print(cb.get('u:king_arthur').value)

# Perform a N1QL Query
row_iter = cb.n1ql_query(N1QLQuery('SELECT name FROM %s WHERE $1 IN interests' % (bucket_name), 'African Swallows'))

# Print each found Row
for row in row_iter: print(row)
