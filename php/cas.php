<?php

$cluster = new CouchbaseCluster('couchbase://localhost');
$bucket = $cluster->openBucket('default');

$bucket->upsert('a_list', array());

// NOTE:
// Adding items to lists in Couchbase 4.5 and higher is possible using the
// subdocument API which is far more efficient than the code described
// here. The code below should not be used in production if the subdocument
// API can be used. The code below exists only to demonstrate the functionality
// of CAS operations.

function add_to_couchbase_list($bkt, $docid, $newval) {
    // Limit the number of retries so we don't infinitely loop
    $retries_remaining = 10;
    do {
        try {
            // Try to get the initial document
            $metadoc = $bkt->get($docid);
        } catch (CouchbaseException $ex) {
            if ($ex->getCode() != COUCHBASE_KEY_ENOENT) {
                throw $ex;
            }
            echo "List $docid does not yet exist. Creating\n";
            try {
                // Insert it because it does not exist. However because several processes
                // are accessing the document concurrently, our insert might also fail
                // if another process has already created it.
                $bkt->insert($docid, array($newval));
                echo "List $docid created\n";
                // We don't need to append a new item because the list is already present.
                return true;
            } catch (CouchbaseException $ex) {
                if ($ex->getCode() != COUCHBASE_KEY_EEXISTS) {
                    throw $ex;
                }
                // If another process create the list, simply continue and attempt to add the element
                echo "List $docid already created by different process\n";
                $retries_remaining++;
                continue;
            }
        }

        $doc = $metadoc->value;
        array_push($doc, $newval);
        try {
            $bkt->replace($docid, $doc, array("cas" => $metadoc->cas));
            return true;
        } catch (CouchbaseException $ex) {
            if ($ex->getCode() != COUCHBASE_KEY_EEXISTS) {
                throw $ex;
            }
            printf("%d: CAS Mismatch (tried %s). Retrying (remaining=%d)\n", posix_getpid(), $metadoc->cas, $retries_remaining);
        }
    } while (--$retries_remaining);
    throw new Exception("Couldn't add item!");
}

add_to_couchbase_list($bucket, 'a_list', posix_getpid());

//mnunberg@mbp15II ~/Source/devguide-examples/php $
//62531: CAS Mismatch (tried 2brb7ujzls). Retrying (remaining=10)
//62525: CAS Mismatch (tried m6i0rsoao). Retrying (remaining=10)
//62529: CAS Mismatch (tried nclciatc0). Retrying (remaining=10)
//62533: CAS Mismatch (tried nclciatc0). Retrying (remaining=10)
//62525: CAS Mismatch (tried nclciatc0). Retrying (remaining=9)
//62535: CAS Mismatch (tried nclciatc0). Retrying (remaining=10)
//62529: CAS Mismatch (tried tl333nk74). Retrying (remaining=9)
//62535: CAS Mismatch (tried tl333nk74). Retrying (remaining=9)
//62525: CAS Mismatch (tried tl333nk74). Retrying (remaining=8)
//62535: CAS Mismatch (tried wpbyebxmo). Retrying (remaining=8)
//62525: CAS Mismatch (tried wpbyebxmo). Retrying (remaining=7)
//62525: CAS Mismatch (tried ztktp0b28). Retrying (remaining=6)
