<?php

/**
 * Query all airports for a given city
 * @param \Couchbase\Bucket $bkt bucket to use
 * @param string $city City to search for
 * @return array Rows for each city
 */
function query_city($bkt, $city) {
    $query = \Couchbase\N1qlQuery::fromString('SELECT airportname FROM `travel-sample` WHERE city=$1 AND type="airport"');
    $query->options['args'] = array($city);
    // The following optimizes the query for repeated invocation. The query string
    // (i.e. the one in fromString) is converted to an optimized form at the
    // server and returned to the SDK. Internally the SDK will re-use this optimized form
    // for future queries if the adhoc flag is set to false
    $query->adhoc(false);
    return $bkt->query($query);
}

$cluster = new \Couchbase\Cluster('couchbase://localhost');
$bucket = $cluster->openBucket('travel-sample');

echo "Airports in Reno:\n";
var_dump(query_city($bucket, "Reno"));

echo "Airports in Dallas:\n";
var_dump(query_city($bucket, "Dallas"));

echo "Aiports in Los Angeles\n";
var_dump(query_city($bucket, "Los Angeles"));