<?php

$root = dirname(__FILE__);
$cluster = new \Couchbase\Cluster(
  "couchbases://127.0.0.1/default" .
  "?truststorepath=$root/../etc/x509-cert/SSLCA/clientdir/trust.pem" .
  "&certpath=$root/../etc/x509-cert/SSLCA/clientdir/client.pem" .
  "&keypath=$root/../etc/x509-cert/SSLCA/clientdir/client.key"
);
$bucket = $cluster->openBucket('default');
