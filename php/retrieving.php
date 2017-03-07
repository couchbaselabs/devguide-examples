<?php
$cluster = new \Couchbase\Cluster("couchbase://localhost");
$bucket = $cluster->openBucket("default");

echo "Getting non-existent key. Should fail\n";
try {
    $bucket->get('non-exist-document');
} catch (\Couchbase\Exception $ex) {
    if ($ex->getCode() != COUCHBASE_KEY_ENOENT) {
        throw new Exception("GRRR");
    }
    printf("Error: %s (0x%x)\n", $ex->getMessage(), $ex->getCode());
}

echo "Upserting...\n";
$bucket->upsert("new_document", array("foo" => "bar"));
echo "Getting\n";
$result = $bucket->get("new_document");
var_dump($result);
echo "Foo is: " . $result->value->foo . "\n";
