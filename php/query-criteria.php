<?php
$cluster = new \Couchbase\Cluster('couchbase://localhost');
$bucket = $cluster->openBucket('travel-sample');

$result = $bucket->query(\Couchbase\N1qlQuery::fromString(
    'SELECT airportname, city, country FROM `travel-sample` ' .
    'WHERE type="airport" AND city="Reno"'
));
var_dump($result->rows);