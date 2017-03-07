<?php

$cluster = new \Couchbase\Cluster('couchbase://192.168.1.194');
$bucket = $cluster->openBucket('default');

echo "Storing with an expiration of 2 seconds\n";
$bucket->upsert('docid', 'value', array('expiry' => 2));

echo "Getting item back immediately... " . $bucket->get('docid')->value . "\n";

echo "Sleeping for 4 seconds...\n";
sleep(4);

echo "Getting item back again\n";
try {
    $bucket->get('docid');
} catch (\Couchbase\Exception $ex) {
    printf("Failed: %s\n", $ex->getMessage());
}

echo "Storing item again (without expiry)\n";
$bucket->upsert('docid', 'value');

echo "Using getAndTouch to retrieve key and modify expiry";
$metadoc = $bucket->getAndTouch('docid', 2);
printf("Current value is %s\n", $metadoc->value);

echo "Sleeping for 4 seconds again\n";
sleep(4);

echo "Getting key again (should fail)\n";
try {
    $bucket->get('docid');
} catch (\Couchbase\Exception $ex) {
    printf("Failed with %s\n", $ex->getMessage());
}

echo "Storing key again\n";
$bucket->upsert('docid', 'someValue');

echo "Using touch (without get). Setting expiry for 1 second\n";
$bucket->touch('docid', 1);

echo "Sleeping for 4 seconds\n";
sleep(4);

echo "Will try to get item again...\n";
try {
    $bucket->get('docid');
} catch (\Couchbase\Exception $ex) {
    printf("Failed with %s\n", $ex->getMessage());
}