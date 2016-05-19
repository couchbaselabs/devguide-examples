<?php

$cluster = new CouchbaseCluster('couchbases://localhost?');
$bucket = $cluster->openBucket('default');
