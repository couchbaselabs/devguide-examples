#!/usr/bin/env python

from couchbase.bucket import Bucket
from couchbase.cluster import *
import os.path

hostname ="localhost"
bucket_name = "default"

# point to certificates, keys and trust stores
# for the purposes of this code,
# the script in etc/x509-cert will generate these

clientdir = "etc/x509-cert/SSLCA/clientdir"
options = dict(certpath=os.path.join(clientdir, "client.pem"),
               truststorepath=os.path.join(clientdir, "trust.pem"),
               keypath=os.path.join(clientdir, "client.key"))

# open a Bucket directly

# Note the `couchbases` in the scheme. This is required for SSL connections!
cb = Bucket('couchbases://{hostname}/default?certpath={certpath}&truststorepath={truststorepath}&keypath={keypath}'.format(hostname=hostname,**options))
print(cb.server_nodes)

# create a Cluster object

cb_cluster = Cluster("http://{}/".format(hostname))

# create an SSL-based Authenticator
authenticator = CertAuthenticator(cluster_username="admin",
                                  cluster_password="password", **options)
# apply this to the cluster
cb_cluster.authenticate(authenticator)

# user this to open a bucket
cb_2 = cb_cluster.open_bucket(bucket_name)

# some example operations

cb_2.upsert("fred", {"hello": "world"})
cb_2.remove("fred")
