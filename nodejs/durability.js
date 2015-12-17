// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster('couchbase://127.0.0.1');

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');
//bucket.durabilityTimeout=10;

// Setup a Document and store in the bucket.
var key = "nodeDevguideExampleDurability";

persistExample(function(persistComplete){
    if(persistComplete){
        replicateExample(function(replicateComplete){
            if(replicateComplete){
                replicateAndPersistExample(function(replicateAndPersistComplete){
                    if(replicateAndPersistComplete) {
                        console.log('Example Successful - Exiting');
                        process.exit(0);
                    }
                });
            }
        });
    }
});

function persistExample(done) {

    // Create a document and assign a "Persist To" value of 1 node.
    // Should Always Succeed, even on single node cluster.
    console.log("==========================================");
    console.log("  BEGIN EXAMPLE: Persist To 1 node");
    bucket.durabilityInterval=1000;
    bucket.upsert(key, {test: "Some Test Value"}, {persist_to: 1}, function (err, res) {
        if (err) throw err;

        if (res) {
            console.log("    CALLBACK: RESULT", res);
            console.log("    Initialized Document, stored to bucket");

            // Get Document
            bucket.get(key, function (err, resRead) {
                if (err) throw err;

                // Print Document Value
                console.log("    Retrieved Document:", resRead.value);
                console.log("  END EXAMPLE");
                console.log("==========================================");
                done(true);
            });
        }
    });
}

function replicateExample(done) {

    // Create a document and assign a "Replicate To" value of 1 node.
    // Should Fail on a single node cluster, succeed on a multi node
    // cluster of 3 or more nodes with at least one replica enabled.
    console.log("==========================================");
    console.log("  BEGIN EXAMPLE: Replicate To 1 node");
    bucket.durabilityInterval=1000;
    bucket.upsert(key, {test: "Some Test Value"}, {replicate_to: 1}, function (err, res) {
        if (err) throw err;

        if (res) {
            console.log("    CALLBACK: RESULT", res);
            console.log("    Initialized Document, stored to bucket");

            // Get Document
            bucket.get(key, function (err, resRead) {
                if (err) throw err;

                // Print Document Value
                console.log("    Retrieved Document:", resRead.value);
                console.log("  END EXAMPLE");
                console.log("==========================================");
                done(true);
            });
        }
    });
}

function replicateAndPersistExample(done) {

    // Create a document and assign a "Replicate To" and a "Persist TO"
    // value of 1 node. Should Fail on a single node cluster, succeed on
    // a multi node cluster of 3 or more nodes with at least one replica
    // enabled.
    console.log("==========================================");
    console.log("  BEGIN EXAMPLE: Replicate To and Persist To 1 node");
    bucket.durabilityInterval=1000;
    bucket.upsert(key, {test: "Some Test Value"}, {replicate_to: 1, persist_to:1}, function (err, res) {
        if (err) throw err;

        if (res) {
            console.log("    CALLBACK: RESULT", res);
            console.log("    Initialized Document, stored to bucket");

            // Get Document
            bucket.get(key, function (err, resRead) {
                if (err) throw err;

                // Print Document Value
                console.log("    Retrieved Document:", resRead.value);
                console.log("  END EXAMPLE");
                console.log("==========================================");
                done(true);
            });
        }
    });
}

