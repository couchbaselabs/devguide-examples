// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster('couchbase://127.0.0.1');

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

// Setup Query
var N1qlQuery = couchbase.N1qlQuery;

// Make a N1QL specific Query to Create a Primary Index or Secondary Index
var query = N1qlQuery.fromString("CREATE PRIMARY INDEX ON `travel-sample`");

// Issue Query to Create the Index
bucket.query(query,function(err,result){
	if (err) throw err;

	// Print Results
	console.log("Result:",result);

  console.log('Example Successful - Exiting');
  process.exit(0);
});
