<?php

$cluster = new \Couchbase\Cluster("couchbase://localhost");
$bucket = $cluster->openBucket("default");

// This always works!
echo "Upserting\n";
$bucket->upsert('docid', array('property' => 'value'));
echo "Getting item back...";
echo "Value is: \n";
print_r($bucket->get('docid')->value);
echo "\n....\n";

echo "Inserting..\n";
echo "Will try to insert the document...\n";
echo "Should fail because the item already exists..\n";
try {
    $bucket->insert('docid', array('property' => 'value'));
    throw new Exception("Shouldn't reach here!");
} catch (\Couchbase\Exception $ex) {
    printf("Got error: Code=0x%x, Message=%s\n", $ex->getCode(), $ex->getMessage());
}
echo "...\n";

echo "Replacing the document. This should work because the item already exists";
$bucket->replace('docid', array("property" => "new_value"));
echo "Getting document again. Should contain the new contents:\n";
print_r($bucket->get('docid')->value);
echo "\n...\n";

echo "Removing document";
$bucket->remove('docid');
echo "Replacing document again. Should fail because document no longer exists\n";

try {
    $bucket->replace("docid", array("property" => "new_value"));
} catch (\Couchbase\Exception $ex) {
    printf("Got error %s\n", $ex->getMessage());
}