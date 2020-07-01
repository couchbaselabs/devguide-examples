var couchbase = require('couchbase');

var N1qlQuery = couchbase.N1qlQuery;

// Update this to your cluster
const endpoint = 'cb.e207a530-a469-492f-89d4-a5392e265c10.dp.cloud.couchbase.com'
const username = 'user'
const password = 'password'
const bucketName = 'couchbasecloudbucket'
// User Input ends here.

// Initialize the Connection
var cluster = new couchbase.Cluster('couchbases://' +endpoint+'?ssl=no_verify', {username: username, password: password});
var bucket = cluster.openBucket(bucketName);

// Create a N1QL Primary Index (but ignore if it exists)
bucket.manager().createPrimaryIndex({ignoreExists: true}, function() {
    // Create and store a document
    bucket.upsert('user:king_arthur', {
        'name': 'Arthur', 'email': 'kingarthur@couchbase.com', 'interests': ['Holy Grail', 'African Swallows']
    },
    function (err, result) {
        // Load the Document and print it
        // Prints Content and Metadata of the stored Document
        bucket.get('user:king_arthur', function (err, result) {
            console.log('Got result: %j', result.value);

            // Perform a N1QL Query
            bucket.query(
                N1qlQuery.fromString('SELECT name FROM '+ bucketName + ' WHERE $1 in interests LIMIT 1'),
                ['African Swallows'],
                function (err, rows) {
                    // Print the result
                    console.log('Got rows: %j', rows);
                });
        });
    });
});
