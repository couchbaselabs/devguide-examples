<?php

/*
 * Put Self-Signed Cluster Certificate from the cluster
 * to /tmp/couchbase-ssl-certificate.pem:
 *
 * $ curl http://localhost:8091/pools/default/certificate > /tmp/couchbase-ssl-certificate.pem
 *
 * In spite of libcouchbase linked with SSL itself, when using in PHP context, module php_openssl
 * have to be loaded before php_couchbase, or it might cause segfaults if other SSL-extensions also
 * loaded. For example, php_curl + php_couchbase without php_openssl loaded will trigger segfault
 * on php_curl unloading.
 */
$cluster = new \Couchbase\Cluster('couchbases://localhost?certpath=/tmp/couchbase-ssl-certificate.pem');
$cluster->authenticateAs('Administrator', 'password');
$bucket = $cluster->openBucket('default');
