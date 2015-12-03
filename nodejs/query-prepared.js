// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster('couchbase://127.0.0.1');

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

// Setup Query
var N1qlQuery = couchbase.N1qlQuery;

// Make a N1QL specific Query, telling Couchbase this will be a prepared statement
var query = N1qlQuery.fromString("SELECT airportname, city, country FROM `travel-sample` " +
    "WHERE type=$1 AND city=$2").adhoc(false);

// Issue Query with parameters passed in array, running as a prepared statement
bucket.query(query,["airport","London"],function(err,result){
    if (err) throw err;

    // Print Results
    console.log("Result:",result);

    // Try a different query, using prepared
    bucket.query(query,["airport","Reno"],function(err,result){
        if(err) throw err;

        // Print Results
        console.log("Result:",result);
        console.log('Example Successful - Exiting');
        process.exit(0);
    });
});
