// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster('couchbase://192.168.99.100');

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

// Setup a new Document and store in the bucket
var key = "nodeDevguideExampleSubdoc";
bucket.upsert(key, {
    firstItem: "Some Test Field Data for firstItem",
    secondItem: "Some Test Field Data for secondItem",
    thirdItem: "Some Test Field Data for thirdItem"
}, function (err, res) {
    if (err) throw err;

    console.log('Initialized document, stored to bucket...');

    // Get Document
    bucket.get(key, function (err, resReadFullDoc1) {
        if (err) throw err;

        // Print Document Value
        console.log("Retrieve full document:", resReadFullDoc1.value, "\n", "\n");

        // Subdoc Operation: Retrieve Document Fragment for two fields, using
        //   LookupIn
        bucket.lookupIn("nodeDevguideExampleSubdoc").
        get("secondItem").get("thirdItem").
        execute(function (err, resSubdocOp1) {
            if (err) throw err;


            // Print the values
            console.log("Retrieve just second and third items:\n", resSubdocOp1.contents, "\n", "\n");

            // Subdoc Operation: Add a fourth item that is an array
            console.log("Add array to the fourth item of the document...");
            bucket.mutateIn("nodeDevguideExampleSubdoc", 0, 0).
            upsert("fourthItem", ["250 GT SWB", "250 GTO", "250 LM", "275 GTB"], true).
            execute(function (err, resSubdocOp2) {
                if (err) throw err;

                // Retrieve Full Document
                bucket.get("nodeDevguideExampleSubdoc", function (err, resReadFullDoc2) {
                    if (err) throw err;

                    // Print the values
                    console.log("Retrieve full document with array added to fourth item:\n", resReadFullDoc2.value, "\n", "\n");

                    // Subdoc Operation: Add a value to a specific position, to the "front"
                    //   of the fourthItem Array, to the "back" of the fourthItem Array, and
                    //   another unique value to the back of the Array in one operation
                    console.log("Add a value to a specific position, to the 'front' " +
                        "of the fourthItem Array, to the 'back' of the fourthItem Array, and " +
                        "another unique value to the back of the Array in one operation...");
                    bucket.mutateIn("nodeDevguideExampleSubdoc", 0, 0).
                    arrayInsert("fourthItem[2]", "250 GTO Series II").
                    pushFront("fourthItem", "250 GT Lusso", false).
                    pushBack("fourthItem", "275 GTB/4", false).
                    addUnique("fourthItem", "288 GTO", false).
                    execute(function (err, resSubdocOp3) {
                        if (err) throw err;

                        // LookupIn to retrieve the changes
                        bucket.lookupIn("nodeDevguideExampleSubdoc").
                        get("fourthItem").
                        execute(function (err, resSubdocOp4) {
                            if (err) throw err;

                            // Print the values
                            console.log("Retrieve modified fourth item:", resSubdocOp4.contents, "\n", "\n");

                            // Subdoc Operation: Remove a value from the fourthItem Array
                            console.log("Remove item at position three in fourth item array...")
                            bucket.mutateIn("nodeDevguideExampleSubdoc", 0, 0).
                            remove("fourthItem[3]").execute(function (err, resSubdocOp5) {
                                if (err) throw err;

                                // Subdoc Operation: Retrieve the revised fourth item
                                bucket.lookupIn("nodeDevguideExampleSubdoc").
                                get("fourthItem").
                                execute(function (err, resSubdocOp6) {
                                    if (err) throw err;

                                    console.log("Retrieved fourth item with array item removed:", resSubdocOp6.contents, "\n", "\n");

                                    console.log('Example Successful - Exiting');
                                    process.exit(0);
                                });
                            });
                        });
                    });
                });
            });
        });
    });
});
