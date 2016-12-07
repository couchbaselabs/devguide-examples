<?php
$cluster = new CouchbaseCluster('couchbase://localhost');
$bucket = $cluster->openBucket('travel-sample');

$result = $bucket->query(CouchbaseN1qlQuery::fromString(
    'SELECT airportname, city, country FROM `travel-sample` ' .
    'WHERE type="airport" AND city="Reno"'
));
var_dump($result->rows);