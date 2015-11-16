package com.couchbase.devguide;

import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.error.DocumentDoesNotExistException;

/**
 * Example of Counters in Java for the Couchbase Developer Guide.
 */
public class Counter extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleCounter";
        // Remove document so we have predictable behavior in this example
        try {
            bucket.remove(key);
        } catch (DocumentDoesNotExistException e) {
            //do nothing, the document is already not here
        }

        try {
            bucket.counter(key, 20);
        } catch (DocumentDoesNotExistException e) {
            LOGGER.info("The counter method failed because the counter doesn't exist yet and no initial value was provided");
        }

        JsonLongDocument rv = bucket.counter(key, 20, 100);
        LOGGER.info("Delta=20, Initial=100. Current value is: " + rv.content());


        rv = bucket.counter(key, 1);
        LOGGER.info("Delta=1. Current value is: " + rv.content());

        rv = bucket.counter(key, -50);
        LOGGER.info("Delta=-50. Current value is: " + rv.content());
    }

    public static void main(String[] args) {
        new Counter().execute();
    }
}
