<?php

$cluster =  new \Couchbase\Cluster('couchbase://localhost');
$cluster->authenticateAs('Administrator', 'password');
$bucket = $cluster->openBucket('default');
$protectedBucket = $cluster->openBucket('protected');
