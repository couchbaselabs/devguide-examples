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

import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.ReplaceOptions;

import java.util.concurrent.CountDownLatch;


/**
 * Example of Cas (Check and Set) handling in Java for the Couchbase Developer Guide.
 * TODO: not tested
 */
public class Cas extends ConnectionBase {

    private static final int PARALLEL = 10;
    private static final String KEY = "javaDevguideExampleCas";

    @Override
    protected void doWork() {
        JsonArray initialDoc = JsonArray.create().add("initial");
        bucket.defaultCollection().upsert(KEY, initialDoc);

        LOGGER.info("Will attempt concurrent document mutations without CAS");
        parallel(false);

        JsonArray currentList = bucket.defaultCollection().get(KEY).contentAsArray();
        LOGGER.info("Current list has " + currentList.size() + " elements");
        if (currentList.size() != PARALLEL) {
            LOGGER.info("Concurrent modifications removed some of our items! " + currentList.toString());
        }

        // Reset the list again
        bucket.defaultCollection().upsert(KEY,initialDoc);

        //The same as above, but using CAS
        LOGGER.info("Will attempt concurrent modifications using CAS");
        parallel(true);

        currentList = bucket.defaultCollection().get(KEY).contentAsArray();
        LOGGER.info("Current list has " + currentList.size() + " elements: " + currentList.toString());
        if (currentList.size() != PARALLEL) {
            LOGGER.error("Expected the whole list of elements - " + currentList.toString());
        }
    }

    public void iterationWithoutCAS(int idx, CountDownLatch latch) {
        //this code plainly ignores the CAS by creating a new document (CAS O)
        JsonArray l = bucket.defaultCollection().get(KEY).contentAsArray();
        l.add("value_"+idx);
        bucket.defaultCollection().replace(KEY, l);
        latch.countDown();
    }

    public void iterationWithCAS(int idx, CountDownLatch latch) {
        String item = "item_" + idx;

        while(true) {
            //GetResult current = bucket.defaultCollection().get(KEY);
            //JsonArray l = bucket.defaultCollection().get(KEY).contentAsArray();
            //l.add( "value_"+idx);

            //we mutated the content of the document, and the SDK injected the CAS value in there as well
            // so we can use it directly
            try {
                GetResult current = bucket.defaultCollection().get(KEY);
                JsonArray l = current.contentAsArray();
                l.add( "value_"+idx);
                bucket.defaultCollection().replace(KEY,l, ReplaceOptions.replaceOptions().cas(current.cas()));
                break; //success! stop the loop
            } catch (RuntimeException e) {
                //in case a parallel execution already updated the document, continue trying
                LOGGER.info(e+" Cas mismatch for item " + item);
            }
        }
        latch.countDown();
    }

    public void parallel(final boolean useCas) {
        final CountDownLatch latch = new CountDownLatch(PARALLEL);
        for (int i = 0; i < PARALLEL; i++) {
            final int idx = i;
            Runnable r = new Runnable() {
                public void run() {
                    if (!useCas) {
                        iterationWithoutCAS(idx, latch);
                    } else {
                        iterationWithCAS(idx, latch);
                    }
                }
            };
            new Thread(r).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Cas().execute();
    }
}
