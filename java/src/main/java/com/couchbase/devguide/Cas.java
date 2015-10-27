package com.couchbase.devguide;

import java.util.concurrent.CountDownLatch;

import com.couchbase.client.java.document.JsonArrayDocument;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.CASMismatchException;

/**
 * Example of Cas (Check and Set) handling in Java for the Couchbase Developer Guide.
 */
public class Cas extends ConnectionBase {

    private static final int PARALLEL = 10;
    private static final String KEY = "javaDevguideExampleCas";

    @Override
    protected void doWork() {
        JsonArrayDocument initialDoc = JsonArrayDocument.create(KEY, JsonArray.empty());
        bucket.upsert(initialDoc);

        LOGGER.info("Will attempt concurrent document mutations without CAS");
        parallel(false);

        JsonArray currentList = bucket.get(KEY, JsonArrayDocument.class).content();
        LOGGER.info("Current list has " + currentList.size() + " elements");
        if (currentList.size() != PARALLEL) {
            LOGGER.info("Concurrent modifications removed some of our items! " + currentList.toString());
        }

        // Reset the list again
        bucket.upsert(initialDoc);

        //The same as above, but using CAS
        LOGGER.info("Will attempt concurrent modifications using CAS");
        parallel(true);

        currentList = bucket.get(KEY, JsonArrayDocument.class).content();
        LOGGER.info("Current list has " + currentList.size() + " elements: " + currentList.toString());
        if (currentList.size() != PARALLEL) {
            LOGGER.error("Expected the whole list of elements - " + currentList.toString());
        }
    }

    public void iterationWithoutCAS(int idx, CountDownLatch latch) {
        //this code plainly ignores the CAS by creating a new document (CAS O)
        JsonArray l = bucket.get(KEY, JsonArrayDocument.class).content();
        l.add("item_" + idx);
        JsonArrayDocument updatedDoc = JsonArrayDocument.create(KEY, l);
        bucket.replace(updatedDoc);

        latch.countDown();
    }

    public void iterationWithCAS(int idx, CountDownLatch latch) {
        String item = "item_" + idx;

        while(true) {
            JsonArrayDocument current = bucket.get(KEY, JsonArrayDocument.class);
            JsonArray l = current.content();
            l.add(item);

            //we mutated the content of the document, and the SDK injected the CAS value in there as well
            // so we can use it directly
            try {
                bucket.replace(current);
                break; //success! stop the loop
            } catch (CASMismatchException e) {
                //in case a parallel execution already updated the document, continue trying
                LOGGER.info("Cas mismatch for item " + item);
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
