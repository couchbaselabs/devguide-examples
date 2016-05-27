<?php
/**
 * The following example demonstrates how you might implement caching
 * for HTTP requests that are being made.  In this case, we make requests
 * to the GitHub api to retrieve couchbaselabs events and cache the result
 * of this request for 5 seconds to reduce the load on GitHub.
 */

/*
 * Create a new Cluster object to represent the connection to our
 * cluster and specify any needed options such as SSL.
 */
$cb = new CouchbaseCluster('couchbase://localhost');

/*
 * We open the default bucket to store our cached data in.
 */
$db = $cb->openBucket('default');


/*
 * Lets define a function which can wrap executing the curl request and
 * handling the cache in one.
 */
function cached_curl_request($path) {
    global $db;

    /*
     * Lets define a key to use for storing out cached request into.  We
     * base this key directly on the path so that we can support caching
     * many different URIs.  We also add a prefix for good measure.
     */
    $cache_key = 'cache_' . $path;

    /*
     * First we make an attempt to retrieve the data from our cache key,
     * if the request fails, an exception will be thrown and we will perform
     * some additional logic rather than returning the value directly.
     */
    try {
        return $db->get($cache_key);
    } catch (Exception $e) {
    }

    /*
     * Since our attempts to retrieve the data we want from our cache failed,
     * we will have to perform the request ourselves.  The following blurb
     * simply executes an HTTP request and stores the result (in json decoded
     * format) into $result.
     */
    $curl = curl_init();
    curl_setopt_array($curl, array(
        CURLOPT_RETURNTRANSFER => 1,
        CURLOPT_USERAGENT => 'Couchbase-Example-App',
        CURLOPT_URL => $path
    ));
    $result = json_decode(curl_exec($curl));
    curl_close($curl);

    /*
     * Now that we've managed to execute the request, we wil attempt to store
     * this data onto Couchbase Server for the next request to read rather than
     * performing another HTTP request.  We perform an insert or update store
     * store the data into our cache key document, we ignore any failures that
     * occur since we already have performed the request and are just caching.
     */
    try {
        $db->upsert('cache_' . $path, $result, array('expiry'=>5));
    } catch (Exception $e) {
    // Ignore errors, since we are only caching data anyways.
    }

    /*
     * Return the result we retrieved earlier
     */
    return $result;
}

/*
 * Lets set up a URI to test on.
 */
$testUri = 'https://api.github.com/users/couchbaselabs/events';

/*
 * Now we use our caching request function to perform the request and dumb
 * the results to the page or console.
 */
$pageval = cached_curl_request($testUri);
var_dump($pageval);
