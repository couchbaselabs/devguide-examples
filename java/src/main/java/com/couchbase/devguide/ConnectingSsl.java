package com.couchbase.devguide;

import java.util.Arrays;
import java.util.List;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import org.apache.log4j.Logger;

public class ConnectingSsl {

    protected static final Logger LOGGER = Logger.getLogger("devguide");

    protected final Cluster cluster;
    protected final Bucket bucket;
    protected final CouchbaseEnvironment env;

    //=== EDIT THESE TO ADAPT TO YOUR COUCHBASE INSTALLATION ===
    public static final String bucketName = "default";
    public static final String bucketPassword = "";
    public static final List<String> nodes = Arrays.asList("127.0.0.1");

    //=== You need to correctly set up your JVM keystore first! ===
    //see instructions in http://developer.couchbase.com/documentation/server/4.0/sdks/java-2.2/managing-connections.html#story-h2-5

    protected ConnectingSsl() {
        //configure the SDK to use SSL and point it to the keystore
        env = DefaultCouchbaseEnvironment.builder()
                .sslEnabled(true)
                .sslKeystoreFile("/path/tokeystore")
                .sslKeystorePassword("password")
        .build();

        //connect to the cluster using the SSL configuration, by hitting one of the given nodes
        cluster = CouchbaseCluster.create(env, nodes);

        //get a Bucket reference from the cluster to the configured bucket
        bucket = cluster.openBucket(bucketName, bucketPassword);
    }

    private void disconnect() {
        //release shared resources and close all open buckets
        cluster.disconnect();

        //also release the environment since we created it ourselves (notice this is an async operation so we block on it)
        env.shutdownAsync().toBlocking().first();
    }

    public void execute() {
        //connection has been done in the constructor
        doWork();
        disconnect();
    }

    /**
     * Override this method to showcase specific examples.
     * Make them executable by adding a main method calling new ExampleClass().execute();
     */
    protected void doWork() {
        //this one just showcases connection methods, see constructor and shutdown()
        LOGGER.info("Connected to the cluster, opened bucket " + bucketName);
    }

    public static void main(String[] args) {
        new ConnectingSsl().execute();
    }
}
