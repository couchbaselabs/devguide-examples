// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster(
    'couchbases://127.0.0.1/' +
    '?truststorepath=../etc/x509-cert/SSLCA/clientdir/trust.pem' +
    '&certpath=../etc/x509-cert/SSLCA/clientdir/client.pem' +
    '&keypath=../etc/x509-cert/SSLCA/clientdir/client.key');

// Authenticate with the cluster
cluster.authenticate(new couchbase.CertAuthenticator());

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

console.log('Example Successful - Exiting');
process.exit(0);
