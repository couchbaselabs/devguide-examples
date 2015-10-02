// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster('couchbase://127.0.0.1');

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

// Setup a new Document and store in the bucket
var key = "nodeDevguideExampleReplace";
bucket.insert(key, {test:"Some Test Value"},function(err, res) {
    if (err) throw err;

    console.log('Initialized Document, stored to bucket');

    // Get Document
    bucket.get(key, function (err, resRead) {
        if (err) throw err;

        // Print Document Value
        console.log("Retrieved Document:", resRead.value);

				// Add to value, and replace
				resRead.value.test2='Some More Test Values';
				var updatedVal=JSON.stringify(resRead.value);
				bucket.replace(key,updatedVal,function(req,resUpdated){
					if (err) throw err;

					// Get Replaced Document Value
					bucket.get(key, function (err, resReadUpdated) {
							if (err) throw err;

							// Print Document Value
							console.log("Retrieved Document:", resReadUpdated.value);

			        console.log('Example Successful - Exiting');
			        process.exit(0);
						});
				});
    });
});
