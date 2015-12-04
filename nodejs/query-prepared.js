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

// Timing Variables to compare execution times
var t1,t2,t3,t4;

// QUERY 1 - PREPARE AND EXECUTE
// Issue Query with parameters passed in array, running as a prepared statement
bucket.query(query,["airport","London"],function(err,result,meta){
    if (err) throw err;

    // Print Results
    console.log("Result:",result);
    console.log("Time Query1:",meta.metrics.elapsedTime);
    t1=meta.metrics.elapsedTime;

    // QUERY 2 - EXECUTE THE IDENTICAL QUERY.
    // Try a the same query, again.  It should have a shorter elapsed time.
    bucket.query(query,["airport","London"],function(err,result,meta){
        if(err) throw err;

        // Print Results
        console.log("Result:",result);
        console.log("Time Query 2:",meta.metrics.elapsedTime);
        t2=meta.metrics.elapsedTime;

        // QUERY 3 - EXECUTE THE PLAN
        // Try the same query, with different parameters.  It should also be fast.
        bucket.query(query,["airport","Seattle"],function(err,result,meta) {
            if (err) throw err;

            // Print Results
            console.log("Result:", result);
            console.log("Time Query 3:", meta.metrics.elapsedTime);
            t3=meta.metrics.elapsedTime;

            // QUERY 4 - EXECUTE THE ORIGINAL.
            // Try the original query, again.
            bucket.query(query,["airport","London"],function(err,result,meta) {
                if (err) throw err;

                // Print Results
                console.log("Result:", result);
                console.log("Time Query 2:", meta.metrics.elapsedTime);
                t4 = meta.metrics.elapsedTime;

                console.log("=====");
                console.log("Query 1 - prepare, execute    :", t1);
                console.log("Query 2 - execute, same params:", t2);
                console.log("Query 3 - execute, new params :", t3);
                console.log("Query 4 - execute, og params  :", t4);
                console.log("=====");
                console.log('Example Successful - Exiting');
                process.exit(0);
            });
        });
    });
});
