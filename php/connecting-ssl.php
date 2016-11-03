<?php

/*
 * Put Self-Signed Cluster Certificate from the cluster
 * to /tmp/couchbase-ssl-certificate.pem
 */
$cluster = new CouchbaseCluster('couchbases://localhost?certpath=/tmp/couchbase-ssl-certificate.pem');
$bucket = $cluster->openBucket('default');
