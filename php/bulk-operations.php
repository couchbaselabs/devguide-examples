<?php
$cluster = new CouchbaseCluster('couchbase://localhost');
$bucket = $cluster->openBucket('default');

$results = $bucket->upsert(array(
    'foo' => array('value' => array('email' => 'foo@foo.com')),
    'bar' => array('value' => array('email' => 'bar@bar.com')),
    'baz' => array('value' => array('email' => 'baz@baz.com'))
));
print_r($results);

// Get them back again
$results = $bucket->get(array('foo', 'bar', 'baz'));
foreach ($results as $docid => $metadoc) {
    // Each document itself has a 'propname'
    echo "Result for $docid\n";
    var_dump($metadoc->value);
    echo "\n";
}