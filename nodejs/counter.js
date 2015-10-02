// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster('couchbase://127.0.0.1');

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

// Setup a new key, initialize as 10, add 2, and retreive it
var key = "nodeDevguideExampleCounter";
bucket.counter(key, 2, {initial: 10}, function(err, res) {
  if (err) throw err;

  console.log('Initialized Counter:', res.value);

  bucket.counter(key, 2, {initial: 10}, function(err, res) {
    if (err) throw err;

    console.log('Incremented Counter:', res.value);

    console.log('Example Successful - Exiting');
    process.exit(0);
  });
});
