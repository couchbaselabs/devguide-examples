#!/usr/bin/env python

from couchbase.bucket import Bucket

# Note the `couchbases` in the scheme. This is required for SSL connections!
cb = Bucket('couchbases://10.0.0.31/default?certpath=/tmp/couchbase-ssl-certificate.pem')
print cb.server_nodes
