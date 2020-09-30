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

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import com.couchbase.client.core.error.DurabilityImpossibleException;
import com.couchbase.client.core.error.ReplicaNotAvailableException;
import com.couchbase.client.core.error.ReplicaNotConfiguredException;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.PersistTo;
import com.couchbase.client.java.kv.ReplicateTo;
import com.couchbase.client.java.kv.UpsertOptions;

/**
 * Example of Durability in Java for the Couchbase Developer Guide.
 * TODO: not tested
 */
public class Durability extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleDurability";
        JsonObject doc = JsonObject.create().put("value","a String is valid JSON");

        LOGGER.info("Storing with maximum factor");
        // In the Java SDK you must specify a factor matching the number of replicas you have configured
        // if you want "maximum" persistence or replication
        // Here we expect 3 replicas configured so we can wait for persistence on 4 nodes total, replication on 3 replicas.
        try {
            bucket.defaultCollection().upsert( key, doc, UpsertOptions.upsertOptions().durability(PersistTo.FOUR,
                ReplicateTo.THREE));
        } catch (Exception e) { //if the durability cannot be met
            if (e instanceof ReplicaNotConfiguredException) {
                //this exception is a fail fast if not enough replicas are configured on the bucket
                LOGGER.info("Couldn't persist to FOUR nor replicate to THREE, not enough replicas configured");
            } else if (e instanceof ReplicaNotAvailableException) {
                //this exception occurs if enough replica are configured on the bucket but currently not enough are online
                //eg. during a failover
                LOGGER.info("Couldn't persist/replicate on 1 replica, not enough replicas online");
            } else {
                LOGGER.error("Durability Exception", e);
            }
        }

        // Store with persisting to master node
        LOGGER.info("Storing with waiting for persistence on MASTER");
        bucket.defaultCollection().upsert( key, doc, UpsertOptions.upsertOptions().durability(PersistTo.ACTIVE, ReplicateTo.NONE));

        LOGGER.info("Storing with waiting for persistence on any two nodes, replication on one replica node");
        try {
            bucket.defaultCollection().upsert( key, doc, UpsertOptions.upsertOptions().durability(PersistTo.TWO, ReplicateTo.ONE).timeout(Duration.ofSeconds(1)));
        } catch (DurabilityImpossibleException e) {
            //if the durability cannot be met (eg. if the cluster detected that the replica wasn't available due to failover)
            LOGGER.error("Durability Exception", e);
        } catch (RuntimeException e) {
            System.out.println(e);
            if (e.getCause() instanceof TimeoutException) {
                //if one of the nodes isn't responsive, a TimeoutException rather than DurabilityException may occur
                LOGGER.warn("The replica didn't notify us in time");
            }
        }
    }

    public static void main(String[] args) {
        new Durability().execute();
    }
}
