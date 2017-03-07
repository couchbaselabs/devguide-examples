<?php

/*
 * This sample assumes that the bucket has configured replication
 */

$cluster =  new \Couchbase\Cluster('couchbase://localhost');
$bucket = $cluster->openBucket('default');

/*
 * In the PHP SDK you can specify "maximum" persistence and
 * replication by specifying -1 for either valie
 */
$bucket->upsert('docid', ['some' => 'value'], ['persist_to' => -1, 'replicate_to' => -1]);

// Store with persisting to master node
$bucket->upsert('docid', ['some' => 'value'], ['persist_to' => 1]);

// Note, this will fail if there are no replicas online
$bucket->upsert('docid', ['some' => 'value'], ['persist_to' => 1, 'replicate_to' => 1]);
