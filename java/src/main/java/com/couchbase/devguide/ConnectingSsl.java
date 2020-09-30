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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.env.ClusterEnvironment;

import org.apache.log4j.Logger;

public class ConnectingSsl {

    protected static final Logger LOGGER = Logger.getLogger("devguide");
    protected final Cluster cluster;
    protected final Bucket bucket;
    protected final ClusterEnvironment env;

    //=== EDIT THESE TO ADAPT TO YOUR COUCHBASE INSTALLATION ===
    public static final String bucketName = "default";
    static String connectstring = "127.0.0.1";
    static String username="Administrator";
    static String password="password";

    //=== You need to correctly set up your JVM keystore first! ===
    //see instructions in http://developer.couchbase.com/documentation/server/4.0/sdks/java-2.2/managing-connections.html#story-h2-5

    protected ConnectingSsl() {
        //configure the SDK to use SSL and point it to the keystore

        env = ClusterEnvironment.builder()
                .securityConfig(SecurityConfig.enableTls(true)
                    .trustStore(Paths.get("/path/tokeystore"),"password", Optional.empty()))
                .build();

        //connect to the cluster using the SSL configuration, by hitting one of the given nodes
        cluster = Cluster.connect(connectstring,
            ClusterOptions.clusterOptions(username, password).environment(env));

        //get a Bucket reference from the cluster to the configured bucket
        bucket = cluster.bucket(bucketName);
    }

    private void disconnect() {
        //release shared resources and close all open buckets
        cluster.disconnect();

        //also release the environment since we created it ourselves (notice this is an async operation so we block on it)
        env.shutdownAsync();
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
