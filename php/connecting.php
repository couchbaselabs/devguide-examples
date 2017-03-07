<?php

$cluster =  new \Couchbase\Cluster('couchbase://localhost');
$bucket = $cluster->openBucket('default');
$protectedBucket = $cluster->openBucket('protected', 's3cr3t');