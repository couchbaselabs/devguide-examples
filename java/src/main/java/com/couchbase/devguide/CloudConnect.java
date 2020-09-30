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

import com.couchbase.client.core.deps.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.query.QueryResult;
import static com.couchbase.client.java.query.QueryOptions.queryOptions;

import java.time.Duration;

public class CloudConnect {
    public static void main(String... args) {
        // Update this to your cluster
        String endpoint = "cb.<your endpoint address>.dp.cloud.couchbase.com";
        String bucketName = "couchbasecloudbucket";
        String username = "user";
        String password = "password";
        // User Input ends here.

        ClusterEnvironment env = ClusterEnvironment.builder()
                .securityConfig(SecurityConfig.enableTls(true)
                        .trustManagerFactory(InsecureTrustManagerFactory.INSTANCE))
                .ioConfig(IoConfig.enableDnsSrv(true))
                .build();
        // env = ClusterEnvironment.builder().build();

        // Initialize the Connection
        Cluster cluster = Cluster.connect(endpoint,
                ClusterOptions.clusterOptions(username, password).environment(env));
        Bucket bucket = cluster.bucket(bucketName);
        bucket.waitUntilReady(Duration.parse("PT10S"));
        Collection collection = bucket.defaultCollection();

        cluster.queryIndexes().createPrimaryIndex(bucketName, CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions().ignoreIfExists(true));

        // Create a JSON Document
        JsonObject arthur = JsonObject.create()
                .put("name", "Arthur")
                .put("email", "kingarthur@couchbase.com")
                .put("interests", JsonArray.from("Holy Grail", "African Swallows"));

        // Store the Document
        collection.upsert(
                "u:king_arthur",
                arthur
        );

        // Load the Document and print it
        // Prints Content and Metadata of the stored Document
        System.out.println(collection.get("u:king_arthur"));

        // Perform a N1QL Query
        QueryResult result = cluster.query(
                String.format("SELECT name FROM `%s` WHERE $1 IN interests", bucketName),
                queryOptions().parameters(JsonArray.from("African Swallows"))
        );

        // Print each found Row
        for (JsonObject row : result.rowsAsObject()) {
            System.out.println(row);
        }
    }
}
