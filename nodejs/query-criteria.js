// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster('couchbase://127.0.0.1');

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

// Setup Query
var N1qlQuery = couchbase.N1qlQuery;

// Make a N1QL specific Query
var query = N1qlQuery.fromString("SELECT airportname, city, country FROM `travel-sample` " +
	"WHERE type='airport' AND city='Reno' ");

// Issue Query
bucket.query(query,function(err,result){
	if (err) throw err;

	// Print Results
	console.log("Result:",result);

  console.log('Example Successful - Exiting');
  process.exit(0);
});
