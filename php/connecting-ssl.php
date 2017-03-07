<?php

/*
 * Put Self-Signed Cluster Certificate from the cluster
 * to /tmp/couchbase-ssl-certificate.pem:
 *
 * $ curl http://localhost:8091/pools/default/certificate > /tmp/couchbase-ssl-certificate.pem
 *
 * At the moment SSL support does not work if php-curl module loaded
 */
$cluster = new \Couchbase\Cluster('couchbases://localhost?certpath=/tmp/couchbase-ssl-certificate.pem');
$bucket = $cluster->openBucket('default');
