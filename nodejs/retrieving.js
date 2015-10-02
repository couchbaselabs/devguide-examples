// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster('couchbase://127.0.0.1');

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

// Setup a Document and store in the bucket.
var key = "nodeDevguideExampleRetrieve";
bucket.insert(key, {test:"Some Test Value"},function(err, res) {
    if (err) throw err;

    console.log('Initialized Document, stored to bucket');

    // Get Document
    bucket.get(key, function (err, resRead) {
        if (err) throw err;

        // Print Document Value
        console.log("Retrieved Document:", resRead.value);

        console.log('Example Successful - Exiting');
        process.exit(0);
    });
});
