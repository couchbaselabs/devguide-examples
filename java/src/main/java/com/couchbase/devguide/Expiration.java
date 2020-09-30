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

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.UpsertOptions;

import java.time.Duration;

/**
 * Example of Expiry/TTL in Java for the Couchbase Developer Guide.
 */
public class Expiration extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleExpiration";
        //create content
        JsonObject content = JsonObject.create().put("some", "value");

        LOGGER.info("Storing with an expiration of 2 seconds");
        bucket.defaultCollection().upsert(key, content, UpsertOptions.upsertOptions().expiry(Duration.ofSeconds(2)));

        LOGGER.info("Getting item back immediately");
        LOGGER.info(bucket.defaultCollection().get(key));

        LOGGER.info("Sleeping for 4 seconds...");
        sleepSeconds(4);
        LOGGER.info("Getting key again (should fail)");
        
        //get returns null if the key doesn't exist
        try {
            if (bucket.defaultCollection()
                .get(key) == null) {
                LOGGER.info("Get failed because item has expired");
            }
        } catch (DocumentNotFoundException dnf){
            LOGGER.info("Get failed because item has expired");
        }

        LOGGER.info("Storing item again (without expiry)");
        bucket.defaultCollection().upsert(key, content);

        LOGGER.info("Using get-and-touch to retrieve key and modify expiry");
        GetResult rv = bucket.defaultCollection().getAndTouch(key, Duration.ofSeconds(2));
        LOGGER.info("Value is:" + rv);

        LOGGER.info("Sleeping for 4 seconds again");
        sleepSeconds(4);
        LOGGER.info("Getting key again (should fail)");
        try {
            if (bucket.defaultCollection()
                .get(key) == null) {
                LOGGER.info("Get failed because item has expired");
            }
        } catch (DocumentNotFoundException dnf){
            LOGGER.info("Get failed because item has expired");
        }

        LOGGER.info("Storing key again...");
        bucket.defaultCollection().upsert(key, content);
        LOGGER.info("Using touch (without get). Setting expiry for 1 second");
        bucket.defaultCollection().touch(key, Duration.ofSeconds(1));

        LOGGER.info("Sleeping for 4 seconds...");
        sleepSeconds(4);
        LOGGER.info("Will try to get item again (should fail)");
        try {
            if (bucket.defaultCollection()
                .get(key) == null) {
                LOGGER.info("Get failed because item has expired");
            }
        } catch (DocumentNotFoundException dnf){
            LOGGER.info("Get failed because item has expired");
        }
    }

    private void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Expiration().execute();
    }
}
