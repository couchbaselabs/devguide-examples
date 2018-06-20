#!/usr/bin/env python

from couchbase.bucket import Bucket
from couchbase.cluster import *

# Note the `couchbases` in the scheme. This is required for SSL connections!
cb = Bucket('couchbases://10.0.0.31/default?certpath=/tmp/couchbase-ssl-certificate.pem')
print(cb.server_nodes)

cb_cluster = Cluster("http://10.0.0.31/")
authenticator = CertAuthenticator(cert_path="/tmp/couchbase-ssl-certificate.pem", key_path="/tmp/couchbase-ssl-key.key", cluster_username="admin",
                                  cluster_password="password")
cb_cluster.authenticate(authenticator)

cb_2 = cb_cluster.open_bucket("default")

cb_2.upsert("fred", {"hello":"world"})

cb_2.remove("fred")
