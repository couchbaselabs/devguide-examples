<?php
$cluster = new CouchbaseCluster('couchbase://localhost');
$bucket = $cluster->openBucket('default');

// Remove the document first so we can have predictable behavior
try {
    $bucket->remove('docid');
} catch (CouchbaseException $ex) {
}

$result = $bucket->counter('docid', 20, array('initial' => 100));
echo 'Delta=20, Initial=100. Current value is: ' . $result->value . "\n";

$result = $bucket->counter('docid', 1);
echo 'Delta=1. Current value is: ' . $result->value . "\n";

$result = $bucket->counter('docid', -50);
echo 'Delta=-50. Current value is: ' . $result->value . "\n";