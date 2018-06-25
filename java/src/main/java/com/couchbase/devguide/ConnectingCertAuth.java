package com.couchbase.devguide;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

/**
 * This example shows how to connect to a couchbase server EE cluster using
 * client certificate authentication.
 *
 * Please make sure to follow the proper server side documentation on how to
 * import the certifactes into the server and set up the Java keystore.
 *
 * https://developer.couchbase.com/documentation/server/current/security/security-x509certsintro.html
 */
public class ConnectingCertAuth {

    public static void main(String... args) {
        CouchbaseEnvironment environment = DefaultCouchbaseEnvironment.builder()
            .sslEnabled(true)
            .certAuthEnabled(true)
            .sslKeystoreFile("/path/to/keystore")
            .sslKeystorePassword("password")
            .sslTruststoreFile("/path/to/truststore") // you can also pack it all in just the keystore
            .sslTruststorePassword("password")
            .build();

        Cluster cluster = CouchbaseCluster.create(environment, "127.0.0.1");
        // IMPORTANT: do NOT call cluster.authenticate() since this is part of the cert auth
        Bucket bucket = cluster.openBucket("travel-sample");

        // perform operations here...
        bucket.get("mydoc");
    }

}
