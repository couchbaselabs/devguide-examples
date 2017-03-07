<?php

// This example depends on php-posix module because of posix_getpid()

$cluster = new \Couchbase\Cluster('couchbase://localhost');
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
        } catch (\Couchbase\Exception $ex) {
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
            } catch (\Couchbase\Exception $ex) {
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
        } catch (\Couchbase\Exception $ex) {
            if ($ex->getCode() != COUCHBASE_KEY_EEXISTS) {
                throw $ex;
            }
            printf("%d: CAS Mismatch (tried %s). Retrying (remaining=%d)\n", posix_getpid(), $metadoc->cas, $retries_remaining);
        }
    } while (--$retries_remaining);
    throw new Exception("Couldn't add item!");
}

add_to_couchbase_list($bucket, 'a_list', posix_getpid());

// $ seq 1 10 | parallel php -d extension=couchbase.so php/cas.php
// 97885: CAS Mismatch (tried bb82yqvebj7k). Retrying (remaining=10)
// 97889: CAS Mismatch (tried bb82yqve5wxs). Retrying (remaining=10)
// 97889: CAS Mismatch (tried bb82yqvg2qrk). Retrying (remaining=9)
// 97883: CAS Mismatch (tried bb82yqvea4n4). Retrying (remaining=10)
// 97888: CAS Mismatch (tried bb82yqvebj7k). Retrying (remaining=10)
// 97888: CAS Mismatch (tried bb82yqvh62o0). Retrying (remaining=9)
// 97886: CAS Mismatch (tried bb82yqvebj7k). Retrying (remaining=10)
// 97886: CAS Mismatch (tried bb82yqvg2qrk). Retrying (remaining=9)
// 97886: CAS Mismatch (tried bb82yqvhzklc). Retrying (remaining=8)
// 97890: CAS Mismatch (tried bb82yqvf0tfk). Retrying (remaining=10)
// 97890: CAS Mismatch (tried bb82yqvh62o0). Retrying (remaining=9)
// 97890: CAS Mismatch (tried bb82yqvixa80). Retrying (remaining=8)
