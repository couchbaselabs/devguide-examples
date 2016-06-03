#!/usr/bin/env python

from couchbase.admin import Admin
from couchbase.bucket import Bucket

adm = Admin('Administrator', '123456', host='localhost', port=8091)
adm.bucket_create('new-bucket',
                  bucket_type='couchbase',
                  bucket_password='s3cr3t')

# Wait for bucket to become ready
adm.wait_ready('new-bucket', timeout=30)

bucket = Bucket('couchbase://localhost/new-bucket', password='s3cr3t')
bucket.upsert('foo', 'bar')

adm.bucket_remove('new-bucket')
