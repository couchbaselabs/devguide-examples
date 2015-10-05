/**
 * Copyright (C) 2015 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */
package com.couchbase.devguide;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;

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
        bucket.upsert(JsonDocument.create(key, 2, content));

        LOGGER.info("Getting item back immediately");
        LOGGER.info(bucket.get(key).content());

        LOGGER.info("Sleeping for 4 seconds...");
        sleepSeconds(4);
        LOGGER.info("Getting key again...");
        
        //get returns null if the key doesn't exist
        if (bucket.get(key) == null) {
            LOGGER.info("Get failed because item has expired");
        }

        LOGGER.info("Storing item again (without expiry)");
        bucket.upsert(JsonDocument.create(key, content));

        LOGGER.info("Using get-and-touch to retrieve key and modify expiry");
        JsonDocument rv = bucket.getAndTouch(key, 2);
        LOGGER.info("Value is:" + rv.content());

        LOGGER.info("Sleeping for 4 seconds again");
        sleepSeconds(4);
        LOGGER.info("Getting key again (should fail)");
        if (bucket.get(key) == null) {
            LOGGER.info("Failed (not found)");
        }

        LOGGER.info("Storing key again...");
        bucket.upsert(JsonDocument.create(key, content));
        LOGGER.info("Using touch (without get). Setting expiry for 1 second");
        bucket.touch(key, 1);

        LOGGER.info("Sleeping for 4 seconds...");
        sleepSeconds(4);
        LOGGER.info("Will try to get item again... (should fail)");
        if (bucket.get(key) == null) {
            LOGGER.info("Get failed because key has expired");
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
