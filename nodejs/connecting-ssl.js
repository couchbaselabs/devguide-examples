// Require Couchbase Module
var couchbase = require('couchbase');

/*
 * Put Self-Signed Cluster Certificate from the cluster
 * to /tmp/couchbase-ssl-certificate.pem:
 *
 * $ curl http://localhost:8091/pools/default/certificate > /tmp/couchbase-ssl-certificate.pem
 */

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster('couchbase://127.0.0.1?certpath=/tmp/couchbase-ssl-certificate.pem');

// Authenticate with the cluster
cluster.authenticate('Administrator', 'password');

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

console.log('Example Successful - Exiting');
process.exit(0);
