<?php
/**
 * The following example demonstrates what happens with CAS value of the document,
 * and how it can be manipulated.
 */

/*
 * Create a new Cluster object to represent the connection to our
 * cluster and specify any needed options such as SSL.
 */
$cluster = new CouchbaseCluster('couchbase://localhost');
/*
 * We open the default bucket to store our docuemtns in.
 */
$bucket = $cluster->openBucket('default');

/*
 * Now insert a document with id 'foo'
 */
$foo1 = $bucket->upsert('foo', array('val' => 1));
/*
 * The CAS value encoded as opaque string, i.e. you are free to pass it around,
 * but shouldn't try to interpret it or perform any operations (e.g. encoding
 * conversion or other string-related functions).
 */
assert('is_string($foo1->cas)');

try {
    /*
     * Now lets see what happens if we use incorrect CAS with replace operation.
     */
    $bucket->replace('foo', array('val' => 2), array('cas' => 'fakecas'));
} catch (CouchbaseException $ex) {
    /*
     * As expected it throws exception telling about CAS mismatch.
     */
    assert('preg_match("/CAS value different than specified/", $ex->getMessage())');
}
/*
 * But works, if we specify correct CAS (the actual value associated with it on server)
 */
$bucket->replace('foo', array('val' => 2), array('cas' => $foo1->cas));

/*
 * Each document modification also updates CAS, and the code below proves it.
 */
$foo2 = $bucket->get('foo');
assert('is_string($foo2->cas)');
assert('$foo1->cas != $foo2->cas');