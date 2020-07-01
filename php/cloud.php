<?php
######## Update this to your cluster
$endpoint = "cb.e207a530-a469-492f-89d4-a5392e265c10.dp.cloud.couchbase.com";
$username = "user";
$password = "password";
$bucketName = "couchbasecloudbucket";
#### User Input ends here.

var_dump("couchbases://$endpoint?ssl=no_verify");
// Initialize the Connection
$myCluster = new CouchbaseCluster("couchbases://$endpoint?ssl=no_verify");
$authenticator = new \Couchbase\PasswordAuthenticator();
$authenticator->username($username)->password($password);
$myCluster->authenticate($authenticator);
$myBucket = $myCluster->openBucket($bucketName);

// Create a N1QL Primary Index (but ignore if it exists)
$myBucket->manager()->createN1qlPrimaryIndex("", true, false);

// Store a Document
$result = $myBucket->upsert("u:king_arthur", array(
    "name" => "Arthur",
    "email" => "kingarthur@couchbase.com",
    "interests" => array("Holy Grail", "African Swallows")
));

# Load the Document and print it
$result = $myBucket->get("u:king_arthur");
var_dump($result->value);

# Perform a N1QL Query
$query = CouchbaseN1qlQuery::fromString("SELECT * FROM `$bucketName` WHERE \$1 IN interests");
$query->positionalParams(array("African Swallows"));

# Print each found Row
$rows = $myBucket->query($query);
echo "Results:\n";
var_dump($rows);