/*
 * Copyright (c) 2020 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.devguide;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import org.apache.log4j.Logger;

import java.time.Duration;

public class ConnectionBase {

    protected static final Logger LOGGER = Logger.getLogger("devguide");

    protected final Cluster cluster;
    protected final Bucket bucket;
    protected final Scope scope;
    protected final Collection collection;
    protected final Scope namedScope;
    protected final Collection namedCollection;

    //=== EDIT THESE TO ADAPT TO YOUR COUCHBASE INSTALLATION ===
    public static final String bucketName = "default";
    public static final String scopeName = "scope-name";
    public static final String collectionName = "collection-name";
    public static final String userName = "Administrator";
    public static final String userPass = "password";
    public static final String seedNode = "127.0.0.1";

    public ConnectionBase() {
        // connect deferred to the cluster by hitting one of the given nodes
        cluster = Cluster.connect(seedNode, userName, userPass);
        // get a Bucket reference from the cluster to the configured bucket
        bucket = cluster.bucket(bucketName);
        // reference the scope and collection
        scope = bucket.defaultScope();
        collection = bucket.defaultCollection();
        namedScope = bucket.scope(scopeName);
        namedCollection = scope.collection(collectionName);
        bucket.waitUntilReady(Duration.ofSeconds(30));
    }

    private void disconnect() {
        //release shared resources and close all open buckets
        cluster.disconnect();
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
        new ConnectionBase().execute();
    }
}
